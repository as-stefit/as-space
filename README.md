# as-space

A Java library for managing rockets and missions.

## Assumptions

- Rocket names must be unique across the system — they serve as identifiers.
- Each newly created rocket starts with a default status: `ON_GROUND`.
- Business logic (e.g., validation of duplicates) is handled at the service layer, not the repository.
- The in-memory repository is the current persistence mechanism and  it may be replaced in the future by a persistent database.
- TDD is followed for each new feature.

## How to use

### Rocket creation

To create a new rocket, use the `createNewRocket` method in the `RocketService` class:

```java
RocketService service = new RocketService(new InMemoryRocketRepository());
Rocket rocket = service.createNewRocket("RedDragon");
```

## Requirements
- Java 17 or higher
- Maven 3.6+