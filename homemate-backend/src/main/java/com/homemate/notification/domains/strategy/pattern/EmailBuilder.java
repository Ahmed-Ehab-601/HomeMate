package com.homemate.notification.domains.strategy.pattern;

import com.homemate.taskmanagement.dto.TaskDto;

public interface EmailBuilder {
    String buildBody(TaskDto taskDto);
    String buildSubject(TaskDto taskDto);
}
