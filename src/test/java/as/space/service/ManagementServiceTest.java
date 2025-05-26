package as.space.service;

import as.space.TestData;
import as.space.exception.*;
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

import java.util.List;
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

        rocketService.createNewRocket(TestData.DRAGON_XL);
        managementService.changeRocketStatus(TestData.DRAGON_XL, RocketStatus.IN_REPAIR);
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
        Mission mission = new Mission(TestData.MARS, MissionStatus.ENDED, 0, 0, 0);
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
        missionService.createNewMission(TestData.MOON);
        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MOON);

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
        missionService.createNewMission(TestData.MARS);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        managementService.changeRocketStatus(TestData.DRAGON_XL, RocketStatus.IN_REPAIR);
        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);
        rocketService.createNewRocket(TestData.RED_DRAGON);
        missionService.createNewMission(TestData.MOON);
        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MOON);

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
        rocketService.createNewRocket(TestData.DRAGON_XL);
        missionService.createNewMission(TestData.MARS);
        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);

        rocketService.createNewRocket(TestData.RED_DRAGON);
        missionService.createNewMission(TestData.MOON);
        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MOON);

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
        Mission mission = new Mission(TestData.MARS, MissionStatus.ENDED, 0, 0, 0);
        missionRepository.save(mission);
        rocketService.createNewRocket(TestData.RED_DRAGON);
        missionService.createNewMission(TestData.MOON);
        managementService.assignRocketToMission(TestData.RED_DRAGON, TestData.MOON);

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
        missionService.createNewMission(TestData.MOON);

        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);

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
        missionService.createNewMission(TestData.MARS);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        managementService.changeRocketStatus(TestData.DRAGON_XL, RocketStatus.IN_REPAIR);
        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);

        missionService.createNewMission(TestData.MOON);
        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
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

        missionService.createNewMission(TestData.MARS);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);
        missionService.createNewMission(TestData.MOON);
        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
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
        Mission mission = new Mission(TestData.MARS, MissionStatus.ENDED, 0, 0, 0);
        missionRepository.save(mission);
        missionService.createNewMission(TestData.MOON);
        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
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

        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        Mission mission = missionService.createNewMission(TestData.MARS);
        Optional<Rocket> rocket = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocket.isPresent());
        assertEquals(RocketStatus.IN_REPAIR, rocket.get().status());
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

        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        managementService.changeRocketStatus(TestData.DRAGON_XL, RocketStatus.IN_REPAIR);
        missionService.createNewMission(TestData.MARS);
        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);
        assertTrue(missionFound.isPresent());

        Optional<Rocket> rocket = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocket.isPresent());
        assertEquals(RocketStatus.IN_REPAIR, rocket.get().status());
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
        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);

        rocketService.createNewRocket(TestData.DRAGON_XL);
        missionService.createNewMission(TestData.MARS);
        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);
        assertTrue(missionFound.isPresent());

        Optional<Rocket> rocket = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocket.isPresent());
        assertEquals(RocketStatus.IN_REPAIR, rocket.get().status());
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
        Mission mission = new Mission(TestData.MARS, MissionStatus.ENDED, 0, 0, 0);
        missionRepository.save(mission);
        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);

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
        assertEquals(0, missionFound.get().inSpaceCnt());
        assertEquals(1, missionFound.get().allRocketsCnt());
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
        assertEquals(0, missionFound.get().inSpaceCnt());
        assertEquals(0, missionFound.get().allRocketsCnt());
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
        assertEquals(2, initialMissionFound.get().inSpaceCnt());
        assertEquals(2, initialMissionFound.get().allRocketsCnt());

        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);

        Optional<Mission> missionFound1 = missionRepository.findByName(TestData.MARS);
        assertTrue(missionFound1.isPresent());
        assertEquals(MissionStatus.PENDING, missionFound1.get().status());
        assertEquals(1, missionFound1.get().inRepairCnt());
        assertEquals(1, missionFound1.get().inSpaceCnt());
        assertEquals(2, missionFound1.get().allRocketsCnt());

        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.ON_GROUND);

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionFound.isPresent());
        assertEquals(RocketStatus.ON_GROUND, rocketFound.get().status());
        assertNull(rocketFound.get().mission());
        assertEquals(MissionStatus.IN_PROGRESS, missionFound.get().status());
        assertEquals(0, missionFound.get().inRepairCnt());
        assertEquals(1, missionFound.get().inSpaceCnt());
        assertEquals(1, missionFound.get().allRocketsCnt());

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
        assertEquals(2, initialMissionFound.get().inSpaceCnt());
        assertEquals(2, initialMissionFound.get().allRocketsCnt());

        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);

        Optional<Mission> missionFound1 = missionRepository.findByName(TestData.MARS);
        assertTrue(missionFound1.isPresent());
        assertEquals(MissionStatus.PENDING, missionFound1.get().status());
        assertEquals(1, missionFound1.get().inRepairCnt());
        assertEquals(1, missionFound1.get().inSpaceCnt());
        assertEquals(2, missionFound1.get().allRocketsCnt());

        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_SPACE);


        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionFound.isPresent());
        assertEquals(RocketStatus.IN_SPACE, rocketFound.get().status());
        assertEquals(MissionStatus.IN_PROGRESS, missionFound.get().status());
        assertEquals(0, missionFound.get().inRepairCnt());
        assertEquals(2, missionFound.get().inSpaceCnt());
        assertEquals(2, missionFound.get().allRocketsCnt());
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
        assertEquals(2, initialMissionFound.get().inSpaceCnt());
        assertEquals(2, initialMissionFound.get().allRocketsCnt());

        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        managementService.changeRocketStatus(TestData.DRAGON_XL, RocketStatus.IN_REPAIR);

        Optional<Mission> missionFound1 = missionRepository.findByName(TestData.MARS);
        assertTrue(missionFound1.isPresent());
        assertEquals(MissionStatus.PENDING, missionFound1.get().status());
        assertEquals(2, missionFound1.get().inRepairCnt());
        assertEquals(0, missionFound1.get().inSpaceCnt());
        assertEquals(2, missionFound1.get().allRocketsCnt());

        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_SPACE);


        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketFound.isPresent());
        assertTrue(missionFound.isPresent());
        assertEquals(RocketStatus.IN_SPACE, rocketFound.get().status());
        assertEquals(MissionStatus.PENDING, missionFound.get().status());
        assertEquals(1, missionFound.get().inRepairCnt());
        assertEquals(1, missionFound.get().inSpaceCnt());
        assertEquals(2, missionFound.get().allRocketsCnt());
    }

    @Test
    void shouldThrowExceptionWhenChangeStatusOfNonExistentRocket() {
        assertThrows(RocketNotFoundException.class, () ->
                managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_SPACE));
    }

    // Assign rockets to mission

    @Test
    void shouldAssignMultipleRocketsToScheduledMissionAllWorkingAndExisting() {
        missionService.createNewMission(TestData.MOON);

        rocketService.createNewRocket(TestData.RED_DRAGON);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);

        managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.DRAGON_XL, TestData.FALCON_HEAVY, TestData.LITTLE_DRAGON), TestData.MOON);

        Optional<Rocket> rocketRedDragonFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Rocket> rocketDragonXlFound = rocketRepository.findByName(TestData.DRAGON_XL);
        Optional<Rocket> rocketFalconHeavyFound = rocketRepository.findByName(TestData.FALCON_HEAVY);
        Optional<Rocket> rocketLittleDragonFound = rocketRepository.findByName(TestData.LITTLE_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MOON);

        assertTrue(rocketRedDragonFound.isPresent());
        assertTrue(rocketDragonXlFound.isPresent());
        assertTrue(rocketFalconHeavyFound.isPresent());
        assertTrue(rocketLittleDragonFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_SPACE, rocketRedDragonFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketDragonXlFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketFalconHeavyFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketLittleDragonFound.get().status());

        assertEquals(TestData.MOON, rocketRedDragonFound.get().mission());
        assertEquals(TestData.MOON, rocketDragonXlFound.get().mission());
        assertEquals(TestData.MOON, rocketFalconHeavyFound.get().mission());
        assertEquals(TestData.MOON, rocketLittleDragonFound.get().mission());
        assertEquals(MissionStatus.IN_PROGRESS, missionFound.get().status());
        assertEquals(0, missionFound.get().inRepairCnt());
        assertEquals(4, missionFound.get().inSpaceCnt());
        assertEquals(4, missionFound.get().allRocketsCnt());
    }

    @Test
    void shouldAssignMultipleRocketsToScheduledMissionOneInRepairAndExisting() {
        missionService.createNewMission(TestData.MOON);

        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);

        managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.DRAGON_XL, TestData.FALCON_HEAVY, TestData.LITTLE_DRAGON), TestData.MOON);

        Optional<Rocket> rocketRedDragonFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Rocket> rocketDragonXlFound = rocketRepository.findByName(TestData.DRAGON_XL);
        Optional<Rocket> rocketFalconHeavyFound = rocketRepository.findByName(TestData.FALCON_HEAVY);
        Optional<Rocket> rocketLittleDragonFound = rocketRepository.findByName(TestData.LITTLE_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MOON);

        assertTrue(rocketRedDragonFound.isPresent());
        assertTrue(rocketDragonXlFound.isPresent());
        assertTrue(rocketFalconHeavyFound.isPresent());
        assertTrue(rocketLittleDragonFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocketRedDragonFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketDragonXlFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketFalconHeavyFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketLittleDragonFound.get().status());

        assertEquals(TestData.MOON, rocketRedDragonFound.get().mission());
        assertEquals(TestData.MOON, rocketDragonXlFound.get().mission());
        assertEquals(TestData.MOON, rocketFalconHeavyFound.get().mission());
        assertEquals(TestData.MOON, rocketLittleDragonFound.get().mission());
        assertEquals(MissionStatus.PENDING, missionFound.get().status());
        assertEquals(1, missionFound.get().inRepairCnt());
        assertEquals(3, missionFound.get().inSpaceCnt());
        assertEquals(4, missionFound.get().allRocketsCnt());
    }

    @Test
    void shouldAssignMultipleRocketsToScheduledMissionSomeInRepairAndExisting() {
        missionService.createNewMission(TestData.MOON);

        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        managementService.changeRocketStatus(TestData.DRAGON_XL, RocketStatus.IN_REPAIR);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);

        managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.DRAGON_XL, TestData.FALCON_HEAVY, TestData.LITTLE_DRAGON), TestData.MOON);

        Optional<Rocket> rocketRedDragonFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Rocket> rocketDragonXlFound = rocketRepository.findByName(TestData.DRAGON_XL);
        Optional<Rocket> rocketFalconHeavyFound = rocketRepository.findByName(TestData.FALCON_HEAVY);
        Optional<Rocket> rocketLittleDragonFound = rocketRepository.findByName(TestData.LITTLE_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MOON);

        assertTrue(rocketRedDragonFound.isPresent());
        assertTrue(rocketDragonXlFound.isPresent());
        assertTrue(rocketFalconHeavyFound.isPresent());
        assertTrue(rocketLittleDragonFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocketRedDragonFound.get().status());
        assertEquals(RocketStatus.IN_REPAIR, rocketDragonXlFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketFalconHeavyFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketLittleDragonFound.get().status());

        assertEquals(TestData.MOON, rocketRedDragonFound.get().mission());
        assertEquals(TestData.MOON, rocketDragonXlFound.get().mission());
        assertEquals(TestData.MOON, rocketFalconHeavyFound.get().mission());
        assertEquals(TestData.MOON, rocketLittleDragonFound.get().mission());
        assertEquals(MissionStatus.PENDING, missionFound.get().status());
        assertEquals(2, missionFound.get().inRepairCnt());
        assertEquals(2, missionFound.get().inSpaceCnt());
        assertEquals(4, missionFound.get().allRocketsCnt());
    }

    @Test
    void shouldAssignMultipleRocketsToScheduledMissionAllWorkingAndOneNonExistentOneAssignedToOtherMission() {
        missionService.createNewMission(TestData.MOON);
        missionService.createNewMission(TestData.MARS);

        rocketService.createNewRocket(TestData.DRAGON_XL);
        managementService.changeRocketStatus(TestData.DRAGON_XL, RocketStatus.IN_REPAIR);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);

        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);

        managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.DRAGON_XL, TestData.FALCON_HEAVY, TestData.LITTLE_DRAGON), TestData.MOON);

        Optional<Rocket> rocketRedDragonFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Rocket> rocketDragonXlFound = rocketRepository.findByName(TestData.DRAGON_XL);
        Optional<Rocket> rocketFalconHeavyFound = rocketRepository.findByName(TestData.FALCON_HEAVY);
        Optional<Rocket> rocketLittleDragonFound = rocketRepository.findByName(TestData.LITTLE_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MOON);

        assertFalse(rocketRedDragonFound.isPresent());
        assertTrue(rocketDragonXlFound.isPresent());
        assertTrue(rocketFalconHeavyFound.isPresent());
        assertTrue(rocketLittleDragonFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocketDragonXlFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketFalconHeavyFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketLittleDragonFound.get().status());

        assertEquals(TestData.MARS, rocketDragonXlFound.get().mission());
        assertEquals(TestData.MOON, rocketFalconHeavyFound.get().mission());
        assertEquals(TestData.MOON, rocketLittleDragonFound.get().mission());
        assertEquals(MissionStatus.IN_PROGRESS, missionFound.get().status());
        assertEquals(0, missionFound.get().inRepairCnt());
        assertEquals(2, missionFound.get().inSpaceCnt());
        assertEquals(2, missionFound.get().allRocketsCnt());
    }

    @Test
    void shouldAssignMultipleRocketsToInProgressMissionAllWorkingAndExisting() {
        missionService.createNewMission(TestData.MOON);
        rocketService.createNewRocket(TestData.BLUE_DRAGON);
        managementService.assignRocketToMission(TestData.BLUE_DRAGON, TestData.MOON);

        rocketService.createNewRocket(TestData.RED_DRAGON);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);

        managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.DRAGON_XL, TestData.FALCON_HEAVY, TestData.LITTLE_DRAGON), TestData.MOON);

        Optional<Rocket> rocketRedDragonFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Rocket> rocketDragonXlFound = rocketRepository.findByName(TestData.DRAGON_XL);
        Optional<Rocket> rocketFalconHeavyFound = rocketRepository.findByName(TestData.FALCON_HEAVY);
        Optional<Rocket> rocketLittleDragonFound = rocketRepository.findByName(TestData.LITTLE_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MOON);

        assertTrue(rocketRedDragonFound.isPresent());
        assertTrue(rocketDragonXlFound.isPresent());
        assertTrue(rocketFalconHeavyFound.isPresent());
        assertTrue(rocketLittleDragonFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_SPACE, rocketRedDragonFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketDragonXlFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketFalconHeavyFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketLittleDragonFound.get().status());

        assertEquals(TestData.MOON, rocketRedDragonFound.get().mission());
        assertEquals(TestData.MOON, rocketDragonXlFound.get().mission());
        assertEquals(TestData.MOON, rocketFalconHeavyFound.get().mission());
        assertEquals(TestData.MOON, rocketLittleDragonFound.get().mission());
        assertEquals(MissionStatus.IN_PROGRESS, missionFound.get().status());
        assertEquals(0, missionFound.get().inRepairCnt());
        assertEquals(5, missionFound.get().inSpaceCnt());
        assertEquals(5, missionFound.get().allRocketsCnt());
    }

    @Test
    void shouldAssignMultipleRocketsToInProgressMissionOneInRepairAndExisting() {
        missionService.createNewMission(TestData.MOON);
        rocketService.createNewRocket(TestData.BLUE_DRAGON);
        managementService.assignRocketToMission(TestData.BLUE_DRAGON, TestData.MOON);

        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);

        managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.DRAGON_XL, TestData.FALCON_HEAVY, TestData.LITTLE_DRAGON), TestData.MOON);

        Optional<Rocket> rocketRedDragonFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Rocket> rocketDragonXlFound = rocketRepository.findByName(TestData.DRAGON_XL);
        Optional<Rocket> rocketFalconHeavyFound = rocketRepository.findByName(TestData.FALCON_HEAVY);
        Optional<Rocket> rocketLittleDragonFound = rocketRepository.findByName(TestData.LITTLE_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MOON);

        assertTrue(rocketRedDragonFound.isPresent());
        assertTrue(rocketDragonXlFound.isPresent());
        assertTrue(rocketFalconHeavyFound.isPresent());
        assertTrue(rocketLittleDragonFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocketRedDragonFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketDragonXlFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketFalconHeavyFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketLittleDragonFound.get().status());

        assertEquals(TestData.MOON, rocketRedDragonFound.get().mission());
        assertEquals(TestData.MOON, rocketDragonXlFound.get().mission());
        assertEquals(TestData.MOON, rocketFalconHeavyFound.get().mission());
        assertEquals(TestData.MOON, rocketLittleDragonFound.get().mission());
        assertEquals(MissionStatus.PENDING, missionFound.get().status());
        assertEquals(1, missionFound.get().inRepairCnt());
        assertEquals(4, missionFound.get().inSpaceCnt());
        assertEquals(5, missionFound.get().allRocketsCnt());
    }

    @Test
    void shouldAssignMultipleRocketsToInProgressMissionSomeInRepairAndExisting() {
        missionService.createNewMission(TestData.MOON);
        rocketService.createNewRocket(TestData.BLUE_DRAGON);
        managementService.assignRocketToMission(TestData.BLUE_DRAGON, TestData.MOON);

        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        managementService.changeRocketStatus(TestData.DRAGON_XL, RocketStatus.IN_REPAIR);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);

        managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.DRAGON_XL, TestData.FALCON_HEAVY, TestData.LITTLE_DRAGON), TestData.MOON);

        Optional<Rocket> rocketRedDragonFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Rocket> rocketDragonXlFound = rocketRepository.findByName(TestData.DRAGON_XL);
        Optional<Rocket> rocketFalconHeavyFound = rocketRepository.findByName(TestData.FALCON_HEAVY);
        Optional<Rocket> rocketLittleDragonFound = rocketRepository.findByName(TestData.LITTLE_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MOON);

        assertTrue(rocketRedDragonFound.isPresent());
        assertTrue(rocketDragonXlFound.isPresent());
        assertTrue(rocketFalconHeavyFound.isPresent());
        assertTrue(rocketLittleDragonFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocketRedDragonFound.get().status());
        assertEquals(RocketStatus.IN_REPAIR, rocketDragonXlFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketFalconHeavyFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketLittleDragonFound.get().status());

        assertEquals(TestData.MOON, rocketRedDragonFound.get().mission());
        assertEquals(TestData.MOON, rocketDragonXlFound.get().mission());
        assertEquals(TestData.MOON, rocketFalconHeavyFound.get().mission());
        assertEquals(TestData.MOON, rocketLittleDragonFound.get().mission());
        assertEquals(MissionStatus.PENDING, missionFound.get().status());
        assertEquals(2, missionFound.get().inRepairCnt());
        assertEquals(3, missionFound.get().inSpaceCnt());
        assertEquals(5, missionFound.get().allRocketsCnt());
    }

    @Test
    void shouldAssignMultipleRocketsToInProgressMissionAllWorkingAndOneNonExistentOneAssignedToOtherMission() {
        missionService.createNewMission(TestData.MOON);
        rocketService.createNewRocket(TestData.BLUE_DRAGON);
        managementService.assignRocketToMission(TestData.BLUE_DRAGON, TestData.MOON);
        missionService.createNewMission(TestData.MARS);

        rocketService.createNewRocket(TestData.DRAGON_XL);
        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);

        managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.DRAGON_XL, TestData.FALCON_HEAVY, TestData.LITTLE_DRAGON), TestData.MOON);

        Optional<Rocket> rocketRedDragonFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Rocket> rocketDragonXlFound = rocketRepository.findByName(TestData.DRAGON_XL);
        Optional<Rocket> rocketFalconHeavyFound = rocketRepository.findByName(TestData.FALCON_HEAVY);
        Optional<Rocket> rocketLittleDragonFound = rocketRepository.findByName(TestData.LITTLE_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MOON);

        assertFalse(rocketRedDragonFound.isPresent());
        assertTrue(rocketDragonXlFound.isPresent());
        assertTrue(rocketFalconHeavyFound.isPresent());
        assertTrue(rocketLittleDragonFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_SPACE, rocketDragonXlFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketFalconHeavyFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketLittleDragonFound.get().status());

        assertEquals(TestData.MARS, rocketDragonXlFound.get().mission());
        assertEquals(TestData.MOON, rocketFalconHeavyFound.get().mission());
        assertEquals(TestData.MOON, rocketLittleDragonFound.get().mission());
        assertEquals(MissionStatus.IN_PROGRESS, missionFound.get().status());
        assertEquals(0, missionFound.get().inRepairCnt());
        assertEquals(3, missionFound.get().inSpaceCnt());
        assertEquals(3, missionFound.get().allRocketsCnt());
    }

    @Test
    void shouldAssignMultipleRocketsToPendingMissionAllWorkingAndExisting() {
        missionService.createNewMission(TestData.MOON);
        rocketService.createNewRocket(TestData.BLUE_DRAGON);
        managementService.assignRocketToMission(TestData.BLUE_DRAGON, TestData.MOON);
        managementService.changeRocketStatus(TestData.BLUE_DRAGON, RocketStatus.IN_REPAIR);

        rocketService.createNewRocket(TestData.RED_DRAGON);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);

        managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.DRAGON_XL, TestData.FALCON_HEAVY, TestData.LITTLE_DRAGON), TestData.MOON);

        Optional<Rocket> rocketRedDragonFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Rocket> rocketDragonXlFound = rocketRepository.findByName(TestData.DRAGON_XL);
        Optional<Rocket> rocketFalconHeavyFound = rocketRepository.findByName(TestData.FALCON_HEAVY);
        Optional<Rocket> rocketLittleDragonFound = rocketRepository.findByName(TestData.LITTLE_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MOON);

        assertTrue(rocketRedDragonFound.isPresent());
        assertTrue(rocketDragonXlFound.isPresent());
        assertTrue(rocketFalconHeavyFound.isPresent());
        assertTrue(rocketLittleDragonFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_SPACE, rocketRedDragonFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketDragonXlFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketFalconHeavyFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketLittleDragonFound.get().status());

        assertEquals(TestData.MOON, rocketRedDragonFound.get().mission());
        assertEquals(TestData.MOON, rocketDragonXlFound.get().mission());
        assertEquals(TestData.MOON, rocketFalconHeavyFound.get().mission());
        assertEquals(TestData.MOON, rocketLittleDragonFound.get().mission());
        assertEquals(MissionStatus.PENDING, missionFound.get().status());
        assertEquals(1, missionFound.get().inRepairCnt());
        assertEquals(4, missionFound.get().inSpaceCnt());
        assertEquals(5, missionFound.get().allRocketsCnt());
    }

    @Test
    void shouldAssignMultipleRocketsToPendingMissionOneInRepairAndExisting() {
        missionService.createNewMission(TestData.MOON);
        rocketService.createNewRocket(TestData.BLUE_DRAGON);
        managementService.assignRocketToMission(TestData.BLUE_DRAGON, TestData.MOON);
        managementService.changeRocketStatus(TestData.BLUE_DRAGON, RocketStatus.IN_REPAIR);

        rocketService.createNewRocket(TestData.RED_DRAGON);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);

        managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.DRAGON_XL, TestData.FALCON_HEAVY, TestData.LITTLE_DRAGON), TestData.MOON);

        Optional<Rocket> rocketRedDragonFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Rocket> rocketDragonXlFound = rocketRepository.findByName(TestData.DRAGON_XL);
        Optional<Rocket> rocketFalconHeavyFound = rocketRepository.findByName(TestData.FALCON_HEAVY);
        Optional<Rocket> rocketLittleDragonFound = rocketRepository.findByName(TestData.LITTLE_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MOON);

        assertTrue(rocketRedDragonFound.isPresent());
        assertTrue(rocketDragonXlFound.isPresent());
        assertTrue(rocketFalconHeavyFound.isPresent());
        assertTrue(rocketLittleDragonFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocketRedDragonFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketDragonXlFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketFalconHeavyFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketLittleDragonFound.get().status());

        assertEquals(TestData.MOON, rocketRedDragonFound.get().mission());
        assertEquals(TestData.MOON, rocketDragonXlFound.get().mission());
        assertEquals(TestData.MOON, rocketFalconHeavyFound.get().mission());
        assertEquals(TestData.MOON, rocketLittleDragonFound.get().mission());
        assertEquals(MissionStatus.PENDING, missionFound.get().status());
        assertEquals(2, missionFound.get().inRepairCnt());
        assertEquals(3, missionFound.get().inSpaceCnt());
        assertEquals(5, missionFound.get().allRocketsCnt());
    }


    @Test
    void shouldAssignMultipleRocketsToPendingMissionAllWorkingAndOneNonExistentOneAssignedToOtherMission() {
        missionService.createNewMission(TestData.MOON);
        rocketService.createNewRocket(TestData.BLUE_DRAGON);
        managementService.assignRocketToMission(TestData.BLUE_DRAGON, TestData.MOON);
        managementService.changeRocketStatus(TestData.BLUE_DRAGON, RocketStatus.IN_REPAIR);
        missionService.createNewMission(TestData.MARS);

        rocketService.createNewRocket(TestData.DRAGON_XL);
        managementService.assignRocketToMission(TestData.DRAGON_XL, TestData.MARS);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);

        managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.DRAGON_XL, TestData.FALCON_HEAVY, TestData.LITTLE_DRAGON), TestData.MOON);

        Optional<Rocket> rocketRedDragonFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Rocket> rocketDragonXlFound = rocketRepository.findByName(TestData.DRAGON_XL);
        Optional<Rocket> rocketFalconHeavyFound = rocketRepository.findByName(TestData.FALCON_HEAVY);
        Optional<Rocket> rocketLittleDragonFound = rocketRepository.findByName(TestData.LITTLE_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MOON);

        assertFalse(rocketRedDragonFound.isPresent());
        assertTrue(rocketDragonXlFound.isPresent());
        assertTrue(rocketFalconHeavyFound.isPresent());
        assertTrue(rocketLittleDragonFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_SPACE, rocketDragonXlFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketFalconHeavyFound.get().status());
        assertEquals(RocketStatus.IN_SPACE, rocketLittleDragonFound.get().status());

        assertEquals(TestData.MARS, rocketDragonXlFound.get().mission());
        assertEquals(TestData.MOON, rocketFalconHeavyFound.get().mission());
        assertEquals(TestData.MOON, rocketLittleDragonFound.get().mission());
        assertEquals(MissionStatus.PENDING, missionFound.get().status());
        assertEquals(1, missionFound.get().inRepairCnt());
        assertEquals(2, missionFound.get().inSpaceCnt());
        assertEquals(3, missionFound.get().allRocketsCnt());
    }

    @Test
    void shouldThrowExceptionWhenAssigningRocketsToNonExistentMission() {
        rocketService.createNewRocket(TestData.RED_DRAGON);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);

        assertThrows(MissionNotFoundException.class, () ->
                managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.DRAGON_XL, TestData.FALCON_HEAVY, TestData.LITTLE_DRAGON), TestData.MOON));
    }

    @Test
    void shouldThrowExceptionWhenAssigningRocketsToEndedMission() {
        Mission mission = new Mission(TestData.MOON, MissionStatus.ENDED, 0, 0, 0);
        missionRepository.save(mission);

        rocketService.createNewRocket(TestData.RED_DRAGON);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);

        assertThrows(CannotAssignToEndedMissionException.class, () ->
                managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.DRAGON_XL, TestData.FALCON_HEAVY, TestData.LITTLE_DRAGON), TestData.MOON));
    }

    // Finish Mission
    @Test
    void shouldFinishMissionAllRocketsInSpace(){
        missionService.createNewMission(TestData.MARS);
        rocketService.createNewRocket(TestData.RED_DRAGON);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);
        managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.DRAGON_XL, TestData.FALCON_HEAVY, TestData.LITTLE_DRAGON), TestData.MARS);

        managementService.finishMission(TestData.MARS);

        Optional<Rocket> rocketRedDragonFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Rocket> rocketDragonXlFound = rocketRepository.findByName(TestData.DRAGON_XL);
        Optional<Rocket> rocketFalconHeavyFound = rocketRepository.findByName(TestData.FALCON_HEAVY);
        Optional<Rocket> rocketLittleDragonFound = rocketRepository.findByName(TestData.LITTLE_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);


        assertTrue(rocketRedDragonFound.isPresent());
        assertTrue(rocketDragonXlFound.isPresent());
        assertTrue(rocketFalconHeavyFound.isPresent());
        assertTrue(rocketLittleDragonFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.ON_GROUND, rocketRedDragonFound.get().status());
        assertEquals(RocketStatus.ON_GROUND, rocketDragonXlFound.get().status());
        assertEquals(RocketStatus.ON_GROUND, rocketFalconHeavyFound.get().status());
        assertEquals(RocketStatus.ON_GROUND, rocketLittleDragonFound.get().status());

        assertNull(rocketRedDragonFound.get().mission());
        assertNull(rocketDragonXlFound.get().mission());
        assertNull(rocketFalconHeavyFound.get().mission());
        assertNull(rocketLittleDragonFound.get().mission());
        assertEquals(MissionStatus.ENDED, missionFound.get().status());
        assertEquals(0, missionFound.get().inRepairCnt());
        assertEquals(0, missionFound.get().inSpaceCnt());
        assertEquals(0, missionFound.get().allRocketsCnt());
    }
    @Test
    void shouldFinishMissionRocketsInSpaceAndInRepair(){
        missionService.createNewMission(TestData.MARS);
        rocketService.createNewRocket(TestData.RED_DRAGON);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);
        managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.DRAGON_XL, TestData.FALCON_HEAVY, TestData.LITTLE_DRAGON), TestData.MARS);

        managementService.finishMission(TestData.MARS);
        managementService.changeRocketStatus(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);
        managementService.changeRocketStatus(TestData.DRAGON_XL, RocketStatus.IN_REPAIR);

        Optional<Rocket> rocketRedDragonFound = rocketRepository.findByName(TestData.RED_DRAGON);
        Optional<Rocket> rocketDragonXlFound = rocketRepository.findByName(TestData.DRAGON_XL);
        Optional<Rocket> rocketFalconHeavyFound = rocketRepository.findByName(TestData.FALCON_HEAVY);
        Optional<Rocket> rocketLittleDragonFound = rocketRepository.findByName(TestData.LITTLE_DRAGON);
        Optional<Mission> missionFound = missionRepository.findByName(TestData.MARS);

        assertTrue(rocketRedDragonFound.isPresent());
        assertTrue(rocketDragonXlFound.isPresent());
        assertTrue(rocketFalconHeavyFound.isPresent());
        assertTrue(rocketLittleDragonFound.isPresent());
        assertTrue(missionFound.isPresent());

        assertEquals(RocketStatus.IN_REPAIR, rocketRedDragonFound.get().status());
        assertEquals(RocketStatus.IN_REPAIR, rocketDragonXlFound.get().status());
        assertEquals(RocketStatus.ON_GROUND, rocketFalconHeavyFound.get().status());
        assertEquals(RocketStatus.ON_GROUND, rocketLittleDragonFound.get().status());

        assertNull(rocketRedDragonFound.get().mission());
        assertNull(rocketDragonXlFound.get().mission());
        assertNull(rocketFalconHeavyFound.get().mission());
        assertNull(rocketLittleDragonFound.get().mission());
        assertEquals(MissionStatus.ENDED, missionFound.get().status());
        assertEquals(0, missionFound.get().inRepairCnt());
        assertEquals(0, missionFound.get().inSpaceCnt());
        assertEquals(0, missionFound.get().allRocketsCnt());
    }

    @Test
    void shouldThrowExceptionWhenTryingToFinishNonexistentMission(){
        assertThrows(MissionNotFoundException.class, () ->
                managementService.finishMission(TestData.MOON));
    }
}
