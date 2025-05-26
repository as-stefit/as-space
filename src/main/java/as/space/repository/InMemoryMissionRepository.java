package as.space.repository;

import as.space.model.Mission;

import java.util.*;

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

    @Override
    public List<Mission> getAllSorted() {
        return store.values().stream()
                .sorted(
                        Comparator.comparingInt(Mission::allRocketsCnt).reversed()
                                .thenComparing(Mission::name, Comparator.reverseOrder())
                )
                .toList();
    }
}
