package com.digitalfix.msusuarios.controller;

import com.digitalfix.msusuarios.domain.AppUser;
import com.digitalfix.msusuarios.dto.StatusUpdateDto;
import com.digitalfix.msusuarios.dto.UserProfileDto;
import com.digitalfix.msusuarios.repository.AppUserRepository;
import com.digitalfix.msusuarios.service.AppUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public ResponseEntity < List < UserProfileDto > > getAllUsers() {
        List < UserProfileDto > users = appUserService.getAllUsers().stream()
                .map(user -> new UserProfileDto(
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCompany().getId(),
                        user.getActive()
                ))
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{oid}")
    public ResponseEntity < UserProfileDto > getUserByOid(@PathVariable String oid) {
        Optional < AppUser > userOpt = appUserRepository.findByAzureOid(oid);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AppUser user = userOpt.get();
        UserProfileDto dto = new UserProfileDto(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCompany().getId(),
                user.getActive()
        );

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/login")
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
        UserProfileDto dto = new UserProfileDto(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCompany().getId(),
                user.getActive()
        );

        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{oid}/status")
    public ResponseEntity < UserProfileDto > updateUserStatus(
            @PathVariable String oid,
            @RequestBody StatusUpdateDto statusDto) {

        AppUser updatedUser = appUserService.updateStatus(oid, statusDto.isActive());

        UserProfileDto dto = new UserProfileDto(
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRole(),
                updatedUser.getCompany().getId(),
                updatedUser.getActive()
        );

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{oid}")
    public ResponseEntity < Void > deleteUser(@PathVariable String oid) {
        appUserService.deleteUser(oid);
        return ResponseEntity.noContent().build();
    }
}