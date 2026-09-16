# dispatch-service

Assigns tendered pickups to drivers. Consumes `shipment-events.v1` from Kafka
(published by shipment-api), keeps a per-terminal dispatch board, and offers
each pickup to the nearest on-duty driver who has the capacity and, for
hazmat, the endorsement. Drivers accept from the driver app through
`POST /api/v1/assignments/{shipmentId}/accept`.

## Stack

Spring Boot 3.2, Java 21, Spring Data JPA + Flyway on Postgres, Spring Kafka,
Micrometer/Prometheus.

```
src/main/java/io/kestrelfreight/dispatch/
  kafka/      ShipmentEventListener, event payload
  service/    DispatchService (board, offers, accepts)
  domain/     Assignment, Driver (JPA)
  repo/       Spring Data repositories
  web/        REST controllers
  config/     security (service bearer token), clock
src/main/resources/db/migration/   Flyway migrations
```

## Local development

```sh
docker compose -f ../shipment-api/docker-compose.yml up -d postgres
docker run -d --name kafka -p 9092:9092 apache/kafka:3.7.0
export DISPATCH_SERVICE_TOKEN=dev-only-token
gradle bootRun
```

Tests: `gradle test`. Build the jar: `gradle build` (`build/libs/dispatch-service.jar`).

Dependencies are locked (`gradle.lockfile`). After changing `build.gradle.kts`,
run `gradle dependencies --write-locks` and commit the lockfile.

## Endpoints

| Method | Path | Notes |
| --- | --- | --- |
| GET | `/api/v1/assignments?terminal=482&date=2026-11-18` | open assignments for a terminal's board |
| POST | `/api/v1/assignments/{shipmentId}/accept` | `{"driverId": "..."}` |
| POST | `/internal/shipment-events` | nudge from shipment-api (cancellations) |
| GET | `/actuator/health`, `/actuator/prometheus` | unauthenticated |

Everything else requires `Authorization: Bearer $DISPATCH_SERVICE_TOKEN`.

## Deploy

ECS service `dispatch-service` (cluster `prod-apps`), 2 tasks, consumer group
`dispatch-service`.
