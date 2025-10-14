package com.allies.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.allies.app.model.Loimoiketban;
import com.allies.app.model.Taikhoan;

@Repository
public interface LoimoiketbanRepository extends JpaRepository<Loimoiketban, Integer> {
    List<Loimoiketban> findByMaTkNhanAndTrangThai(Taikhoan maTkNhan, String trangThai);
    List<Loimoiketban> findByMaTkGui(Taikhoan maTkGui);
    Optional<Loimoiketban> findByMaTkGuiAndMaTkNhan(Taikhoan maTkGui, Taikhoan maTkNhan);
}
