package as.space.service;

import as.space.exception.*;
import as.space.model.Mission;
import as.space.model.MissionStatus;
import as.space.model.Rocket;
import as.space.model.RocketStatus;
import as.space.repository.MissionRepository;
import as.space.repository.RocketRepository;

import java.util.List;

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

        if (rocket.mission() != null) {
            throw new RocketAlreadyAssignedException(rocketName);
        }

        RocketStatus newRocketStatus = rocket.status();
        MissionStatus newMissionStatus = mission.status();
        int allRocketsCnt = mission.allRocketsCnt();
        int inSpaceCnt = mission.inSpaceCnt();
        int inRepairCnt = mission.inRepairCnt();

        if (newRocketStatus == RocketStatus.ON_GROUND) {
            newRocketStatus = RocketStatus.IN_SPACE;
            inSpaceCnt++;
        }
        if (newMissionStatus == MissionStatus.SCHEDULED) {
            newMissionStatus = MissionStatus.IN_PROGRESS;
        }
        if (newRocketStatus == RocketStatus.IN_REPAIR) {
            newMissionStatus = MissionStatus.PENDING;
            inRepairCnt++;
        }
        allRocketsCnt++;
        Rocket updatedRocketRecord = new Rocket(rocketName, newRocketStatus, missionName);
        Mission updatedMissionRecord = new Mission(missionName, newMissionStatus, allRocketsCnt, inSpaceCnt, inRepairCnt);
        rocketRepository.save(updatedRocketRecord);
        missionRepository.save(updatedMissionRecord);
    }

    public void assignRocketsToMission(List<String> rockets, String missionName){
        for (String rocketName : rockets){
            try {
                assignRocketToMission(rocketName, missionName);
            }catch(RocketNotFoundException | RocketAlreadyAssignedException e){
                // rockets not assigned
            }
        }
    }

    public void changeRocketStatus(String rocketName, RocketStatus status) {
        Rocket rocket = rocketRepository.findByName(rocketName).orElseThrow(() -> new RocketNotFoundException(rocketName));
        String currentMission = rocket.mission();
        RocketStatus initialStatus = rocket.status();
        RocketStatus newStatus = rocket.status();
        String newMission = rocket.mission();

        if (initialStatus == RocketStatus.ON_GROUND) {
            switch (status) {
                case IN_REPAIR:
                    newStatus = status;
                    break;
                case IN_SPACE:
                    throw new OperationNotAllowedException("Rocket can be sent in space only by assigning it to mission.");
                default:
                    return;
            }
        }
        if (initialStatus == RocketStatus.IN_SPACE) {
            switch (status) {
                case IN_REPAIR:
                    newStatus = status;
                    updateMission(currentMission, 0, -1,1);
                    break;
                case ON_GROUND:
                    newStatus = status;
                    newMission = null;
                    updateMission(currentMission, -1, -1, 0);
                    break;
                default:
                    return;
            }
        }
        if (initialStatus == RocketStatus.IN_REPAIR) {
            switch (status) {
                case IN_SPACE:
                    if(currentMission!=null){
                        newStatus = status;
                        updateMission(currentMission, 0, 1, -1);
                        break;
                    }else{
                        throw new OperationNotAllowedException("Rocket can be sent in space only by assigning it to mission.");
                    }
                case ON_GROUND:
                    if (currentMission != null) {
                        updateMission(currentMission, -1, 0, -1);
                    }
                    newStatus = status;
                    newMission = null;
                    break;
                default:
                    return;
            }
        }

        Rocket updatedRocketRecord = new Rocket(rocket.name(), newStatus, newMission);
        rocketRepository.save(updatedRocketRecord);
    }

    private void updateMission(String missionName, int changeAllRockets, int changeInSpace, int changeInRepair) {
        Mission mission = missionRepository.findByName(missionName).orElseThrow(() -> new MissionNotFoundException(missionName));
        int allRocketsCnt = mission.allRocketsCnt();
        int inSpaceCnt = mission.inSpaceCnt();
        int inRepairCnt = mission.inRepairCnt();
        MissionStatus status;

        allRocketsCnt += changeAllRockets;
        inSpaceCnt += changeInSpace;
        inRepairCnt += changeInRepair;

        if (allRocketsCnt > 0) {
            if (inRepairCnt > 0) {
                status = MissionStatus.PENDING;
            } else {
                status = MissionStatus.IN_PROGRESS;
            }
        } else {
            status = MissionStatus.SCHEDULED;
        }

        Mission updatedMissionRecord = new Mission(missionName, status, allRocketsCnt, inSpaceCnt, inRepairCnt);
        missionRepository.save(updatedMissionRecord);
    }
}
