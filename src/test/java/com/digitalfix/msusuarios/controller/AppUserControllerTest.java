package com.digitalfix.msusuarios.controller;

import com.digitalfix.msusuarios.domain.AppUser;
import com.digitalfix.msusuarios.domain.Company;
import com.digitalfix.msusuarios.dto.UserProfileDto;
import com.digitalfix.msusuarios.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppUserControllerTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private AppUserController appUserController;

    @Test
    public void givenExistingUser_whenGetByOid_thenReturn200AndDto() {
        Company company = new Company();
        company.setName("Empresa Test");

        AppUser user = new AppUser();
        user.setAzureOid("123-oid");
        user.setName("Chris");
        user.setEmail("chris@test.com");
        user.setRole("ADMIN");
        user.setActive(true);
        user.setCompany(company);

        when(appUserRepository.findByAzureOid("123-oid")).thenReturn(Optional.of(user));

        // Ejecutar el método directamente (¡AQUÍ ESTÁ LA CORRECCIÓN!)
        ResponseEntity< UserProfileDto > response = appUserController.getUserByOid("123-oid");

        // Verificar resultados
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Chris", response.getBody().getName());
        assertEquals("Empresa Test", response.getBody().getCompanyName());
    }

    @Test
    public void givenNonExistingUser_whenGetByOid_thenReturn404() {
        when(appUserRepository.findByAzureOid("unknown-oid")).thenReturn(Optional.empty());

        ResponseEntity< UserProfileDto > response = appUserController.getUserByOid("unknown-oid");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}