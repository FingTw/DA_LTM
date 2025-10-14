package com.allies.app.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.allies.app.model.Loimoiketban;
import com.allies.app.model.Taikhoan;
import com.allies.app.repository.LoimoiketbanRepository;
import com.allies.app.repository.TaikhoanRepository;

@Service
public class LoimoiketbanService {

    @Autowired
    private LoimoiketbanRepository loimoiketbanRepository;
    @Autowired
    private TaikhoanRepository taikhoanRepository;
    @Autowired
    private QuanheService quanheService;

    public Loimoiketban sendFriendRequest(Integer senderId, Integer receiverId, String noiDung) {
        Taikhoan sender = taikhoanRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Người gửi không tồn tại"));
        Taikhoan receiver = taikhoanRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Người nhận không tồn tại"));

        // Kiểm tra xem đã gửi lời mời hay quan hệ đã tồn tại
        if (loimoiketbanRepository.findByMaTkGuiAndMaTkNhan(sender, receiver).isPresent()) {
            throw new IllegalStateException("Lời mời đã được gửi");
        }
        if (quanheService.getFriends(senderId).contains(receiver)) {
            throw new IllegalStateException("Đã là bạn bè");
        }

        Loimoiketban request = new Loimoiketban();
        request.setMaTkGui(sender);
        request.setMaTkNhan(receiver);
        request.setNoiDungLoiMoi(noiDung);
        request.setThoiGianGui(Instant.now());
        request.setTrangThai("PENDING");
        return loimoiketbanRepository.save(request);
    }

    public Loimoiketban acceptFriendRequest(Integer requestId) {
        Loimoiketban request = loimoiketbanRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Lời mời không tồn tại"));
        if (!"PENDING".equals(request.getTrangThai())) {
            throw new IllegalStateException("Lời mời không còn pending");
        }

        request.setTrangThai("ACCEPTED");
        loimoiketbanRepository.save(request);

        // Tạo quan hệ trong Quanhe
        quanheService.addFriend(request.getMaTkGui().getMaTk(), request.getMaTkNhan().getMaTk());

        return request;
    }

    public Loimoiketban rejectFriendRequest(Integer requestId) {
        Loimoiketban request = loimoiketbanRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Lời mời không tồn tại"));
        if (!"PENDING".equals(request.getTrangThai())) {
            throw new IllegalStateException("Lời mời không còn pending");
        }
        request.setTrangThai("REJECTED");
        return loimoiketbanRepository.save(request);
    }

    public List<Loimoiketban> getPendingRequests(Integer receiverId) {
        Taikhoan receiver = taikhoanRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Người nhận không tồn tại"));
        return loimoiketbanRepository.findByMaTkNhanAndTrangThai(receiver, "PENDING");
    }
}