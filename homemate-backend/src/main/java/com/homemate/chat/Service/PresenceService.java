package com.homemate.chat.Service;

import com.homemate.chat.Enum.OnlineStatus;
import com.homemate.chat.dto.PresenceUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PresenceService.class);

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

    /**
     * Update presence - called from /app/presence/update (explicit login/logout)
     * This should ALWAYS broadcast to inform all clients
     */
    public void updatePresence(PresenceUpdate update) {
        updatePresenceInternal(update, true); // forcebroadcast = true
    }

    /**
     * Update presence from heartbeat - called from /app/presence/heartbeat
     * This should only update timestamp, not broadcast
     */
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

        logger.info("📥 Updating presence for {} {}: {} (explicit: {}, timestamp: {})",
                userType, id, isOnline ? "ONLINE" : "OFFLINE", isExplicitUpdate, now);

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
                // Status changed - always broadcast
                shouldBroadcast = true;
                reason = "status changed";
            } else if (isExplicitUpdate && !shouldThrottle) {
                // Explicit update (login/logout) - broadcast unless throttled
                shouldBroadcast = true;
                reason = "explicit update";
            } else if (shouldThrottle) {
                reason = "throttled (too soon)";
            } else {
                reason = "heartbeat (no change)";
            }

            logger.info("👤 User {} - Current: {}, New: {}, Explicit: {}, Will broadcast: {} ({})",
                    id, currentStatus, isOnline, isExplicitUpdate, shouldBroadcast, reason);

            if (shouldBroadcast) {
                userOnlineStatus.put(id, isOnline);
                userLastBroadcast.put(id, now);

                logger.info("📡 Broadcasting User {} status: {} -> {}",
                        id, currentStatus, isOnline ? "ONLINE" : "OFFLINE");
                logger.info("📤 Sending to /send/presence with payload: {}", update);

                messagingTemplate.convertAndSend("/send/presence", update);

                logger.info("✅ User {} broadcast SENT. Current online users: {}",
                        id, userOnlineStatus);
            } else {
                // Just update timestamp, don't broadcast
                userOnlineStatus.put(id, isOnline);
                logger.debug("⏰ User {} presence updated without broadcast ({})", id, reason);
            }

        } else if (Objects.equals(userType, "tasker")) {
            // Update last seen timestamp
            taskerLastSeen.put(id, now);

            Boolean currentStatus = taskerOnlineStatus.get(id);

            // Get last broadcast time
            LocalDateTime lastBroadcast = taskerLastBroadcast.get(id);
            boolean shouldThrottle = false;

            if (lastBroadcast != null) {
                long secondsSinceLastBroadcast = Duration.between(lastBroadcast, now).getSeconds();
                shouldThrottle = secondsSinceLastBroadcast < MIN_BROADCAST_INTERVAL_SECONDS;
            }

            // Decide whether to broadcast
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

            logger.info("👷 Tasker {} - Current: {}, New: {}, Explicit: {}, Will broadcast: {} ({})",
                    id, currentStatus, isOnline, isExplicitUpdate, shouldBroadcast, reason);

            if (shouldBroadcast) {
                taskerOnlineStatus.put(id, isOnline);
                taskerLastBroadcast.put(id, now);

                logger.info("📡 Broadcasting Tasker {} status: {} -> {}",
                        id, currentStatus, isOnline ? "ONLINE" : "OFFLINE");
                logger.info("📤 Sending to /send/presence with payload: {}", update);

                messagingTemplate.convertAndSend("/send/presence", update);

                logger.info("✅ Tasker {} broadcast SENT. Current online taskers: {}",
                        id, taskerOnlineStatus);
            } else {
                // Just update timestamp, don't broadcast
                taskerOnlineStatus.put(id, isOnline);
                logger.debug("⏰ Tasker {} presence updated without broadcast ({})", id, reason);
            }

        } else {
            logger.warn("⚠️ Unknown user type: {}", userType);
        }
    }

    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    public void checkUserStatus() {
        LocalDateTime now = LocalDateTime.now();

        logger.debug("🔍 Checking presence status for all users...");

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

                    logger.info("⏰ User {} timeout - setting to OFFLINE ({}s since last seen)",
                            userId, secondsSinceLastSeen);
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

                    logger.info("⏰ Tasker {} timeout - setting to OFFLINE ({}s since last seen)",
                            taskerId, secondsSinceLastSeen);
                    messagingTemplate.convertAndSend("/send/presence", offlineUpdate);
                    taskerLastBroadcast.put(taskerId, now);
                }
            }
        });
    }

    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupOldEntries() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        int usersBefore = userLastSeen.size();
        int taskersBefore = taskerLastSeen.size();

        userLastSeen.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        taskerLastSeen.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        userLastBroadcast.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        taskerLastBroadcast.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));

        int usersRemoved = usersBefore - userLastSeen.size();
        int taskersRemoved = taskersBefore - taskerLastSeen.size();

        if (usersRemoved > 0 || taskersRemoved > 0) {
            logger.info("🧹 Cleanup: Removed {} users, {} taskers (older than 24h)",
                    usersRemoved, taskersRemoved);
        }
    }

    public void forceUserOffline(Long userId) {
        userOnlineStatus.put(userId, false);

        PresenceUpdate offlineUpdate = new PresenceUpdate(
                userId,
                "user",
                OnlineStatus.OFFLINE,
                LocalDateTime.now()
        );

        logger.info("🔴 Force User {} OFFLINE", userId);
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

        logger.info("🔴 Force Tasker {} OFFLINE", taskerId);
        messagingTemplate.convertAndSend("/send/presence", offlineUpdate);
    }

    public Boolean isUserOnline(Long userId) {
        Boolean status = userOnlineStatus.getOrDefault(userId, false);
        logger.debug("✓ isUserOnline({}) = {}", userId, status);
        return status;
    }

    public Boolean isTaskerOnline(Long taskerId) {
        Boolean status = taskerOnlineStatus.getOrDefault(taskerId, false);
        logger.debug("✓ isTaskerOnline({}) = {}", taskerId, status);
        return status;
    }

    public Map<String, Object> getPresenceDebugInfo() {
        Map<String, Object> debug = new java.util.HashMap<>();
        debug.put("userOnlineStatus", new java.util.HashMap<>(userOnlineStatus));
        debug.put("taskerOnlineStatus", new java.util.HashMap<>(taskerOnlineStatus));
        debug.put("userLastSeen", new java.util.HashMap<>(userLastSeen));
        debug.put("taskerLastSeen", new java.util.HashMap<>(taskerLastSeen));
        logger.info("📊 Debug info requested: {}", debug);
        return debug;
    }
    public ConcurrentHashMap<Long, Boolean> getAllUserOnlineStatus() {
        return new ConcurrentHashMap<>(userOnlineStatus);
    }

    public ConcurrentHashMap<Long, Boolean> getAllTaskerOnlineStatus() {
        return new ConcurrentHashMap<>(taskerOnlineStatus);
    }
}