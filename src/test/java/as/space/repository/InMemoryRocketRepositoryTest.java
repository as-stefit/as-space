package as.space.repository;

import as.space.TestData;
import as.space.model.Rocket;
import as.space.model.RocketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        Rocket rocket = new Rocket(TestData.RED_DRAGON, RocketStatus.ON_GROUND);

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

        Rocket first = new Rocket(TestData.RED_DRAGON, RocketStatus.ON_GROUND);
        Rocket second = new Rocket(TestData.RED_DRAGON, RocketStatus.IN_REPAIR);

        repository.save(first);
        repository.save(second);

        Optional<Rocket> rocketFound = repository.findByName(TestData.RED_DRAGON);

        assertTrue(rocketFound.isPresent());
        assertEquals(RocketStatus.IN_REPAIR, rocketFound.get().status());
    }
}
