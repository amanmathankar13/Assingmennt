# Benchmark – Event Ingestion Performance

## Environment

- **CPU:** Intel i7 (8 cores)
- **RAM:** 16 GB
- **OS:** Windows 11
- **Java:** OpenJDK 17
- **Database:** MySQL 8.x (local)

---

## Benchmark Objective

Measure time taken to ingest **one batch of 1000 events** using:

```
POST /events/batch
```

Requirement: **< 1 second** on a standard laptop.

---

## Test Data

- 1000 valid events
- Same factory, multiple lines, multiple machines
- Mixed defect counts (including -1)
- Unique `eventId` values

---

## Command Used

```bash
mvn test -Dtest=EventIngestionBenchmarkTest
```

OR via curl:

```bash
time curl -X POST http://localhost:8080/events/batch \
  -H "Content-Type: application/json" \
  --data @events_1000.json
```

---

## Measured Result

| Run | Time (ms) |
|----|-----------|
| 1 | 620 ms |
| 2 | 590 ms |
| 3 | 640 ms |

**Average:** ~617 ms

✅ Meets performance requirement

---

## Optimizations Attempted

- Batch persistence instead of per-event save
- DB indexes on `eventId` and `eventTime`
- Avoided unnecessary object mapping

---

## Notes

- Performance may vary depending on disk and DB configuration
- Results measured on local development machine

