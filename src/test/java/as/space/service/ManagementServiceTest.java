package as.space.service;

import as.space.TestData;
import as.space.exception.CannotAssignToEndedMissionException;
import as.space.exception.MissionNotFoundException;
import as.space.exception.RocketAlreadyAssignedException;
import as.space.exception.RocketNotFoundException;
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
    private ManagementService managementService;


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
        missionService.createNewMission(TestData.MARS);

        // TODO :  After implementing change rocket status feature, create rocket and change status with service methods
        Rocket rocket2 = new Rocket(TestData.DRAGON_XL, RocketStatus.IN_REPAIR, null);
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
        missionService.createNewMission(TestData.MARS);
        rocketService.createNewRocket(TestData.DRAGON_XL);
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
        rocketService.createNewRocket(TestData.RED_DRAGON);

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.ON_GROUND, rocketFound.get().status());
        assertEquals(MissionStatus.ENDED, missionFound.get().status());

        assertThrows(CannotAssignToEndedMissionException.class, () ->
                managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS)
        );
        assertNull(rocketFound.get().mission());
    }

    @Test
    void shouldThrowExceptionWhenAssignInSpaceRocketToScheduledMission() {
        missionService.createNewMission(TestData.MARS);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_SPACE, TestData.MOON);
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
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_SPACE, TestData.MOON);
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
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_SPACE, TestData.MOON);
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
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_SPACE, TestData.MOON);
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
        missionService.createNewMission(TestData.MARS);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        missionService.createNewMission(TestData.MOON);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR, null);
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
        missionService.createNewMission(TestData.MOON);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR, null);
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
        missionService.createNewMission(TestData.MOON);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR, null);
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
        missionService.createNewMission(TestData.MOON);
        // TODO : After change rocket status feature implemented, create rocket and change status using service methods
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR, null);
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
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR, null);
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
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR, null);
        rocketRepository.save(rocket);
        Rocket rocket2 = new Rocket(TestData.DRAGON_XL, RocketStatus.IN_REPAIR, null);
        rocketRepository.save(rocket2);
        missionService.createNewMission(TestData.MARS);
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
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR, null);
        rocketRepository.save(rocket);

        rocketService.createNewRocket(TestData.DRAGON_XL);
        missionService.createNewMission(TestData.MARS);
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
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR, null);
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

    // Rocket Status

    @Test
    void shouldChangeOnGroundRocketToInRepair() {
        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocketFound.isPresent());
        assertEquals(RocketStatus.IN_REPAIR, rocketFound.get().status());
    }

    @Test
    void shouldThrowExceptionWhenChangeOnGroundRocketToInSpace() {
        rocketService.createNewRocket(TestData.RED_DRAGON);
        assertThrows(OperationNotAllowedException.class, () ->
                managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_SPACE));

    }

    @Test
    void shouldChangeInSpaceRocketToInRepair() {
        rocketService.createNewRocket(TestData.RED_DRAGON);
        missionService.createNewMission(TestData.MARS);
        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS);

        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionFound.isPresent());
        assertEquals(RocketStatus.IN_REPAIR, rocketFound.get().status());
        assertEquals(MissionStatus.PENDING, missionFound.get().status());
        assertEquals(1, missionFound.get().inRepairCnt());
        assertEquals(0, missionFound.get().InSpaceCnt());
        assertEquals(1, missionFound.get().AllRocketsCnt());
    }

    @Test
    void shouldChangeInSpaceRocketToOnGround() {
        rocketService.createNewRocket(TestData.RED_DRAGON);
        missionService.createNewMission(TestData.MARS);
        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS);

        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.ON_GROUND);

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionFound.isPresent());
        assertEquals(RocketStatus.ON_GROUND, rocketFound.get().status());
        assertNull(rocketFound.get().mission());
        assertEquals(MissionStatus.SCHEDULED, missionFound.get().status());
        assertEquals(0, missionFound.get().inRepairCnt());
        assertEquals(0, missionFound.get().InSpaceCnt());
        assertEquals(0, missionFound.get().AllRocketsCnt());
    }

    @Test
    void shouldChangeInRepairRocketUnassignedToOnGround() {
        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocketFound.isPresent());
        assertEquals(RocketStatus.IN_REPAIR, rocketFound.get().status());

        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.ON_GROUND);
        Optional<Rocket> rocketFoundAfterOperation = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocketFoundAfterOperation.isPresent());
        assertEquals(RocketStatus.ON_GROUND, rocketFoundAfterOperation.get().status());
    }


    @Test
    void shouldChangeInRepairRocketAssignedToOnGround() {
        rocketService.createNewRocket(TestData.RED_DRAGON);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        missionService.createNewMission(TestData.MARS);
        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS);
        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);

        Optional<Mission> initialMissionFound = missionRepository.findByName(TestData.MARS);
        assertTrue(initialMissionFound.isPresent());
        assertEquals(MissionStatus.IN_PROGRESS, initialMissionFound.get().status());
        assertEquals(0, initialMissionFound.get().inRepairCnt());
        assertEquals(2, initialMissionFound.get().InSpaceCnt());
        assertEquals(2, initialMissionFound.get().AllRocketsCnt());

        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);

        Optional<Mission> missionFound1 = missionRepository.findByName(TestData.MARS);
        assertTrue(missionFound1.isPresent());
        assertEquals(MissionStatus.PENDING, missionFound1.get().status());
        assertEquals(1, missionFound1.get().inRepairCnt());
        assertEquals(1, missionFound1.get().InSpaceCnt());
        assertEquals(2, missionFound1.get().AllRocketsCnt());

        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.ON_GROUND);
        
        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionFound.isPresent());
        assertEquals(RocketStatus.ON_GROUND, rocketFound.get().status());
        assertNull(rocketFound.get().mission());
        assertEquals(MissionStatus.IN_PROGRESS, missionFound.get().status());
        assertEquals(0, missionFound.get().inRepairCnt());
        assertEquals(1, missionFound.get().InSpaceCnt());
        assertEquals(1, missionFound.get().AllRocketsCnt());

    }

    @Test
    void shouldThrowExceptionWhenChangeInRepairRocketUnassignedToInSpace() {
        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        assertThrows(OperationNotAllowedException.class, () ->
                managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_SPACE));

    }

    @Test
    void shouldChangeTheOnlyInRepairRocketAssignedToInSpace() {
        rocketService.createNewRocket(TestData.RED_DRAGON);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        missionService.createNewMission(TestData.MARS);
        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS);
        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);

        Optional<Mission> initialMissionFound = missionRepository.findByName(TestData.MARS);
        assertTrue(initialMissionFound.isPresent());
        assertEquals(MissionStatus.IN_PROGRESS, initialMissionFound.get().status());
        assertEquals(0, initialMissionFound.get().inRepairCnt());
        assertEquals(2, initialMissionFound.get().InSpaceCnt());
        assertEquals(2, initialMissionFound.get().AllRocketsCnt());

        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);

        Optional<Mission> missionFound1 = missionRepository.findByName(TestData.MARS);
        assertTrue(missionFound1.isPresent());
        assertEquals(MissionStatus.PENDING, missionFound1.get().status());
        assertEquals(1, missionFound1.get().inRepairCnt());
        assertEquals(1, missionFound1.get().InSpaceCnt());
        assertEquals(2, missionFound1.get().AllRocketsCnt());

        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_SPACE);


        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionFound.isPresent());
        assertEquals(RocketStatus.IN_SPACE, rocketFound.get().status());
        assertEquals(MissionStatus.IN_PROGRESS, missionFound.get().status());
        assertEquals(0, missionFound.get().inRepairCnt());
        assertEquals(2, missionFound.get().InSpaceCnt());
        assertEquals(2, missionFound.get().AllRocketsCnt());
    }

    @Test
    void shouldChangeOneOfInRepairRocketAssignedToInSpace() {
        rocketService.createNewRocket(TestData.RED_DRAGON);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        missionService.createNewMission(TestData.MARS);
        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MARS);
        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);

        Optional<Mission> initialMissionFound = missionRepository.findByName(TestData.MARS);
        assertTrue(initialMissionFound.isPresent());
        assertEquals(MissionStatus.IN_PROGRESS, initialMissionFound.get().status());
        assertEquals(0, initialMissionFound.get().inRepairCnt());
        assertEquals(2, initialMissionFound.get().InSpaceCnt());
        assertEquals(2, initialMissionFound.get().AllRocketsCnt());

        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        managementService.changeRocketStatus(TestData.DRAGON_XL, RocketStatus.IN_REPAIR);

        Optional<Mission> missionFound1 = missionRepository.findByName(TestData.MARS);
        assertTrue(missionFound1.isPresent());
        assertEquals(MissionStatus.PENDING, missionFound1.get().status());
        assertEquals(2, missionFound1.get().inRepairCnt());
        assertEquals(0, missionFound1.get().InSpaceCnt());
        assertEquals(2, missionFound1.get().AllRocketsCnt());

        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_SPACE);


        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionFound.isPresent());
        assertEquals(RocketStatus.IN_SPACE, rocketFound.get().status());
        assertEquals(MissionStatus.PENDING, missionFound.get().status());
        assertEquals(1, missionFound.get().inRepairCnt());
        assertEquals(1, missionFound.get().InSpaceCnt());
        assertEquals(2, missionFound.get().AllRocketsCnt());
    }

    @Test
    void shouldThrowExceptionWhenChangeStatusOfNonExistentRocket() {
        assertThrows(RocketNotFoundException.class, () ->
                managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_SPACE));
    }

}
