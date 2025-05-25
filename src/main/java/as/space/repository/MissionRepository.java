package as.space.repository;

import as.space.model.Mission;

import java.util.Optional;

public interface MissionRepository {
    void save(Mission mission);

    Optional<Mission> findByName(String name);
}
