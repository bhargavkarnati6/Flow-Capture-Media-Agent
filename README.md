# Flow Capture Media Agent

A Java application demonstrating a clean, layered backend architecture: it **ingests** media metadata from an external source, **persists** it to a real relational database via JDBC, and **serves** it back out through a REST API — all built on the Java SE standard library plus one JDBC driver, with an automated JUnit test suite.

Built as a portfolio piece for a Software Engineering Co-op application, specifically to demonstrate the qualifications called out in the job posting: object-oriented backend development in Java, REST API design, relational database usage, and automated testing.

---

## 1. Project Structure

This project follows the **Maven standard directory layout** so it opens correctly in any IDE and builds via a standard `mvn` workflow.

```
flow-capture-media-agent/
├── README.md
├── pom.xml                                      # Build descriptor: sqlite-jdbc + JUnit 5 + shade plugin
├── .gitignore
└── src/
    ├── main/java/com/flowcapture/mediaagent/
    │   ├── Main.java                              # Entry point / composition root
    │   ├── model/
    │   │   ├── MediaAsset.java                     # Abstract domain base class
    │   │   └── VideoAsset.java                     # Concrete domain subclass
    │   ├── repository/
    │   │   ├── AssetDao.java                       # Persistence contract (interface)
    │   │   └── AssetDaoImpl.java                   # JDBC/SQLite persistence implementation
    │   ├── service/
    │   │   ├── IngestionService.java               # Business logic contract (interface)
    │   │   └── IngestionServiceImpl.java           # HTTP fetch + JSON parsing implementation
    │   ├── api/
    │   │   ├── AssetApiServer.java                 # Wraps the JDK's built-in HttpServer
    │   │   ├── AssetsHandler.java                  # Serves GET /assets and GET /assets/{id}
    │   │   ├── IngestHandler.java                  # Serves POST /ingest
    │   │   └── HttpResponses.java                  # Shared response-writing helper
    │   ├── util/
    │   │   └── JsonUtil.java                       # Shared JSON parse/escape helpers
    │   └── exception/
    │       └── AssetIngestionException.java        # Custom checked exception
    └── test/java/com/flowcapture/mediaagent/
        ├── model/VideoAssetTest.java
        ├── repository/AssetDaoImplTest.java         # Runs against a real temp SQLite DB
        ├── service/IngestionServiceImplTest.java    # Tests parsing logic without hitting the network
        └── util/JsonUtilTest.java
```

> Each `.java` file's folder must match its `package` declaration exactly — never flatten these into one directory, or the compiler will reject them.

---

## 2. Compilation, Execution & Testing

This project now has a real dependency (the SQLite JDBC driver), so **Maven is required** to manage the classpath — plain `javac` alone won't have access to the driver jar.

### Prerequisites
- JDK 11 or later (`java -version` / `javac -version`)
- Maven 3.6+ (`mvn -version`)

### Run the test suite

```bash
mvn test
```

This runs all JUnit 5 tests: JSON parsing/escaping, domain model behavior, and full DAO round-trips against a real temporary SQLite database created fresh for each test.

### Build and run

```bash
mvn package
java -jar target/media-agent-1.0.0.jar
```

`mvn package` produces a **self-contained runnable jar** (via the Shade plugin) with the SQLite driver bundled in — no manual classpath assembly needed.

### What happens when it runs

1. `Main` wires up the DAO, Service, and API tiers.
2. It seeds the database with one ingested asset (an async HTTP call to a public JSON test endpoint, parsed into a `VideoAsset`, persisted via JDBC to `media_agent.db`).
3. It starts a REST server on `http://localhost:8080`.
4. From another terminal, you can interact with it live:

```bash
curl http://localhost:8080/assets
curl http://localhost:8080/assets/VID-1001
curl -X POST "http://localhost:8080/ingest?sourceUrl=https://jsonplaceholder.typicode.com/posts/2&assetId=VID-1002"
curl http://localhost:8080/assets
```

Press `Ctrl+C` to stop the server.

---

## 3. Architecture Breakdown

```
┌────────────┐     ┌────────────────┐     ┌────────────────┐     ┌──────────────────┐
│  API LAYER  │     │ SERVICE LAYER   │     │  MODEL LAYER    │     │ REPOSITORY / DAO   │
│ (HTTP I/O)  │ ──▶ │ (business logic)│ ──▶ │ (data & state)  │ ◀── │ (SQL persistence)  │
│             │     │                 │     │                 │     │                    │
│ AssetApiSvr │     │ IngestionService│     │ MediaAsset (abs)│     │ AssetDao (interface)│
│ handlers    │     │ Impl            │     │ VideoAsset      │     │ AssetDaoImpl (JDBC) │
└────────────┘     └────────────────┘     └────────────────┘     └──────────────────┘
```

