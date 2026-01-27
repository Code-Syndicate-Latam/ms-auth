package com.urbe.ms_auth.services.Impl;

import com.urbe.ms_auth.entity.PermissionsEntity;
import com.urbe.ms_auth.repository.PermissionsRepository;
import com.urbe.ms_auth.services.PermissionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionsServiceImpl implements PermissionsService {

    @Autowired
    private PermissionsRepository permissionsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PermissionsEntity> findAll() {
        return permissionsRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PermissionsEntity> findById(Long id) {
        return permissionsRepository.findById(id);
    }

    @Override
    @Transactional
    public PermissionsEntity save(PermissionsEntity permissionsEntity) {
        return permissionsRepository.save(permissionsEntity);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        permissionsRepository.deleteById(id);
    }
}