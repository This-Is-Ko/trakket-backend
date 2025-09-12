package org.trakket.dto.motorsport.thesportsdb;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SportsDbMetadata {
    private Integer competitionId;
    private String season;
}
