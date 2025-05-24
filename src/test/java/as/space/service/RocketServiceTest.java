package as.space.service;

import as.space.TestData;
import as.space.exception.RocketAlreadyExistsException;
import as.space.model.Rocket;
import as.space.model.RocketStatus;
import as.space.repository.InMemoryRocketRepository;
import as.space.repository.RocketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RocketServiceTest {
    private RocketRepository rocketRepository;
    private RocketService rocketService;

    @BeforeEach
    public void setUp() {
        rocketRepository = new InMemoryRocketRepository();
        rocketService = new RocketService(rocketRepository);
    }

    @Test
    void shouldCreateRocketWithGivenNameAndInitialStatus() {
        RocketStatus initialStatus = RocketStatus.ON_GROUND;

        Rocket rocket = rocketService.createNewRocket(TestData.RED_DRAGON);

        assertEquals(TestData.RED_DRAGON, rocket.name());
        assertEquals(initialStatus, rocket.status());

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocketFound.isPresent());
        assertEquals(TestData.RED_DRAGON, rocketFound.get().name());
        assertEquals(initialStatus, rocketFound.get().status());
    }

    @Test
    void shouldThrowExceptionWhenRocketWithTheSameNameAlreadyExists() {
        rocketService.createNewRocket(TestData.RED_DRAGON);

        assertThrows(RocketAlreadyExistsException.class, () -> {
            rocketService.createNewRocket(TestData.RED_DRAGON);
        });
    }
}
