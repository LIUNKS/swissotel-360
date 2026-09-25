# Hexagonal Architecture and DDD (Domain-Driven Design) Rules

This project strictly follows the principles of **Hexagonal Architecture (Ports and Adapters)** combined with **Domain-Driven Design (DDD)**. 
Any code generated, modified, or refactored by the AI assistant MUST adhere to the following rules and layer structure.

## 1. Golden Rule (Dependencies)
- Dependencies must always point **inwards**.
- The `domain` layer must NOT depend on anything external (zero Spring MVC, JPA, Jackson, or framework-specific annotations). It must be pure Java.
- The `application` layer orchestrates use cases and depends only on `domain`.
- The `infrastructure` and `interfaces` layers are external adapters that depend on `application` and `domain`.

---

## 2. Bounded Contexts Structure (Modules)
Each module in the system represents an independent Bounded Context. They must never share databases directly or couple their internal models. Cross-communication is done strictly through the `interfaces` layer.
Each Bounded Context is divided into 4 main layers:

### A. Domain Layer (`domain/`)
The heart of the business logic. It has no knowledge of databases or web frameworks.
- **model/aggregates:** Main classes that protect business invariants and consistency. Entry points for operations.
- **model/entities:** Entities with a lifecycle, governed by an Aggregate Root.
- **model/valueobjects:** Immutable objects that validate themselves upon creation (use Java `record` whenever possible).
- **exceptions:** Business-specific exceptions (`DomainException`).
- **commands / queries:** `record` structures that transport data for use cases.
- **events:** Internal Domain Events (`DomainEvent`).
- **repositories:** Ports (Interfaces). Pure contracts for persistence.

### B. Application Layer (`application/`)
Orchestrates the flow but contains NO business logic.
- **internal/commandservices:** Handle write Commands. They delegate to the Aggregate and save via the Repository.
- **internal/queryservices:** Handle read Queries.
- **internal/eventhandlers:** Listen to internal domain events and react accordingly.
- **internal/outboundservices/acl:** Outbound adapters. Where this domain consumes the facade (ACL) of other domains.

### C. Infrastructure Layer (`infrastructure/`)
The outside world, technology, and frameworks.
- **persistence/jpa/adapters:** Implement the domain `Repository` interfaces (The Adapters).
- **persistence/jpa/repositories:** Spring Data JPA interfaces.
- **persistence/jpa/entities:** Classes with `@Entity`, mapping to the database.
- **persistence/jpa/assemblers:** Translate Aggregates (Domain) ↔ Entities (JPA).
- **persistence/jpa/converters:** `AttributeConverter`s to map Value Objects to Database primitives.

### D. Interfaces Layer (`interfaces/`)
Entry points (Driving Adapters). How the outside world communicates with the domain.
- **rest/:** HTTP Controllers. They contain zero business logic.
- **rest/resources:** DTOs (Data Transfer Objects). Use Java `record`. Define the JSON payload format.
- **rest/transform:** Translate DTOs ↔ Commands/Queries and Domain/Entities ↔ DTOs.
- **acl/:** Anti-Corruption Layer (Facade). The contract (interface) this Bounded Context offers to other modules for synchronous calls, avoiding entity coupling.
- **events/:** Integration Events. Public events emitted by this domain for asynchronous communication with the rest of the system.

---

## 3. Implementation Guidelines (Conventions)

1. **Immutability:** Use native Java `record`s for Commands, Queries, Events, Resources (DTOs), and Value Objects.
2. **Value Objects vs Database Entities:** JPA Entities are technical artifacts. They must NEVER leak into the Domain layer or be returned in a REST Controller.
3. **Inter-Domain Communication:**
   - **Synchronous:** Inject the Facade interface (ACL) of the target domain via an Outbound Service. Never query another domain's repository directly.
   - **Asynchronous:** Publish Integration Events and listen to them via EventHandlers.
4. **Transformations:** Use stateless "Assembler" classes (static methods) to translate between models (e.g., `Resource` to `Command`, or `JPA Entity` to `Domain Aggregate`).

---

## 4. Spring Boot Specific Practices (Framework)

1. **Dependency Injection:** NEVER use `@Autowired` on fields. Always use **Constructor Injection** with `final` attributes.
2. **API Documentation (Swagger/OpenAPI):** 
   - All controllers in `interfaces/rest` must be annotated with `@Tag`.
   - All endpoints (`@GetMapping`, `@PostMapping`, etc.) must have `@Operation` and `@ApiResponses`.
   - DTOs (`Resources`) must use `@Schema` to document examples and validation constraints.
3. **Data Validation (Bean Validation):** 
   - Always use `@Valid` in controller parameters that receive a Resource.
   - Use `@NotBlank`, `@NotNull`, `@Size`, etc., on the fields of DTOs (`Resources`) and `Commands`.
4. **Package Naming:** Strictly follow the format `[company].[project].[bounded_context].[layer].[sublayer]`. All lowercase, no special characters.

---

## 5. Shared Kernel

Any code that needs to be reused by multiple Bounded Contexts must be placed in the `shared` module. 
**CRITICAL RULE:** The `shared` module MUST replicate the 4-layer structure (`domain`, `application`, `infrastructure`, `interfaces`). Generic utilities must be placed in their corresponding layer to avoid breaking dependency isolation (e.g., Aggregate base classes go in `shared/domain`, while HTTP error formatters go in `shared/interfaces`).
