package org.trakket.dto.football;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.trakket.enums.Gender;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FootballTeamDto {
    private Long id;
    private String name;
    private String shortName;
    private String country;
    private String logoUrl;
    private String[] alternativeNames;
    private Gender gender;
}


