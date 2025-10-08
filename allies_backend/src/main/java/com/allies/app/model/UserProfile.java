package com.allies.app.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "`User`") // SQL table is User (backticks for reserved word safety)
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "MA_TK", nullable = false, unique = true)
    private Integer maTk;

    @Column(name = "TEN")
    private String ten;

    @Column(name = "DIA_CHI")
    private String diaChi;

    @Column(name = "SDT")
    private String sdt;

    @Column(name = "MAIL")
    private String mail;
}
