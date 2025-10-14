package com.allies.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.allies.app.model.Quanhe;
import com.allies.app.model.Taikhoan;

@Repository
public interface QuanheRepository extends JpaRepository<Quanhe, Integer> {
    
    List<Quanhe> findByMaTkAOrMaTkB(Taikhoan maTkA, Taikhoan maTkB);
    @Query("SELECT q FROM Quanhe q WHERE (q.maTkA = ?1 AND q.maTkB = ?2) OR (q.maTkA = ?2 AND q.maTkB = ?1)")
    Optional<Quanhe> findExistingRelation(Taikhoan userA, Taikhoan userB);
}
