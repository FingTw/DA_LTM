// File: TypingStatusDto.java
package com.allies.app.dto;

public class TypingStatusDto {
    
    private String senderUsername;
    private String receiverUsername; // Dành cho 1-1
    private Integer groupId; // Dành cho nhóm
    private boolean isTyping; // true/false

    // Constructor, Getters và Setters (cần được bổ sung)
    // ...
    
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    
    public String getReceiverUsername() { return receiverUsername; }
    public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }
    
    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
    
    public boolean isTyping() { return isTyping; }
    public void setTyping(boolean isTyping) { this.isTyping = isTyping; }
}