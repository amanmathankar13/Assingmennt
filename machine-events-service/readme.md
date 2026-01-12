# Machine Events Service

## 1. Architecture

The application follows a layered Spring Boot architecture:

```
Controller  ->  Service  ->  Repository  ->  Database
                |
                v
               DTOs
```

- **Controller**: Accepts batch ingestion and analytics requests
- **Service (EventServiceImpl)**: Core business logic (validation, dedupe, update)
- **Repository (Spring Data JPA)**: Persistence and analytics queries
- **Database (MySQL)**: Durable event storage

Batch ingestion is handled transactionally to ensure consistency.

---

## 2. Dedupe / Update Logic

### Event Identity
- `eventId` is the **unique business key**

### Comparison Strategy

When an incoming event arrives:

1. Look up existing record by `eventId`
2. If not found → **Accepted**
3. If found:
   - Compare **payload fields**:
     - machineId
     - lineId
     - factoryId
     - durationMs
     - defectCount
     - eventTime

### Outcomes

| Case | Result |
|----|----|
| Same payload | Deduped |
| Different payload + newer receivedTime | Updated |
| Different payload + older receivedTime | Rejected |

### Winning Record Rule

> **Server-generated `receivedTime` always wins**

Client timestamps are never trusted for ordering updates.

---

## 3. Thread Safety

Thread safety is guaranteed by:

- **Database-level unique constraint** on `event_id`
- **@Transactional** boundary on batch ingestion
- **Row-level locking** handled by the database
- No shared mutable in-memory state

Concurrent inserts for the same eventId resolve safely at DB level.

---

## 4. Data Model

### Event Table

| Column | Type |
|----|----|
| id | BIGINT (PK) |
| event_id | VARCHAR (UNIQUE) |
| machine_id | VARCHAR |
| line_id | VARCHAR |
| factory_id | VARCHAR |
| duration_ms | BIGINT |
| defect_count | INT |
| event_time | TIMESTAMP |
| received_time | TIMESTAMP |

Indexes:
- `event_id (UNIQUE)`
- `(factory_id, line_id, event_time)`

---

## 5. Performance Strategy (1000 events < 1 sec)

Optimizations applied:

- Batch ingestion using `saveAll`
- Single DB lookup per eventId
- No per-event flush
- JPQL constructor projections for analytics
- Pageable instead of LIMIT
- Minimal object allocation

Result: **~1000 events ingested well under 1 second** on local machine.

---

## 6. Edge Cases & Assumptions

### Edge Cases Handled
- Negative duration → rejected
- Duplicate payload → deduped
- Older updates → rejected
- Missing mandatory fields → rejected

### Assumptions
- eventId uniquely identifies an event
- receivedTime is authoritative
- Batch size fits in memory

Tradeoff: DB lookup per event instead of caching for correctness.

---

## 7. Setup & Run Instructions

### Prerequisites
- Java 21
- Maven
- MySQL

### Run

```bash
mvn clean install
mvn spring-boot:run
```

---

## 8. What I Would Improve With More Time

- Bulk existence check for eventIds
- Async ingestion with Kafka
- Partitioning by factoryId
- Metrics & monitoring
- Load testing with JMH

---

## Author

Aman Mathankar

