package com.homemate.chat;

import com.homemate.chat.Enum.OnlineStatus;
import com.homemate.chat.Service.PresenceService;
import com.homemate.chat.dto.PresenceUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PresenceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private PresenceService presenceService;

    private static final Long USER_ID = 1L;
    private static final Long TASKER_ID = 2L;
    private static final String USER_TYPE = "user";
    private static final String TASKER_TYPE = "tasker";

    @BeforeEach
    public void setUp() {
        // Reset presence service state before each test
        presenceService = new PresenceService(messagingTemplate);
    }

    // ==================== UPDATE PRESENCE TESTS ====================

    @Test
    public void testUpdatePresence_UserGoesOnline() {
        PresenceUpdate update = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());

        presenceService.updatePresence(update);

        assertTrue(presenceService.isUserOnline(USER_ID));
        verify(messagingTemplate).convertAndSend(eq("/send/presence"), any(PresenceUpdate.class));
    }

    @Test
    public void testUpdatePresence_UserGoesOffline() {
        // First set user online
        PresenceUpdate onlineUpdate = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        presenceService.updatePresence(onlineUpdate);

        // Then set offline
        PresenceUpdate offlineUpdate = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.OFFLINE, LocalDateTime.now());
        presenceService.updatePresence(offlineUpdate);

        assertFalse(presenceService.isUserOnline(USER_ID));
        verify(messagingTemplate, times(2)).convertAndSend(eq("/send/presence"), any(PresenceUpdate.class));
    }

    @Test
    public void testUpdatePresence_TaskerGoesOnline() {
        PresenceUpdate update = new PresenceUpdate(TASKER_ID, TASKER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());

        presenceService.updatePresence(update);

        assertTrue(presenceService.isTaskerOnline(TASKER_ID));
        verify(messagingTemplate).convertAndSend(eq("/send/presence"), any(PresenceUpdate.class));
    }

    @Test
    public void testUpdatePresence_TaskerGoesOffline() {
        // First set tasker online
        PresenceUpdate onlineUpdate = new PresenceUpdate(TASKER_ID, TASKER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        presenceService.updatePresence(onlineUpdate);

        // Then set offline
        PresenceUpdate offlineUpdate = new PresenceUpdate(TASKER_ID, TASKER_TYPE, OnlineStatus.OFFLINE, LocalDateTime.now());
        presenceService.updatePresence(offlineUpdate);

        assertFalse(presenceService.isTaskerOnline(TASKER_ID));
        verify(messagingTemplate, times(2)).convertAndSend(eq("/send/presence"), any(PresenceUpdate.class));
    }

    @Test
    public void testUpdatePresence_NullTimestamp_SetsCurrentTime() {
        PresenceUpdate update = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, null);

        presenceService.updatePresence(update);

        assertNotNull(update.getTime());
        assertTrue(presenceService.isUserOnline(USER_ID));
    }

    @Test
    public void testUpdatePresence_UnknownUserType_NoUpdate() {
        PresenceUpdate update = new PresenceUpdate(USER_ID, "unknown", OnlineStatus.ONLINE, LocalDateTime.now());

        presenceService.updatePresence(update);

        // Should not crash, just log warning
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    public void testUpdatePresence_MultipleUsersOnline() {
        PresenceUpdate user1 = new PresenceUpdate(1L, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        PresenceUpdate user2 = new PresenceUpdate(2L, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());

        presenceService.updatePresence(user1);
        presenceService.updatePresence(user2);

        assertTrue(presenceService.isUserOnline(1L));
        assertTrue(presenceService.isUserOnline(2L));
        verify(messagingTemplate, times(2)).convertAndSend(eq("/send/presence"), any(PresenceUpdate.class));
    }

    // ==================== HEARTBEAT TESTS ====================

    @Test
    public void testUpdatePresenceFromHeartbeat_DoesNotBroadcastIfNoChange() {
        // First explicit update
        PresenceUpdate onlineUpdate = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        presenceService.updatePresence(onlineUpdate);

        reset(messagingTemplate); // Reset mock to verify next call

        // Heartbeat with same status
        PresenceUpdate heartbeat = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        presenceService.updatePresenceFromHeartbeat(heartbeat);

        // Should not broadcast again for heartbeat with no change
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
        assertTrue(presenceService.isUserOnline(USER_ID));
    }

    @Test
    public void testUpdatePresenceFromHeartbeat_BroadcastsOnStatusChange() {
        // Set user online
        PresenceUpdate onlineUpdate = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        presenceService.updatePresence(onlineUpdate);

        reset(messagingTemplate);

        // Heartbeat with different status (offline)
        PresenceUpdate offlineHeartbeat = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.OFFLINE, LocalDateTime.now());
        presenceService.updatePresenceFromHeartbeat(offlineHeartbeat);

        // Should broadcast because status changed
        verify(messagingTemplate).convertAndSend(eq("/send/presence"), any(PresenceUpdate.class));
        assertFalse(presenceService.isUserOnline(USER_ID));
    }

    // ==================== FORCE OFFLINE TESTS ====================

    @Test
    public void testForceUserOffline() {
        // Set user online first
        PresenceUpdate onlineUpdate = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        presenceService.updatePresence(onlineUpdate);

        assertTrue(presenceService.isUserOnline(USER_ID));

        // Force offline
        presenceService.forceUserOffline(USER_ID);

        assertFalse(presenceService.isUserOnline(USER_ID));
        verify(messagingTemplate, times(2)).convertAndSend(eq("/send/presence"), any(PresenceUpdate.class));
    }

    @Test
    public void testForceTaskerOffline() {
        // Set tasker online first
        PresenceUpdate onlineUpdate = new PresenceUpdate(TASKER_ID, TASKER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        presenceService.updatePresence(onlineUpdate);

        assertTrue(presenceService.isTaskerOnline(TASKER_ID));

        // Force offline
        presenceService.forceTaskerOffline(TASKER_ID);

        assertFalse(presenceService.isTaskerOnline(TASKER_ID));
        verify(messagingTemplate, times(2)).convertAndSend(eq("/send/presence"), any(PresenceUpdate.class));
    }

    @Test
    public void testForceUserOffline_WhenAlreadyOffline() {
        presenceService.forceUserOffline(USER_ID);

        assertFalse(presenceService.isUserOnline(USER_ID));
        verify(messagingTemplate).convertAndSend(eq("/send/presence"), any(PresenceUpdate.class));
    }

    @Test
    public void testForceTaskerOffline_WhenAlreadyOffline() {
        presenceService.forceTaskerOffline(TASKER_ID);

        assertFalse(presenceService.isTaskerOnline(TASKER_ID));
        verify(messagingTemplate).convertAndSend(eq("/send/presence"), any(PresenceUpdate.class));
    }

    // ==================== IS ONLINE TESTS ====================

    @Test
    public void testIsUserOnline_WhenOnline() {
        PresenceUpdate update = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        presenceService.updatePresence(update);

        Boolean result = presenceService.isUserOnline(USER_ID);

        assertTrue(result);
    }

    @Test
    public void testIsUserOnline_WhenOffline() {
        PresenceUpdate onlineUpdate = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        presenceService.updatePresence(onlineUpdate);

        PresenceUpdate offlineUpdate = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.OFFLINE, LocalDateTime.now());
        presenceService.updatePresence(offlineUpdate);

        Boolean result = presenceService.isUserOnline(USER_ID);

        assertFalse(result);
    }

    @Test
    public void testIsUserOnline_WhenNeverSeenBefore() {
        Boolean result = presenceService.isUserOnline(999L);

        assertFalse(result); // Default to false
    }

    @Test
    public void testIsTaskerOnline_WhenOnline() {
        PresenceUpdate update = new PresenceUpdate(TASKER_ID, TASKER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        presenceService.updatePresence(update);

        Boolean result = presenceService.isTaskerOnline(TASKER_ID);

        assertTrue(result);
    }

    @Test
    public void testIsTaskerOnline_WhenOffline() {
        PresenceUpdate onlineUpdate = new PresenceUpdate(TASKER_ID, TASKER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        presenceService.updatePresence(onlineUpdate);

        PresenceUpdate offlineUpdate = new PresenceUpdate(TASKER_ID, TASKER_TYPE, OnlineStatus.OFFLINE, LocalDateTime.now());
        presenceService.updatePresence(offlineUpdate);

        Boolean result = presenceService.isTaskerOnline(TASKER_ID);

        assertFalse(result);
    }

    @Test
    public void testIsTaskerOnline_WhenNeverSeenBefore() {
        Boolean result = presenceService.isTaskerOnline(999L);

        assertFalse(result); // Default to false
    }

    // ==================== GET ALL STATUS TESTS ====================

    @Test
    public void testGetAllUserOnlineStatus() {
        PresenceUpdate user1 = new PresenceUpdate(1L, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        PresenceUpdate user2 = new PresenceUpdate(2L, USER_TYPE, OnlineStatus.OFFLINE, LocalDateTime.now());

        presenceService.updatePresence(user1);
        presenceService.updatePresence(user2);

        ConcurrentHashMap<Long, Boolean> result = presenceService.getAllUserOnlineStatus();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(1L));
        assertFalse(result.get(2L));
    }

    @Test
    public void testGetAllTaskerOnlineStatus() {
        PresenceUpdate tasker1 = new PresenceUpdate(1L, TASKER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        PresenceUpdate tasker2 = new PresenceUpdate(2L, TASKER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());

        presenceService.updatePresence(tasker1);
        presenceService.updatePresence(tasker2);

        ConcurrentHashMap<Long, Boolean> result = presenceService.getAllTaskerOnlineStatus();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(1L));
        assertTrue(result.get(2L));
    }

    @Test
    public void testGetAllUserOnlineStatus_EmptyWhenNoUsers() {
        ConcurrentHashMap<Long, Boolean> result = presenceService.getAllUserOnlineStatus();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllTaskerOnlineStatus_EmptyWhenNoTaskers() {
        ConcurrentHashMap<Long, Boolean> result = presenceService.getAllTaskerOnlineStatus();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== GET PRESENCE DEBUG INFO TESTS ====================

    @Test
    public void testGetPresenceDebugInfo() {
        PresenceUpdate user = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        PresenceUpdate tasker = new PresenceUpdate(TASKER_ID, TASKER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());

        presenceService.updatePresence(user);
        presenceService.updatePresence(tasker);

        Map<String, Object> debug = presenceService.getPresenceDebugInfo();

        assertNotNull(debug);
        assertTrue(debug.containsKey("userOnlineStatus"));
        assertTrue(debug.containsKey("taskerOnlineStatus"));
        assertTrue(debug.containsKey("userLastSeen"));
        assertTrue(debug.containsKey("taskerLastSeen"));
    }

    @Test
    public void testGetPresenceDebugInfo_EmptyState() {
        Map<String, Object> debug = presenceService.getPresenceDebugInfo();

        assertNotNull(debug);
        assertEquals(4, debug.size());
    }

    // ==================== BROADCAST VERIFICATION TESTS ====================

    @Test
    public void testUpdatePresence_VerifyBroadcastPayload() {
        PresenceUpdate update = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());

        presenceService.updatePresence(update);

        ArgumentCaptor<PresenceUpdate> captor = ArgumentCaptor.forClass(PresenceUpdate.class);
        verify(messagingTemplate).convertAndSend(eq("/send/presence"), captor.capture());

        PresenceUpdate captured = captor.getValue();
        assertEquals(USER_ID, captured.getId());
        assertEquals(USER_TYPE, captured.getUserType());
        assertEquals(OnlineStatus.ONLINE, captured.getOnlineStatus());
        assertNotNull(captured.getTime());
    }

    @Test
    public void testForceUserOffline_VerifyBroadcastPayload() {
        presenceService.forceUserOffline(USER_ID);

        ArgumentCaptor<PresenceUpdate> captor = ArgumentCaptor.forClass(PresenceUpdate.class);
        verify(messagingTemplate).convertAndSend(eq("/send/presence"), captor.capture());

        PresenceUpdate captured = captor.getValue();
        assertEquals(USER_ID, captured.getId());
        assertEquals(USER_TYPE, captured.getUserType());
        assertEquals(OnlineStatus.OFFLINE, captured.getOnlineStatus());
    }

    @Test
    public void testForceTaskerOffline_VerifyBroadcastPayload() {
        presenceService.forceTaskerOffline(TASKER_ID);

        ArgumentCaptor<PresenceUpdate> captor = ArgumentCaptor.forClass(PresenceUpdate.class);
        verify(messagingTemplate).convertAndSend(eq("/send/presence"), captor.capture());

        PresenceUpdate captured = captor.getValue();
        assertEquals(TASKER_ID, captured.getId());
        assertEquals(TASKER_TYPE, captured.getUserType());
        assertEquals(OnlineStatus.OFFLINE, captured.getOnlineStatus());
    }

    // ==================== EDGE CASES ====================

    @Test
    public void testUpdatePresence_SameStatusMultipleTimes_OnlyBroadcastsOnChange() {
        PresenceUpdate update1 = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        presenceService.updatePresence(update1);

        // Wait a bit to avoid throttling
        try {
            Thread.sleep(6000); // Wait more than MIN_BROADCAST_INTERVAL_SECONDS
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        reset(messagingTemplate);

        PresenceUpdate update2 = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now().plusSeconds(6));
        presenceService.updatePresence(update2);

        // Should broadcast because it's an explicit update after throttle period
        verify(messagingTemplate).convertAndSend(eq("/send/presence"), any(PresenceUpdate.class));
    }

    @Test
    public void testUpdatePresence_RapidUpdates_ThrottlesBroadcasts() {
        PresenceUpdate update1 = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        presenceService.updatePresence(update1);

        reset(messagingTemplate);

        // Immediate second update (within throttle window)
        PresenceUpdate update2 = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        presenceService.updatePresence(update2);

        // Should be throttled (no broadcast)
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    public void testUpdatePresence_StatusToggle_BroadcastsBoth() {
        PresenceUpdate onlineUpdate = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, LocalDateTime.now());
        presenceService.updatePresence(onlineUpdate);

        PresenceUpdate offlineUpdate = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.OFFLINE, LocalDateTime.now());
        presenceService.updatePresence(offlineUpdate);

        // Should broadcast both times (status changed)
        verify(messagingTemplate, times(2)).convertAndSend(eq("/send/presence"), any(PresenceUpdate.class));
    }
    @Test
    public void testCheckUserStatus_UserTimesOut() {
        // Set user online with old timestamp (more than 60 seconds ago)
        LocalDateTime oldTime = LocalDateTime.now().minusSeconds(70);
        PresenceUpdate onlineUpdate = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, oldTime);
        presenceService.updatePresence(onlineUpdate);

        assertTrue(presenceService.isUserOnline(USER_ID));
        reset(messagingTemplate);

        // Run the scheduled check
        presenceService.checkUserStatus();

        // User should now be offline
        assertFalse(presenceService.isUserOnline(USER_ID));

        // Should broadcast offline status
        ArgumentCaptor<PresenceUpdate> captor = ArgumentCaptor.forClass(PresenceUpdate.class);
        verify(messagingTemplate).convertAndSend(eq("/send/presence"), captor.capture());

        PresenceUpdate captured = captor.getValue();
        assertEquals(USER_ID, captured.getId());
        assertEquals(USER_TYPE, captured.getUserType());
        assertEquals(OnlineStatus.OFFLINE, captured.getOnlineStatus());
    }

    @Test
    public void testCheckUserStatus_TaskerTimesOut() {
        // Set tasker online with old timestamp (more than 60 seconds ago)
        LocalDateTime oldTime = LocalDateTime.now().minusSeconds(75);
        PresenceUpdate onlineUpdate = new PresenceUpdate(TASKER_ID, TASKER_TYPE, OnlineStatus.ONLINE, oldTime);
        presenceService.updatePresence(onlineUpdate);

        assertTrue(presenceService.isTaskerOnline(TASKER_ID));
        reset(messagingTemplate);

        // Run the scheduled check
        presenceService.checkUserStatus();

        // Tasker should now be offline
        assertFalse(presenceService.isTaskerOnline(TASKER_ID));

        // Should broadcast offline status
        ArgumentCaptor<PresenceUpdate> captor = ArgumentCaptor.forClass(PresenceUpdate.class);
        verify(messagingTemplate).convertAndSend(eq("/send/presence"), captor.capture());

        PresenceUpdate captured = captor.getValue();
        assertEquals(TASKER_ID, captured.getId());
        assertEquals(TASKER_TYPE, captured.getUserType());
        assertEquals(OnlineStatus.OFFLINE, captured.getOnlineStatus());
    }

    @Test
    public void testCheckUserStatus_UserStillActive_NoTimeout() {
        // Set user online with recent timestamp (less than 60 seconds ago)
        LocalDateTime recentTime = LocalDateTime.now().minusSeconds(30);
        PresenceUpdate onlineUpdate = new PresenceUpdate(USER_ID, USER_TYPE, OnlineStatus.ONLINE, recentTime);
        presenceService.updatePresence(onlineUpdate);

        assertTrue(presenceService.isUserOnline(USER_ID));
        reset(messagingTemplate);

        // Run the scheduled check
        presenceService.checkUserStatus();

        // User should still be online
        assertTrue(presenceService.isUserOnline(USER_ID));

        // Should NOT broadcast anything
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    @Test
    public void testCheckUserStatus_TaskerStillActive_NoTimeout() {
        // Set tasker online with recent timestamp (less than 60 seconds ago)
        LocalDateTime recentTime = LocalDateTime.now().minusSeconds(45);
        PresenceUpdate onlineUpdate = new PresenceUpdate(TASKER_ID, TASKER_TYPE, OnlineStatus.ONLINE, recentTime);
        presenceService.updatePresence(onlineUpdate);

        assertTrue(presenceService.isTaskerOnline(TASKER_ID));
        reset(messagingTemplate);

        // Run the scheduled check
        presenceService.checkUserStatus();

        // Tasker should still be online
        assertTrue(presenceService.isTaskerOnline(TASKER_ID));

        // Should NOT broadcast anything
        verify(messagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }


}