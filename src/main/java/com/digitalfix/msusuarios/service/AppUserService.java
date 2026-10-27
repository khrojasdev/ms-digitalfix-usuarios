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
            return sincronizarConElToken(user, email, name, role);
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
        newUser.setRole(role == null || role.isBlank() ? "CLIENTE" : role.trim().toUpperCase());
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

    /**
     * El tenant manda sobre el rol: si el token trae un rol distinto al que
     * quedo guardado (por ejemplo, lo pasaron de CLIENTE a TECNICO en Entra
     * ID), se actualiza aqui y no hay que volver a asignarlo a mano. Nombre y
     * correo tambien se ponen al dia. Si el token no trae rol, se conserva el
     * guardado.
     */
    private AppUser sincronizarConElToken(AppUser user, String email, String name, String role) {
        boolean cambioRol = role != null && !role.isBlank() && !role.trim().equalsIgnoreCase(user.getRole());
        boolean cambioDatos = false;
        if (email != null && !email.isBlank() && !email.equalsIgnoreCase(user.getEmail())) {
            user.setEmail(email);
            cambioDatos = true;
        }
        if (name != null && !name.isBlank() && !name.equals(user.getName())) {
            user.setName(name);
            cambioDatos = true;
        }
        if (cambioRol) {
            user.setRole(role.trim().toUpperCase());
        }
        if (!cambioRol && !cambioDatos) {
            return user;
        }
        AppUser actualizado = appUserRepository.save(user);
        if (cambioRol) {
            eventos.publicarCambioRol(actualizado);
        }
        return actualizado;
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