package org.trakket.service.motorsport.mototiming;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.trakket.dto.motorsport.mototiming.MotoTimingClassificationsDto;
import org.trakket.dto.motorsport.mototiming.MotoTimingEventDto;
import org.trakket.dto.motorsport.mototiming.MotoTimingKeySessionDto;
import org.trakket.dto.motorsport.mototiming.MotoTimingResultsResponse;
import org.trakket.dto.motorsport.mototiming.MotoTimingScheduleResponse;
import org.trakket.enums.EventStatus;
import org.trakket.enums.ExternalMotorsportSource;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.model.MotorsportEvent;
import org.trakket.repository.MotorsportEventRepository;
import reactor.core.publisher.Mono;


import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;


/**
 * Service to import season events from MotoTiming (mototiming.live)
 */
@Service
@Slf4j
public class MotoTimingService {
    private final WebClient motoTimingWebClient;
    private final MotorsportEventRepository eventRepository;

    public MotoTimingService(@Qualifier("motoTimingWebClient") WebClient motoTimingWebClient,
                             MotorsportEventRepository eventRepository) {
        this.motoTimingWebClient = motoTimingWebClient;
        this.eventRepository = eventRepository;
    }

    public void syncEvents(MotorsportCompetition competition) {
        updateResults(competition);
    }

