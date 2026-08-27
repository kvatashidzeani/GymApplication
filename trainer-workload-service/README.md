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

```bash
cd trainer-workload-service
mvn spring-boot:run
```

- Port: **8082**
- Health: `http://localhost:8082/actuator/health`
- Registers with Eureka as **`trainer-workload-service`** (`http://localhost:8761`)
- **JWT Bearer** required on `/workload/**` (shared `gymcrm.jwt.secret` with Gym CRM)

Start **eureka-server** first, then this service.

### Auth example

```http
POST /workload
Authorization: Bearer <jwt-from-gym-crm>
Content-Type: application/json
```


## API

### Update (training planned / cancelled) → 200 OK

`POST /workload`

```json
{
  "trainerUsername": "Mike.Brown",
  "trainerFirstName": "Mike",
  "trainerLastName": "Brown",
  "isActive": true,
  "trainingDate": "2026-03-15",
  "trainingDuration": 60,
  "actionType": "ADD"
}
```

### Read nested summary

```http
GET /workload/{trainerUsername}
GET /workload/{trainerUsername}?year=2026&month=3
GET /workload/{trainerUsername}/{year}/{month}
```
