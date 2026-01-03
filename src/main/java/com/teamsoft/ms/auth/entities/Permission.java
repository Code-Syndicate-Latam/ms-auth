package com.teamsoft.ms.auth.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permission")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permission_generator")
    @SequenceGenerator(name = "permission_generator",
            sequenceName = "permission_permission_id_seq",
            allocationSize = 1
    )
    @Column(name = "permission_id")
    private Long permissionId;


    @Column(name = "\"READ\"", nullable = false)
    private boolean read;


    @Column(name = "\"WRITE\"", nullable = false)
    private boolean write;


    @Column(name = "\"UPDATE\"", nullable = false)
    private boolean update;


    @Column(name = "\"DELETE\"", nullable = false)
    private boolean delete;


    @Column(name = "status", nullable = false)
    private boolean status = true;

}
