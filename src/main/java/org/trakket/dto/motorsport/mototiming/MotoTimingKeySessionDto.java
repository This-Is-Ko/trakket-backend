package org.trakket.dto.motorsport.mototiming;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MotoTimingKeySessionDto {

    @JsonProperty("session_shortname")
    private String sessionShortname;

    @JsonProperty("session_name")
    private String sessionName;

    @JsonProperty("start_datetime_utc")
    private String startDatetimeUtc;

}
