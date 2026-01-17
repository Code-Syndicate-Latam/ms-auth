package com.teamsoft.ms.auth.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "role")
public class Role {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_generator")
    @SequenceGenerator(name = "role_generator", sequenceName = "role_role_id_seq", allocationSize = 1
    )
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_name", length = 50, nullable = false)
    private String roleName;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    private boolean status = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission_code")
    private List<String> permissions = new ArrayList<>();
}