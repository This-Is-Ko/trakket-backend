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
public class MotoTimingScheduleResponse {
    private Integer season;

    @JsonProperty("calendar")
    private List<MotoTimingEventDto> calendar;
}
