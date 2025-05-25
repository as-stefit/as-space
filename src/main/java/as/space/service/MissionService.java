package as.space.service;

import as.space.exception.MissionAlreadyExistsException;
import as.space.model.Mission;
import as.space.model.MissionStatus;
import as.space.repository.MissionRepository;

public class MissionService {
    final private MissionRepository missionRepository;

    public MissionService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    public Mission createNewMission(String missionName) {
        if (missionRepository.findByName(missionName).isPresent()) {
            throw new MissionAlreadyExistsException(missionName);
        }
        Mission mission = new Mission(missionName, MissionStatus.SCHEDULED);
        missionRepository.save(mission);
        return mission;
    }
}
