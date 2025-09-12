package org.trakket.dto.motorsport.mototiming;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MotoTimingResultsResponse {
    private Integer season;

    @JsonProperty("events")
    private List<MotoTimingEventDto> events;

    @JsonProperty("event")
    private MotoTimingEventDto event;

    @JsonProperty("classifications")
    private List<MotoTimingClassificationsDto> classifications;
}
