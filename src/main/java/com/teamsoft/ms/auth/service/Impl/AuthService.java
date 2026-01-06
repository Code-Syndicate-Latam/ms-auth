package com.teamsoft.ms.auth.service.Impl;

import com.teamsoft.ms.auth.entities.User;
import com.teamsoft.ms.auth.exception.RoleNotFoundException;
import com.teamsoft.ms.auth.model.request.RegisterRequest;
import com.teamsoft.ms.auth.model.request.external.CreateUserRequest;
import com.teamsoft.ms.auth.repository.RoleRepository;
import com.teamsoft.ms.auth.repository.UserRepository;
import com.teamsoft.ms.auth.service.IAuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Instant;


@Service
@RequiredArgsConstructor
@Transactional
public class AuthService implements IAuthService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final WebClient usersWebClient;
    @Override
    public void register(RegisterRequest req){
        var createUserReq = CreateUserRequest.builder()
                .firstName(req.firstName)
                .identificationNumber(req.identificationNumber)
                .documentTypeId(req.documentTypeId)
                .firstLastName(req.firstLastName)
                .phone(req.phone)
                .secondLastName(req.secondLastName)
                .middleName(req.middleName)
                .address(req.address)
                .dateOfBirth(req.dateOfBirth)
                .build();
        // Llamar a creacion de usuario ms-usuarios si se desea
        usersWebClient.post()
                .uri("")
                .bodyValue(createUserReq)
                .retrieve()
                .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class)
                                .flatMap(body -> reactor.core.publisher.Mono.error(
                                        new RuntimeException("Users-service error " + resp.statusCode() + ": " + body)
                                ))
                )
                .toBodilessEntity()
                .block();

        Long roleId = parseRoleId(req.rol);
        if (roleId == null || !roleRepository.existsById(roleId)) {
            throw new RoleNotFoundException("Role not found: " + roleId);
        }

        User user = User.builder()
                .email(req.email)
                .passwordHash(passwordEncoder.encode(req.password))
                .enabled(true)
                .createdAt(Instant.now())
                .roleId(roleId)
                .build();
        userRepository.save(user);
    }

    private Long parseRoleId(String rol){
        if (rol == null) return null;
        try{
            return Long.parseLong(rol);
        }catch(NumberFormatException ex){
            return null;
        }
    }

}
