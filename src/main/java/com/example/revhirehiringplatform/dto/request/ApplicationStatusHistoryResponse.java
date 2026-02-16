package com.example.revhirehiringplatform.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApplicationStatusHistoryResponse {
    private Long id;
    private String oldStatus;
    private String newStatus;
    private String changedByUserName;
    private String comment;
    private LocalDateTime changedAt;
}