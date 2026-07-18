package org.trakket.dto.landing;

import java.util.List;

public record LandingEventsResponse(
        List<LandingEventDto> events
) {}
