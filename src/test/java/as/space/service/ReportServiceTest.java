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
    private RocketService rocketService;
    private MissionService missionService;
    private ManagementService managementService;
    private ReportService reportService;


    @BeforeEach
    void setUp() {
        RocketRepository rocketRepository = new InMemoryRocketRepository();
        MissionRepository missionRepository = new InMemoryMissionRepository();
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
                "  Dragon 1 - IN_SPACE\n" +
                "  Dragon 2 - IN_SPACE\n" +
                "  Dragon 3 - IN_SPACE\n" +
                "  Dragon 4 - IN_SPACE\n" +
                "Zeus - IN_PROGRESS - 2 dragons\n" +
                "  Dragon XL - IN_SPACE\n" +
                "  Falcon Heavy - IN_SPACE\n" +
                "Venus - PENDING - 2 dragons\n" +
                "  Dragon 5 - IN_REPAIR\n" +
                "  Little Dragon - IN_SPACE\n" +
                "Moon - IN_PROGRESS - 2 dragons\n" +
               "  Blue Dragon - IN_SPACE\n" +
               "  Red Dragon - IN_SPACE\n" +
                "Mars - ENDED - 0 dragons\n"+
                "Double Landing - SCHEDULED - 0 dragons\n"
                ;

        String report = reportService.generateReport();

        assertEquals(expectedReport, report);
    }
}
