package com.urlshortener.web;

import com.urlshortener.domain.Link;
import com.urlshortener.service.AnalyticsService;
import com.urlshortener.service.LinkService;
import com.urlshortener.web.dto.ClickEventView;
import com.urlshortener.web.dto.StatsResponse;
import com.urlshortener.web.error.LinkNotFoundException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/stats")
public class StatsController {

    private final LinkService linkService;
    private final AnalyticsService analyticsService;

    public StatsController(LinkService linkService, AnalyticsService analyticsService) {
        this.linkService = linkService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{code}")
    public StatsResponse stats(@PathVariable("code") String code) {
        Link link = linkService.findByCode(code)
                .orElseThrow(() -> new LinkNotFoundException(code));
        return analyticsService.buildStats(link);
    }

    @GetMapping("/{code}/clicks")
    public List<ClickEventView> recentClicks(
            @PathVariable("code") String code,
            @RequestParam(name = "limit", defaultValue = "50") @Min(1) @Max(500) int limit,
            @RequestParam(name = "offset", defaultValue = "0") @Min(0) int offset) {
        Link link = linkService.findByCode(code)
                .orElseThrow(() -> new LinkNotFoundException(code));
        return analyticsService.recentClicks(link, limit, offset);
    }
}
