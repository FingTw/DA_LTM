package com.allies.app.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.allies.app.model.QuanHe;

public interface QuanHeRepository extends JpaRepository<QuanHe, Integer> {

    // Lấy id bạn bè khi biết ma_tk
    @Query(value = "SELECT CASE WHEN q.MA_TK_A = :maTk THEN q.MA_TK_B ELSE q.MA_TK_A END " +
                   "FROM QuanHe q WHERE q.MA_TK_A = :maTk OR q.MA_TK_B = :maTk", nativeQuery = true)
    List<Integer> findFriendIdsByMaTk(Integer maTk);
}
