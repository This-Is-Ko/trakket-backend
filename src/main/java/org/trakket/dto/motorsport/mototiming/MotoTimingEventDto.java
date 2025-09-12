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
public class MotoTimingEventDto {
    private Long id;

    private String shortname;

    private String name;

    private String hashtag;

    private String circuit;

    @JsonProperty("country_code")
    private String countryCode;

    private String country;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("local_tz_offset")
    private Integer localTzOffset;

    private Integer test;

    @JsonProperty("has_timing")
    private Integer hasTiming;

    @JsonProperty("friendly_name")
    private String friendlyName;

    private String dates;

    @JsonProperty("key_session_times")
    private List<MotoTimingKeySessionDto> keySessionTimes;

    @JsonProperty("last_session_end_time")
    private String lastSessionEndTime;
}
