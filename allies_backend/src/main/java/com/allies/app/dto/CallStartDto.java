// File: CallStartDto.java
package com.allies.app.dto;

import java.util.List;

public class CallStartDto {
    
    // ID của người khởi tạo cuộc gọi (có thể lấy từ JWT, nhưng vẫn nên có)
    private Integer callerId; 
    
    // Loại cuộc gọi: AUDIO, VIDEO, GROUP_AUDIO, GROUP_VIDEO
    private String callType; 
    
    // Danh sách các ID người tham gia (bắt buộc cho gọi 1-1 và gọi nhóm)
    private List<Integer> participantIds; 
    
    // ID nhóm (Nếu cuộc gọi được khởi tạo trong ngữ cảnh nhóm)
    private Integer groupId; 

    // Constructor, Getters và Setters (cần được bổ sung)
    // ...
    
    public Integer getCallerId() { return callerId; }
    public void setCallerId(Integer callerId) { this.callerId = callerId; }
    
    public String getCallType() { return callType; }
    public void setCallType(String callType) { this.callType = callType; }
    
    public List<Integer> getParticipantIds() { return participantIds; }
    public void setParticipantIds(List<Integer> participantIds) { this.participantIds = participantIds; }
    
    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
}