package com.school.manage.dto;

import lombok.Data;

@Data
public class AiChatRequest {

    /** Links to Homework.id — null for free-form questions */
    private String homeworkId;

    /** Existing conversation ID to continue — null for new conversation */
    private String conversationId;

    /** The student's message/question */
    private String message;

    /** TUTOR | SOLVE | PRACTICE */
    private String mode;

    /** Preferred language: en | hi | hinglish (defaults to en) */
    private String language = "en";
}
