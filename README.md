A gym management system with two microservices: a main Gym CRM API (trainings, trainees, trainers, JWT
auth) and a Trainer Workload service that tracks monthly training duration. 
Services communicate asynchronously via ActiveMQ, register with Eureka, use PostgreSQL and MongoDB, and include Resilience4j circuit breaking.
Covered the stack with Cucumber BDD component/integration tests and packaged it with Docker (standalone and full networked compose with DB, queue, and discovery).
