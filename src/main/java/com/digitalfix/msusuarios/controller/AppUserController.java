package com.digitalfix.msusuarios.controller;

import com.digitalfix.msusuarios.domain.AppUser;
import com.digitalfix.msusuarios.domain.Company;
import com.digitalfix.msusuarios.dto.StatusUpdateDto;
import com.digitalfix.msusuarios.dto.UserProfileDto;
import com.digitalfix.msusuarios.repository.AppUserRepository;
import com.digitalfix.msusuarios.service.AppUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
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

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity < List < UserProfileDto > > getAllUsers() {
        List < UserProfileDto > users = appUserService.getAllUsers().stream()
                .map(AppUserController::aPerfil)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{oid}")
    @Transactional(readOnly = true)
    public ResponseEntity < UserProfileDto > getUserByOid(@PathVariable String oid) {
        Optional < AppUser > userOpt = appUserRepository.findByAzureOid(oid);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(aPerfil(userOpt.get()));
    }

    @PostMapping("/login")
    @Transactional
    public ResponseEntity loginUser() {
        Jwt jwt = (Jwt) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String oid = jwt.getClaimAsString("oid");
        String name = jwt.getClaimAsString("name");

        String email = jwt.getClaimAsString("preferred_username");
        if (email == null) {
            email = jwt.getClaimAsString("email");
        }

        List azureRoles = jwt.getClaimAsStringList("roles");
        String assignedRole = "CLIENTE";

        if (azureRoles != null && !azureRoles.isEmpty()) {
            assignedRole = String.valueOf(azureRoles.get(0));
        }

        AppUser user = appUserService.autoProvision(oid, email, name, assignedRole);

        return ResponseEntity.ok(aPerfil(user));
    }

    @PutMapping("/{oid}/status")
    @Transactional
    public ResponseEntity < UserProfileDto > updateUserStatus(
            @PathVariable String oid,
            @RequestBody StatusUpdateDto statusDto) {

        AppUser updatedUser = appUserService.updateStatus(oid, statusDto.isActive());

        return ResponseEntity.ok(aPerfil(updatedUser));
    }

    @DeleteMapping("/{oid}")
    public ResponseEntity < Void > deleteUser(@PathVariable String oid) {
        appUserService.deleteUser(oid);
        return ResponseEntity.noContent().build();
    }

    /**
     * La empresa viene en una relacion LAZY: leer su nombre obliga a cargarla.
     * Por eso los dos GET son transaccionales, y no se confia en open-in-view,
     * que esta activo por omision pero puede apagarse cualquier dia.
     */
    private static UserProfileDto aPerfil(AppUser user) {
        Company company = user.getCompany();
        return new UserProfileDto(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                company == null ? null : company.getId(),
                company == null ? null : company.getName(),
                user.getActive()
        );
    }
}
