package com.digitalfix.msusuarios.controller;

import com.digitalfix.msusuarios.dto.StatusUpdateDto;
import com.digitalfix.msusuarios.service.AppUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import com.digitalfix.msusuarios.domain.AppUser;
import com.digitalfix.msusuarios.dto.UserProfileDto;
import com.digitalfix.msusuarios.repository.AppUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
    public ResponseEntity loginUser(@AuthenticationPrincipal Jwt jwt) {
        String oid = jwt.getClaimAsString("oid");
        String name = jwt.getClaimAsString("name");

        String email = jwt.getClaimAsString("preferred_username");
        if (email == null) {
            email = jwt.getClaimAsString("email");
        }

        // Extraemos el arreglo de roles desde Azure
        List azureRoles = jwt.getClaimAsStringList("roles");
        String assignedRole = "CLIENTE"; // Rol por defecto si Azure no envía ninguno

        if (azureRoles != null && !azureRoles.isEmpty()) {
            assignedRole = String.valueOf(azureRoles.get(0));
        }

        // Pasamos el role extraído a tu servicio actualizado
        AppUser user = appUserService.autoProvision(oid, email, name, assignedRole);

        UserProfileDto dto = new UserProfileDto(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCompany().getName(),
                user.getActive()
        );

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{oid}/status")
    public ResponseEntity updateUserStatus(
            @PathVariable String oid,
            @RequestBody StatusUpdateDto statusDto) {

        AppUser updatedUser = appUserService.updateStatus(oid, statusDto.isActive());

        UserProfileDto dto = new UserProfileDto(
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRole(),
                updatedUser.getCompany().getName(),
                updatedUser.getActive()
        );

        return ResponseEntity.ok(dto);
    }
}