package com.allies.app.service;

import com.allies.app.model.Account;
import com.allies.app.model.UserProfile;
import com.allies.app.payload.FriendDTO;
import com.allies.app.repository.AccountRepository;
import com.allies.app.repository.QuanHeRepository;
import com.allies.app.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    private final AccountRepository accountRepo;
    private final QuanHeRepository quanHeRepo;
    private final UserProfileRepository userProfileRepo;

    public FriendshipService(AccountRepository accountRepo,
                             QuanHeRepository quanHeRepo,
                             UserProfileRepository userProfileRepo) {
        this.accountRepo = accountRepo;
        this.quanHeRepo = quanHeRepo;
        this.userProfileRepo = userProfileRepo;
    }

    // Lấy danh sách bạn (ACCEPTED) cho ma_tk
    public List<FriendDTO> getFriendsOf(Integer maTk) {
        List<Integer> friendIds = quanHeRepo.findFriendIdsByMaTk(maTk);
        if (friendIds == null || friendIds.isEmpty()) return Collections.emptyList();

        List<Account> accounts = accountRepo.findAllById(friendIds);
        Map<Integer, UserProfile> profileMap = userProfileRepo.findAllById(friendIds)
                .stream().collect(Collectors.toMap(UserProfile::getMaTk, p -> p));

        return accounts.stream().map(a -> {
            UserProfile up = profileMap.get(a.getId());
            FriendDTO dto = new FriendDTO();
            dto.setMaTk(a.getId());
            dto.setUsername(a.getUsername());
            dto.setAvarta(a.getAvarta());
            if (up != null) {
                dto.setFullName(up.getTen());
                dto.setMail(up.getMail());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    // Tìm kiếm người dùng theo keyword (loại bỏ chính mình và loại bỏ bạn đã là friend)
    public List<FriendDTO> searchUsers(Integer requesterMaTk, String keyword) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();

        // Tìm tất cả Account có TEN_DN chứa keyword OR User.TEN chứa keyword
        List<Account> matchedAccounts = accountRepo.findAll().stream()
                .filter(a -> (a.getUsername() != null && a.getUsername().toLowerCase().contains(kw)))
                .collect(Collectors.toList());

        // Kết hợp với profile tìm theo TEN (User.TEN)
        List<UserProfile> profileMatches = userProfileRepo.findAll().stream()
                .filter(p -> p.getTen() != null && p.getTen().toLowerCase().contains(kw))
                .collect(Collectors.toList());

        // Add accounts from profileMatches (by MA_TK) if not already present
        Set<Integer> ids = matchedAccounts.stream().map(Account::getId).collect(Collectors.toSet());
        for (UserProfile p : profileMatches) {
            if (p.getMaTk() != null && !ids.contains(p.getMaTk())) {
                accountRepo.findById(p.getMaTk()).ifPresent(matchedAccounts::add);
                ids.add(p.getMaTk());
            }
        }

        // Exclude self
        matchedAccounts = matchedAccounts.stream()
                .filter(a -> !Objects.equals(a.getId(), requesterMaTk))
                .collect(Collectors.toList());

        // Exclude already friends
        List<Integer> friendIds = quanHeRepo.findFriendIdsByMaTk(requesterMaTk);
        Set<Integer> friendSet = friendIds == null ? Set.of() : new HashSet<>(friendIds);

        List<FriendDTO> result = new ArrayList<>();
        for (Account a : matchedAccounts) {
            if (friendSet.contains(a.getId())) continue;
            UserProfile up = userProfileRepo.findByMaTk(a.getId()).orElse(null);
            FriendDTO dto = new FriendDTO(a.getId(), a.getUsername(), a.getAvarta(),
                    up != null ? up.getTen() : null,
                    up != null ? up.getMail() : null);
            result.add(dto);
        }
        return result;
    }
}
