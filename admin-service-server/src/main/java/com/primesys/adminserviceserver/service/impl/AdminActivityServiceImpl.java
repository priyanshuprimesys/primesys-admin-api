package com.primesys.adminserviceserver.service.impl;

import com.primesys.adminservicemongodb.entity.AdminActivitySessionEntity;
import com.primesys.adminservicemongodb.entity.AdminDailyActivityEntity;
import com.primesys.adminservicemongodb.repository.AdminActivitySessionRepository;
import com.primesys.adminservicemongodb.repository.AdminDailyActivityRepository;
import com.primesys.adminserviceserver.dtos.activity.CheckinResponse;
import com.primesys.adminserviceserver.service.AdminActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminActivityServiceImpl implements AdminActivityService {

    private final AdminActivitySessionRepository sessionRepository;
    private final AdminDailyActivityRepository dailyActivityRepository;
    private final MongoTemplate mongoTemplate;

    private static final long STALE_THRESHOLD_MS = 90_000L;

    @Override
    public CheckinResponse checkin(String userId, String userName, Integer roleId) {
        // close any prior active session for this user
        sessionRepository.findByUserIdAndActiveTrue(userId).forEach(existing -> {

            Query q = new Query(Criteria.where("session_id").is(existing.getSessionId()).and("active").is(true));

            mongoTemplate.updateFirst(q,
                    new Update().set("active", false).set("checkout_at", System.currentTimeMillis()),
                    AdminActivitySessionEntity.class);

            log.info("Closed prior session {} for user {}", existing.getSessionId(), userId);
        });
        long now = System.currentTimeMillis();

        // upsert daily activity record
        String today = LocalDate.now(ZoneId.of("UTC")).toString();
        Query dailyQuery = new Query(Criteria.where("user_id").is(userId).and("date").is(today));
        Update dailyUpdate = new Update().setOnInsert("first_checkin_at", now).set("user_name", userName)
                .set("role_id", roleId).set("last_heartbeat_at", now);
        mongoTemplate.upsert(dailyQuery, dailyUpdate, AdminDailyActivityEntity.class);

        // create new session
        AdminActivitySessionEntity session = AdminActivitySessionEntity.builder()
                .sessionId(UUID.randomUUID().toString()).userId(userId).userName(userName).roleId(roleId)
                .checkedInAt(now).lastHeartbeat(now).active(true).build();

        AdminActivitySessionEntity saved = sessionRepository.save(session);
        log.info("Checkin: user={} session={}", userId, saved.getSessionId());
        return new CheckinResponse(saved.getSessionId(), saved.getCheckedInAt());
    }

    @Override
    public String heartbeat(String sessionId, String userId) {
        sessionRepository.findBySessionIdAndActiveTrue(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("No active session: " + sessionId));

        long now = System.currentTimeMillis();

        Query q = new Query(Criteria.where("session_id").is(sessionId).and("active").is(true));
        mongoTemplate.updateFirst(q, new Update().set("last_heartbeat", now), AdminActivitySessionEntity.class);

        // keep daily record fresh
        String today = LocalDate.now(ZoneId.of("UTC")).toString();
        Query dailyQuery = new Query(Criteria.where("user_id").is(userId).and("date").is(today));
        mongoTemplate.updateFirst(dailyQuery, new Update().set("last_heartbeat_at", now),
                AdminDailyActivityEntity.class);

        log.debug("Heartbeat: session={} user={}", sessionId, userId);
        return "ok";
    }

    @Override
    public String checkout(String sessionId, String userId) {
        sessionRepository.findBySessionIdAndActiveTrue(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("No active session: " + sessionId));

        long now = System.currentTimeMillis();

        Query q = new Query(Criteria.where("session_id").is(sessionId).and("active").is(true));
        mongoTemplate.updateFirst(q, new Update().set("active", false).set("checkout_at", now),
                AdminActivitySessionEntity.class);

        // record last activity time on checkout
        String today = LocalDate.now(ZoneId.of("UTC")).toString();
        Query dailyQuery = new Query(Criteria.where("user_id").is(userId).and("date").is(today));
        mongoTemplate.updateFirst(dailyQuery, new Update().set("last_heartbeat_at", now),
                AdminDailyActivityEntity.class);

        log.info("Checkout: session={} user={}", sessionId, userId);
        return "Checked out successfully";
    }

    @Override
    public List<AdminActivitySessionEntity> getActiveSessions() {
        long cutoff = System.currentTimeMillis() - STALE_THRESHOLD_MS;
        return sessionRepository.findByActiveTrue().stream()
                .filter(s -> s.getLastHeartbeat() != null && s.getLastHeartbeat() >= cutoff)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminDailyActivityEntity> getDailyLog(String from, String to, String userId) {
        Criteria criteria = Criteria.where("date").gte(from).lte(to);
        if (userId != null && !userId.isBlank()) {
            criteria = criteria.and("user_id").is(userId);
        }
        Query query = new Query(criteria).with(Sort.by(Sort.Direction.DESC, "date"));
        return mongoTemplate.find(query, AdminDailyActivityEntity.class);
    }
}
