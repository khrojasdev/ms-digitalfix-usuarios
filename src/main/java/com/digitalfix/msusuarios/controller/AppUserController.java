package com.digitalfix.msusuarios.controller;

import com.digitalfix.msusuarios.service.AppUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import com.digitalfix.msusuarios.domain.AppUser;
import com.digitalfix.msusuarios.dto.UserProfileDto;
import com.digitalfix.msusuarios.repository.AppUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserRepository appUserRepository;
    private final AppUserService appUserService;

    public AppUserController(AppUserRepository appUserRepository, AppUserService appUserService) {
        this.appUserRepository = appUserRepository;
        this.appUserService = appUserService;
    }

    @GetMapping("/{oid}")
    public ResponseEntity< UserProfileDto > getUserByOid(@PathVariable String oid) {
        Optional< AppUser > userOpt = appUserRepository.findByAzureOid(oid);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AppUser user = userOpt.get();
        UserProfileDto dto = new UserProfileDto(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCompany().getName(),
                user.getActive()
        );

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/login")
    public ResponseEntity< UserProfileDto > login(@AuthenticationPrincipal Jwt jwt) {
        // 1. Extraer los datos seguros directamente del token de Azure
        String oid = jwt.getClaimAsString("oid");
        String name = jwt.getClaimAsString("name");

        // Azure a veces guarda el correo en "preferred_username" o en "email"
        String email = jwt.getClaimAsString("preferred_username");
        if (email == null) {
            email = jwt.getClaimAsString("email");
        }

        // 2. Llamar a tu regla de negocio (crea el usuario o devuelve el existente)
        AppUser user = appUserService.autoProvision(oid, email, name);

        // 3. Convertir a DTO para responder
        UserProfileDto dto = new UserProfileDto(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCompany().getName(),
                user.getActive()
        );

        return ResponseEntity.ok(dto);
    }
}