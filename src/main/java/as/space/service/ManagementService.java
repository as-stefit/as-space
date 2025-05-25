package as.space.service;

import as.space.exception.CannotAssignToEndedMissionException;
import as.space.exception.MissionNotFoundException;
import as.space.exception.RocketAlreadyAssignedException;
import as.space.exception.RocketNotFoundException;
import as.space.model.Mission;
import as.space.model.MissionStatus;
import as.space.model.Rocket;
import as.space.model.RocketStatus;
import as.space.repository.MissionRepository;
import as.space.repository.RocketRepository;

public class ManagementService {
    final private RocketRepository rocketRepository;
    final private MissionRepository missionRepository;

    public ManagementService(RocketRepository rocketRepository, MissionRepository missionRepository) {
        this.rocketRepository = rocketRepository;
        this.missionRepository = missionRepository;
    }

    public void assignRocketToMission(String rocketName, String missionName) {

        Mission mission = missionRepository.findByName(missionName).orElseThrow(() -> new MissionNotFoundException(missionName));
        Rocket rocket = rocketRepository.findByName(rocketName).orElseThrow(() -> new RocketNotFoundException(rocketName));

        if (mission.status() == MissionStatus.ENDED) {
            throw new CannotAssignToEndedMissionException(missionName);
        }

        if (rocket.mission()!=null) {
            throw new RocketAlreadyAssignedException(rocketName);
        }

        RocketStatus newRocketStatus = rocket.status();
        MissionStatus newMissionStatus = mission.status();

        if (newRocketStatus == RocketStatus.ON_GROUND) {
            newRocketStatus = RocketStatus.IN_SPACE;
        }
        if(newMissionStatus== MissionStatus.SCHEDULED){
            newMissionStatus = MissionStatus.IN_PROGRESS;
        }
        if(newRocketStatus==RocketStatus.IN_REPAIR){
            newMissionStatus = MissionStatus.PENDING;
        }
        Rocket updatedRocketRecord = new Rocket(rocketName, newRocketStatus, missionName);
        Mission updatedMissionRecord = new Mission(missionName, newMissionStatus);
        rocketRepository.save(updatedRocketRecord);
        missionRepository.save(updatedMissionRecord);
    }
}
