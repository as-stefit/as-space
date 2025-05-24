package as.space.service;

import as.space.exception.RocketAlreadyExistsException;
import as.space.model.Rocket;
import as.space.model.RocketStatus;
import as.space.repository.RocketRepository;

public class RocketService {
    final private RocketRepository rocketRepository;

    public RocketService(RocketRepository rocketRepository) {
        this.rocketRepository = rocketRepository;
    }

    public Rocket createNewRocket(String rocketName){
        if (rocketRepository.findByName(rocketName).isPresent()) {
            throw new RocketAlreadyExistsException(rocketName);
        }
        Rocket rocket = new Rocket(rocketName, RocketStatus.ON_GROUND);
        rocketRepository.save(rocket);
        return rocket;
    }
}
