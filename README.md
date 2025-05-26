# as-space

A Java library for managing rockets and missions.

## Assumptions

- Rocket and Mission names must be unique across the system — they serve as identifiers.
- Each newly created rocket starts with a default status: `ON_GROUND`.
- Each newly created mission starts with a default status: `SCHEDULED`.
- Business logic (e.g., validation of duplicates) is handled at the service layer, not the repository.
- The in-memory repository is the current persistence mechanism, and it may be replaced in the future by a persistent
  database.
- Rockets already assigned to mission cannot be assigned to new mission.
- Rockets and Missions have to be created first in order to be managed.
- Rockets `ON_GROUND` can be changed to `IN_REPAIR` by service.
- Rockets `ON_GROUND` can be changed to `IN_SPACE` by only assigning to mission.
- Rocket can be assigned to mission one by one, or multiple at once.
- If it's needed to verify if rocket assignment to mission was successful, use one by one method. For multiple
  assignment, no exception is thrown for nonexistent / already assigned rockets.
- Mission statuses are managed by changing rocket statuses. The only direct mission status change possible is to finish mission (set ENDED status).
- There was one inconsistency in requirements. It was stated that : 
“On ground” – initial status, where the rocket is not assigned to any mission 
and:
“Pending” – at least one rocket is assigned and one or more assigned rockets are in repair
but then in report there was an example where Luna 1 mission is in Status "Pending" having 2 dragons in status "On Ground"
- It was decided to implement status requirements, assuming there was an error in report example.
- Rocket-to-mission assignment is stored in the Rocket record to prioritize data consistency in mission and rocket management operations. This design may slightly delay report generation, as data must be derived from rocket records, but ensures a single source of truth. The system favors consistency over efficiency.
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
service.assignRocketToMission("Red Dragon","Mars");
```

To assign multiple rockets at once to mission, use the `assignRocketsToMission` method in the `ManagementService` class:

```java
import as.space.repository.InMemoryRocketRepository;

ManagementService service = new ManagementService(new InMemoryRocketRepository(), new InMemoryMissionRepository());
service.assignRocketsToMission(List.of("Red Dragon", "Blue Dragon"), "Mars");
```

To change rocket status, use the `changeRocketStatus` method in the `ManagementService` class:

```java
import as.space.repository.InMemoryRocketRepository;

ManagementService service = new ManagementService(new InMemoryRocketRepository(), new InMemoryMissionRepository());
service.changeRocketStatus("Red Dragon",RocketStatus.IN_REPAIR);
```

To finish mission, use the `finishMission` method in the `ManagementService` class:

```java
import as.space.repository.InMemoryRocketRepository;

ManagementService service = new ManagementService(new InMemoryRocketRepository(), new InMemoryMissionRepository());
service.finishMission("Mars");
```
### Report Generation

To genrate a report, use the `generateReport` method in the `ReportService` class:

```java
ReportService service = new ReportService(new InMemoryRocketRepository(), new InMemoryMissionRepository());
String report = service.generateReport();
```

## Requirements

- Java 17 or higher
- Maven 3.6+