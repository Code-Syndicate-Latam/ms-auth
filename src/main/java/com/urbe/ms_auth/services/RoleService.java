package com.urbe.ms_auth.services;

import com.urbe.ms_auth.entity.RoleEntity;
import java.util.List;
import java.util.Optional;

public interface RoleService {
    List<RoleEntity> findAll();
    Optional<RoleEntity> findById(Long id);
    RoleEntity save(RoleEntity roleEntity);
    void deleteById(Long id);
}