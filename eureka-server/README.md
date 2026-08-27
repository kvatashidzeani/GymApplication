# Eureka Discovery Service

Standalone Netflix Eureka server for Gym CRM microservices.

## Run

```bash
cd eureka-server
mvn spring-boot:run
```

- Dashboard: **http://localhost:8761**
- Port: **8761**

## Start order

1. `eureka-server` (8761)
2. `trainer-workload-service` (8082)
3. Gym CRM `GymRestApplication` (8081)

Registered apps appear under **Instances currently registered with Eureka**.
