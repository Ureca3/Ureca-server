package com.ureca.unity.domain.call.service;

public interface RecordingService {
    String acquire(String channelName, String uid);
    String start(String resourceId, String channelName, String uid, String token);
    void stop(String resourceId, String sid, String channelName, String uid, Long userId);
}
