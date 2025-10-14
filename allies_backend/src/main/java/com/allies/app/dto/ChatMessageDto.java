// File: ChatMessageDto.java
package com.allies.app.dto;

public class ChatMessageDto {
    
    private String noiDung;
    private String loaiTinNhan; // TEXT, IMAGE, VIDEO
    
    // Dành cho chat 1-1
    private Integer receiverId;
    private String receiverUsername;
    
    // Dành cho chat nhóm
    private Integer groupId;
    private Integer mediaId; // Nếu là tin nhắn media

    // Constructor, Getters và Setters (cần được bổ sung)
    // ...
    
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    
    public String getLoaiTinNhan() { return loaiTinNhan; }
    public void setLoaiTinNhan(String loaiTinNhan) { this.loaiTinNhan = loaiTinNhan; }
    
    public Integer getReceiverId() { return receiverId; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }
    
    public String getReceiverUsername() { return receiverUsername; }
    public void setReceiverUsername(String receiverUsername) { this.receiverUsername = receiverUsername; }
    
    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }
    
    public Integer getMediaId() { return mediaId; }
    public void setMediaId(Integer mediaId) { this.mediaId = mediaId; }
}