    public void updateResults(MotorsportCompetition competition) {
        if (competition == null) {
            log.error("Unable to update MotoTiming results - competition is null");
            throw new IllegalArgumentException("Competition is null");
        }
        log.info("Updating results {} events from MotoTiming", competition.getDisplayName());

        Integer season = 2025;
        MotoTimingResultsResponse resp = motoTimingWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/results")
                        .queryParam("q_season", season)
                        .queryParam("q_category", "MotoGP")
                        .queryParam("q_session", "RAC")
//                        .queryParam("q_event", "RAC")
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("MotoTiming results response error: " + body))))
                .bodyToMono(MotoTimingResultsResponse.class)
                .block();

        if (resp == null || resp.getEvent() == null || resp.getClassifications() == null) {
            log.info("No MotoTiming results found");
            throw new RuntimeException("No MotoTiming results found");
        }

        MotorsportEvent event = findExistingEvent(null, resp.getEvent().getName());
        if (event == null) {
            event = new MotorsportEvent();
        }

        upsertFromDto(event, resp.getEvent(), resp.getClassifications().get(0), competition, season, null);
    }

    public void importSeasonEvents(MotorsportCompetition competition) {
        if (competition == null) {
            log.error("Unable to import MotoTiming season events - competition is null");
            return;
        }
        log.info("Importing {} events from MotoTiming", competition.getDisplayName());

        MotoTimingScheduleResponse resp = motoTimingWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/schedule")
                        .queryParam("filter", "all")
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(body -> Mono.error(new RuntimeException("MotoTiming schedule response error: " + body))))
                .bodyToMono(MotoTimingScheduleResponse.class)
                .block();

        if (resp == null || resp.getCalendar() == null) {
            log.info("No MotoTiming calendar found for {}", competition.getDisplayName());
            return;
        }

        Integer season = resp.getSeason();
        List<MotoTimingEventDto> calendar = resp.getCalendar();

        int roundCounter = 1;
        for (MotoTimingEventDto dto : calendar) {
            try {
                boolean isRaceWeekend = dto.getTest() != null && dto.getTest() == 0;
                addEventsFromSchedule(dto, competition, season, isRaceWeekend ? roundCounter : null);
                if (isRaceWeekend) {
                    roundCounter++;
                }
            } catch (Exception e) {
                log.error("Failed to import {} event {}: {}", competition.getDisplayName(), dto.getId(), e.getMessage());
            }
        }
        log.info("Finished importing events from MotoTiming");
    }

    private void addEventsFromSchedule(MotoTimingEventDto eventDto, MotorsportCompetition competition, Integer season, Integer round) {
        Long extId = eventDto.getId();
        MotorsportEvent event = findExistingEvent(extId, null);
        if (event == null) {
            event = new MotorsportEvent();
        }
        upsertFromDto(event, eventDto, null, competition, season, round);
    }

    private void upsertFromDto(MotorsportEvent event, MotoTimingEventDto eventDto, MotoTimingClassificationsDto classificationsDto, MotorsportCompetition competition, Integer season, Integer round) {
        if (eventDto == null) return;
        Long extId = eventDto.getId();

        // do not update completed events
        if (event.getStatus() == EventStatus.COMPLETED) {
            return;
        }

        // Skip test sessions
        if (eventDto.getName().toLowerCase().contains("test")) {
            return;
        }

        event.setCompetition(competition);
        event.setSeason(season);

        // race / display names
        event.setRaceName(eventDto.getName());
        event.setCircuitName(eventDto.getCircuit());

        event.setExternalSource(ExternalMotorsportSource.MOTO_TIMING);
        event.setExternalSourceId(extId);

        // base Event fields
        event.setRound(round);
        event.setLocation(eventDto.getCountry());

        // determine race datetime: prefer the key_session with shortname 'RAC'
        LocalDateTime raceDateTime = findRaceSessionDate(eventDto);
        if (raceDateTime == null) {
        // fallback to the event's start_date if present
            raceDateTime = parseOffsetToLocalDateTime(eventDto.getStartDate());
        }
        event.setDateTime(raceDateTime);
        event.setLastUpdated(LocalDateTime.now());

        String isoCode = eventDto.getCountryCode();
        if (isoCode != null) {
            event.setFlagUrl(String.format("https://flagcdn.com/%s.svg", isoCode.toLowerCase()));
        }

        // status handling: if last_session_end_time exists and is in the past -> COMPLETED
        OffsetDateTime lastSessionEnd = parseOffset(eventDto.getLastSessionEndTime());
        if (lastSessionEnd != null && lastSessionEnd.isBefore(OffsetDateTime.now())) {
            event.setStatus(EventStatus.COMPLETED);
            if (classificationsDto != null) {
                event.setWinner(classificationsDto.getRiderName());
//                event.setWinnerTeam(classificationsDto.getTeamName());
            }
        } else {
            event.setStatus(EventStatus.SCHEDULED);
        }

        eventRepository.save(event);
    }

    private MotorsportEvent findExistingEvent(Long extId, String raceName) {
        if (extId != null) {
            Optional<MotorsportEvent> existing = eventRepository.findByExternalSourceAndExternalSourceId(
                    ExternalMotorsportSource.MOTO_TIMING, extId);
            return existing.orElse(null);
        } else {
            Optional<MotorsportEvent> existing = eventRepository.findByExternalSourceAndRaceName(
                    ExternalMotorsportSource.MOTO_TIMING, raceName);
            return existing.orElse(null);
        }
    }

    private LocalDateTime findRaceSessionDate(MotoTimingEventDto dto) {
        if (dto == null || dto.getKeySessionTimes() == null || dto.getKeySessionTimes().isEmpty()) return null;

        // prefer explicit RAC session
        Optional<MotoTimingKeySessionDto> rac = dto.getKeySessionTimes().stream()
                .filter(k -> k.getSessionShortname() != null && k.getSessionShortname().equalsIgnoreCase("RAC"))
                .findFirst();
        if (rac.isPresent()) {
            return parseOffsetToLocalDateTime(rac.get().getStartDatetimeUtc());
        }

        // fallback: look for session_name mentioning Grand Prix / Grand Prix
        Optional<MotoTimingKeySessionDto> gp = dto.getKeySessionTimes().stream()
                .filter(k -> k.getSessionName() != null && k.getSessionName().toLowerCase().contains("grand prix"))
                .findFirst();
        if (gp.isPresent()) {
            return parseOffsetToLocalDateTime(gp.get().getStartDatetimeUtc());
        }

        // last fallback: choose the last session in the list
        MotoTimingKeySessionDto last = dto.getKeySessionTimes().get(dto.getKeySessionTimes().size() - 1);
        return parseOffsetToLocalDateTime(last.getStartDatetimeUtc());
    }

    private OffsetDateTime parseOffset(String ts) {
        if (ts == null || ts.isBlank()) return null;
        try {
            return OffsetDateTime.parse(ts);
        } catch (Exception e) {
            // try to be permissive: strip microseconds if necessary
            try {
                String cleaned = ts.replaceAll("(\\.\\d{3,6})Z$", "Z");
                return OffsetDateTime.parse(cleaned);
            } catch (Exception e2) {
                log.debug("Failed to parse OffsetDateTime {}: {}", ts, e2.getMessage());
                return null;
            }
        }
    }

    private LocalDateTime parseOffsetToLocalDateTime(String ts) {
        OffsetDateTime odt = parseOffset(ts);
        return odt != null ? odt.toLocalDateTime() : null;
    }
}
