package com.digitalfix.msusuarios.service;

import com.digitalfix.msusuarios.domain.AppUser;
import com.digitalfix.msusuarios.domain.Company;
import com.digitalfix.msusuarios.repository.AppUserRepository;
import com.digitalfix.msusuarios.repository.CompanyRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final CompanyRepository companyRepository;

    public AppUserService(AppUserRepository appUserRepository, CompanyRepository companyRepository) {
        this.appUserRepository = appUserRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public AppUser autoProvision(String azureOid, String email, String name, String role) {
        Optional< AppUser > existingUser = appUserRepository.findByAzureOid(azureOid);

        if (existingUser.isPresent()) {
            AppUser user = existingUser.get();
            if (!user.getActive()) {
                throw new IllegalStateException("El usuario se encuentra inactivo. Acceso denegado.");
            }
            return user;
        }

        Company defaultCompany = companyRepository.findById(1L).orElseGet(() -> {
            Company newCompany = new Company();
            newCompany.setName("Empresa por Defecto");
            return companyRepository.save(newCompany);
        });

        AppUser newUser = new AppUser();
        newUser.setAzureOid(azureOid);
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setCompany(defaultCompany);
        newUser.setRole(role); // <-- Asignamos el rol proveniente de Azure
        newUser.setActive(true); // <-- Nos aseguramos de que inicie activo

        try {
            // saveAndFlush fuerza la escritura inmediata para gatillar el error de UNIQUE si hay colisión
            return appUserRepository.saveAndFlush(newUser);
        } catch (DataIntegrityViolationException e) {
            // Si otro hilo lo creó fracciones de segundo antes, lo recuperamos
            return appUserRepository.findByAzureOid(azureOid).orElseThrow();
        }
    }

    public AppUser updateStatus(String oid, boolean active) {
        AppUser user = appUserRepository.findByAzureOid(oid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con OID: " + oid));
        user.setActive(active);
        return appUserRepository.save(user);
    }
}