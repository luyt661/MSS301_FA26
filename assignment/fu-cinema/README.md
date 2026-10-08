# FU Cinema Booking System

A microservices-based cinema booking system built with Spring Boot 4.1.0, Spring Cloud 2025.1.3, and multiple databases (SQL Server, MongoDB, MySQL).

## 🚀 How to Run

1. Start all infrastructure services (Databases):
   ```bash
   docker compose up -d
   ```
   *Wait until `cinema-sqlserver` is healthy (can check via `docker ps`).*

2. Start the microservices in the following order (either via IDE or terminal):
   - **customer-service**: `mvn spring-boot:run` (Port 8081)
   - **movie-service**: `mvn spring-boot:run` (Port 8082 - auto seeds data)
   - **booking-service**: `mvn spring-boot:run` (Port 8083)
   - **api-gateway**: `mvn spring-boot:run` (Port 9000)

## 🔑 Test Accounts

The databases are pre-seeded with the following accounts for testing:

| Role | Email | Password |
|------|-------|----------|
| **ADMIN** | `admin@fucinema.com` | `@@abc123@@` |
| **CUSTOMER** | `an@gmail.com` | `123456` |

## 🧪 Testing with Postman

1. Import the environment file `postman/FUCinema-Local.postman_environment.json` into Postman.
2. Ensure you have the `FUCinema-Local` environment selected.
3. Import your Postman Collection containing the test cases from F1 to F10.
4. Run the Collection using **Collection Runner** to see the results.

### Collection Runner Result
*(Please attach the screenshot of your Postman Collection Runner results here for submission)*
