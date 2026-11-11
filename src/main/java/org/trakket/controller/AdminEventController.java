package org.trakket.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.trakket.dto.admin.FootballEventUpdateRequest;
import org.trakket.dto.admin.MotorsportEventUpdateRequest;
import org.trakket.dto.football.FootballEventDto;
import org.trakket.dto.motorsport.MotorsportEventDto;
import org.trakket.enums.EventStatus;
import org.trakket.enums.FootballCompetition;
import org.trakket.enums.MotorsportCompetition;
import org.trakket.mapper.FootballEventMapper;
import org.trakket.mapper.MotorsportEventMapper;
import org.trakket.model.FootballEvent;
import org.trakket.model.MotorsportEvent;
import org.trakket.service.football.FootballEventService;
import org.trakket.service.motorsport.MotorsportEventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/events")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminEventController {

    private final FootballEventService footballEventService;
    private final MotorsportEventService motorsportEventService;

    // ─── Football ───────────────────────────────────────────────────────────

    @GetMapping("/football")
    public ResponseEntity<Map<String, Object>> getFootballEvents(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(name = "competition", required = false) FootballCompetition competition,
            @RequestParam(name = "status", required = false) EventStatus status,
            @RequestParam(name = "ascending", required = false) Boolean ascending
    ) {
        Sort sort = (ascending != null && ascending)
                ? Sort.by("dateTime").ascending()
                : Sort.by("dateTime").descending();

        var pageable = PageRequest.of(page, pageSize, sort);

        Specification<FootballEvent> spec = Specification.unrestricted();
        if (competition != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("competition"), competition));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        var eventPage = footballEventService.findAll(spec, pageable);

        List<FootballEventDto> eventDtos = eventPage.stream()
                .map(FootballEventMapper::toDto)
                .toList();

        return ResponseEntity.ok(Map.of(
                "events", eventDtos,
                "pageNumber", eventPage.getNumber(),
                "pageSize", eventPage.getSize(),
                "totalElements", eventPage.getTotalElements(),
                "last", eventPage.isLast()
        ));
    }

    @PutMapping("/football/{id}")
    public ResponseEntity<FootballEventDto> updateFootballEvent(
            @PathVariable("id") Long id,
            @Valid @RequestBody FootballEventUpdateRequest request
    ) {
        FootballEvent event = footballEventService.updateEvent(id, request);
        return ResponseEntity.ok(FootballEventMapper.toDto(event));
    }

    // ─── Motorsport ─────────────────────────────────────────────────────────

    @GetMapping("/motorsport")
    public ResponseEntity<Map<String, Object>> getMotorsportEvents(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(name = "competition", required = false) MotorsportCompetition competition,
            @RequestParam(name = "status", required = false) EventStatus status,
            @RequestParam(name = "ascending", required = false) Boolean ascending
    ) {
        Sort sort = (ascending != null && ascending)
                ? Sort.by("dateTime").ascending()
                : Sort.by("dateTime").descending();

        var pageable = PageRequest.of(page, pageSize, sort);

        Specification<MotorsportEvent> spec = Specification.unrestricted();
        if (competition != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("competition"), competition));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        var eventPage = motorsportEventService.findAll(spec, pageable);

        List<MotorsportEventDto> eventDtos = eventPage.stream()
                .map(MotorsportEventMapper::toDto)
                .toList();

        return ResponseEntity.ok(Map.of(
                "events", eventDtos,
                "pageNumber", eventPage.getNumber(),
                "pageSize", eventPage.getSize(),
                "totalElements", eventPage.getTotalElements(),
                "last", eventPage.isLast()
        ));
    }

    @PutMapping("/motorsport/{id}")
    public ResponseEntity<MotorsportEventDto> updateMotorsportEvent(
            @PathVariable("id") Long id,
            @Valid @RequestBody MotorsportEventUpdateRequest request
    ) {
        MotorsportEvent event = motorsportEventService.updateEvent(id, request);
        return ResponseEntity.ok(MotorsportEventMapper.toDto(event));
    }
}
