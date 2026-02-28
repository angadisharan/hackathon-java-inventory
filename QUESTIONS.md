# Questions

Here are 2 questions related to the codebase. There's no right or wrong answer - we want to understand your reasoning.

## Question 1: API Specification Approaches

When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded everything directly. 

What are your thoughts on the pros and cons of each approach? Which would you choose and why?

**Answer:**
```txt
OPENAPI-FIRST (Warehouse API)
- We define the API in warehouse-openapi.yaml: paths, request/response schemas, status codes.
- The Quarkus OpenAPI generator produces the WarehouseResource interface and Warehouse bean; we implement WarehouseResourceImpl to wire domain logic.
- Pros: Single source of truth; Swagger/OpenAPI docs stay in sync; client SDKs can be generated; contract is explicit for frontend/partners; changes to the API require updating the spec first, which encourages design review.
- Cons: Generated code can be rigid (e.g. method names, return types); we must map between API models and domain models (e.g. com.warehouse.api.beans.Warehouse vs domain Warehouse); adding new operations means editing YAML and regenerating; tooling and version upgrades can force churn.

HAND-CODED (Product, Store)
- We write JAX-RS resources directly (ProductResource, StoreResource): @Path, @GET/@POST, request/response types, exception handling.
- Pros: Full control over signatures, naming, and structure; no codegen step; easy to tailor to domain (e.g. Store’s event firing, transaction sync); fast to iterate for internal or simple APIs.
- Cons: No single machine-readable contract; docs (e.g. Swagger) may drift unless maintained separately; client generation is manual or ad hoc; consistency across endpoints depends on discipline.

RECOMMENDATION
- I would use OpenAPI-first for any API that is (or will be) consumed by multiple clients, frontends, or partners, or where the contract is a deliverable. For this codebase, that fits the Warehouse API.
- I would keep hand-coded resources for internal, stable, or prototype endpoints (like Product and Store here) unless we need a formal contract or client generation. If the system grows and Product/Store become part of a public or multi-team API, I’d introduce an OpenAPI spec for them and align with the same pattern as Warehouse.
```

---

## Question 2: Testing Strategy

Given the need to balance thorough testing with time and resource constraints, how would you prioritize tests for this project? 

Which types of tests (unit, integration, parameterized, etc.) would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
PRIORITY ORDER (given time/resource limits)

1. Unit tests on domain use cases (highest value)
   - Test business rules in isolation with mocked ports (e.g. ArchiveWarehouseUseCase, ReplaceWarehouseUseCase, CreateWarehouseUseCase). They are fast, stable, and document behaviour. Focus on validation paths, error messages, and state changes. Example: ArchiveWarehouseUseCaseTest for “cannot archive non-existent”, “cannot archive already archived”, and successful archive.

2. Integration tests for REST and persistence
   - Test that HTTP endpoints wire correctly to use cases and return the right status codes and bodies (e.g. WarehouseEndpointIT, ProductEndpointTest). Test transaction boundaries and side effects (e.g. StoreTransactionIntegrationTest: legacy not notified on failed store creation). Use the same stack as production (e.g. real DB in tests via H2 or Testcontainers) so we catch mapping and transaction bugs.

3. Parameterized tests for validation edge cases
   - One test method, many inputs (e.g. WarehouseValidationTest with invalid capacity/location/duplicate code). They expand coverage with little extra code and make it easy to add new cases. Use for “all invalid combinations” and boundary values.

4. Concurrency / optimistic locking tests (where the domain requires it)
   - Tests like ArchiveWarehouseUseCaseTest.testConcurrentArchiveAndStockUpdateCausesOptimisticLockException and ReplaceWarehouseUseCaseTest.testConcurrentReplaceCausesLostUpdates ensure we don’t lose updates and that the repository uses @Version correctly. Add these where the assignment explicitly cares about concurrency.

5. Broader integration tests (e.g. WarehouseConcurrencyIT, WarehouseTestcontainersIT)
   - Run in CI when possible; they are slower and may need DB/containers. Use for “real DB” behaviour and concurrent create/read scenarios.

KEEPING COVERAGE EFFECTIVE OVER TIME
- Prefer testing behaviour (inputs → outputs, errors) over implementation details so refactors don’t break tests unnecessarily.
- Keep use-case tests independent of REST/DB: mock WarehouseStore, LocationResolver, etc., so domain logic can be tested without infrastructure.
- Add a new parameterized case whenever we discover a new validation or edge case; add an integration test when we add or change an endpoint or transaction boundary.
- Run unit and parameterized tests on every commit; run heavier integration and IT tests in CI (and optionally on pre-push). Fix flakiness immediately (e.g. by fixing transaction boundaries or test data isolation) so the suite stays trustworthy.
```
