# Gym Application

Gym management platform built as Spring Boot microservices. The main Gym CRM service manages trainees, trainers, and trainings. The Trainer Workload service tracks each trainer’s monthly training hours. Services communicate asynchronously over ActiveMQ, register with Eureka, and can run locally or fully in Docker.

---

## Architecture

```
┌─────────────┐     JWT HTTP      ┌──────────────────────────┐
│   Client    │ ───────────────►  │  Gym CRM (Main) :8081    │
└─────────────┘                   │  PostgreSQL / H2         │
                                  └────────────┬─────────────┘
                                               │ JMS publish
                                               ▼
                                  ┌──────────────────────────┐
                                  │  ActiveMQ :61616         │
                                  │  queue: workload.events  │
                                  └────────────┬─────────────┘
                                               │ JMS consume
                                               ▼
                                  ┌──────────────────────────┐
                                  │  Workload (Report) :8082 │
                                  │  MongoDB                 │
                                  └──────────────────────────┘

Both services register with Eureka Discovery (:8761)
Workload publish is protected by Resilience4j Circuit Breaker
```

| Service | Port | Role |
|---------|------|------|
| **gym-crm** | 8081 | Main API — trainees, trainers, trainings, JWT auth |
| **trainer-workload-service** | 8082 | Report API — trainer monthly workload summaries |
| **eureka-server** | 8761 | Service discovery |
| **PostgreSQL** | 5432 / 5433 (Docker) | Gym CRM persistence |
| **MongoDB** | 27017 | Workload document store |
| **ActiveMQ** | 61616 (OpenWire), 8161 (console) | Async messaging |

---

## Tech stack

| Area | Technology |
|------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5, Spring Cloud 2023.0.3 |
| Build | Maven |
| Main DB | PostgreSQL (H2 for tests / standalone Docker) |
| Report DB | MongoDB |
| Messaging | Apache ActiveMQ (JMS) |
| Discovery | Netflix Eureka |
| Resilience | Resilience4j Circuit Breaker |
| Security | Spring Security + JWT (JJWT) |
| API docs | Swagger / OpenAPI |
| Observability | Spring Actuator, Micrometer, Prometheus |
| Cross-cutting | Spring AOP (operation logging) |
| Containers | Docker, Docker Compose |
| IDE | IntelliJ IDEA |

---

## Modules

```
GymApplication/
├── src/                          # Gym CRM (main microservice)
├── trainer-workload-service/     # Workload / report microservice
├── eureka-server/                # Eureka discovery server
├── Dockerfile                    # Main service image
├── docker-compose.yml            # Full stack (integrations ON)
├── docker-compose.standalone.yml # Apps only (integrations OFF)
└── pom.xml
```

### Gym CRM (Main)

- REST API for trainee / trainer registration, profiles, trainings
- Layered design: Controller → Facade → Service → DAO
- JWT login / logout with token blacklist
- Publishes ADD / DELETE training events to ActiveMQ
- Circuit breaker around workload publishing
- Seed data from `initial-data.json`

### Trainer Workload (Report)

- Consumes workload events from ActiveMQ
- Stores nested summaries: Trainer → Year → Month → duration
- MongoDB in normal mode; in-memory storage for Docker standalone / component tests
- JWT-secured REST API to query trainer workload

### Eureka Server

- Standalone discovery registry on port 8761

---

## Design patterns used

| Pattern | Where |
|---------|--------|
| **Facade** | `GymFacade` — single entry for controllers |
| **DAO** | `UserDao`, `TraineeDao`, `TrainerDao`, `TrainingDao` |
| **Repository** | Spring Data `TrainerWorkloadRepository` |
| **Strategy** | `WorkloadStorage` → Mongo / InMemory by profile |
| **Proxy / AOP** | `OperationLoggingAspect` |
| **Chain of Responsibility** | JWT + transaction logging filters |
| **DTO** | Request / response objects separate from entities |
| **Dependency Injection** | Spring constructor injection |
| **Circuit Breaker** | Resilience4j on `WorkloadClient` |
| **Publish / Subscribe** | ActiveMQ workload events |

---

## Spring profiles

| Profile | Purpose |
|---------|---------|
| `local` | Developer machine (Postgres + local ActiveMQ / Eureka) |
| `docker` | Standalone containers — integrations disabled (H2 / in-memory) |
| `docker-integrated` | Full Docker stack — Postgres, Mongo, ActiveMQ, Eureka |
| `integration-test` | Combined CRM + workload test context |
| `component-test` | Single-service Cucumber component tests |
| `console` | Console demo without Eureka / messaging |

---

## Testing

### Tools

- **JUnit 5** — unit tests
- **Mockito** — mocking dependencies (`@Mock`, `@InjectMocks`)
- **Spring Boot Test** — `@SpringBootTest`, `@AutoConfigureMockMvc`
- **MockMvc** — HTTP API tests without a browser
- **Cucumber** — BDD scenarios (Given / When / Then)
- **JUnit Platform Suite** — Cucumber suite runners
- **H2** — in-memory SQL for tests
- **Flapdoodle embedded Mongo** — in-memory MongoDB
- **Embedded ActiveMQ broker** — in-process JMS
- **Awaitility** — wait for async message processing

