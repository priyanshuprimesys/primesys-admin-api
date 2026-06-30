package com.primesys.adminserviceserver.service.impl;

import com.mongodb.client.MongoCollection;
import com.primesys.adminserviceserver.constants.AnalyticsConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsService {

    private final MongoTemplate mongoTemplate;
    private final String collectionName = "issue_data";

    // Utilities for zero-filling counts
    private LinkedHashMap<String, Integer> zeroFillCounts(List<Document> rawList, List<String> expectedKeys) {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        if (expectedKeys == null || expectedKeys.isEmpty()) {
            if (rawList != null) {
                for (Document d : rawList) {
                    Object id = d.get("_id");
                    if (id == null)
                        continue; // Skip null keys
                    String key = id.toString();
                    if (key.equals("null"))
                        continue; // Skip "null" string keys
                    map.put(key, d.getInteger("count", 0));
                }
            }
            return map;
        }
        // Initialize with zeros for expected keys only
        expectedKeys.forEach(k -> map.put(k, 0));
        if (rawList != null) {
            for (Document d : rawList) {
                String key = Objects.toString(d.get("_id"), null);
                if (key == null || key.equals("null"))
                    continue; // Skip null keys
                if (!expectedKeys.contains(key))
                    continue; // Skip keys not expected
                map.put(key, d.getInteger("count", 0));
            }
        }
        return map;
    }

    private List<Map<String, Integer>> zeroFillCountsAsArray(List<Document> rawList, List<String> expectedKeys) {
        List<Map<String, Integer>> result = new ArrayList<>();

        if (expectedKeys == null || expectedKeys.isEmpty()) {
            if (rawList != null) {
                for (Document d : rawList) {
                    String key = Objects.toString(d.get("_id"), null);
                    if (key == null || key.equals("null"))
                        continue;
                    int count = d.getInteger("count", 0);
                    result.add(Collections.singletonMap(key, count));
                }
            }
            return result;
        }

        Map<String, Integer> filledMap = new LinkedHashMap<>();
        expectedKeys.forEach(k -> filledMap.put(k, 0));

        if (rawList != null) {
            for (Document d : rawList) {
                String key = Objects.toString(d.get("_id"), null);
                if (key == null || key.equals("null"))
                    continue;
                if (!expectedKeys.contains(key))
                    continue;
                filledMap.put(key, d.getInteger("count", 0));
            }
        }

        for (Map.Entry<String, Integer> entry : filledMap.entrySet()) {
            result.add(Collections.singletonMap(entry.getKey(), entry.getValue()));
        }

        return result;
    }

    // Helper: build period labels for trend
    private List<String> buildPeriods(LocalDate start, LocalDate end, String trendMode) {
        List<String> periods = new ArrayList<>();
        LocalDate cur = start;
        switch (trendMode.toLowerCase()) {
        case "weekly":
            WeekFields wf = WeekFields.ISO;
            while (!cur.isAfter(end)) {
                periods.add(String.format("%d-%02d", cur.getYear(), cur.get(wf.weekOfWeekBasedYear())));
                cur = cur.plusWeeks(1);
            }
            break;
        case "monthly":
            while (!cur.isAfter(end)) {
                periods.add(String.format("%d-%02d", cur.getYear(), cur.getMonthValue()));
                cur = cur.plusMonths(1);
            }
            break;
        default:
            while (!cur.isAfter(end)) {
                periods.add(cur.toString());
                cur = cur.plusDays(1);
            }
        }
        return periods;
    }

    // Helper: zero-fill trend data
    private LinkedHashMap<String, LinkedHashMap<String, Integer>> zeroFillTrend(List<Document> rawTrend,
            LocalDate start, LocalDate end, String trendMode, List<String> statusOrder) {

        LinkedHashMap<String, LinkedHashMap<String, Integer>> trendMap = new LinkedHashMap<>();
        buildPeriods(start, end, trendMode).forEach(p -> {
            LinkedHashMap<String, Integer> inner = new LinkedHashMap<>();
            statusOrder.forEach(s -> inner.put(s, 0));
            trendMap.put(p, inner);
        });

        if (rawTrend != null) {
            for (Document d : rawTrend) {
                Document id = d.get("_id", Document.class);
                if (id == null)
                    continue;
                String period = id.getString("period");
                String status = id.getString("issue_status");
                int cnt = d.getInteger("count", 0);
                trendMap.computeIfAbsent(period, k -> {
                    LinkedHashMap<String, Integer> inner = new LinkedHashMap<>();
                    statusOrder.forEach(s -> inner.put(s, 0));
                    return inner;
                });
                trendMap.get(period).put(status, cnt);
            }
        }
        return trendMap;
    }

    public Map<String, Object> getAnalytics(Long startEpochMillis, Long endEpochMillis, String trendMode,
            String assigneeId, int page, int size, Map<String, String> extraFieldValues) {

        Instant now = Instant.now();
        if (endEpochMillis == null)
            endEpochMillis = now.toEpochMilli() / 1000;
        if (startEpochMillis == null)
            startEpochMillis = now.minus(Duration.ofDays(365)).toEpochMilli() / 1000;

        long startMillis = startEpochMillis;
        long endMillis = endEpochMillis;

        LocalDate startDate = Instant.ofEpochMilli(startMillis * 1000).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(endMillis * 1000).atZone(ZoneId.systemDefault()).toLocalDate();

        log.info("startMillis={} -> {}", startMillis, startDate);
        log.info("endMillis={} -> {}", endMillis, endDate);
        log.info("assigneeId={}", assigneeId);
        log.info("extraFieldValues={}", extraFieldValues);

        int skip = page * size;

        // Period expression
        String periodExpr;
        switch (trendMode.toLowerCase()) {
        case "weekly":
            periodExpr = "{ $concat: [" + "{ $dateToString: { format: '%Y', date: { $toDate: '$post_time' } } }, '-', "
                    + "{ $toString: { $isoWeek: { $toDate: '$post_time' } } }" + "] }";
            break;
        case "monthly":
            periodExpr = "{ $dateToString: { format: '%Y-%m', date: { $toDate: '$post_time' } } }";
            break;
        default:
            periodExpr = "{ $dateToString: { format: '%Y-%m-%d', date: { $toDate: '$post_time' } } }";
        }

        // Build initial pipeline
        List<Document> pipeline = new ArrayList<>();
        pipeline.add(new Document("$match",
                new Document("post_time", new Document("$gte", startMillis).append("$lte", endMillis))));

        Document proj = new Document().append("issue_status", 1).append("priority", 1).append("category", 1)
                .append("tags", 1).append("assignee", 1).append("assignee_name", 1).append("post_time", 1)
                .append("period", Document.parse(periodExpr));
        pipeline.add(new Document("$project", proj));

        // Build facets
        Document facets = new Document();

        // GLOBAL counts (date only)
        facets.put("global_issue_status", Arrays.asList(
                new Document("$match",
                        new Document("post_time", new Document("$gte", startMillis).append("$lte", endMillis))),
                new Document("$group", new Document("_id", "$issue_status").append("count", new Document("$sum", 1)))));
        facets.put("global_priority", Arrays.asList(
                new Document("$match",
                        new Document("post_time", new Document("$gte", startMillis).append("$lte", endMillis))),
                new Document("$group", new Document("_id", "$priority").append("count", new Document("$sum", 1)))));
        // Example for global_category
        List<Document> globalCategory = Arrays.asList(
                new Document("$match",
                        new Document("post_time", new Document("$gte", startMillis).append("$lte", endMillis))),
                new Document("$group", new Document("_id", "$category").append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("count", -1)) // optional, to sort by count desc
        );
        facets.put("global_category", globalCategory);

        // Example for global_tags
        List<Document> globalTags = Arrays.asList(
                new Document("$match",
                        new Document("post_time", new Document("$gte", startMillis).append("$lte", endMillis))),
                new Document("$unwind", new Document("path", "$tags").append("preserveNullAndEmptyArrays", false)),
                new Document("$group", new Document("_id", "$tags").append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("count", -1)) // optional
        );
        facets.put("global_tags", globalTags);

        // DIVISION-WISE GLOBAL COUNT

        facets.put("division_global_count", Arrays.asList(
                new Document("$match", new Document("division_id", new Document("$exists", true).append("$ne", null))),
                new Document("$group", new Document("_id", "$division_id").append("count", new Document("$sum", 1))),
                new Document("$sort", new Document("count", -1))));

        // DIVISION-WISE USER COUNT
        if (assigneeId != null && !assigneeId.isBlank()) {
            facets.put("division_user_count",
                    Arrays.asList(
                            new Document("$match",
                                    new Document("assignee", assigneeId).append("post_time",
                                            new Document("$gte", startMillis).append("$lte", endMillis))),
                            new Document("$group",
                                    new Document("_id", "$division_id").append("count", new Document("$sum", 1))),
                            new Document("$sort", new Document("count", -1))));
        } else {
            facets.put("division_user_count", Collections.emptyList());
        }

        // TODAY counts
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        long t0 = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long t1 = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1;
        facets.put("today_issue_status", Arrays.asList(
                new Document("$match", new Document("post_time", new Document("$gte", t0).append("$lte", t1))),
                new Document("$group", new Document("_id", "$issue_status").append("count", new Document("$sum", 1)))));

        // USER counts
        if (assigneeId != null && !assigneeId.isBlank()) {
            facets.put("user_issue_status",
                    Arrays.asList(new Document("$match", new Document("assignee", assigneeId)), new Document("$group",
                            new Document("_id", "$issue_status").append("count", new Document("$sum", 1)))));
            facets.put("user_priority", Arrays.asList(new Document("$match", new Document("assignee", assigneeId)),
                    new Document("$group", new Document("_id", "$priority").append("count", new Document("$sum", 1)))));
            List<Document> uCategory = new ArrayList<>();
            uCategory.add(new Document("$match", new Document("assignee", assigneeId).append("post_time",
                    new Document("$gte", startMillis).append("$lte", endMillis))));
            uCategory.add(
                    new Document("$group", new Document("_id", "$category").append("count", new Document("$sum", 1))));
            uCategory.add(new Document("$sort", new Document("count", -1)));
            facets.put("user_category", uCategory);

            List<Document> uTags = new ArrayList<>();
            uTags.add(new Document("$match", new Document("assignee", assigneeId).append("post_time",
                    new Document("$gte", startMillis).append("$lte", endMillis))));
            uTags.add(
                    new Document("$unwind", new Document("path", "$tags").append("preserveNullAndEmptyArrays", false)));
            uTags.add(new Document("$group", new Document("_id", "$tags").append("count", new Document("$sum", 1))));
            uTags.add(new Document("$sort", new Document("count", -1)));
            facets.put("user_tags", uTags);

        } else {
            facets.put("user_issue_status", Collections.emptyList());
            facets.put("user_priority", Collections.emptyList());
            facets.put("user_category", Collections.emptyList());
            facets.put("user_tags", Collections.emptyList());
        }

        // TREND
        facets.put("trend", Arrays.asList(
                // Step 1: Project + convert seconds to millis + format date
                new Document("$project", new Document("issue_status", 1).append("priority", 1).append("category", 1)
                        .append("tags", 1).append("assignee", 1).append("assignee_name", 1).append("post_time", 1)
                        .append("period",
                                new Document("$dateToString", new Document().append("format", "%Y-%m-%d").append("date",
                                        new Document("$toDate",
                                                new Document("$multiply", Arrays.asList("$post_time", 1000))))))),
                // Step 2: Group by period + status
                new Document("$group",
                        new Document("_id", new Document("period", "$period").append("issue_status", "$issue_status"))
                                .append("count", new Document("$sum", 1))),
                // Step 3: Sort by period
                new Document("$sort", new Document("_id.period", 1))));

        // COMBINATION counts
        Document reduceExpr = new Document("$reduce",
                new Document("input", "$tags").append("initialValue", "").append("in",
                        new Document("$cond", Arrays.asList(new Document("$eq", Arrays.asList("$$value", "")), "$$this",
                                new Document("$concat", Arrays.asList("$$value", ",", "$$this"))))));
        facets.put("combination_counts",
                Arrays.asList(
                        new Document("$project",
                                new Document("combo", new Document("$concat",
                                        Arrays.asList(new Document("$ifNull", Arrays.asList("$assignee_name", "")), "|",
                                                new Document("$ifNull", Arrays.asList("$issue_status", "")), "|",
                                                new Document("$ifNull", Arrays.asList("$category", "")), "|",
                                                new Document("$ifNull", Arrays.asList(reduceExpr, "")))))),
                        new Document("$group",
                                new Document("_id", "$combo").append("count", new Document("$sum", 1)))));

        // RAW issues (paginated)
        facets.put("raw_issues", Arrays.asList(new Document("$sort", new Document("_id", Integer.valueOf(-1))),
                new Document("$skip", skip), new Document("$limit", size)));

        // Build the base match doc with time range and dynamic filters
        Document baseDynamicMatchDoc = new Document("post_time",
                new Document("$gte", startMillis).append("$lte", endMillis));
        if (extraFieldValues != null && !extraFieldValues.isEmpty()) {
            extraFieldValues.forEach((key, value) -> {
                if (value != null && !value.isBlank()) {
                    List<String> values = Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                    if (values.size() == 1) {
                        baseDynamicMatchDoc.put(key, values.get(0));
                    } else if (values.size() > 1) {
                        baseDynamicMatchDoc.put(key, new Document("$in", values));
                    }
                }
            });
        }

        // For each facet, use a *copy* of baseMatchDoc to avoid mutability issues
        facets.put("filter_issue_count", Arrays.asList(new Document("$match", new Document(baseDynamicMatchDoc)),
                new Document("$group", new Document("_id", "$issue_status").append("count", new Document("$sum", 1)))));

        // EXTRA fields
        // if (extraFields != null) {
        // for (String field : extraFields) {
        // String gKey = "global_e_" + field;
        // String uKey = "user_e_" + field;
        // facets.put(gKey, Arrays.asList(
        // new Document("$match", new Document("post_time", new Document("$gte", startMillis).append("$lte",
        // endMillis))),
        // new Document("$group", new Document("_id", "$" + field).append("count", new Document("$sum", 1)))
        // ));
        //
        // if (assigneeId != null && !assigneeId.isBlank()) {
        // facets.put(uKey, Arrays.asList(
        // new Document("$match", new Document("assignee", assigneeId)),
        // new Document("$group", new Document("_id", "$" + field).append("count", new Document("$sum", 1)))
        // ));
        // } else {
        // facets.put(uKey, Collections.emptyList());
        // }
        // }
        // }

        pipeline.add(new Document("$facet", facets));

        MongoCollection<Document> coll = mongoTemplate.getCollection(collectionName);
        log.info("pipeline -> {}", pipeline);

        Document result = coll.aggregate(pipeline).into(new ArrayList<>()).stream().findFirst().orElse(new Document());

        Map<String, Object> response = new LinkedHashMap<>();

        // Populate response using zeroFillCounts and trend helper
        response.put("global_issue_status",
                zeroFillCounts(result.getList("global_issue_status", Document.class, Collections.emptyList()),
                        AnalyticsConstants.STATUS_ORDER));
        response.put("global_priority",
                zeroFillCounts(result.getList("global_priority", Document.class, Collections.emptyList()),
                        AnalyticsConstants.PRIORITY_ORDER));
        log.info("global_category -> {}", zeroFillCountsAsArray(
                result.getList("global_category", Document.class, Collections.emptyList()), null));

        response.put("global_category", zeroFillCountsAsArray(
                result.getList("global_category", Document.class, Collections.emptyList()), null));
        response.put("global_tags",
                zeroFillCountsAsArray(result.getList("global_tags", Document.class, Collections.emptyList()), null));

        response.put("user_issue_status",
                zeroFillCounts(result.getList("user_issue_status", Document.class, Collections.emptyList()),
                        AnalyticsConstants.STATUS_ORDER));
        response.put("user_priority",
                zeroFillCounts(result.getList("user_priority", Document.class, Collections.emptyList()),
                        AnalyticsConstants.PRIORITY_ORDER));
        response.put("user_category",
                zeroFillCountsAsArray(result.getList("user_category", Document.class, Collections.emptyList()), null));
        response.put("user_tags",
                zeroFillCountsAsArray(result.getList("user_tags", Document.class, Collections.emptyList()), null));
        response.put("filter_issue_count", zeroFillCountsAsArray(
                result.getList("filter_issue_count", Document.class, Collections.emptyList()), null));

        log.info("division_global_count -> {}",
                result.getList("division_global_count", Document.class, Collections.emptyList()));

        response.put("division_global_count", zeroFillCountsAsArray(
                result.getList("division_global_count", Document.class, Collections.emptyList()), null));

        response.put("division_user_count", zeroFillCountsAsArray(
                result.getList("division_user_count", Document.class, Collections.emptyList()), null));

        List<Map<String, Object>> combos = result.getList("combination_counts", Document.class, Collections.emptyList())
                .stream().map(d -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("combo", d.getString("_id"));
                    m.put("count", d.getInteger("count", 0));
                    return m;
                }).collect(Collectors.toList());
        response.put("combination_counts", combos);

        response.put("trend", zeroFillTrend(result.getList("trend", Document.class, Collections.emptyList()), startDate,
                endDate, trendMode, AnalyticsConstants.STATUS_ORDER));
        response.put("raw_issues", result.getList("raw_issues", Document.class, Collections.emptyList()));

        // if (extraFields != null) {
        // extraFields.forEach(field -> {
        // String gKey = "global_" + field;
        // String uKey = "user_" + field;
        //
        // List<Document> rawG = result.getList(gKey, Document.class, Collections.emptyList());
        // List<Document> rawU = result.getList(uKey, Document.class, Collections.emptyList());
        //
        // List<String> expected = valMap.getOrDefault(field,
        // AnalyticsConstants.DEFAULT_EXTRA_FIELD_KEYS.getOrDefault(field, List.of()));
        // if (expected.isEmpty()) {
        // Set<String> detected = new LinkedHashSet<>();
        // rawG.forEach(d -> detected.add(Objects.toString(d.get("_id"), "")));
        // if (detected.isEmpty()) rawU.forEach(d -> detected.add(Objects.toString(d.get("_id"), "")));
        // expected = new ArrayList<>(detected);
        // }
        //
        // response.put(gKey, zeroFillCounts(rawG, expected));
        // response.put(uKey, zeroFillCounts(rawU, expected));
        // });
        // }

        return response;
    }
}
