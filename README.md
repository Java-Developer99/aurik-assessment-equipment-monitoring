# Industrial Equipment Monitoring Backend

A backend service for ingesting industrial equipment telemetry from multiple vendors, normalizing vendor-specific payloads into a canonical event model, processing events asynchronously, and producing a deterministic operational view for machines and plants.

The application currently supports vendor-specific ingestion for **PulseForge** and **ThermexWatch**, with the architecture designed to support additional vendors such as MaintaFlow.

---

## 1. Overview

Industrial equipment vendors may provide telemetry using different:

* Field names
* Identifier formats
* Timestamp formats
* Severity representations
* Measurement units
* Alert/event codes
* 
The system is designed around deterministic and explainable business rules rather than machine learning.

---

## 2. Architecture

### High-level architecture

`
                         +-------------------+
                         |    PulseForge     |
                         +---------+---------+
                                   |
                         +---------v---------+
                         |                   |
                         |  Ingestion APIs   |
                         |                   |
                         +---------+---------+
                                   |
                         +---------v---------+
                         | Ingestion Record  |
                         | + Raw Payload     |
                         +---------+---------+
                                   |
                            Async Processing
                                   |
                         +---------v---------+
                         | Vendor Normalizer |
                         +---------+---------+
                                   |
                         +---------v---------+
                         | Canonical Events  |
                         +---------+---------+
                                   |
                         +---------v---------+
                         | Operational State |
                         +---------+---------+
                                   |
                    +--------------+--------------+
                    |                             |
             +------v------+               +------v------+
             | Machine API |               | Plant API   |
             +-------------+               +-------------+
``

ThermexWatch follows the same ingestion and processing pipeline through its own vendor-specific normalizer.

---

## 3. Technology Stack

| Component        | Technology                  |
| ---------------- | --------------------------- |
| Language         | Java 17                     |
| Framework        | Spring Boot                 |
| Build Tool       | Maven Wrapper               |
| Database         | PostgreSQL 17               |
| Persistence      | Spring Data JPA / Hibernate |
| Containerization | Docker / Docker Compose     |
| API              | REST                        |
| JSON Processing  | Jackson                     |
| Testing          | JUnit / Spring testing      |

---

## 4. Why Java + Spring Boot?

The assessment lists several possible technology choices. Java + Spring Boot was selected because it is the implementation stack I have the strongest production experience with.

This also provides:

* Strong type safety
* Mature REST support
* Spring Data JPA for persistence
* Straightforward dependency injection
* Good testing support
* Clear separation between controllers, services, repositories and vendor-specific processing components

The implementation intentionally avoids introducing unnecessary infrastructure such as Kafka, Redis, Kubernetes, or microservices because the current assessment scope can be handled with a simpler architecture.

---

## 5. Project Structure

The project follows a layered architecture with vendor-specific normalization components.

``
src/
├── main/
│   ├── java/
│   │   └── ...
│   │       ├── controller/
│   │       ├── service/
│   │       ├── repository/
│   │       ├── entity/
│   │       ├── dto/
│   │       ├── normalizer/
│   │       ├── reference/
│   │       └── config/
│   │
│   └── resources/
│       └── application.yml
│
└── test/
    └── java/
        └── ...
`

The exact package names may vary, but the main responsibility boundaries are:

* **Controllers** — expose REST APIs
* **Services** — coordinate application/business logic
* **Normalizers** — convert vendor-specific payloads to the canonical representation
* **Repositories** — database access
* **Entities** — persistence models
* **Reference resolution** — machine/line/entity mapping
* **Operational state processing** — derives the current machine state

---

# 6. Prerequisites

Install the following:

* Java 17
* Docker Desktop
* Git

Maven does not need to be installed globally because the project includes the Maven Wrapper.

Verify Java:

``bash
java -version
`

The application is intended to run with Java 17.

Verify Docker:

``bash
docker --version
`

---

# 7. Local Setup

Clone the repository:

``bash
git clone <REPOSITORY_URL>
cd <PROJECT_DIRECTORY>
`

The application uses PostgreSQL through Docker Compose.

Start the database:

``bash
docker compose up -d
`

