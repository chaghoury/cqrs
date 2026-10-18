# AGENTS.md — AI Agent Guidance

> **Single source of truth** for all AI agents working in this repository.  
> `GEMINI.md` and `CLAUDE.md` both delegate here.

---

## Table of Contents

- [AGENTS.md — AI Agent Guidance](#agentsmd--ai-agent-guidance)
  - [Table of Contents](#table-of-contents)
  - [1. Project Overview](#1-project-overview)
  - [2. Technology Stack](#2-technology-stack)
  - [3. Project Structure](#3-project-structure)
  - [4. Build, Test \& Run Commands](#4-build-test--run-commands)
  - [5. API-First Workflow](#5-api-first-workflow)
  - [6. Module Boundaries (Spring Modulith)](#6-module-boundaries-spring-modulith)
  - [7. CQRS \& Event Sourcing Conventions](#7-cqrs--event-sourcing-conventions)
    - [Commands](#commands)
    - [Events](#events)
    - [Queries](#queries)
    - [Aggregate (`AccountAggregate`)](#aggregate-accountaggregate)
    - [Projection (`AccountProjection`)](#projection-accountprojection)
  - [8. Controller Pattern](#8-controller-pattern)
  - [9. Key URLs](#9-key-urls)
  - [10. Critical DO NOTs](#10-critical-do-nots)
  - [11. Adding a New Module](#11-adding-a-new-module)

---

## 1. Project Overview

This is a **CQRS + Event Sourcing** application built with [Axon Framework](https://www.axoniq.io/) on Spring Boot.
It follows a **modular monolith** approach using Spring Modulith, where the codebase is organized into vertical
feature modules (e.g., `account`, `user`) rather than horizontal layers (controllers / services / repositories).

The API is defined **contract-first** (API-First): the OpenAPI YAML specification is the single source of truth,
and Spring controller interfaces are **generated** from it at build time.

---

## 2. Technology Stack

| Concern               | Library / Version                                                       |
| --------------------- | ----------------------------------------------------------------------- |
| Runtime               | Java 21                                                                 |
| Framework             | Spring Boot 4.1.1                                                       |
| CQRS / Event Sourcing | Axon Framework 4.13.2 (in-memory event store — no Axon Server required) |
| Modular Monolith      | Spring Modulith 2.1.1                                                   |
| API Docs              | springdoc-openapi 3.1.0 (Swagger UI)                                    |
| Code Generation       | openapi-generator-maven-plugin (Spring server stubs)                    |
| Build                 | Maven (Wrapper: `./mvnw`)                                               |

---

## 3. Project Structure

```
src/
├── main/
│   ├── java/com/ultims/cqrs/
│   │   ├── CqrsApplication.java                  # Entry point
│   │   │
│   │   ├── account/                              # ← PUBLIC API of the account module
│   │   │   ├── CreateAccountCommand.java
│   │   │   ├── DepositMoneyCommand.java
│   │   │   ├── WithdrawMoneyCommand.java
│   │   │   ├── FindAccountByIdQuery.java
│   │   │   ├── FindAllAccountsQuery.java
│   │   │   ├── AccountView.java
│   │   │   ├── AccountCreatedEvent.java
│   │   │   ├── MoneyDepositedEvent.java
│   │   │   ├── MoneyWithdrawnEvent.java
│   │   │   └── internal/                         # ← PRIVATE internals (not accessible from other modules)
│   │   │       ├── command/
│   │   │       │   ├── AccountAggregate.java
│   │   │       │   └── AccountCommandController.java
│   │   │       └── query/
│   │   │           ├── AccountProjection.java
│   │   │           └── AccountQueryController.java
│   │   │
│   │   ├── hello/
│   │   │   └── internal/
│   │   │       └── HelloController.java
│   │   │
│   │   └── config/                               # Cross-cutting infrastructure config
│   │       ├── AxonConfig.java
│   │       ├── OpenApiConfig.java
│   │       ├── OpenApiController.java
│   │       └── WebMvcRegistrationsConfig.java
│   │
│   └── resources/
│       ├── application.yaml
│       └── static/
│           └── openapi.yaml                      # ← THE OpenAPI spec (edit this, not generated files)
│
├── test/
│   └── java/com/ultims/cqrs/
│       ├── CqrsApplicationTests.java             # Context load + Spring Modulith verify()
│       ├── account/internal/command/
│       │   ├── AccountAggregateTest.java
│       │   └── AccountCommandControllerTest.java
│       ├── account/internal/query/
│       │   ├── AccountProjectionTest.java
│       │   └── AccountQueryControllerTest.java
│       ├── hello/internal/HelloControllerTest.java
│       └── config/OpenApiControllerTest.java
│
└── target/generated-sources/                     # ⚠️ GENERATED — do NOT edit manually
    └── openapi/
        └── src/main/java/com/ultims/cqrs/
            ├── api/          # Generated interfaces: AccountApi, HelloApi
            └── dto/          # Generated DTOs
```

---

## 4. Build, Test & Run Commands

```bash
# Regenerate OpenAPI interfaces and DTOs from the YAML spec
./mvnw generate-sources

# Full clean build (includes code generation)
./mvnw clean compile

# Run all tests
./mvnw test

# Start the application (available at http://localhost:8080)
./mvnw spring-boot:run

# Package as JAR
./mvnw clean package

# Skip tests during packaging
./mvnw clean package -DskipTests
```

> **Always run `./mvnw generate-sources` after editing `openapi.yaml`** before writing or modifying any controller code.

---

## 5. API-First Workflow

The OpenAPI specification drives the entire API surface. Follow this workflow strictly:

```
1. Edit  →  src/main/resources/static/openapi.yaml
2. Run   →  ./mvnw generate-sources
3. Code  →  Implement/override methods in the appropriate controller
```

**Generated artifacts** (in `target/generated-sources/openapi/src/main/java/com/ultims/cqrs/`):

- `api/AccountApi.java` — interface with all account endpoints (unified `Account` tag)
- `api/HelloApi.java` — interface with hello endpoints
- `dto/*.java` — request/response DTOs

**Never add routing annotations** (`@GetMapping`, `@PostMapping`, `@RequestMapping`, etc.) to controllers.
All routing comes from implementing the generated interface.

---

## 6. Module Boundaries (Spring Modulith)

Spring Modulith enforces strict package-level visibility rules at test time via `ApplicationModuleTest`.

| Visibility  | Package pattern                      | Rule                                        |
| ----------- | ------------------------------------ | ------------------------------------------- |
| **Public**  | `com.ultims.cqrs.account` (root)     | Accessible from any other module            |
| **Private** | `com.ultims.cqrs.account.internal.*` | Only accessible within the `account` module |

**Rules to follow:**

- Only types in the **root package** of a module (e.g., `account/`) may be referenced by other modules.
- Types inside `internal/` are **private** — never import them from a sibling module.
- When a new module needs to use something from `account`, expose it via the root package, not by importing internals.
- `CqrsApplicationTests` runs `ApplicationModules.of(CqrsApplication.class).verify()` — this will **fail** if boundaries are violated.

---

## 7. CQRS & Event Sourcing Conventions

### Commands

- Command classes live in the **module's root package** (public).
- They are plain Java records or classes annotated with `@TargetAggregateIdentifier` on the ID field.
- Dispatched via `CommandGateway.sendAndWait(command)` from the command controller.
- Handled in the aggregate with `@CommandHandler`.

### Events

- Event classes live in the **module's root package** (public).
- Raised by the aggregate with `AggregateLifecycle.apply(event)`.
- Handled in the projection with `@EventHandler`.

### Queries

- Query classes live in the **module's root package** (public).
- Dispatched via `QueryGateway.query(query, ResponseTypes.instanceOf(...))` from the query controller.
- Handled in the projection with `@QueryHandler`.
- **Do NOT use `ResponseTypes.optionalInstanceOf(...)`** — use `ResponseTypes.instanceOf(AccountView.class)` and handle the nullability in the controller (return `404` when the result is `null`).

### Aggregate (`AccountAggregate`)

- Annotated with `@Aggregate`.
- ID field annotated with `@AggregateIdentifier`.
- No-arg constructor required (for Axon deserialization).
- State mutation only happens inside `@EventSourcingHandler` methods.

### Projection (`AccountProjection`)

- Annotated with `@Component`.
- Maintains a read model in memory (or a database for production).
- `@EventHandler` methods update the in-memory state.
- `@QueryHandler` methods answer queries from the read model.

---

## 8. Controller Pattern

Both `AccountCommandController` and `AccountQueryController` implement the **same** generated `AccountApi` interface
and each overrides only the operations relevant to its concern.

`WebMvcRegistrationsConfig` is in place to prevent Spring MVC from raising ambiguous mapping errors when two
controllers implement the same interface.

```java
// Command controller — handles write operations only
@RestController
public class AccountCommandController implements AccountApi {

    @Override
    public ResponseEntity<Void> createAccount(CreateAccountRequest request) { ... }

    @Override
    public ResponseEntity<Void> depositMoney(String id, DepositMoneyRequest request) { ... }

    @Override
    public ResponseEntity<Void> withdrawMoney(String id, WithdrawMoneyRequest request) { ... }
}

// Query controller — handles read operations only
@RestController
public class AccountQueryController implements AccountApi {

    @Override
    public ResponseEntity<AccountResponse> getAccountById(String id) { ... }

    @Override
    public ResponseEntity<List<AccountResponse>> getAllAccounts() { ... }
}
```

**Neither controller adds any routing annotations.** Routing is inherited from `AccountApi`.

---

## 9. Key URLs

| Resource                | URL                                           |
| ----------------------- | --------------------------------------------- |
| Application root        | `http://localhost:8080`                       |
| Swagger UI              | `http://localhost:8080/swagger-ui/index.html` |
| Raw OpenAPI spec (YAML) | `http://localhost:8080/openapi.yaml`          |
| Raw OpenAPI spec (JSON) | `http://localhost:8080/v3/api-docs`           |

---

## 10. Critical DO NOTs

| ❌ DO NOT                                              | ✅ DO instead                                                             |
| ------------------------------------------------------ | ------------------------------------------------------------------------- |
| Add `@GetMapping` / `@PostMapping` to controllers      | Let routing come from the generated `AccountApi` interface                |
| Edit files under `target/generated-sources/`           | Edit `openapi.yaml`, then run `./mvnw generate-sources`                   |
| Use `ResponseTypes.optionalInstanceOf(...)`            | Use `ResponseTypes.instanceOf(AccountView.class)` and null-check          |
| Import `internal.*` types from a sibling module        | Expose needed types in the module's root package                          |
| Duplicate endpoint operations across both controllers  | Each controller implements only its own subset of `AccountApi` operations |
| Add Swagger/OpenAPI annotations to controllers or DTOs | The spec is generated from YAML — annotations create duplicates           |
| Start Axon Server                                      | The app uses in-memory event store (`axon.axonserver.enabled: false`)     |
| Create a new OpenAPI YAML alongside the existing one   | There is one spec: `src/main/resources/static/openapi.yaml`               |

---

## 11. Adding a New Module

Follow this checklist when adding a new vertical feature module (e.g., `user`):

1. **Create the root package:** `com.ultims.cqrs.user/`
   - Place all public-facing types here: commands, queries, events, views.

2. **Create the internal package:** `com.ultims.cqrs.user/internal/`
   - Place private types here: aggregate, projection, controllers.

3. **Update `openapi.yaml`:**
   - Add a new tag (e.g., `User`) and define all endpoints.
   - Run `./mvnw generate-sources` to generate `UserApi` and DTOs.

4. **Implement controllers:**
   - Create `UserCommandController implements UserApi` (write ops only).
   - Create `UserQueryController implements UserApi` (read ops only).
   - Do NOT add routing annotations.

5. **Write tests:**
   - Unit tests for the aggregate, projection, and both controllers.
   - Verify module boundaries are not violated (Spring Modulith `verify()` runs automatically in `CqrsApplicationTests`).

6. **Do NOT cross module boundaries:**
   - If `user` needs something from `account`, only reference types in `com.ultims.cqrs.account` (root), never in `com.ultims.cqrs.account.internal.*`.
