# Benchmark Results

## System Specs

- CPU: Intel i7 (10th Gen)
- RAM: 16 GB
- OS: Windows 10 (64-bit)
- Java: OpenJDK 21
- Database: MySQL 8.x (local)

---

## Benchmark Command

Batch ingestion executed via JUnit integration test:

```bash
mvn test -Dtest=MachineEventsServiceApplicationTests
```

Test used: `testIngest1000Events()`

---

## Test Scenario

- Batch size: 1000 events
- All events valid
- Unique eventIds
- Single transactional ingestion

---

## Measured Timing

| Operation | Time |
|----|----|
| Ingest 1000 events | ~350–450 ms |

Measured using `System.nanoTime()` inside test execution.

---

## Optimizations Attempted

- saveAll batching
- Avoided per-record flush
- Reduced object mapping
- JPQL projections for analytics
- Indexed event_id

---

## Observations

- Database write latency dominates
- CPU usage minimal
- Linear scaling observed up to 5k events

---

## Future Improvements

- JDBC batch tuning
- Parallel ingestion
- Kafka-based buffering
- JMH microbenchmarks

---

## Notes

Results may vary depending on hardware and DB configuration.

