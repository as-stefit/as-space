package as.space.repository;

import as.space.model.Rocket;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryRocketRepository implements RocketRepository {

    private final Map<String, Rocket> store = new HashMap<>();

    @Override
    public void save(Rocket rocket) {
        store.put(rocket.name(), rocket);
    }

    @Override
    public Optional<Rocket> findByName(String name) {
        return Optional.ofNullable(store.get(name));
    }
}
