package com.urbe.ms_auth.services.Impl;


import com.urbe.ms_auth.entity.UserEntity;
import com.urbe.ms_auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("The User " + username + " not found"));
        
        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();

        // se agregan los roles a la lista de autorizaciones
        userEntity.getRoles().forEach(role -> 
            authorityList.add(new SimpleGrantedAuthority("ROLE_".concat(role.getRoleEnum().name())))
        );

        // se agregan los permisos a las lista de autorizaciones
        userEntity.getRoles().stream()
                .flatMap(role -> role.getPermissionsList().stream())
                .forEach(permission -> {
                    if (Boolean.TRUE.equals(permission.getCanRead())) {
                        authorityList.add(new SimpleGrantedAuthority("READ"));
                    }
                    if (Boolean.TRUE.equals(permission.getCanWrited())) {
                        authorityList.add(new SimpleGrantedAuthority("WRITE"));
                    }
                    if (Boolean.TRUE.equals(permission.getCanUpdate())) {
                        authorityList.add(new SimpleGrantedAuthority("UPDATE"));
                    }
                    if (Boolean.TRUE.equals(permission.getCanDelete())) {
                        authorityList.add(new SimpleGrantedAuthority("DELETE"));
                    }
                });

        return new User(
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.isEnabled(),
                userEntity.isAccountNoExpired(),
                userEntity.isCredentialNoExpired(),
                userEntity.isAccountNoLocked(),
                authorityList
        );
    }
}
