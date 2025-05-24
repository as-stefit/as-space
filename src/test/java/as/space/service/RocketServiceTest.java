package as.space.service;

import as.space.TestData;
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

        assertEquals(TestData.RED_DRAGON, rocket.getName());
        assertEquals(initialStatus, rocket.getStatus());

        Optional<Rocket> rocketFound = rocketRepository.findByName(TestData.RED_DRAGON);
        assertTrue(rocketFound.isPresent());
        assertEquals(TestData.RED_DRAGON, rocketFound.get().getName());
        assertEquals(initialStatus, rocketFound.get().getStatus());
    }

    @Test
    void shouldThrowExceptionWhenRocketWithTheSameNameAlreadyExists() {
        rocketService.createNewRocket(TestData.RED_DRAGON);

        assertThrows(RocketAlreadyExistsException.class, () -> {
            rocketService.createNewRocket(TestData.RED_DRAGON);
        });
    }
}
