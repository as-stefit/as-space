package as.space.repository;

import as.space.model.Mission;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryMissionRepository implements MissionRepository {
    private final Map<String, Mission> store = new HashMap<>();
    @Override
    public void save(Mission mission) {
        store.put(mission.name(), mission);
    }

    @Override
    public Optional<Mission> findByName(String name) {
        return Optional.ofNullable(store.get(name));
    }
}