Verify the container:

``bash
docker ps
`

The PostgreSQL database is configured for the application.

---

# 8. Configuration

The application uses environment variables with local defaults.

Example configuration:

`yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/aurik_monitoring}
    username: ${DB_USERNAME:aurik}
    password: ${DB_PASSWORD:aurik}

server:
  port: ${PORT:8080}

app:
  processing:
    stale-minutes: ${STALE_MINUTES:30}
``

Important configuration:

| Variable        | Default                                             | Purpose               |
| --------------- | --------------------------------------------------- | --------------------- |
| `DB_URL`        | `jdbc:postgresql://localhost:5432/aurik_monitoring` | PostgreSQL connection |
| `DB_USERNAME`   | `aurik`                                             | Database user         |
| `DB_PASSWORD`   | `aurik`                                             | Database password     |
| `PORT`          | `8080`                                              | Application port      |
| `STALE_MINUTES` | `30`                                                | Freshness threshold   |

For a local assessment environment, the provided Docker Compose configuration can be used directly.

---

# 9. Running the Application

Start PostgreSQL:

`bash
docker compose up -d
``

Start the Spring Boot application using the Maven Wrapper.

### Windows

`powershell
.\mvnw.cmd spring-boot:run
``

### Linux / macOS

``bash
./mvnw spring-boot:run
`

The application starts on:

``text
http://localhost:8080
`

---

# 10. Health Check

The application exposes a health endpoint.

``http
GET /actuator/health
`

Example:

``text
http://localhost:8080/actuator/health
`

Expected response:

```json
{
  "status": "UP"
}
```

---

# 11. API Endpoints

## Vendor ingestion

### PulseForge

```http
POST /api/v1/ingestion/pulseforge
```

### ThermexWatch

```http
POST /api/v1/ingestion/thermexwatch
```

### Ingestion status

```http
GET /api/v1/ingestion/{ingestionId}
```

---

## Machine operational view

```http
GET /api/v1/machines/{machineId}/operational-view
```

Example:

```http
GET /api/v1/machines/EQ-001/operational-view
```

Example response:

```json
{
  "machineId": "EQ-001",
  "plantId": "PLANT_01",
  "lineId": "LINE-A",
  "derivedStatus": "ATTENTION_REQUIRED",
  "attentionLevel": "CRITICAL",
  "attentionRequired": true,
  "reasonCodes": [
    "TEMPERATURE_CRITICAL"
  ],
  "latestRelevantEventTime": "2026-10-06T06:05:00Z",
  "processingStatus": "PROCESSED",
  "sourceEventRefs": [
    "TW-CONFLICT-001"
  ],
  "lastProcessedAt": "2026-10-06T06:05:35.801871Z",
  "stale": false
}
```

---

## Plant summary

```http
GET /api/v1/plants/{plantId}/summary
```

Example:

```http
GET /api/v1/plants/PLANT_01/summary
```

The response provides:

* Machine status counts
* Line-level attention counts
* Critical machines
* Stale machine count

---

# 12. Vendor Normalization

Each vendor has its own payload structure.

Instead of allowing vendor-specific structures to propagate through the application, vendor payloads are converted into a canonical internal representation.

For example, ThermexWatch provides:

```text
assetCode
productionLine
timestampMs
alertCode
level
vibration_g
temperature_f
```

These are normalized into canonical concepts such as:

```text
machineId
lineId
eventTime
eventType
severity
vibrationMmS
temperatureC
```

---

## Unit normalization

ThermexWatch provides measurements in different units.

The normalizer converts:

```text
Fahrenheit → Celsius
g → mm/s
```

This allows downstream processing to work with consistent units regardless of the source vendor.

---

# 13. Entity Resolution

Vendor-specific identifiers are resolved against reference data.

For example:

```text
ThermexWatch productionLine = "A"
```

is resolved to:

```text
LINE-A
```

Machine and line references are resolved before creating canonical events.

