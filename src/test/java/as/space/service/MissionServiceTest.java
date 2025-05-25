package as.space.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class MissionServiceTest {
    private MissionRepository missionRepository;
    private MissionService missionService;

    @BeforeEach
    void setUp() {
        missionRepository = new InMemoryMissionRepository();
        missionService = new MissionService(missionRepository);
    }

    @Test
    void shouldCreateMissionWithScheduledStatus() {
        Mission mission = missionService.createNewMission(TestData.MARS);

        assertEquals(TestData.MARS, mission.name());
        assertEquals(MissionStatus.SCHEDULED, mission.status());

        Optional<Mission> found = missionRepository.findByName(TestData.MARS);
        assertTrue(found.isPresent());
        assertEquals(TestData.MARS, found.get().name());
        assertEquals(MissionStatus.SCHEDULED, found.get().status());
    }

    @Test
    void shouldThrowExceptionWhenMissionWithSameNameExists() {
        missionService.createNewMission(TestData.MARS);

        assertThrows(MissionAlreadyExistsException.class, () ->
                missionService.createNewMission(TestData.MARS)
        );
    }
}
