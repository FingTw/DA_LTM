package com.allies.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "QuanHe")
public class QuanHe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MA_QUAN_HE")
    private Integer id;

    @Column(name = "MA_TK_A", nullable = false)
    private Integer maTkA;

    @Column(name = "MA_TK_B", nullable = false)
    private Integer maTkB;

    @Column(name = "NGAY_KET_BAN")
    private LocalDateTime ngayKetBan;
}