### Test levels

1. **Unit tests** — services, facade, DAOs, validators, JWT, filters (mocked collaborators)
2. **Component tests (Cucumber)**  
   - Gym CRM: `GymCrmCucumberSuite` → `features/gym-crm/`  
   - Workload: `WorkloadCucumberSuite` → `features/workload/`
3. **Integration tests (Cucumber)**  
   - `IntegrationCucumberSuite` → `features/integration/workload-messaging.feature`  
   - Boots both microservices, publishes via ActiveMQ, asserts Mongo workload with Awaitility  
   - Positive: training updates monthly minutes  
   - Negative: 401 / 404 leave workload unchanged

### Run tests

```bash
# From project root
mvn test

# Workload module only
mvn -f trainer-workload-service/pom.xml test
```

---

## Run locally (without Docker)

### Prerequisites

- Java 17+
- Maven 3.9+
- PostgreSQL (DB `gymapplication`)
- MongoDB
- ActiveMQ (OpenWire `tcp://localhost:61616`)
- Eureka (optional for full discovery)

### Start services

```bash
# Eureka
mvn -f eureka-server/pom.xml spring-boot:run

# Gym CRM (default profile: local)
mvn spring-boot:run

# Workload
mvn -f trainer-workload-service/pom.xml spring-boot:run
```

Useful URLs:

- Gym CRM API: http://localhost:8081  
- Swagger UI: http://localhost:8081/swagger-ui.html  
- Health: http://localhost:8081/actuator/health  
- Workload: http://localhost:8082  
- Eureka: http://localhost:8761  

---

## Run with Docker

### SubTask 1 — integrations OFF (standalone)

Apps only; no Postgres, Mongo, ActiveMQ, or Eureka. Profile: `docker`.

```bash
docker compose -f docker-compose.standalone.yml up -d --build
curl http://127.0.0.1:8081/actuator/health
curl http://127.0.0.1:8082/actuator/health
docker compose -f docker-compose.standalone.yml down
```

### SubTask 2 — integrations ON (networked stack)

Shared bridge network `gym-net` with Postgres, MongoDB, ActiveMQ, Eureka. Profile: `docker-integrated`.

```bash
docker compose up -d --build
docker compose ps
docker network inspect gym-net
curl http://127.0.0.1:8081/actuator/health
curl http://127.0.0.1:8082/actuator/health
```

| Container | Host port |
|-----------|-----------|
| gym-crm | 8081 |
| trainer-workload-service | 8082 |
| gym-postgres | 5433 → 5432 |
| gym-mongo | 27017 |
| gym-activemq | 61616, 8161 |
| eureka-server | 8761 |

> Stop local IntelliJ apps on 8081/8082 before starting Docker, or port bind will fail.

### SubTask 3 — shell + logs

```bash
docker exec -it gym-crm sh
# inside: ps -ef | grep java ; wget -qO- http://127.0.0.1:8081/actuator/health ; exit

docker exec -it trainer-workload-service sh
docker logs gym-crm --tail 50
docker logs trainer-workload-service --tail 50
docker logs eureka-server --tail 30
```

---

## Security

- JWT Bearer authentication (HS256, shared secret via `JWT_SECRET`)
- Login issues a token; logout blacklists it
- Failed login attempts are rate-limited (`LoginAttemptService`)
- CORS configured for local frontends
- Swagger endpoints are public; business APIs require auth

---

## Messaging flow

1. Trainee adds or deletes a training in Gym CRM  
2. `WorkloadClient` publishes `WorkloadUpdateRequest` to `workload.events.queue`  
3. Circuit breaker wraps the publish (fallback logs if broker is down)  
4. `WorkloadMessageListener` consumes the message  
5. Duration is applied to the trainer’s year/month summary in MongoDB  
6. Invalid messages can be sent to a dead-letter queue (DLQ)

Type-id mapping maps Gym CRM’s `com.gymcrm.client.WorkloadUpdateRequest` to the workload service DTO so Jackson can deserialize across services.

---

## Configuration highlights

Environment variables (Docker / deploy):

| Variable | Meaning |
|----------|---------|
| `SPRING_PROFILES_ACTIVE` | `docker` or `docker-integrated` |
| `JWT_SECRET` | Shared HMAC secret (≥ 32 chars) |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | Postgres for Gym CRM |
| `MONGODB_URI` | Mongo connection for workload |
| `ACTIVEMQ_BROKER_URL` | e.g. `tcp://activemq:61616` |
| `EUREKA_SERVER` | e.g. `http://eureka-server:8761/eureka/` |

---

## License

Internship / educational project — Gym CRM microservices training assignment.
