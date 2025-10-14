package com.allies.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FriendRequestDto {
    @NotNull(message = "Sender ID is required")
    private Integer senderId;

    @NotNull(message = "Receiver ID is required")
    private Integer receiverId;

    @Size(max = 255, message = "Content must not exceed 255 characters")
    private String noiDung;

    // Getters và setters
    public Integer getSenderId() {
        return senderId;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }
}