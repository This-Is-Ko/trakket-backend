package org.trakket.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AdminStatsResponse {

    private long totalUsers;
    private long totalFootballEvents;
    private long totalMotorsportEvents;
    private long footballWatchedCount;
    private long motorsportWatchedCount;
    private Map<String, Long> footballWatchStatusDistribution;
    private Map<String, Long> motorsportWatchStatusDistribution;

}
