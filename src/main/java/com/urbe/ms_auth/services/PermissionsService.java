package com.urbe.ms_auth.services;

import com.urbe.ms_auth.entity.PermissionsEntity;
import java.util.List;
import java.util.Optional;

public interface PermissionsService {
    List<PermissionsEntity> findAll();
    Optional<PermissionsEntity> findById(Long id);
    PermissionsEntity save(PermissionsEntity permissionsEntity);
    void deleteById(Long id);
}