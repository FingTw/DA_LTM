package com.allies.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.allies.app.model.Nhom;
import com.allies.app.model.Taikhoan;

@Repository
public interface NhomRepository extends JpaRepository<Nhom, Integer> {
    List<Nhom> findByNguoiTao(Taikhoan nguoiTao);
    @Query("SELECT tv.maNhom FROM Thanhviennhom tv WHERE tv.maTk = :user")
    List<Nhom> findGroupsByMember(@Param("user") Taikhoan user);
}
