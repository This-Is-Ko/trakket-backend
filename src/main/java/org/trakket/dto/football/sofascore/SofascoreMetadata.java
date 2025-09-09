package org.trakket.dto.football.sofascore;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.trakket.enums.Gender;

@Data
@AllArgsConstructor
public class SofascoreMetadata {

    private Integer competitionId;
    private Integer seasonId;

}
