package com.allies.app.service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.allies.app.model.Quanhe;
import com.allies.app.model.Taikhoan;
import com.allies.app.repository.QuanheRepository;
import com.allies.app.repository.TaikhoanRepository;

@Service
public class QuanheService {

    @Autowired
    private QuanheRepository quanheRepository;

    @Autowired
    private TaikhoanRepository taikhoanRepository;

    public Quanhe addFriend(Integer userId1, Integer userId2) {
        Taikhoan user1 = taikhoanRepository.findById(userId1)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng 1 không tồn tại"));
        Taikhoan user2 = taikhoanRepository.findById(userId2)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng 2 không tồn tại"));

        // Kiểm tra quan hệ đã tồn tại
        if (quanheRepository.findExistingRelation(user1, user2).isPresent()) {
            throw new IllegalStateException("Mối quan hệ đã tồn tại");
        }

        Quanhe quanhe = new Quanhe();
        quanhe.setMaTkA(user1);
        quanhe.setMaTkB(user2);
        quanhe.setNgayKetBan(Instant.now());
        return quanheRepository.save(quanhe);
    }

    public List<Taikhoan> getFriends(Integer userId) {
        Taikhoan user = taikhoanRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        List<Quanhe> relations = quanheRepository.findByMaTkAOrMaTkB(user, user);
        return relations.stream()
                .map(q -> q.getMaTkA().equals(user) ? q.getMaTkB() : q.getMaTkA())
                .collect(Collectors.toList());
    }
}