package org.trakket.service.motorsport.jolpi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.trakket.dto.motorsport.jolpicaf1.RacesResponseDto;
import org.trakket.enums.EventStatus;
import org.trakket.enums.ExternalMotorsportSource;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.model.MotorsportEvent;
import org.trakket.repository.MotorsportEventRepository;
import org.trakket.repository.MotorsportTeamRepository;
import org.trakket.service.CountryCodeService;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormulaOneDataService {

    private final WebClient webClient;
    private final MotorsportEventRepository eventRepository;
    private final MotorsportTeamRepository motorsportTeamRepository;
    private final CountryCodeService countryCodeService;

    private static final String BASE_URL = "https://api.jolpi.ca/ergast/f1";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Initial season race schedule.
     */
    public void importRaceSchedule(int season) {
        String url = BASE_URL + "/" + season + ".json";
        RacesResponseDto response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(RacesResponseDto.class)
                .onErrorResume(e -> {
                    log.error("Failed to fetch race schedule for season {}", season, e);
                    return Mono.empty();
                })
                .block();

        if (response != null && response.getMRData() != null) {
            List<RacesResponseDto.Race> races = response.getMRData().getRaceTable().getRaces();
            races.forEach(race -> saveOrUpdateRaceResult(race, season, true, false));
            log.info("Saved {} race schedules to database for season {}", races.size(), season);
        }
    }

    /**
     * Initial season race results.
     */
    public void importSeasonResults(int season) {
        int limit = 100;
        int offset = 0;
        int total = Integer.MAX_VALUE;
        int savedCount = 0;

        while (offset < total) {
            String url = BASE_URL + "/" + season + "/results.json?limit=" + limit + "&offset=" + offset;

            int logOffset = offset;
            RacesResponseDto response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(RacesResponseDto.class)
                    .onErrorResume(e -> {
                        log.error("Failed to fetch season results for season {} at offset {}", season, logOffset, e);
                        return Mono.empty();
                    })
                    .block();

            if (response == null || response.getMRData() == null) {
                break;
            }

            RacesResponseDto.MRData mrData = response.getMRData();
            List<RacesResponseDto.Race> races = mrData.getRaceTable().getRaces();

            races.forEach(race -> saveOrUpdateRaceResult(race, season, true, true));
            savedCount += races.size();

            // pagination counters
            total = Integer.parseInt(mrData.getTotal());
            offset += limit;
        }

        log.info("Saved {} race results to database for season {}", savedCount, season);
    }

    /**
     * Latest race result
     */
    public void importLatestRaceResult(int season) {
        String url = BASE_URL + "/" + season + "/last/results.json";

        RacesResponseDto response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(RacesResponseDto.class)
                .onErrorResume(e -> {
                    log.error("Failed to fetch latest race result for season {}", season, e);
                    return Mono.empty();
                })
                .block();

        if (response != null && response.getMRData() != null) {
            List<RacesResponseDto.Race> races = response.getMRData().getRaceTable().getRaces();
            races.forEach(race -> saveOrUpdateRaceResult(race, season, false, true));
            log.info("Saved latest race result to database for season {}", season);
        }
    }

    @Transactional
    public void saveOrUpdateRaceResult(RacesResponseDto.Race race, int season, boolean checkEventStatus, boolean updateExisting) {
        MotorsportEvent existingEvent = eventRepository
                .findByCompetitionAndSeasonAndRound(
                        MotorsportCompetition.FORMULA_ONE,
                        season,
                        Integer.parseInt(race.getRound())
                ).orElse(null);

        MotorsportEvent mappedEvent = toEntity(race, true);

        if (existingEvent != null) {
            if (!updateExisting || checkEventStatus && EventStatus.COMPLETED.equals(existingEvent.getStatus())) {
                log.info("Event already COMPLETED for round {} and season {}. Skipping update.", race.getRound(), season);
                return;
            }
            existingEvent.updateResult(mappedEvent);
            eventRepository.save(existingEvent);
            log.info("Event updated for round {} and season {}.", race.getRound(), season);
        } else {
            eventRepository.save(mappedEvent);
        }
    }

    public MotorsportEvent toEntity(RacesResponseDto.Race race, boolean includeWinner) {
        MotorsportEvent event = new MotorsportEvent();

        event.setCompetition(MotorsportCompetition.FORMULA_ONE);
        event.setStatus(EventStatus.SCHEDULED);
        event.setExternalSource(ExternalMotorsportSource.JOLPICA_F1);
        event.setLastUpdated(LocalDateTime.now());
        event.setSeason(Integer.parseInt(race.getSeason()));
        event.setRound(Integer.parseInt(race.getRound()));
        event.setRaceName(race.getRaceName());
        event.setCircuitName(race.getCircuit().getCircuitName());

        if (race.getCircuit() != null && race.getCircuit().getLocation() != null) {
            String countryName = race.getCircuit().getLocation().getCountry();
            String loc = race.getCircuit().getLocation().getLocality() + ", " + countryName;
            event.setLocation(loc);
            String isoCode = countryCodeService.getCountryCode(countryName);
            if (isoCode != null) {
                event.setFlagUrl(String.format("https://flagcdn.com/%s.svg", isoCode.toLowerCase()));
            }
        }

        event.setExternalLink(race.getUrl());

        // Combine Date + Time
        if (race.getDate() != null) {
            String dateTimeStr = race.getDate();
            if (race.getTime() != null) {
                dateTimeStr += "T" + race.getTime();
                event.setDateTime(LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER));
            } else {
                event.setDateTime(LocalDate.parse(dateTimeStr).atStartOfDay());
            }
        }

        // Winner - for result api response
        if (includeWinner && race.getResults() != null && !race.getResults().isEmpty()) {
            RacesResponseDto.Result winnerResult = race.getResults()
                    .stream()
                    .filter(r -> "1".equals(r.getPosition()))
                    .findFirst()
                    .orElse(null);
            if (winnerResult != null) {
                String winner = winnerResult.getDriver().getGivenName() + " " +
                        winnerResult.getDriver().getFamilyName();
                event.setWinner(winner);
                if (winnerResult.getConstructor() != null) {
                    motorsportTeamRepository.findByName(winnerResult.getConstructor().getName())
                            .ifPresent(event::setWinnerTeam);
                }
                event.setStatus(EventStatus.COMPLETED);
            }
        }

        return event;
    }
}
