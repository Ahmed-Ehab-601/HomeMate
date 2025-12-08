package com.homemate.notification.observer.utils;

import com.homemate.notification.domains.dto.TaskDtoNtofication;
import com.homemate.notification.domains.dto.TaskDtoNtofication;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplate {
    public String buildTaskAccepted(TaskDtoNtofication taskDto){
         return null;
    }
    public String buildTaskRejected(TaskDtoNtofication taskDto){
         return null;
    }
    public String buildTaskStatusChanged(TaskDtoNtofication taskDto){
         return null;
    }
    public String buildTaskRequest(TaskDtoNtofication taskDto){
         return null;
    }
    public String buildTaskReschedule(TaskDtoNtofication taskDto){
         return null;
    }

    public String buildVerificationCode(String code){
         return "Your HomeMate verification code is " + code + "\n" +
                "This code expires in " + com.homemate.notification.config.RedisConfig.OTP_TTL_MINUTES + " minutes.";
    }
    public String buildForgotPassword(String code){
         return null;
    }


}
