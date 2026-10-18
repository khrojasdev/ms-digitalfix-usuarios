package com.digitalfix.msusuarios.service;

import com.digitalfix.msusuarios.domain.AppUser;
import com.digitalfix.msusuarios.domain.Company;
import com.digitalfix.msusuarios.messaging.UsuarioEventoPublisher;
import com.digitalfix.msusuarios.repository.AppUserRepository;
import com.digitalfix.msusuarios.repository.CompanyRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final CompanyRepository companyRepository;
    private final UsuarioEventoPublisher eventos;

    public AppUserService(AppUserRepository appUserRepository, CompanyRepository companyRepository,
                          UsuarioEventoPublisher eventos) {
        this.appUserRepository = appUserRepository;
        this.companyRepository = companyRepository;
        this.eventos = eventos;
    }

    @Transactional
    public AppUser autoProvision(String azureOid, String email, String name, String role) {
        Optional < AppUser > existingUser = appUserRepository.findByAzureOid(azureOid);

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
        newUser.setRole(role);
        newUser.setActive(true);

        try {
            AppUser creado = appUserRepository.saveAndFlush(newUser);
            eventos.publicarCreado(creado);
            return creado;
        } catch (DataIntegrityViolationException e) {
            // Inserción concurrente: el usuario ya existía, no es una creación nueva.
            return appUserRepository.findByAzureOid(azureOid).orElseThrow();
        }
    }

    public List < AppUser > getAllUsers() {
        return appUserRepository.findAll();
    }

    public AppUser updateStatus(String oid, boolean active) {
        AppUser user = appUserRepository.findByAzureOid(oid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con OID: " + oid));
        user.setActive(active);
        AppUser actualizado = appUserRepository.save(user);
        eventos.publicarCambioEstado(actualizado);
        return actualizado;
    }

    @Transactional
    public void deleteUser(String oid) {
        AppUser user = appUserRepository.findByAzureOid(oid)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con OID para eliminar: " + oid));

        appUserRepository.delete(user);
    }
}