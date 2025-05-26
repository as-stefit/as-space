package as.space.repository;

import as.space.model.Mission;

import java.util.List;
import java.util.Optional;

public interface MissionRepository {
    void save(Mission mission);

    Optional<Mission> findByName(String name);

    List<Mission> getAllSorted();
}
