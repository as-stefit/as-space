# as-space

A Java library for managing rockets and missions.

## Assumptions

- Rocket and Mission names must be unique across the system — they serve as identifiers.
- Each newly created rocket starts with a default status: `ON_GROUND`.
- Each newly created mission starts with a default status: `SCHEDULED`.
- Business logic (e.g., validation of duplicates) is handled at the service layer, not the repository.
- The in-memory repository is the current persistence mechanism, and it may be replaced in the future by a persistent database.
- Rockets already assigned to mission cannot be assigned to new mission.
- Rockets and Missions have to be created first in order to be managed.
- Rockets `ON_GROUND` can be changed to `IN_REPAIR` by service.
- Rockets `ON_GROUND` can be changed to `IN_SPACE` by only assigning to mission.
- TDD is followed for each new feature.

## How to use

### Rocket creation

To create a new rocket, use the `createNewRocket` method in the `RocketService` class:

```java
RocketService service = new RocketService(new InMemoryRocketRepository());
Rocket rocket = service.createNewRocket("Red Dragon");
```

### Mission creation

To create a new mission, use the `createNewMission` method in the `MissionService` class:

```java
MissionService service = new MissionService(new InMemoryMissionRepository());
Mission mission = service.createNewMission("Mars");
```

### Resource Management

To assign rocket to mission, use the `assignRocketToMission` method in the `ManagementService` class:

```java
import as.space.repository.InMemoryRocketRepository;

ManagementService service = new ManagementService(new InMemoryRocketRepository(), new InMemoryMissionRepository());
service.assignRocketToMission("Red Dragon", "Mars");
```

## Requirements
- Java 17 or higher
- Maven 3.6+