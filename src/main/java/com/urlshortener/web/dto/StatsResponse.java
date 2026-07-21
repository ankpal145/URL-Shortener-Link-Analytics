package com.urlshortener.web.dto;

import java.time.Instant;
import java.util.List;

public record StatsResponse(
        String code,
        String originalUrl,
        boolean customAlias,
        Instant createdAt,
        long totalClicks,
        long uniqueVisitors,
        Instant firstClickAt,
        Instant lastClickAt,
        List<LabeledCount> topReferrers,
        List<LabeledCount> topUserAgents,
        List<DailyCount> clicksByDay
) { }
