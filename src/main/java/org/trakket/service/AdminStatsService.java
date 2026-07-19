package org.trakket.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.trakket.dto.admin.AdminStatsResponse;
import org.trakket.enums.WatchedStatus;
import org.trakket.repository.FootballEventRepository;
import org.trakket.repository.MotorsportEventRepository;
import org.trakket.repository.UserRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserRepository userRepository;
    private final FootballEventRepository footballEventRepository;
    private final MotorsportEventRepository motorsportEventRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AdminStatsResponse getStats() {
        long totalUsers = userRepository.count();
        long totalFootballEvents = footballEventRepository.count();
        long totalMotorsportEvents = motorsportEventRepository.count();

        // Count watched (non-UNWATCHED) football events globally
        long footballWatchedCount = entityManager.createQuery(
                "SELECT COUNT(DISTINCT ws.event.id) FROM FootballEventWatchStatus ws WHERE ws.status <> :unwatched",
                Long.class)
                .setParameter("unwatched", WatchedStatus.UNWATCHED)
                .getSingleResult();

        // Count watched (non-UNWATCHED) motorsport events globally
        long motorsportWatchedCount = entityManager.createQuery(
                "SELECT COUNT(DISTINCT ws.event.id) FROM MotorsportEventWatchStatus ws WHERE ws.status <> :unwatched",
                Long.class)
                .setParameter("unwatched", WatchedStatus.UNWATCHED)
                .getSingleResult();

        // Football watch status distribution (global)
        Map<String, Long> footballDist = new LinkedHashMap<>();
        List<Object[]> fbRows = entityManager.createQuery(
                "SELECT ws.status, COUNT(ws) FROM FootballEventWatchStatus ws GROUP BY ws.status",
                Object[].class)
                .getResultList();
        for (Object[] row : fbRows) {
            footballDist.put(((WatchedStatus) row[0]).name(), (Long) row[1]);
        }
        // Fill in zeros for missing statuses
        for (WatchedStatus status : WatchedStatus.values()) {
            footballDist.putIfAbsent(status.name(), 0L);
        }

        // Motorsport watch status distribution (global)
        Map<String, Long> motorsportDist = new LinkedHashMap<>();
        List<Object[]> msRows = entityManager.createQuery(
                "SELECT ws.status, COUNT(ws) FROM MotorsportEventWatchStatus ws GROUP BY ws.status",
                Object[].class)
                .getResultList();
        for (Object[] row : msRows) {
            motorsportDist.put(((WatchedStatus) row[0]).name(), (Long) row[1]);
        }
        for (WatchedStatus status : WatchedStatus.values()) {
            motorsportDist.putIfAbsent(status.name(), 0L);
        }

        return new AdminStatsResponse(
                totalUsers,
                totalFootballEvents,
                totalMotorsportEvents,
                footballWatchedCount,
                motorsportWatchedCount,
                footballDist,
                motorsportDist
        );
    }
}
