// File: GroupCreationDto.java
package com.allies.app.dto;

import java.util.List;

public class GroupCreationDto {
    
    // Tên của nhóm chat
    private String tenNhom;
    
    // ID của người dùng khởi tạo (có thể lấy từ JWT, nhưng vẫn nên truyền vào)
    private Integer nguoiTaoId;
    
    // Danh sách các ID người tham gia ban đầu
    private List<Integer> thanhVienIds; 

    // Constructor, Getters và Setters (cần được bổ sung)
    // ...
    
    public String getTenNhom() { return tenNhom; }
    public void setTenNhom(String tenNhom) { this.tenNhom = tenNhom; }
    
    public Integer getNguoiTaoId() { return nguoiTaoId; }
    public void setNguoiTaoId(Integer nguoiTaoId) { this.nguoiTaoId = nguoiTaoId; }
    
    public List<Integer> getThanhVienIds() { return thanhVienIds; }
    public void setThanhVienIds(List<Integer> thanhVienIds) { this.thanhVienIds = thanhVienIds; }
}