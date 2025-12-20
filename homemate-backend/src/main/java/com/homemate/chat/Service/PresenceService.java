package com.homemate.chat.Service;

import com.homemate.chat.Enum.OnlineStatus;
import com.homemate.chat.dto.PresenceUpdate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private final SimpMessagingTemplate messagingTemplate;

    private final Map<Long, LocalDateTime> userLastSeen = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> taskerLastSeen = new ConcurrentHashMap<>();

    private final Map<Long, Boolean> userOnlineStatus = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> taskerOnlineStatus = new ConcurrentHashMap<>();

    // Track last broadcast time to avoid spamming
    private final Map<Long, LocalDateTime> userLastBroadcast = new ConcurrentHashMap<>();
    private final Map<Long, LocalDateTime> taskerLastBroadcast = new ConcurrentHashMap<>();

    private static final int PRESENCE_TIMEOUT_SECONDS = 60;
    private static final int MIN_BROADCAST_INTERVAL_SECONDS = 5; // Don't broadcast more than once per 5 seconds

    public PresenceService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void updatePresence(PresenceUpdate update) {
        updatePresenceInternal(update, true); // forcebroadcast = true
    }
    public void updatePresenceFromHeartbeat(PresenceUpdate update) {
        updatePresenceInternal(update, false); // forcebroadcast = false
    }

    private void updatePresenceInternal(PresenceUpdate update, boolean isExplicitUpdate) {
        if (update.getTime() == null) {
            update.setTime(LocalDateTime.now());
        }

        Long id = update.getId();
        String userType = update.getUserType();
        Boolean isOnline = update.getOnlineStatus().equals(OnlineStatus.ONLINE);
        LocalDateTime now = update.getTime();


        if (Objects.equals(userType, "user")) {
            // Update last seen timestamp
            userLastSeen.put(id, now);

            // Get current status
            Boolean currentStatus = userOnlineStatus.get(id);

            // Get last broadcast time
            LocalDateTime lastBroadcast = userLastBroadcast.get(id);
            boolean shouldThrottle = false;

            if (lastBroadcast != null) {
                long secondsSinceLastBroadcast = Duration.between(lastBroadcast, now).getSeconds();
                shouldThrottle = secondsSinceLastBroadcast < MIN_BROADCAST_INTERVAL_SECONDS;
            }

            // Decide whether to broadcast
            boolean shouldBroadcast = false;
            String reason = "";

            if (currentStatus == null) {
                // New user - always broadcast
                shouldBroadcast = true;
                reason = "new user";
            } else if (!currentStatus.equals(isOnline)) {
                shouldBroadcast = true;
                reason = "status changed";
            } else if (isExplicitUpdate && !shouldThrottle) {
                shouldBroadcast = true;
                reason = "explicit update";
            } else if (shouldThrottle) {
                reason = "throttled (too soon)";
            } else {
                reason = "heartbeat (no change)";
            }


            if (shouldBroadcast) {
                userOnlineStatus.put(id, isOnline);
                userLastBroadcast.put(id, now);

                messagingTemplate.convertAndSend("/send/presence", update);

            } else {
                userOnlineStatus.put(id, isOnline);
            }

        } else if (Objects.equals(userType, "tasker")) {
            taskerLastSeen.put(id, now);

            Boolean currentStatus = taskerOnlineStatus.get(id);
            LocalDateTime lastBroadcast = taskerLastBroadcast.get(id);
            boolean shouldThrottle = false;

            if (lastBroadcast != null) {
                long secondsSinceLastBroadcast = Duration.between(lastBroadcast, now).getSeconds();
                shouldThrottle = secondsSinceLastBroadcast < MIN_BROADCAST_INTERVAL_SECONDS;
            }

            boolean shouldBroadcast = false;
            String reason = "";

            if (currentStatus == null) {
                shouldBroadcast = true;
                reason = "new tasker";
            } else if (!currentStatus.equals(isOnline)) {
                shouldBroadcast = true;
                reason = "status changed";
            } else if (isExplicitUpdate && !shouldThrottle) {
                shouldBroadcast = true;
                reason = "explicit update";
            } else if (shouldThrottle) {
                reason = "throttled (too soon)";
            } else {
                reason = "heartbeat (no change)";
            }

            if (shouldBroadcast) {
                taskerOnlineStatus.put(id, isOnline);
                taskerLastBroadcast.put(id, now);

                messagingTemplate.convertAndSend("/send/presence", update);

            } else {
                // Just update timestamp, don't broadcast
                taskerOnlineStatus.put(id, isOnline);
            }

        } else {
            }
    }

    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    public void checkUserStatus() {
        LocalDateTime now = LocalDateTime.now();


        // Check users
        userLastSeen.forEach((userId, lastSeen) -> {
            long secondsSinceLastSeen = Duration.between(lastSeen, now).getSeconds();

            if (secondsSinceLastSeen > PRESENCE_TIMEOUT_SECONDS) {
                Boolean isOnline = userOnlineStatus.get(userId);

                if (isOnline != null && isOnline) {
                    userOnlineStatus.put(userId, false);

                    PresenceUpdate offlineUpdate = new PresenceUpdate(
                            userId,
                            "user",
                            OnlineStatus.OFFLINE,
                            now
                    );
            messagingTemplate.convertAndSend("/send/presence", offlineUpdate);
                    userLastBroadcast.put(userId, now);
                }
            }
        });

        // Check taskers
        taskerLastSeen.forEach((taskerId, lastSeen) -> {
            long secondsSinceLastSeen = Duration.between(lastSeen, now).getSeconds();

            if (secondsSinceLastSeen > PRESENCE_TIMEOUT_SECONDS) {
                Boolean isOnline = taskerOnlineStatus.get(taskerId);

                if (isOnline != null && isOnline) {
                    taskerOnlineStatus.put(taskerId, false);

                    PresenceUpdate offlineUpdate = new PresenceUpdate(
                            taskerId,
                            "tasker",
                            OnlineStatus.OFFLINE,
                            now
                    );

                    messagingTemplate.convertAndSend("/send/presence", offlineUpdate);
                    taskerLastBroadcast.put(taskerId, now);
                }
            }
        });
    }



    public void forceUserOffline(Long userId) {
        userOnlineStatus.put(userId, false);

        PresenceUpdate offlineUpdate = new PresenceUpdate(
                userId,
                "user",
                OnlineStatus.OFFLINE,
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/send/presence", offlineUpdate);
    }

    public void forceTaskerOffline(Long taskerId) {
        taskerOnlineStatus.put(taskerId, false);

        PresenceUpdate offlineUpdate = new PresenceUpdate(
                taskerId,
                "tasker",
                OnlineStatus.OFFLINE,
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/send/presence", offlineUpdate);
    }

    public Boolean isUserOnline(Long userId) {
        Boolean status = userOnlineStatus.getOrDefault(userId, false);
        return status;
    }

    public Boolean isTaskerOnline(Long taskerId) {
        Boolean status = taskerOnlineStatus.getOrDefault(taskerId, false);
        return status;
    }

    public Map<String, Object> getPresenceDebugInfo() {
        Map<String, Object> debug = new java.util.HashMap<>();
        debug.put("userOnlineStatus", new java.util.HashMap<>(userOnlineStatus));
        debug.put("taskerOnlineStatus", new java.util.HashMap<>(taskerOnlineStatus));
        debug.put("userLastSeen", new java.util.HashMap<>(userLastSeen));
        debug.put("taskerLastSeen", new java.util.HashMap<>(taskerLastSeen));
        return debug;
    }
    public ConcurrentHashMap<Long, Boolean> getAllUserOnlineStatus() {
        return new ConcurrentHashMap<>(userOnlineStatus);
    }

    public ConcurrentHashMap<Long, Boolean> getAllTaskerOnlineStatus() {
        return new ConcurrentHashMap<>(taskerOnlineStatus);
    }
}