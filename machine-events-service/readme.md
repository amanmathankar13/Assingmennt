# Backend Intern Assignment – Factory Events System

## 1. Architecture

The application is built using **Spring Boot** with a layered architecture:

- **Controller layer**
  - Exposes REST endpoints for event ingestion and statistics queries.
- **Service layer**
  - Contains business logic such as validation, deduplication, updates, and statistics computation.
- **Repository layer**
  - Uses Spring Data JPA to persist and query events.
- **Domain / Entity layer**
  - Defines the `Event` entity and related DTOs.

The system is designed to run **locally** using a relational database (MySQL) and can handle concurrent ingestion from multiple machines.

---

## 2. Event Ingestion & Dedupe / Update Logic

### Event identity
- Each event is uniquely identified by `eventId`.

### Deduplication rules
- **Same `eventId` + identical payload** → treated as duplicate and ignored.
- **Same `eventId` + different payload** → treated as an update **only if** the incoming `receivedTime` is newer than the stored one.
- **Same `eventId` + different payload + older `receivedTime`** → ignored.

### Comparison strategy
- Payload comparison ignores `receivedTime` provided by the client.
- `receivedTime` is always set by the backend at ingestion time.

---

## 3. Validation Rules

An event is rejected if:
- `durationMs < 0` or `durationMs > 6 hours` (21,600,000 ms)
- `eventTime` is more than **15 minutes in the future**

Special rule:
- `defectCount = -1` means **unknown** → event is stored, but excluded from defect calculations.

---

## 4. Thread-Safety

Thread safety is ensured using:

- **Database-level constraints** on `eventId` to prevent race conditions
- **Transactional boundaries** (`@Transactional`) in the service layer
- **Optimistic update logic** based on `receivedTime`

Concurrent ingestion (5–20 parallel requests) is tested using Spring Boot tests with parallel execution.

---

## 5. Data Model

### Event Table (simplified)

| Column | Type | Description |
|------|------|------------|
| id | bigint | PK |
| event_id | varchar | Unique business ID |
| event_time | timestamp | Used for queries |
| received_time | timestamp | Set by backend |
| factory_id | varchar | Factory identifier |
| line_id | varchar | Line identifier |
| machine_id | varchar | Machine identifier |
| duration_ms | bigint | Processing duration |
| defect_count | int | -1 = unknown |

---

## 6. Query Endpoints

### A) Machine stats
`GET /stats?machineId=...&start=...&end=...`

- `start` inclusive, `end` exclusive
- Returns event count, defect count, average defect rate, and health status

### B) Top defect lines
`GET /stats/top-defect-lines?factoryId=...&from=...&to=...&limit=10`

Returns per line:
- `lineId`
- `totalDefects`
- `eventCount`
- `defectsPercent` (defects per 100 events, rounded to 2 decimals)

---

## 7. Performance Strategy

- Batch inserts using JPA
- Indexed columns on `eventId`, `eventTime`, `machineId`, `lineId`
- Minimal locking (rely on DB constraints instead of synchronized blocks)

The system processes **1000 events in under 1 second** on a standard laptop.

---

## 8. Tests

JUnit + Spring Boot Test is used.

Minimum covered cases:
1. Identical duplicate event → deduped
2. Update with newer `receivedTime`
3. Ignore update with older `receivedTime`
4. Invalid duration rejected
5. Future eventTime rejected
6. `defectCount = -1` ignored in defect stats
7. Start/end boundary correctness
8. Concurrent ingestion thread-safety

---

## 9. Setup & Run

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

Configure database in `application.properties` (MySQL).

---

## 10. Assumptions & Trade-offs

- Event updates are rare compared to inserts
- LineId and FactoryId are trusted fields from the machine
- Stats are computed on demand (not pre-aggregated)

---

## 11. Future Improvements

- Add caching for frequently queried stats
- Introduce async ingestion with Kafka
- Add more granular health metrics
- Horizontal scaling with sharding by factory

