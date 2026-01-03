package com.teamsoft.ms.auth.repository;

import com.teamsoft.ms.auth.entities.PermissionRole;
import com.teamsoft.ms.auth.entities.keys.PermissionRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRoleRepository extends JpaRepository<PermissionRole, PermissionRoleId> {
}
