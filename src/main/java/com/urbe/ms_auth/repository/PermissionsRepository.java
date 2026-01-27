package com.urbe.ms_auth.repository;

import com.urbe.ms_auth.entity.PermissionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionsRepository extends JpaRepository<PermissionsEntity, Long> {
}