This prevents vendor-specific identifiers from leaking into the operational-state logic.

---

# 14. Asynchronous Processing

The ingestion endpoint acknowledges the request before the complete processing workflow finishes.

Typical flow:

``text
POST vendor payload
       |
       v
Create ingestion record
       |
       v
Return ACCEPTED
       |
       v
Background processing
       |
       v
Normalize vendor records
       |
       v
Persist canonical events
       |
       v
Recompute machine state
       |
       v
PROCESSED / FAILED
`

The ingestion status can then be queried using:

```http
GET /api/v1/ingestion/{ingestionId}
```

This separates request acceptance from downstream processing.

---

# 15. Idempotency and Duplicate Handling

Duplicate records should not create duplicate canonical events.

The primary idempotency key is:

```text
vendor + source_record_id
```

A secondary semantic duplicate check is also used based on:

```text
vendor
machine
event time
event type
```

The canonical event table also enforces a database-level uniqueness constraint for:

```text
vendor + source_record_id
```

Example duplicate result:

```json
{
  "recordsReceived": 1,
  "recordsProcessed": 0,
  "recordsDuplicate": 1,
  "recordsFailed": 0
}
```

This allows the ingestion workflow to distinguish duplicates from actual failures.

---

# 16. Malformed Input Handling

Invalid vendor records are rejected rather than being silently converted into incorrect canonical events.

For example, if:

```json
"vibration_g": "not-a-number"
```

is provided where a numeric value is required, the record fails normalization.

Example result:

```json
{
  "recordsReceived": 1,
  "recordsProcessed": 0,
  "recordsDuplicate": 0,
  "recordsFailed": 1,
  "errorMessage": "vibration_g must be numeric"
}
```

---

# 17. Out-of-Order Events

The system uses the **event timestamp** rather than ingestion/arrival order when determining the current operational state.

Therefore, receiving an older event after a newer event does not automatically cause the machine's current state to move backward.

The operational state is based on the most recent relevant event timestamps.

---

# 18. Conflict Resolution

Different vendors may report different conditions for the same machine.

The system uses a deterministic severity hierarchy:

```text
CRITICAL
   >
HIGH
   >
MEDIUM
   >
LOW
   >
NORMAL
```

For the current operational state:

1. Determine the latest event from each vendor.
2. Ignore vendor events outside the configured freshness window.
3. Compare the active vendor states.
4. The highest severity becomes the machine's attention level.
5. Corresponding reason codes and source references are retained.

For example:

```text
PulseForge:
HIGH_VIBRATION → HIGH

ThermexWatch:
TEMPERATURE_CRITICAL → CRITICAL

Result:
TEMPERATURE_CRITICAL → CRITICAL
```

This provides deterministic and explainable conflict resolution.

---

# 19. Machine Operational State

The machine operational view contains derived information rather than simply returning raw telemetry.

The response includes:

* Current derived status
* Attention level
* Whether attention is required
* Reason codes
* Latest relevant event time
* Processing status
* Source event references
* Last processing time
* Freshness/staleness

This allows consumers of the API to understand **why** a machine requires attention.

---

# 20. Freshness and Stale Data

A configurable freshness threshold is used.

Default:

```text
30 minutes
```

Events older than the configured threshold are considered stale for current operational-state evaluation.

Freshness is represented separately from processing status.

For example:

```text
processingStatus = PROCESSED
stale = true
```

means that the event was successfully processed but its timestamp is too old to represent the current machine condition.

---

# 21. Testing

Run the complete automated test suite using the Maven Wrapper.

### Windows

```powershell
.\mvnw.cmd clean test
```

### Linux / macOS

```bash
./mvnw clean test
```

The tests cover vendor-specific normalization and application behavior.

In addition to automated tests, the following scenarios were manually validated against the running application:

* Successful PulseForge ingestion
* Successful ThermexWatch ingestion
* Asynchronous ingestion status transition
* Entity resolution
* Temperature unit conversion
* Vibration unit conversion
* Duplicate ingestion
* Malformed vendor data
* Out-of-order events
* Cross-vendor conflicting updates
* Machine operational view
* Plant/line summary
* Fresh versus stale events

---

# 22. Example End-to-End Flow

A typical flow is:

```text
1. Vendor sends telemetry
          |
          v
