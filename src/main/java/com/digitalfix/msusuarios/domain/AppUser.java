package com.digitalfix.msusuarios.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "azure_oid", nullable = false, unique = true)
    private String azureOid;

    @Column(nullable = false)
    private String email;

    @Column(name = "nombre")
    private String name;

    @Column(name = "rol", nullable = false)
    private String role;

    @Column(name = "activo", nullable = false)
    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compania_id", nullable = false)
    private Company company;

    @Column(name = "creacion", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.active == null) {
            this.active = true;
        }
        if (this.role == null) {
            this.role = "CLIENTE";
        }
    }
}