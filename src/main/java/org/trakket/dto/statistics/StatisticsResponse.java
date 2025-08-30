package org.trakket.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatisticsResponse {
    private StatisticsOverall overall;
    private StatisticsFootball football;
    private StatisticsMotorsport motorsport;
}