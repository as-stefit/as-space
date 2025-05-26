package as.space.repository;

import as.space.TestData;
import as.space.model.Rocket;
import as.space.model.RocketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryRocketRepositoryTest {

    private InMemoryRocketRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryRocketRepository();

    }

    @Test
    void shouldSaveAndFindRocketByName() {
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.ON_GROUND, null);

        repository.save(rocket);
        Optional<Rocket> rocketFound = repository.findByName(TestData.RED_DRAGON);

        assertTrue(rocketFound.isPresent());
        assertEquals(TestData.RED_DRAGON, rocketFound.get().name());
        assertEquals(RocketStatus.ON_GROUND, rocketFound.get().status());
    }

    @Test
    void shouldReturnEmptyWhenRocketNotFound() {
        Optional<Rocket> rocketFound = repository.findByName(TestData.RED_DRAGON);

        assertFalse(rocketFound.isPresent());
    }

    @Test
    void shouldOverwriteExistingRocket() {

        Rocket first = new Rocket(TestData.RED_DRAGON, RocketStatus.ON_GROUND, null);
        Rocket second = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR, null);

        repository.save(first);
        repository.save(second);

        Optional<Rocket> rocketFound = repository.findByName(TestData.RED_DRAGON);

        assertTrue(rocketFound.isPresent());
        assertEquals(RocketStatus.IN_REPAIR, rocketFound.get().status());
    }

    @Test
    void shouldReturnAllRocketsAssignedToMission() {
        Rocket redDragonRocket = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_SPACE, TestData.MARS);
        Rocket blueDragonRocket = new Rocket(TestData.BLUE_DRAGON, RocketStatus.IN_SPACE, TestData.MARS);
        Rocket littleDragonRocket = new Rocket(TestData.LITTLE_DRAGON, RocketStatus.IN_SPACE, TestData.MOON);
        Rocket falconHeavyRocket = new Rocket(TestData.FALCON_HEAVY, RocketStatus.ON_GROUND, null);
        repository.save(redDragonRocket);
        repository.save(blueDragonRocket);
        repository.save(littleDragonRocket);
        repository.save(falconHeavyRocket);

        List<Rocket> rocketsMarsFound = repository.findByMission(TestData.MARS);
        List<Rocket> rocketsMoonFound = repository.findByMission(TestData.MOON);
        List<Rocket> rocketsNoMissionFound = repository.findByMission(null);
        List<Rocket> rocketsVenusFound = repository.findByMission(TestData.VENUS);
        assertEquals(2, rocketsMarsFound.size());
        assertEquals(List.of(redDragonRocket, blueDragonRocket), rocketsMarsFound);
        assertEquals(1, rocketsMoonFound.size());
        assertEquals(List.of(littleDragonRocket), rocketsMoonFound);
        assertEquals(1, rocketsNoMissionFound.size());
        assertEquals(List.of(falconHeavyRocket), rocketsNoMissionFound);
        assertTrue(rocketsVenusFound.isEmpty());
        assertEquals(List.of(), rocketsVenusFound);

    }
}
