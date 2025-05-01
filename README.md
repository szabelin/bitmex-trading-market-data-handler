# Lunar Trading Market Data Handler

This project implements a real-time market data handler for BitMEX's `orderBookL2` feed. It listens to the BitMEX
WebSocket stream, maintains an in-memory order book (bids and asks), and publishes full or partial snapshots which are
either persisted to a database or evaluated in-memory depending on the test or production configuration.

Built with **Java 21**, **RxJava 3**, and **Spring Boot 3**, the system is fast, modular, and designed for reactive data
handling and testability. Snapshots are configurable to include only the top N levels of the book and are emitted as
immutable domain objects.

---

## 🚀 How to Run

### ▶️ Start the Application

```bash
./gradlew bootRun
```

This will:

- Connect to the BitMEX WebSocket
- Subscribe to `orderBookL2:XBTUSD` and `orderBookL2:ETHUSD`
- Start building and publishing order book snapshots

---

## ✅ Testing

### ▶️ Run **acceptance tests** (in-memory, fast)

```bash
./gradlew test --tests "*AcceptanceTests"
```

### ▶️ Run **integration tests** (H2 DB + websocket)

```bash
./gradlew test --tests "*IntegrationTests"
```

---

## 🧪 Testing Modes Explained

The system supports two test modes which use the **same test assertions and logic**, but differ in data handling:

| Mode            | Behavior                                                                           |
|-----------------|------------------------------------------------------------------------------------|
| **Acceptance**  | Uses in-memory snapshot storage. No DB or I/O. Fast and lightweight.               |
| **Integration** | Uses real H2 database and a websocket. Snapshots are written and queried via JDBC. |

The mode is selected at test runtime via the `TestingLevel` configuration, which can be overridden in CI environments
such as **TeamCity** to dynamically switch between test types.

---

## ⚙️ Configuration

All configuration is located in `application.yml`.

```yaml
orderbook:
  snapshot:
    levels: 10  # Number of top levels to include in each snapshot (0 = all)
```

Use `0` to retain the entire book, or a positive number to limit to top N bids/asks.

---

## 📦 Technologies Used

| Technology        | Purpose                                               |
|-------------------|-------------------------------------------------------|
| Java 21           | Modern records, performance, structured code          |
| Spring Boot 3     | Application lifecycle, DI, configuration              |
| RxJava 3          | High-performance stream handling (pub-sub, filtering) |
| OkHttp            | BitMEX WebSocket client                               |
| H2 Database       | In-memory or file-based SQL database for snapshots    |
| JUnit 5 + AssertJ | Clean, fluent assertions for test validation          |
| Awaitility        | Async condition testing                               |

---

## ⚡ RxJava Use Case

RxJava powers the core data flow:

- Publish/subscribe architecture
- Snapshot emission via `PublishProcessor`
- Backpressure-safe buffering
- Custom thread assignment (e.g. `.observeOn(Schedulers.io())`) or on a dedicated thread

This gives you full control over how messages flow, transform, and persist.

---

## 📂 Project Structure

```
src/
├── main/
│   ├── domain/            → BitMEX messages (immutable records)
│   ├── websocket/         → WebSocket listener & reconnect logic
│   ├── orderbook/         → Book update logic & snapshot generation
│   ├── service/           → Snapshot publishing & persistence
│   ├── config/            → Spring, Rx, and DB setup
│   ├── reader/            → H2 database snapshot reader
│
├── test/
│   ├── acceptance/        → Acceptance test suite
│   ├── integration/       → Integration test suite
│   └── testsupport/       → `Given` and `Expect` DSL for testing
```

---

## 🗃️ Inspecting the H2 Database (Integration Tests)

When using the file-based H2 DB, you can inspect the data using a GUI
like [DataGrip](https://www.jetbrains.com/datagrip/) or H2 Console.

### JDBC connection:

```
jdbc:h2:file:./data/orderbookdb
User: sa
Password: [leave blank]
```

This will let you view order book snapshots and levels in real-time or post-run.

---

## 🧼 Graceful Shutdown

- WebSocket is closed via `webSocket.close(1000, "Shutting down")`
- RxJava subscriptions are disposed
- H2 connections are cleaned up using `@PreDestroy` hooks and shutdown callbacks

---

---

## 🗃️ Database Structure

The application uses an H2 database with two normalized tables to store order book data efficiently:

### 1. `order_book_snapshots`

This table stores high-level metadata about each snapshot:

| Column      | Type      | Description                   |
|-------------|-----------|-------------------------------|
| snapshot_id | BIGINT    | Auto-incremented primary key  |
| symbol      | VARCHAR   | Trading symbol (e.g., XBTUSD) |
| timestamp   | TIMESTAMP | Snapshot timestamp (UTC)      |

### 2. `order_book_levels`

This table stores the individual bid/ask levels for each snapshot:

| Column      | Type      | Description                                |
|-------------|-----------|--------------------------------------------|
| snapshot_id | BIGINT    | Foreign key to `order_book_snapshots`      |
| side        | VARCHAR   | 'BUY' or 'SELL'                            |
| price       | DOUBLE    | Price at that level                        |
| size        | BIGINT    | Size at that price level                   |
| level_id    | BIGINT    | ID from the exchange to track order levels |
| timestamp   | TIMESTAMP | Copy of the entry’s last update time       |

- Indexes are used to optimize top-N lookups and filtering by symbol and time.
- All levels are written on every snapshot (no diffing or partial updates).
- Timestamp reflects the exchange timestamp that caused us to publish the snapshot 

## 🔍 Querying Latest Snapshots (SQL Examples)

To retrieve the **latest snapshot** for a specific symbol and side:

### 🔹 Latest Top 10 Bids for ETHUSD

```sql
SELECT l.*
FROM order_book_levels l
         JOIN order_book_snapshots s ON l.snapshot_id = s.snapshot_id
WHERE s.symbol = 'ETHUSD'
  AND l.side = 'BUY'
  AND s.snapshot_id = (
    SELECT MAX(snapshot_id)
    FROM order_book_snapshots
    WHERE symbol = 'ETHUSD'
)
ORDER BY l.price DESC
LIMIT 10;
```

### 🔹 Latest Top 10 Asks for ETHUSD

```sql
SELECT l.*
FROM order_book_levels l
         JOIN order_book_snapshots s ON l.snapshot_id = s.snapshot_id
WHERE s.symbol = 'ETHUSD'
  AND l.side = 'SELL'
  AND s.snapshot_id = (
    SELECT MAX(snapshot_id)
    FROM order_book_snapshots
    WHERE symbol = 'ETHUSD'
)
ORDER BY l.price ASC
LIMIT 10;
```

---

## 🧠 Performance Notes

- Order book levels are stored in `TreeMap` for O(log n) price-level sorting
- While not a requirement, the expectation for the downstream consumers is that the book is sorted
- Fast ID lookup via `Map<Long, OrderBookEntry>`
- Snapshots are immutable and GC-friendly
- If N number of levels to publish is specified (not 0), efficient algorithm is applied to determine if a publication is required

---

