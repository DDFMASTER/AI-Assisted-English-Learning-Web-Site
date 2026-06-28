package Entities;

import java.time.LocalDateTime;

public class AdminActionLog {
    private Long userId;
    private String targetType;
    private Long targetId;
    private String actionType;
    private String parameter;
    private LocalDateTime timestamp;

    public AdminActionLog() {}

    public AdminActionLog(Long userId, String targetType, Long targetId,
                          String actionType, String parameter, LocalDateTime timestamp) {
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.actionType = actionType;
        this.parameter = parameter;
        this.timestamp = timestamp;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getParameter() { return parameter; }
    public void setParameter(String parameter) { this.parameter = parameter; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