- **API Layer** (`api` package) — the newest tier. Uses the JDK's built-in `com.sun.net.httpserver.HttpServer` (no framework) to expose the ingestion workflow and stored data over real HTTP endpoints. It depends only on the `IngestionService` and `AssetDao` interfaces, never on their concrete implementations.
- **Service Layer** (`service` package) — owns the ingestion *workflow*: fetching from an external source, parsing the response, and coordinating persistence. Knows nothing about HTTP servers or SQL.
- **Model Layer** (`model` package) — owns the *shape and identity* of a media asset, including how it serializes itself to JSON (via a polymorphic `toJson()` method).
- **Repository/DAO Layer** (`repository` package) — owns *persistence mechanics* against a real embedded SQLite database via JDBC, using parameterized SQL and `INSERT ... ON CONFLICT DO UPDATE` (upsert) semantics so re-ingesting the same asset id updates the row instead of duplicating it.
- **`Main`** — the composition root, the only place concrete implementations are instantiated and wired together.

---

## 4. Core OOP Principles Utilized

| Principle | Where it lives | How it's demonstrated |
|---|---|---|
| **Encapsulation** | `MediaAsset`, `VideoAsset` | All fields are `private`; access is only through public getters/setters. |
| **Inheritance** | `VideoAsset extends MediaAsset` | Reuses `assetId`, `title`, `status` from the parent; adds video-specific metadata. |
| **Abstraction / Polymorphism** | `MediaAsset.getAssetType()`, `MediaAsset.toJson()` | Both are abstract methods; `AssetsHandler` calls `toJson()` on any `MediaAsset` polymorphically, without knowing the concrete subclass. |
| **Interface Segregation** | `AssetDao`, `IngestionService` | Each interface exposes only the operations its consumers need. The API layer, service layer, and tests all depend on these interfaces, never on `AssetDaoImpl` or `IngestionServiceImpl` directly. |
| **Checked Exception Handling** | `AssetIngestionException` | Forces every caller of ingestion/persistence operations to explicitly handle failure. |
| **Dependency Injection (manual)** | `Main`, all constructors | Every class receives its collaborators through its constructor rather than constructing them itself, which is what makes `AssetDaoImplTest` able to inject a temporary test database. |

---

## 5. Testing Strategy

- **`JsonUtilTest`** — pure unit tests on the JSON parsing/escaping helpers, including edge cases (missing key, null input, multiple keys).
- **`VideoAssetTest`** — verifies encapsulation (getters/setters), polymorphic behavior (`getAssetType()`), and JSON serialization correctness.
- **`AssetDaoImplTest`** — an integration-style test that runs against a **real** SQLite database file created fresh in a JUnit-managed temporary directory per test (`@TempDir`), proving the SQL round-trips actually work — including a dedicated test proving the upsert logic updates rather than duplicates.
- **`IngestionServiceImplTest`** — tests the JSON-to-domain-object parsing logic directly (the `parseVideoAsset` method is package-private specifically to make this possible) using fixed JSON strings, so the suite runs fast and deterministically without a live network call on every run.

---

## 6. How to Talk About This Project in an Interview

1. **Lead with the architecture, then the "why."**
   *"The DAO layer persists through JDBC to SQLite, but the service and API layers only ever depend on the `AssetDao` interface. If we swapped SQLite for PostgreSQL in production, only `AssetDaoImpl` would change."*

2. **Be ready to explain the upsert decision.**
   *"My first version of this used a flat file and just appended rows, which meant re-running ingestion created duplicates. I fixed that at the database level with `INSERT ... ON CONFLICT DO UPDATE`, and I wrote a specific test — `saveAsset_withSameId_updatesExistingRowInsteadOfDuplicating` — to prove it."* This shows you iterate and verify, not just implement.

3. **Justify not using a web framework.**
   *"I used the JDK's built-in `HttpServer` instead of Spring Boot deliberately, to keep the REST layer's mechanics fully visible and explainable line-by-line. In a real production service I'd reach for Spring Boot or a similar framework for things like request validation, routing, and middleware — but for a learning-focused project, hand-rolling it means I actually understand what the framework would otherwise hide."*

4. **Explain the package-private testing pattern.**
   *"`parseVideoAsset` doesn't have an access modifier, which makes it package-private. That's deliberate — it lets my test suite call it directly with fixed JSON strings, so my tests are fast and don't depend on network availability, while the network-calling method (`fetchRawJson`) stays private since nothing external needs to test it in isolation."*

5. **Acknowledge the deliberate simplifications, and what you'd change for production scale.**
   *"I hand-rolled JSON parsing to keep the example transparent. At production scale I'd use Jackson, and I'd add connection pooling (e.g. HikariCP) rather than opening a new JDBC connection per call, plus proper request validation and structured logging on the API layer."*

---

## 7. Tech Stack

- **Language:** Java SE 11+
- **Persistence:** SQLite via JDBC (`org.xerial:sqlite-jdbc`) — a real embedded relational database, not a flat file
- **REST API:** JDK-native `com.sun.net.httpserver.HttpServer` — no external web framework
- **Testing:** JUnit 5 (Jupiter)
- **Build:** Maven (`mvn test`, `mvn package`) — produces a self-contained runnable jar via the Shade plugin
