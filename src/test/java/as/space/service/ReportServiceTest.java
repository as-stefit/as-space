package as.space.service;

import as.space.TestData;
import as.space.model.RocketStatus;
import as.space.repository.InMemoryMissionRepository;
import as.space.repository.InMemoryRocketRepository;
import as.space.repository.MissionRepository;
import as.space.repository.RocketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReportServiceTest {
    private RocketRepository rocketRepository;
    private MissionRepository missionRepository;
    private RocketService rocketService;
    private MissionService missionService;
    private ManagementService managementService;
    private ReportService reportService;


    @BeforeEach
    void setUp() {
        rocketRepository = new InMemoryRocketRepository();
        missionRepository = new InMemoryMissionRepository();
        rocketService = new RocketService(rocketRepository);
        missionService = new MissionService(missionRepository);

        managementService = new ManagementService(rocketRepository, missionRepository);
        reportService = new ReportService(rocketRepository, missionRepository);
    }

    @Test
    void shouldProduceCorrectReport1(){
        missionService.createNewMission(TestData.MARS);
        missionService.createNewMission(TestData.MOON);
        missionService.createNewMission(TestData.VENUS);
        missionService.createNewMission(TestData.TRANSIT);
        missionService.createNewMission(TestData.ZEUS);
        missionService.createNewMission(TestData.DOUBLE_LANDING);

        rocketService.createNewRocket(TestData.RED_DRAGON);
        rocketService.createNewRocket(TestData.DRAGON_XL);
        rocketService.createNewRocket(TestData.FALCON_HEAVY);
        rocketService.createNewRocket(TestData.LITTLE_DRAGON);
        rocketService.createNewRocket(TestData.BLUE_DRAGON);
        rocketService.createNewRocket(TestData.DRAGON_1);
        rocketService.createNewRocket(TestData.DRAGON_2);
        rocketService.createNewRocket(TestData.DRAGON_3);
        rocketService.createNewRocket(TestData.DRAGON_4);
        rocketService.createNewRocket(TestData.DRAGON_5);
        rocketService.createNewRocket(TestData.DRAGON_6);
        rocketService.createNewRocket(TestData.DRAGON_7);

        managementService.assignRocketsToMission(List.of(TestData.DRAGON_1, TestData.DRAGON_2, TestData.DRAGON_3, TestData.DRAGON_4), TestData.TRANSIT);
        managementService.assignRocketsToMission(List.of(TestData.FALCON_HEAVY, TestData.DRAGON_XL), TestData.ZEUS);
        managementService.assignRocketsToMission(List.of(TestData.RED_DRAGON, TestData.BLUE_DRAGON), TestData.MOON);
        managementService.assignRocketsToMission(List.of(TestData.LITTLE_DRAGON, TestData.DRAGON_5), TestData.VENUS);
        managementService.changeRocketStatus(TestData.DRAGON_5, RocketStatus.IN_REPAIR);
        managementService.assignRocketsToMission(List.of(TestData.DRAGON_6, TestData.DRAGON_7), TestData.MARS);
        managementService.finishMission(TestData.MARS);

        String expectedReport =
                "Transit - IN_PROGRESS - 4 dragons\n" +
                "  Dragon 1 - In Space\n" +
                "  Dragon 2 - In Space\n" +
                "  Dragon 3 - In Space\n" +
                "  Dragon 4 - In Space\n" +
                "Moon - IN_PROGRESS - 2 dragons\n" +
               "  Red Dragon - In Space\n" +
               "  Blue Dragon - In Space\n" +
               "Venus - PENDING - 2 dragons\n" +
                "  Dragon 5 - In Repair\n" +
                "  Little Dragon - In Space\n" +
                "Double Landing - SCHEDULED - 0 dragons\n" +
                "MARS - ENDED - 0 dragons\n" 
                ;

        String report = reportService.generateReport();

        assertEquals(expectedReport, report);
    }
}
