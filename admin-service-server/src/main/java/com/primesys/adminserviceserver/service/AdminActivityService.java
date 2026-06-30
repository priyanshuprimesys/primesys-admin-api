package com.primesys.adminserviceserver.service;

import com.primesys.adminservicemongodb.entity.AdminActivitySessionEntity;
import com.primesys.adminservicemongodb.entity.AdminDailyActivityEntity;
import com.primesys.adminserviceserver.dtos.activity.CheckinResponse;

import java.util.List;

public interface AdminActivityService {

    CheckinResponse checkin(String userId, String userName, Integer roleId);

    String heartbeat(String sessionId, String userId);

    String checkout(String sessionId, String userId);

    List<AdminActivitySessionEntity> getActiveSessions();

    List<AdminDailyActivityEntity> getDailyLog(String from, String to, String userId);
}
