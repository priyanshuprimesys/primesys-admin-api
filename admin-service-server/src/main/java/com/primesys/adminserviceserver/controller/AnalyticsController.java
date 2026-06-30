package com.primesys.adminserviceserver.controller;

import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.service.impl.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin("*")
@RestController
@RequestMapping("v2/issue/detail-analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping
    public HttpApiResponse<Map<String, Object>> getAnalytics(@RequestParam(required = false) Long startEpoch,
            @RequestParam(required = false) Long endEpoch, @RequestParam(defaultValue = "daily") String trendMode,
            @RequestParam(required = true) String assigneeId, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, @RequestParam Map<String, String> allRequestParams // capture
    // all query
    // params
    ) {
        // Remove known params from the map so they don't mix with filters
        allRequestParams.remove("startEpoch");
        allRequestParams.remove("endEpoch");
        allRequestParams.remove("trendMode");
        allRequestParams.remove("assigneeId");
        allRequestParams.remove("page");
        allRequestParams.remove("size");
        allRequestParams.remove("extraFields");

        // Now allRequestParams contains ONLY your dynamic filters
        Map<String, String> extraFieldValues = allRequestParams;
        HttpApiResponse<Map<String, Object>> result = new HttpApiResponse<>(analyticsService.getAnalytics(startEpoch,
                endEpoch, trendMode, assigneeId, page, size, extraFieldValues));

        return result;
    }
}

// GET
// v2/issue/detail-analytics?startEpoch=1754700000000&endEpoch=1754886540000&trendMode=daily&assigneeId=658a7f9ef7c9f16b673a03e9&page=0&size=10&extraFields=category,tags

// GET /api/analytics?
// startDate=2025-08-01T00:00:00&
// endDate=2025-08-10T23:59:59&
// trendMode=daily&
// assigneeId=658a7f9ef7c9f16b673a03e9&
// page=0&
// size=10&
// extraFields=category,tags&
// extraFieldValues[category]=BUG,FEATURE
// Response will include keys:
//
// global_issue_status, global_priority, global_category, global_tags
//
// user_issue_status, user_priority, user_category, user_tags
//
// combination_counts (list of combos with counts)
//
// trend (period -> {status:count}) — zero-filled for all periods in range
//
// raw_issues (paginated issue documents)
//
// plus global_<extraField> and user_<extraField> for any extraFields requested (zero-filled from
// request/defaults/auto-detect)
