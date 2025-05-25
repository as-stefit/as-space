package as.space.service;

import as.space.TestData;
import as.space.model.Mission;
import as.space.model.MissionStatus;
import as.space.model.Rocket;
import as.space.model.RocketStatus;
import as.space.repository.InMemoryMissionRepository;
import as.space.repository.InMemoryRocketRepository;
import as.space.repository.MissionRepository;
import as.space.repository.RocketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ManagementServiceTest {
    private RocketRepository rocketRepository;
    private MissionRepository missionRepository;
    private RocketService rocketService;
    private MissionService missionService;


    @BeforeEach
    void setUp() {
        rocketRepository = new InMemoryRocketRepository();
        missionRepository = new InMemoryMissionRepository();
        rocketService = new RocketService(rocketRepository);
        missionService = new MissionService(missionRepository);

        managementService = new ManagementService(rocketRepository, missionRepository);
    }

    @Test
    void shouldAssignOnGroundRocketToScheduledMission() {

        Rocket rocket = rocketService.createNewRocket(TestData.RED_DRAGON);
        Mission mission = missionService.createNewMission(TestData.MARS);
        assertEquals(RocketStatus.ON_GROUND, rocket.status());
        assertEquals(MissionStatus.SCHEDULED, mission.status());

        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS);
        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_SPACE, rocketFound.get().status());
        assertEquals(TestData.MARS, rocketFound.get().mission());
        assertEquals(MissionStatus.IN_PROGRESS, missionFound.get().status());
    }

    @Test
    void shouldAssignOnGroundRocketToPendingMission() {
        Rocket rocket = rocketService.createNewRocket(TestData.RED_DRAGON);
        Mission mission = missionService.createNewMission(TestData.MARS);

        // TODO :  After implementing change rocket status feature, create rocket and change status with service methods
        Rocket rocket2 = new Rocket(TestData.DRAGON_XL, RocketStatus.IN_REPAIR);
        rocketRepository.save(rocket2);
        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.ON_GROUND, rocket.status());
        assertEquals(MissionStatus.PENDING, missionFound.get().status());

        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS);
        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionAfterAssignFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionAfterAssignFound.isPresent());

        assertEquals(RocketStatus.IN_SPACE, rocketFound.get().status());
        assertEquals(TestData.MARS, rocketFound.get().mission());
        assertEquals(MissionStatus.PENDING, missionAfterAssignFound.get().status());
    }

    @Test
    void shouldAssignOnGroundRocketToInProgressMission() {
        Rocket rocket = rocketService.createNewRocket(TestData.RED_DRAGON);
        Mission mission = missionService.createNewMission(TestData.MARS);
        Rocket rocket2 = rocketService.createNewRocket(TestData.DRAGON_XL);
        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.ON_GROUND, rocket.status());
        assertEquals(MissionStatus.IN_PROGRESS, missionFound.get().status());

        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS);
        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionAfterAssignFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionAfterAssignFound.isPresent());

        assertEquals(RocketStatus.IN_SPACE, rocketFound.get().status());
        assertEquals(TestData.MARS, rocketFound.get().mission());
        assertEquals(MissionStatus.IN_PROGRESS, missionAfterAssignFound.get().status());
    }

    @Test
    void shouldThrowExceptionWhenAssignOnGroundRocketToEndedMission() {
        // TODO : After change mission status feature implemented, create mission and change status using service methods
        Mission mission = new Mission(TestData.MARS, MissionStatus.ENDED);
        missionRepository.save(mission);
        Rocket rocket = rocketService.createNewRocket(TestData.RED_DRAGON);

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.ON_GROUND, rocketFound.get().status());
        assertEquals(MissionStatus.ENDED, missionFound.get().status());

        assertThrows(CannotAssignToEndedMissionException.class, () ->
                managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS)
        );
        assertEquals(null, rocketFound.get().mission());
    }

    @Test
    void shouldThrowExceptionWhenAssignInSpaceRocketToScheduledMission() {
        Mission mission = missionService.createNewMission(TestData.MARS);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_SPACE);
        rocketRepository.save(rocket);

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        String initialMission = rocketFound.get().mission();
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_SPACE, rocketFound.get().status());
        assertEquals(MissionStatus.SCHEDULED, missionFound.get().status());

        assertThrows(RocketAlreadyAssignedException.class, () ->
                managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS)
        );
        Optional<Rocket> rocketFoundAfterAssignmentTry = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocketFoundAfterAssignmentTry.isPresent());
        assertEquals(initialMission, rocketFoundAfterAssignmentTry.get().mission());
    }

    @Test
    void shouldThrowExceptionWhenAssignInSpaceRocketToPendingMission() {
        // TODO : After change mission status feature implemented, create mission and change status using service methods
        Mission mission = new Mission(TestData.MARS, MissionStatus.PENDING);
        missionRepository.save(mission);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_SPACE);
        rocketRepository.save(rocket);

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        String initialMission = rocketFound.get().mission();
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_SPACE, rocketFound.get().status());
        assertEquals(MissionStatus.PENDING, missionFound.get().status());

        assertThrows(RocketAlreadyAssignedException.class, () ->
                managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS)
        );
        Optional<Rocket> rocketFoundAfterAssignmentTry = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocketFoundAfterAssignmentTry.isPresent());
        assertEquals(initialMission, rocketFoundAfterAssignmentTry.get().mission());
    }

    @Test
    void shouldThrowExceptionWhenAssignInSpaceRocketToInProgressMission() {
        // TODO : After change mission status feature implemented, create mission and change status using service methods
        Mission mission = new Mission(TestData.MARS, MissionStatus.IN_PROGRESS);
        missionRepository.save(mission);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_SPACE);
        rocketRepository.save(rocket);

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        String initialMission = rocketFound.get().mission();
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_SPACE, rocketFound.get().status());
        assertEquals(MissionStatus.IN_PROGRESS, missionFound.get().status());

        assertThrows(RocketAlreadyAssignedException.class, () ->
                managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS)
        );
        Optional<Rocket> rocketFoundAfterAssignmentTry = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocketFoundAfterAssignmentTry.isPresent());
        assertEquals(initialMission, rocketFoundAfterAssignmentTry.get().mission());
    }

    @Test
    void shouldThrowExceptionWhenAssignInSpaceRocketToEndedMission() {
        // TODO : After change mission status feature implemented, create mission and change status using service methods
        Mission mission = new Mission(TestData.MARS, MissionStatus.ENDED);
        missionRepository.save(mission);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_SPACE);
        rocketRepository.save(rocket);

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        String initialMission = rocketFound.get().mission();
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_SPACE, rocketFound.get().status());
        assertEquals(MissionStatus.ENDED, missionFound.get().status());

        assertThrows(CannotAssignToEndedMissionException.class, () ->
                managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS)
        );
        Optional<Rocket> rocketFoundAfterAssignmentTry = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocketFoundAfterAssignmentTry.isPresent());
        assertEquals(initialMission, rocketFoundAfterAssignmentTry.get().mission());
    }

    @Test
    void shouldThrowExceptionWhenAssignInRepairAssignedRocketToScheduledMission() {
        Mission mission = missionService.createNewMission(TestData.MARS);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Mission mission2 = missionService.createNewMission(TestData.MOON);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        rocketRepository.save(rocket);
        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MOON);

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        String initialMission = rocketFound.get().mission();
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocketFound.get().status());
        assertEquals(MissionStatus.SCHEDULED, missionFound.get().status());

        assertThrows(RocketAlreadyAssignedException.class, () ->
                managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS)
        );
        Optional<Rocket> rocketFoundAfterAssignmentTry = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocketFoundAfterAssignmentTry.isPresent());
        assertEquals(initialMission, rocketFoundAfterAssignmentTry.get().mission());
    }

    @Test
    void shouldThrowExceptionWhenAssignInRepairAssignedRocketToPendingMission() {
        // TODO : After change mission status feature implemented, create mission and change status using service methods
        Mission mission = new Mission(TestData.MARS, MissionStatus.PENDING);
        missionRepository.save(mission);
        Mission mission2 = missionService.createNewMission(TestData.MOON);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        rocketRepository.save(rocket);
        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MOON);

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        String initialMission = rocketFound.get().mission();
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocketFound.get().status());
        assertEquals(MissionStatus.PENDING, missionFound.get().status());

        assertThrows(RocketAlreadyAssignedException.class, () ->
                managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS)
        );
        Optional<Rocket> rocketFoundAfterAssignmentTry = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocketFoundAfterAssignmentTry.isPresent());
        assertEquals(initialMission, rocketFoundAfterAssignmentTry.get().mission());
    }

    @Test
    void shouldThrowExceptionWhenAssignInRepairAssignedRocketToInProgressMission() {
        // TODO : After change mission status feature implemented, create mission and change status using service methods
        Mission mission = new Mission(TestData.MARS, MissionStatus.IN_PROGRESS);
        missionRepository.save(mission);
        Mission mission2 = missionService.createNewMission(TestData.MOON);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        rocketRepository.save(rocket);
        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MOON);

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        String initialMission = rocketFound.get().mission();
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocketFound.get().status());
        assertEquals(MissionStatus.IN_PROGRESS, missionFound.get().status());

        assertThrows(RocketAlreadyAssignedException.class, () ->
                managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS)
        );
        Optional<Rocket> rocketFoundAfterAssignmentTry = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocketFoundAfterAssignmentTry.isPresent());
        assertEquals(initialMission, rocketFoundAfterAssignmentTry.get().mission());
    }

    @Test
    void shouldThrowExceptionWhenAssignInRepairAssignedRocketToEndedMission() {
        // TODO : After change mission status feature implemented, create mission and change status using service methods
        Mission mission = new Mission(TestData.MARS, MissionStatus.ENDED);
        missionRepository.save(mission);
        Mission mission2 = missionService.createNewMission(TestData.MOON);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        rocketRepository.save(rocket);
        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MOON);

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        String initialMission = rocketFound.get().mission();
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocketFound.get().status());
        assertEquals(MissionStatus.ENDED, missionFound.get().status());

        assertThrows(CannotAssignToEndedMissionException.class, () ->
                managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS)
        );
        Optional<Rocket> rocketFoundAfterAssignmentTry = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocketFoundAfterAssignmentTry.isPresent());
        assertEquals(initialMission, rocketFoundAfterAssignmentTry.get().mission());
    }

    @Test
    void shouldAssignInRepairUnassignedRocketToScheduledMission() {
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        rocketRepository.save(rocket);
        Mission mission = missionService.createNewMission(TestData.MARS);
        assertEquals(RocketStatus.IN_REPAIR, rocket.status());
        assertEquals(MissionStatus.SCHEDULED, mission.status());

        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS);
        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocketFound.get().status());
        assertEquals(TestData.MARS, rocketFound.get().mission());
        assertEquals(MissionStatus.PENDING, missionFound.get().status());
    }

    @Test
    void shouldAssignInRepairUnassignedRocketToPendingMission() {
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        rocketRepository.save(rocket);
        Rocket rocket2 = new Rocket(TestData.DRAGON_XL, RocketStatus.IN_REPAIR);
        rocketRepository.save(rocket2);
        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocket.status());
        assertEquals(MissionStatus.PENDING, missionFound.get().status());

        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS);
        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionAfterAssignFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionAfterAssignFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocketFound.get().status());
        assertEquals(TestData.MARS, rocketFound.get().mission());
        assertEquals(MissionStatus.PENDING, missionAfterAssignFound.get().status());
    }

    @Test
    void shouldAssignInRepairUnassignedRocketToInProgressMission() {
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        rocketRepository.save(rocket);

        Rocket rocket2 = rocketService.createNewRocket(TestData.DRAGON_XL);
        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocket.status());
        assertEquals(MissionStatus.IN_PROGRESS, missionFound.get().status());

        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS);
        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionAfterAssignFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionAfterAssignFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocketFound.get().status());
        assertEquals(TestData.MARS, rocketFound.get().mission());
        assertEquals(MissionStatus.PENDING, missionAfterAssignFound.get().status());

    }

    @Test
    void shouldThrowExceptionWhenAssignInRepairRocketToEndedMission() {
        // TODO : After change mission status feature implemented, create mission and change status using service methods
        Mission mission = new Mission(TestData.MARS, MissionStatus.ENDED);
        missionRepository.save(mission);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        rocketRepository.save(rocket);

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        String initialMission = rocketFound.get().mission();
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocketFound.get().status());
        assertEquals(MissionStatus.ENDED, missionFound.get().status());

        assertThrows(CannotAssignToEndedMissionException.class, () ->
                managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS)
        );
        Optional<Rocket> rocketFoundAfterAssignmentTry = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocketFoundAfterAssignmentTry.isPresent());
        assertEquals(initialMission, rocketFoundAfterAssignmentTry.get().mission());
    }

    @Test
    void shouldThrowExceptionWhenRocketDoesNotExist() {
        missionService.createNewMission(TestData.MARS);
        assertThrows(RocketNotFoundException.class, () ->
                managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS)
        );
    }

    @Test
    void shouldThrowExceptionWhenMissionDoesNotExist() {
        rocketService.createNewRocket(TestData.RED_DRAGON);
        assertThrows(MissionNotFoundException.class, () ->
                managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS)
        );
    }

}
