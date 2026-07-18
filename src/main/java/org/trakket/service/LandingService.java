package org.trakket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.trakket.dto.landing.LandingEventDto;
import org.trakket.dto.landing.LandingEventsResponse;
import org.trakket.enums.EventStatus;
import org.trakket.model.FootballEvent;
import org.trakket.model.MotorsportEvent;
import org.trakket.repository.FootballEventRepository;
import org.trakket.repository.MotorsportEventRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LandingService {

    private static final int TARGET_EVENT_COUNT = 8;

    private final FootballEventRepository footballEventRepository;
    private final MotorsportEventRepository motorsportEventRepository;

    public LandingEventsResponse getLandingEvents() {
        List<EventStatus> activeStatuses = List.of(EventStatus.COMPLETED, EventStatus.IN_PROGRESS);

        List<FootballEvent> recentFootball = footballEventRepository.findRecentByStatusIn(activeStatuses);
        List<MotorsportEvent> recentMotorsport = motorsportEventRepository.findRecentByStatusIn(activeStatuses);

        List<LandingEventDto> footballDtos = recentFootball.stream()
                .map(this::toLandingDto)
                .toList();

        List<LandingEventDto> motorsportDtos = recentMotorsport.stream()
                .map(this::toLandingDto)
                .toList();

        List<LandingEventDto> selected = selectDiverseEvents(footballDtos, motorsportDtos);
        return new LandingEventsResponse(selected);
    }

    private List<LandingEventDto> selectDiverseEvents(
            List<LandingEventDto> footballEvents,
            List<LandingEventDto> motorsportEvents
    ) {
        // Group events by (sportType, competition) for diversity
        Map<String, Queue<LandingEventDto>> grouped = new LinkedHashMap<>();

        for (LandingEventDto event : footballEvents) {
            String key = "FOOTBALL::" + event.competition();
            grouped.computeIfAbsent(key, k -> new LinkedList<>()).add(event);
        }

        for (LandingEventDto event : motorsportEvents) {
            String key = "MOTORSPORT::" + event.competition();
            grouped.computeIfAbsent(key, k -> new LinkedList<>()).add(event);
        }

        if (grouped.isEmpty()) {
            return List.of();
        }

        // Separate group keys by sport type so we can alternate between sports
        List<String> footballGroupKeys = grouped.keySet().stream()
                .filter(k -> k.startsWith("FOOTBALL::"))
                .collect(Collectors.toCollection(ArrayList::new));
        List<String> motorsportGroupKeys = grouped.keySet().stream()
                .filter(k -> k.startsWith("MOTORSPORT::"))
                .collect(Collectors.toCollection(ArrayList::new));

        List<LandingEventDto> result = new ArrayList<>();

        // Phase 1: alternate between sport types, taking one per competition group
        // This guarantees motorsport events won't be crowded out by the many football competitions
        int fi = 0, mi = 0;
        boolean footballTurn = !footballGroupKeys.isEmpty();

        while (result.size() < TARGET_EVENT_COUNT) {
            boolean tookOne = false;

            if (footballTurn) {
                if (fi < footballGroupKeys.size()) {
                    String key = footballGroupKeys.get(fi);
                    Queue<LandingEventDto> queue = grouped.get(key);
                    if (!queue.isEmpty()) {
                        result.add(queue.poll());
                        tookOne = true;
                    }
                    if (queue.isEmpty()) {
                        footballGroupKeys.remove(fi);
                    } else {
                        fi++;
                    }
                } else if (mi < motorsportGroupKeys.size()) {
                    // Football exhausted, switch to motorsport
                    footballTurn = false;
                    continue;
                }
            } else {
                if (mi < motorsportGroupKeys.size()) {
                    String key = motorsportGroupKeys.get(mi);
                    Queue<LandingEventDto> queue = grouped.get(key);
                    if (!queue.isEmpty()) {
                        result.add(queue.poll());
                        tookOne = true;
                    }
                    if (queue.isEmpty()) {
                        motorsportGroupKeys.remove(mi);
                    } else {
                        mi++;
                    }
                } else if (fi < footballGroupKeys.size()) {
                    // Motorsport exhausted, switch to football
                    footballTurn = true;
                    continue;
                }
            }

            if (!tookOne) {
                break;
            }

            // Switch turns to the other sport if it still has groups
            if (footballTurn && mi < motorsportGroupKeys.size()) {
                footballTurn = false;
            } else if (!footballTurn && fi < footballGroupKeys.size()) {
                footballTurn = true;
            }
        }

        // Phase 2: if we still need more events, fall back to filling from any remaining queues
        if (result.size() < TARGET_EVENT_COUNT) {
            for (Queue<LandingEventDto> queue : grouped.values()) {
                while (!queue.isEmpty() && result.size() < TARGET_EVENT_COUNT) {
                    result.add(queue.poll());
                }
            }
        }

        return result;
    }

    private LandingEventDto toLandingDto(FootballEvent event) {
        return new LandingEventDto(
                event.getId(),
                "FOOTBALL",
                event.getCompetition().getDisplayName(),
                event.getCompetition().getLogoUrl(),
                event.getTitle(),
                event.getSubtitle(),
                event.getDateTime(),
                event.getStatus(),
                event.getLocation(),
                // Football fields
                event.getHomeTeam().getName(),
                event.getHomeTeam().getLogoUrl(),
                event.getHomeScore(),
                event.getAwayTeam().getName(),
                event.getAwayTeam().getLogoUrl(),
                event.getAwayScore(),
                // Motorsport fields (null)
                null, null, null, null
        );
    }

    private LandingEventDto toLandingDto(MotorsportEvent event) {
        return new LandingEventDto(
                event.getId(),
                "MOTORSPORT",
                event.getCompetition().getDisplayName(),
                event.getCompetition().getLogoUrl(),
                event.getTitle(),
                event.getSubtitle(),
                event.getDateTime(),
                event.getStatus(),
                event.getLocation(),
                // Football fields (null)
                null, null, null, null, null, null,
                // Motorsport fields
                event.getRaceName(),
                event.getCircuitName(),
                event.getWinner(),
                event.getFlagUrl()
        );
    }
}
