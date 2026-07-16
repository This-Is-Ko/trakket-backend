package org.trakket.dto.motorsport;

import org.trakket.dto.PaginatedResponse;

import java.util.List;

public class MotorsportEventsWithStatusResponse extends PaginatedResponse {

    private List<MotorsportEventWithStatus> events;

    public MotorsportEventsWithStatusResponse() {}

    public MotorsportEventsWithStatusResponse(int pageNumber, int pageSize, long totalElements, boolean last, List<MotorsportEventWithStatus> events) {
        super(pageNumber, pageSize, totalElements, last);
        this.events = events;
    }

    public List<MotorsportEventWithStatus> getEvents() { return events; }
    public void setEvents(List<MotorsportEventWithStatus> events) { this.events = events; }
}