2. POST /api/v1/ingestion/{vendor}
          |
          v
3. Ingestion record created
          |
          v
4. API returns ACCEPTED
          |
          v
5. Background processing begins
          |
          v
6. Vendor payload normalized
          |
          v
7. Duplicate validation
          |
          v
8. Canonical event persisted
          |
          v
9. Machine state recomputed
          |
          v
10. GET /api/v1/ingestion/{id}
          |
          v
11. PROCESSED / FAILED
          |
          v
12. Machine operational view available
```

---

# 23. Assumptions

The implementation makes the following assumptions:

1. A vendor's source record identifier is stable and can be used for idempotency.
2. Reference data is available for resolving machines and lines.
3. Event timestamps represent when the machine event occurred rather than when the system received it.
4. Events older than the configured freshness threshold are considered stale for current-state evaluation.
5. The latest event from each vendor represents that vendor's latest known machine condition.
6. Higher severity takes precedence when multiple current vendor states conflict.
7. Vendor-specific payloads are normalized before operational-state processing.
8. Raw vendor records are retained with canonical events for traceability.

---

# 24. Trade-offs

### Simplicity over distributed infrastructure

The application uses Spring Boot background processing and PostgreSQL rather than introducing Kafka, RabbitMQ, Redis, or a microservice architecture.

This keeps the solution:

* Simple to run
* Easy to understand
* Easy to test
* Appropriate for the assessment scope

For substantially higher event volumes, a durable event broker and independently scalable workers would be a reasonable next step.

### Database schema management

The current setup uses Hibernate schema generation for local development.

For production, a migration tool such as Flyway or Liquibase would be preferable.

### Deterministic rules instead of ML

Operational status is calculated using explicit severity and freshness rules.

This makes the system explainable and predictable and avoids introducing unnecessary machine-learning complexity.

---

# 25. Limitations

The current implementation is intentionally scoped to the assessment.

Potential areas for further production hardening include:

* More vendor adapters
* Durable message queues
* Dead-letter queue handling
* Advanced retry policies
* Authentication and authorization
* API rate limiting
* Database migrations
* Metrics and monitoring
* Distributed tracing
* Horizontal worker scaling
* Stronger schema/version management
* Automated vendor contract testing

These are documented as production improvements rather than implemented unnecessarily for the current scope.

---

# 26. Production Improvements

If this system were taken to production at significantly higher scale, the next improvements would include:

```text
Vendor APIs
     |
     v
Message Broker
(Kafka / RabbitMQ)
     |
     v
Scalable Processing Workers
     |
     +---- Retry
     |
     +---- Dead Letter Queue
     |
     v
Canonical Event Store
     |
     v
Operational State
     |
     v
API / Monitoring
```

Additional production concerns would include:

* Authentication
* Authorization
* Rate limiting
* Observability
* Metrics
* Distributed tracing
* Database migration management
* Horizontal scaling
* Vendor contract/version management

---

# 27. AI Usage Disclosure

AI assistance was used during development of this assessment.

The AI assistant was used for:

* Implementation guidance
* Debugging assistance
* Identifying potential edge cases
* Generating test scenarios
* Reviewing design alternatives
* Documentation drafting
* README structure and wording

The final architecture, technology choice, implementation decisions, debugging validation, API testing, and assessment trade-offs were reviewed and verified by the candidate.

AI-generated suggestions were not treated as authoritative without validation. The application behavior was tested locally against the running Spring Boot application, including duplicate, malformed, out-of-order, freshness, and cross-vendor conflict scenarios.

---

# 28. Submission Notes

The project is intended to be run locally using Java 17, Docker Compose, and PostgreSQL.

The primary deliverable is the Git repository containing:

* Source code
* Automated tests
* Docker configuration
* README
* Architecture/design documentation
* Maven Wrapper
* Git commit history
