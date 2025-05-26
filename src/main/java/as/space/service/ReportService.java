package as.space.service;

import as.space.model.Mission;
import as.space.model.Rocket;
import as.space.repository.MissionRepository;
import as.space.repository.RocketRepository;

import java.util.List;

public class ReportService {
    final private RocketRepository rocketRepository;
    final private MissionRepository missionRepository;

    public ReportService(RocketRepository rocketRepository, MissionRepository missionRepository) {
        this.rocketRepository = rocketRepository;
        this.missionRepository = missionRepository;
    }

    public String generateReport() {
        List<Mission> missions = missionRepository.getAllSorted();
        StringBuilder report = new StringBuilder();

        for (Mission mission : missions) {
            report.append(mission.name()).append(" - ").append(mission.status().name()).append(" - ").append(mission.allRocketsCnt()).append(" dragons\n");

            List<Rocket> rockets = rocketRepository.findByMission(mission.name());
            for (Rocket rocket : rockets) {
                String rocketPart = "  " + rocket.name() + " - " + rocket.status().name() + "\n";
                report.append(rocketPart);
            }
        }

        return report.toString();
    }
}
