package as.space.repository;

import as.space.model.Rocket;

import java.util.List;
import java.util.Optional;

public interface RocketRepository {
    void save(Rocket rocket);
    Optional<Rocket> findByName(String name);
    List<Rocket> findByMission(String mission);
}
