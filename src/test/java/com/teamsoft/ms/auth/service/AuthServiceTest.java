package com.teamsoft.ms.auth.service;

import com.teamsoft.ms.auth.entities.User;
import com.teamsoft.ms.auth.exception.RoleNotFoundException;
import com.teamsoft.ms.auth.model.request.RegisterRequest;
import com.teamsoft.ms.auth.repository.RoleRepository;
import com.teamsoft.ms.auth.repository.UserRepository;
import com.teamsoft.ms.auth.service.Impl.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
    }

    @Test
    void registerShouldPersistUserWhenRoleExists() {
        RegisterRequest request = buildRegisterRequest("1");
        when(roleRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertThat(saved.getEmail()).isEqualTo(request.email);
        assertThat(saved.getRoleId()).isEqualTo(1L);
        assertThat(saved.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(saved.isEnabled()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();

        verify(roleRepository).existsById(1L);
        verify(passwordEncoder, times(2)).encode(request.password);
    }

    @Test
    void registerShouldThrowWhenRoleIdIsInvalid() {
        RegisterRequest request = buildRegisterRequest("ADMIN");

        assertThrows(RoleNotFoundException.class, () -> authService.register(request));
        verify(roleRepository, never()).existsById(anyLong());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerShouldThrowWhenRoleDoesNotExist() {
        RegisterRequest request = buildRegisterRequest("5");
        when(roleRepository.existsById(5L)).thenReturn(false);

        assertThrows(RoleNotFoundException.class, () -> authService.register(request));
        verify(roleRepository).existsById(5L);
        verify(userRepository, never()).save(any(User.class));
    }

    private RegisterRequest buildRegisterRequest(String roleId) {
        RegisterRequest req = new RegisterRequest();
        req.name = "John";
        req.lastName = "Doe";
        req.email = "john.doe@example.com";
        req.documentType = "CC";
        req.documentNumber = "123";
        req.password = "secret";
        req.phone = "555";
        req.rol = roleId;
        return req;
    }
}
