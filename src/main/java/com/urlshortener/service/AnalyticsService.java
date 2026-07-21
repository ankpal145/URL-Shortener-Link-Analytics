package com.urlshortener.service;

import com.urlshortener.domain.ClickEvent;
import com.urlshortener.domain.Link;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.web.dto.ClickEventView;
import com.urlshortener.web.dto.DailyCount;
import com.urlshortener.web.dto.LabeledCount;
import com.urlshortener.web.dto.StatsResponse;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {

    private static final int DEFAULT_TOP_N = 5;
    private static final int STATS_WINDOW_DAYS = 14;

    private final ClickEventRepository clickRepository;
    private final IpHasher ipHasher;
    private final Clock clock;

    public AnalyticsService(ClickEventRepository clickRepository, IpHasher ipHasher, Clock clock) {
        this.clickRepository = clickRepository;
        this.ipHasher = ipHasher;
        this.clock = clock;
    }

    @Transactional
    public void recordClick(Link link, String referer, String userAgent, String ip) {
        ClickEvent event = new ClickEvent(
                link.getId(),
                Instant.now(clock),
                truncate(referer, 512),
                truncate(userAgent, 512),
                ipHasher.hash(ip));
        clickRepository.save(event);
    }

    @Transactional(readOnly = true)
    public StatsResponse buildStats(Link link) {
        Long linkId = link.getId();
        long totalClicks = clickRepository.countByLinkId(linkId);
        long uniqueVisitors = clickRepository.countDistinctIpHashByLinkId(linkId);
        Instant firstClick = clickRepository.findFirstClickAt(linkId);
        Instant lastClick = clickRepository.findLastClickAt(linkId);

        var pageable = PageRequest.of(0, DEFAULT_TOP_N);
        List<LabeledCount> topReferrers = mapCounts(clickRepository.topReferrers(linkId, pageable));
        List<LabeledCount> topUserAgents = mapCounts(clickRepository.topUserAgents(linkId, pageable));

        Instant since = Instant.now(clock).minus(STATS_WINDOW_DAYS, ChronoUnit.DAYS);
        List<Instant> stamps = clickRepository.findClickedAtSince(linkId, since);
        List<DailyCount> byDay = bucketByDay(stamps, STATS_WINDOW_DAYS);

        return new StatsResponse(
                link.getCode(),
                link.getOriginalUrl(),
                link.isCustomAlias(),
                link.getCreatedAt(),
                totalClicks,
                uniqueVisitors,
                firstClick,
                lastClick,
                topReferrers,
                topUserAgents,
                byDay);
    }

    @Transactional(readOnly = true)
    public List<ClickEventView> recentClicks(Link link, int limit, int offset) {
        int safeLimit = Math.min(Math.max(limit, 1), 500);
        int safeOffset = Math.max(offset, 0);
        int page = safeOffset / safeLimit;
        return clickRepository
                .findByLinkIdOrderByClickedAtDesc(link.getId(), PageRequest.of(page, safeLimit))
                .stream()
                .map(ClickEventView::from)
                .toList();
    }

    private List<LabeledCount> mapCounts(List<Object[]> rows) {
        List<LabeledCount> out = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            out.add(new LabeledCount((String) row[0], ((Number) row[1]).longValue()));
        }
        return out;
    }

    private List<DailyCount> bucketByDay(List<Instant> stamps, int windowDays) {
        Map<LocalDate, Long> counts = new TreeMap<>();
        LocalDate today = LocalDate.now(clock.withZone(ZoneOffset.UTC));
        for (int i = windowDays - 1; i >= 0; i--) {
            counts.put(today.minusDays(i), 0L);
        }
        for (Instant t : stamps) {
            LocalDate day = t.atZone(ZoneOffset.UTC).toLocalDate();
            counts.merge(day, 1L, Long::sum);
        }
        List<DailyCount> out = new ArrayList<>(counts.size());
        counts.forEach((day, count) -> out.add(new DailyCount(day.toString(), count)));
        return out;
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
