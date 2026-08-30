# Trainer Workload Microservice

Stores **monthly training summaries** in **MongoDB** using a nested document schema.

## MongoDB schema (`trainer_training_summary`)

```json
{
  "_id": "Mike.Brown",
  "trainerUsername": "Mike.Brown",
  "trainerFirstName": "Mike",
  "trainerLastName": "Brown",
  "trainerStatus": true,
  "years": [
    {
      "year": 2026,
      "months": [
        {
          "month": 8,
          "trainingSummaryDuration": 60
        }
      ]
    }
  ]
}
```

| Field | Description |
|-------|-------------|
| `trainerUsername` | Trainer username (document id) |
| `trainerFirstName` | Trainer first name |
| `trainerLastName` | Trainer last name |
| `trainerStatus` | Active flag (`Boolean`) |
| `years` | List of yearly summaries |
| `years[].year` | Calendar year |
| `years[].months` | List of monthly summaries |
| `years[].months[].month` | Month (1–12) |
| `years[].months[].trainingSummaryDuration` | Total training minutes (`Integer`) |

## Field types & validations

| Field | Type | Required |
|-------|------|----------|
| `trainerUsername` | `String` | Yes |
| `trainerFirstName` | `String` | Yes |
| `trainerLastName` | `String` | Yes |
| `trainerStatus` | `Boolean` | Yes |
| `trainingDate` | `Date` (`LocalDate`) | Yes (in events) |
| `trainingDuration` | `Integer` (number) | Yes, `> 0` |
| `trainingSummaryDuration` | `Integer` (number) | Yes, `>= 0` |
| `actionType` | `ADD` / `DELETE` | Yes (in events) |

Incoming ActiveMQ events are validated by `WorkloadMessageValidator` using Jakarta Bean Validation on `WorkloadUpdateRequest`. Invalid messages go to the DLQ.

## Logging (transaction + operation)

Two logging levels, correlated by `transactionId` (`X-Transaction-Id` header / MDC):

| Level | Logger | Scope |
|-------|--------|--------|
| **Transaction** | `com.gymcrm.workload.logging.Transaction` | HTTP requests (`TransactionLoggingFilter`) and JMS messages (`JmsTransactionLogging`) |
| **Operation** | `com.gymcrm.workload.logging.Operation` | Service methods (`OperationLoggingAspect` on `com.gymcrm.workload.service..*`) |

- Gym CRM propagates `X-Transaction-Id` over HTTP and ActiveMQ.
- `TransactionContext` stores the id in SLF4J MDC — log pattern: `[tx:%X{transactionId}]`.
- REST: transaction start/finish around each HTTP call.
- JMS: transaction start/finish around each queue message; DLQ routing logged as `status=DLQ`.

## Event processing (`TrainerWorkloadEventService`)

When an ADD event arrives from ActiveMQ:

1. **(a)** Search trainer document by `trainerUsername`
2. **(b)** If not found — create document with `year` / `month` from `trainingDate` and `trainingSummaryDuration = trainingDuration`
3. **(c)** If found — locate the matching year/month for `trainingDate`
4. **(d)** Add `trainingDuration` to the monthly `trainingSummaryDuration`
5. **(e)** Save document via `TrainerWorkloadRepository` (MongoDB)

## Repository (`TrainerWorkloadRepository`)

Search and update by **trainer username**:

| Method | Action |
|--------|--------|
| `findByTrainerUsername(username)` | Search document by username |
| `findByTrainerFirstNameAndTrainerLastName(firstName, lastName)` | Search by trainer name (uses compound index) |
| `existsByTrainerUsername(username)` | Check if summary exists |
| `updateByTrainerUsername(workload)` | Replace document matched by username (upsert) |
| `deleteByTrainerUsername(username)` | Delete by username |

Custom update is implemented in `TrainerWorkloadRepositoryImpl` using `MongoTemplate.findAndReplace` on `_id` (= username).

## Indexes

| Index | Fields | Purpose |
|-------|--------|---------|
| `trainer_name_idx` | `trainerFirstName`, `trainerLastName` | Speed up search by trainer first and last name |

Created via `@CompoundIndex` on `TrainerWorkload` when the service starts (`spring.data.mongodb.auto-index-creation=true`).

Repository query: `findByTrainerFirstNameAndTrainerLastName(firstName, lastName)`.

## Run

1. Start **MongoDB** (default: `mongodb://localhost:27017/gym_workload`).
2. Start **Apache ActiveMQ** (`tcp://localhost:61616`).
3. Start **eureka-server** (`http://localhost:8761`) — optional.
4. Start this service:

```bash
cd trainer-workload-service
mvn spring-boot:run
```

- Port: **8082**
- Health: `http://localhost:8082/actuator/health`
- MongoDB URI: `spring.data.mongodb.uri` (env `MONGODB_URI`)

## Messaging (from Gym CRM)

Gym CRM publishes `WorkloadUpdateRequest` JSON to **`workload.events.queue`**.
Invalid messages are forwarded to **`workload.events.dlq`**.

## REST API (read-only)

```http
GET /workload/{trainerUsername}
GET /workload/{trainerUsername}?year=2026&month=8
GET /workload/{trainerUsername}/{year}/{month}
```

JWT Bearer required on `/workload/**`.
