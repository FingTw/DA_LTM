package com.allies.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.allies.app.model.Taikhoan;   

@Repository
public interface TaikhoanRepository extends JpaRepository<Taikhoan, Integer> {
    Optional<Taikhoan> findByTenDn(String tenDn);
    boolean existsByTenDn(String tenDn);

    public List<Taikhoan> findByTenDnContainingIgnoreCase(String query);
}
