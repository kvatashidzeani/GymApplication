# Trainer Workload Microservice

Separate Spring Boot app that stores **monthly training summaries** in an **in-memory nested structure**:

```
Trainer
 ├── username, firstName, lastName, status
 └── years[]
      └── year
           └── months[]
                └── month + trainingSummaryDuration
```

## Run

1. Start **Apache ActiveMQ** (default broker: `tcp://localhost:61616`, user/password `admin`/`admin`).
2. Start **eureka-server** (`http://localhost:8761`).
3. Start this service:

```bash
cd trainer-workload-service
mvn spring-boot:run
```

- Port: **8082**
- Health: `http://localhost:8082/actuator/health`
- Registers with Eureka as **`trainer-workload-service`**
- Consumes workload events from ActiveMQ queue **`workload.events.queue`**
- **JWT** is validated from the JMS `Authorization` message property (shared `gymcrm.jwt.secret` with Gym CRM)

## Messaging (from Gym CRM)

Gym CRM publishes `WorkloadUpdateRequest` JSON messages when trainings are created or deleted (trainee cascade).

Message properties:

- `X-Transaction-Id` — correlation id for distributed logging
- `Authorization` — `Bearer <jwt>`

## REST API (read-only)

Workload **updates** are asynchronous (ActiveMQ). REST is used only to **query** summaries.

```http
GET /workload/{trainerUsername}
GET /workload/{trainerUsername}?year=2026&month=3
GET /workload/{trainerUsername}/{year}/{month}
```

JWT Bearer required on `/workload/**` read endpoints.
