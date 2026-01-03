package com.teamsoft.ms.auth.repository;

import com.teamsoft.ms.auth.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {


}
