package org.trakket.service.motorsport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.service.discord.DiscordAlertService;
import org.trakket.service.motorsport.jolpi.FormulaOneDataService;
import org.trakket.service.motorsport.mototiming.MotoTimingService;
import org.trakket.service.motorsport.sportsdb.SportsDbService;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MotorsportSyncService {

    private final FormulaOneDataService formulaOneDataService;
    private final SportsDbService sportsDbService;
    private final MotoTimingService motoTimingService;
    private final DiscordAlertService discordAlertService;

    @Value("${motorsport.sync.season}")
    private Integer season;

    @Scheduled(cron = "0 0 9,21 * * *")
    public void syncEvents() {
        log.info("Starting motorsport events syncing");

        Map<MotorsportCompetition, Runnable> tasks = Map.of(
                MotorsportCompetition.FORMULA_ONE, () -> formulaOneDataService.importLatestRaceResult(season),
                MotorsportCompetition.WORLD_ENDURANCE_CHAMPIONSHIP, () -> sportsDbService.syncEvents(MotorsportCompetition.WORLD_ENDURANCE_CHAMPIONSHIP),
                MotorsportCompetition.MOTOGP, () -> motoTimingService.syncEvents(MotorsportCompetition.MOTOGP)
        );

        tasks.forEach((competition, task) -> {
            try {
                log.info("Starting {} event syncing", competition.getDisplayName());
                task.run();
            } catch (Exception ex) {
                log.error("Failed to sync {} events", competition.getDisplayName(), ex);
                discordAlertService.sendCompetitionSyncFailedAlert(competition.getDisplayName(), ex);
            }
        });
        log.info("Finished motorsport events syncing");
    }

    public void initCompetitionEvents(MotorsportCompetition competition) {
        if (competition == null) return;
        Map<MotorsportCompetition, Runnable> importTasks = Map.of(
                MotorsportCompetition.FORMULA_ONE, () -> {
                    formulaOneDataService.importRaceSchedule(season);
                    formulaOneDataService.importSeasonResults(season);
                },
                MotorsportCompetition.WORLD_ENDURANCE_CHAMPIONSHIP, () -> sportsDbService.importSeasonEvents(MotorsportCompetition.WORLD_ENDURANCE_CHAMPIONSHIP),
                MotorsportCompetition.MOTOGP, () -> motoTimingService.importSeasonEvents(MotorsportCompetition.MOTOGP)
        );

        Runnable task = importTasks.get(competition);
        if (task != null) {
            try {
                task.run();
            } catch (Exception ex) {
                log.error("Failed to import events for competition: {}", competition.getDisplayName(), ex);
            }
        } else {
            log.warn("No import logic defined for competition: {}", competition.getDisplayName());
        }
    }
}
