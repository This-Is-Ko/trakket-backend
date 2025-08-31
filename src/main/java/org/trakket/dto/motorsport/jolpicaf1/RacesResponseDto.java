package org.trakket.dto.motorsport.jolpicaf1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RacesResponseDto {
    @JsonProperty("MRData")
    private MRData MRData;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MRData {
        @JsonProperty("RaceTable")
        private RaceTable RaceTable;
        private String total;
        private String limit;
        private String offset;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RaceTable {
        @JsonProperty("Races")
        private List<Race> Races;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Race {
        private String season;
        private String round;
        private String raceName;
        private String date;
        private String time;
        private String url;

        @JsonProperty("Circuit")
        private Circuit Circuit;

        @JsonProperty("Results")
        private List<Result> Results;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Circuit {
        private String circuitName;

        @JsonProperty("Location")
        private Location Location;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        private String locality;
        private String country;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String position;

        @JsonProperty("Driver")
        private Driver Driver;

        @JsonProperty("Constructor")
        private Constructor Constructor;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Driver {
        private String givenName;
        private String familyName;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Constructor {
        private String name;
    }
}
