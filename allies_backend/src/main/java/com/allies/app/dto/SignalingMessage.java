// File: SignalingMessage.java
package com.allies.app.dto;

public class SignalingMessage {

    // ID của người gửi (Người khởi tạo tín hiệu)
    private Integer senderId;

    // Tên đăng nhập của người nhận (dùng để định tuyến qua SimpMessagingTemplate)
    private String receiverUsername;

    // ID nhóm (nếu là gọi nhóm)
    private Integer groupId;

    // ID người nhận (dùng để định tuyến qua SimpMessagingTemplate)
    private Integer receiverId;

    // Loại tín hiệu: OFFER, ANSWER, ICE_CANDIDATE, HANGUP
    private String type;

    // Nội dung chính của tín hiệu (chứa SDP hoặc ICE Candidate)
    private String content;

    // Constructor, Getters và Setters (cần được bổ sung)
    // ...
    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

}
