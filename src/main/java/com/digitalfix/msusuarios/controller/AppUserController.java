package com.digitalfix.msusuarios.controller;

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

    public AppUserController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
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
}