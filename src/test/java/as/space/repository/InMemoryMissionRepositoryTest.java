package as.space.repository;

import as.space.TestData;
import as.space.model.Mission;
import as.space.model.MissionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryMissionRepositoryTest {

    private InMemoryMissionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMissionRepository();
    }

    @Test
    void shouldSaveAndFindMissionByName() {
        Mission mission = new Mission(TestData.MARS, MissionStatus.SCHEDULED,0,0,0);
        repository.save(mission);

        Optional<Mission> missionFound = repository.findByName(TestData.MARS);

        assertTrue(missionFound.isPresent());
        assertEquals(TestData.MARS, missionFound.get().name());
        assertEquals(MissionStatus.SCHEDULED, missionFound.get().status());
    }

    @Test
    void shouldReturnEmptyWhenMissionNotFound() {
        Optional<Mission> missionFound = repository.findByName(TestData.MARS);
        assertFalse(missionFound.isPresent());
    }

    @Test
    void shouldOverrideExistingMission() {
        Mission mission1 = new Mission(TestData.MARS, MissionStatus.SCHEDULED,0,0,0);
        Mission mission2 = new Mission(TestData.MARS, MissionStatus.IN_PROGRESS,1,1,0);

        repository.save(mission1);
        repository.save(mission2);

        Optional<Mission> missionFound = repository.findByName(TestData.MARS);
        assertTrue(missionFound.isPresent());
        assertEquals(MissionStatus.IN_PROGRESS, missionFound.get().status());
    }
}
