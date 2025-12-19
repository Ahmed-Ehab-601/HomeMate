package com.homemate.notification.domains.exception;

import lombok.Getter;

@Getter
public class EmailTemplateException extends RuntimeException {
    private final String templateType;
    private final String missingField;

    public EmailTemplateException(String message, String templateType, String missingField) {
        super(message);
        this.templateType = templateType;
        this.missingField = missingField;
    }

    public EmailTemplateException(String message, String templateType) {
        super(message);
        this.templateType = templateType;
        this.missingField = null;
    }

}
