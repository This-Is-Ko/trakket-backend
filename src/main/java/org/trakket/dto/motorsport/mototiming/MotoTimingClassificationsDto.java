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
public class MotoTimingClassificationsDto {
    private Integer position;

    @JsonProperty("rider_name")
    private String riderName;

    @JsonProperty("team_name")
    private String teamName;

    @JsonProperty("constructor_name")
    private String constructorName;
}
