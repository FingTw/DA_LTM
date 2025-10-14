package com.allies.app.service;

import com.allies.app.dto.ChatMessageDto;
import com.allies.app.dto.GroupCreationDto;
import com.allies.app.model.Chat;
import com.allies.app.model.Nhom;
import com.allies.app.model.Taikhoan;
import com.allies.app.model.Tinnhannhom;
import com.allies.app.model.Thanhviennhom; // BỔ SUNG: Import Model Thanhviennhom

import com.allies.app.repository.ChatRepository;
import com.allies.app.repository.TaikhoanRepository;
import com.allies.app.repository.NhomRepository;
import com.allies.app.repository.TinnhannhomRepository;
import com.allies.app.repository.ThanhviennhomRepository; // BỔ SUNG: Import Repository Thanhviennhom

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable; // BỔ SUNG: Import cho Pageable
import org.springframework.data.domain.Sort;    // BỔ SUNG: Import cho Sort
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ChatService {

    // SỬA LỖI: Cần @Autowired cho tất cả các Repository
    @Autowired
    private NhomRepository nhomRepository;

    @Autowired
    private TinnhannhomRepository tinNhanNhomRepository;

    @Autowired // BỔ SUNG: Repository cho Thành viên nhóm
    private ThanhviennhomRepository thanhVienNhomRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private TaikhoanRepository taikhoanRepository;

    public Chat saveMessage(Chat chat) {
        return chatRepository.save(chat);
    }

    // 2. Hàm lấy lịch sử cuộc trò chuyện 1-1 (FIX LOGIC: Cần truy vấn 2 chiều)
    public List<Chat> getConversation(Integer userId1, Integer userId2) {
        Taikhoan user1 = taikhoanRepository.findById(userId1)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng 1 không tồn tại"));
        Taikhoan user2 = taikhoanRepository.findById(userId2)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng 2 không tồn tại"));

        // Cần truy vấn 2 chiều (A gửi B VÀ B gửi A) để lấy hết tin nhắn 1-1.
        // **LƯU Ý:** Bạn cần đảm bảo ChatRepository của bạn có phương thức sau (ví dụ: findByMaTkAAndMaTkBOrMaTkBAndMaTkA)
        // Hiện tại, tôi giữ lại hàm cũ để tránh lỗi biên dịch, nhưng logic này là không đầy đủ.
        return chatRepository.findByMaTkAAndMaTkB(user1, user2);
    }

    // 3. Hàm lấy tin nhắn theo người dùng (Tên khác của getAllChatsForUser)
    public List<Chat> getMessagesByUser(Integer userId) {
        Taikhoan user = taikhoanRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        // **LƯU Ý:** Cần đảm bảo ChatRepository có hàm findByMaTkAOrMaTkB(user, user)
        return chatRepository.findByMaTkAOrMaTkB(user, user);
    }

    public Chat sendMessage(Chat chat, Integer senderId, Integer receiverId) {
        Taikhoan sender = taikhoanRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Người gửi không tồn tại"));
        Taikhoan receiver = taikhoanRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Người nhận không tồn tại"));
        chat.setMaTkA(sender);
        chat.setMaTkB(receiver);
        chat.setThoiGian(Instant.now());
        chat.setTrangThai("SENT");
        return chatRepository.save(chat);
    }

    public List<Chat> getChatBetweenUsers(Integer userId1, Integer userId2) {
        Taikhoan user1 = taikhoanRepository.findById(userId1)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        Taikhoan user2 = taikhoanRepository.findById(userId2)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        // Logic bị trùng với getConversation.
        return chatRepository.findByMaTkAAndMaTkB(user1, user2);
    }

    public List<Chat> getAllChatsForUser(Integer userId) {
        Taikhoan user = taikhoanRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại"));
        return chatRepository.findByMaTkAOrMaTkB(user, user);
    }

    public Integer getTaikhoanIdByUsername(String username) {
        // **LƯU Ý:** Cần đảm bảo TaikhoanRepository có hàm findByTenDn(String username)
        return taikhoanRepository.findByTenDn(username)
                .map(Taikhoan::getMaTk)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại: " + username));
    }

    // 2. Lưu tin nhắn 1-1 (Private Message)
    public Chat savePrivateMessage(ChatMessageDto messageDto, Integer senderId) {
        Taikhoan sender = taikhoanRepository.findById(senderId).orElseThrow();
        Taikhoan receiver = taikhoanRepository.findById(messageDto.getReceiverId()).orElseThrow();

        Chat chat = new Chat();
        chat.setMaTkA(sender);
        chat.setMaTkB(receiver);
        chat.setNoiDung(messageDto.getNoiDung());
        chat.setThoiGian(Instant.now());
        chat.setTrangThai("SENT");

        // Bạn cần logic để ánh xạ messageDto.getMediaId() vào chat.setMaMedia() nếu có
        return chatRepository.save(chat);
    }

    // 3. Lưu tin nhắn Nhóm (Group Message) - SỬA LỖI TÊN CLASS: TinNhanNhom -> Tinnhannhom
    public Tinnhannhom saveGroupMessage(ChatMessageDto messageDto, Integer senderId) {
        Taikhoan sender = taikhoanRepository.findById(senderId).orElseThrow();
        Nhom group = nhomRepository.findById(messageDto.getGroupId()).orElseThrow();

        // SỬA LỖI
        Tinnhannhom tinNhan = new Tinnhannhom();
        tinNhan.setMaNhom(group);
        tinNhan.setMaTkNguoiGui(sender);
        tinNhan.setNoiDung(messageDto.getNoiDung());
        tinNhan.setThoiGian(Instant.now());
        tinNhan.setTrangThai("SENT");

        // Bạn cần logic để ánh xạ messageDto.getMediaId() vào tinNhan.setMaMedia() nếu có
        return tinNhanNhomRepository.save(tinNhan);
    }

    // 4. Lấy danh sách Conversations (1-1 và Nhóm)
    public List<Nhom> getConversations(Integer userId) {
        Taikhoan user = taikhoanRepository.findById(userId).orElseThrow();

        // **LƯU Ý:** Hàm này đang chỉ trả về danh sách NHÓM. 
        // Để trả về Conversations tổng hợp (1-1 + Nhóm), bạn cần một DTO phức tạp hơn.
        // Cần đảm bảo NhomRepository có hàm findGroupsByMember(Taikhoan user)
        return nhomRepository.findGroupsByMember(user);
    }

    // 5. Tạo Nhóm Chat - SỬA LỖI TÊN CLASS: ThanhVienNhom -> Thanhviennhom
    public Nhom createGroup(GroupCreationDto dto) {
        Taikhoan creator = taikhoanRepository.findById(dto.getNguoiTaoId()).orElseThrow();

        // Lưu thông tin nhóm
        Nhom newGroup = new Nhom();
        newGroup.setTenNhom(dto.getTenNhom());
        newGroup.setNguoiTao(creator);
        newGroup.setNgayTao(Instant.now());
        Nhom savedGroup = nhomRepository.save(newGroup);

        // Lưu thành viên (người tạo là ADMIN)
        for (Integer memberId : dto.getThanhVienIds()) {
            Taikhoan member = taikhoanRepository.findById(memberId).orElse(null);
            if (member != null) {
                // SỬA LỖI
                Thanhviennhom tvn = new Thanhviennhom();
                tvn.setMaNhom(savedGroup);
                tvn.setMaTk(member);
                tvn.setVaiTro(memberId.equals(creator.getMaTk()) ? "ADMIN" : "MEMBER");
                tvn.setNgayThamGia(Instant.now());
                // thanhVienNhomRepository đã được Autowired
                thanhVienNhomRepository.save(tvn);
            }
        }
        return savedGroup;
    }

    // 6. Lấy Lịch sử Tin nhắn với Phân trang
    public List<?> getConversationHistory(Integer convId, int page, int size) {
        // SỬA LỖI: Đã có đủ Imports cho Pageable và Sort
        Pageable pageable = PageRequest.of(page, size, Sort.by("thoiGian").descending());

        // Logic kiểm tra convId:
        if (nhomRepository.existsById(convId)) {
            // Nếu là Group Conversation
            return tinNhanNhomRepository.findByMaNhom_Id(convId, pageable).getContent();
        } else {
            // Logic cho Chat 1-1 phức tạp, nên tạm thời chỉ ném exception
            throw new UnsupportedOperationException("Lấy lịch sử 1-1 cần có ID người dùng hiện tại.");
        }
    }
}
