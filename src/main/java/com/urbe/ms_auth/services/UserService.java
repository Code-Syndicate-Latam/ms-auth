package com.urbe.ms_auth.services;

import com.urbe.ms_auth.entity.UserEntity;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserEntity> findAll();
    Optional<UserEntity> findById(Long id);
    Optional<UserEntity> findByUsername(String username);
    UserEntity save(UserEntity userEntity);
    void deleteById(Long id);
}