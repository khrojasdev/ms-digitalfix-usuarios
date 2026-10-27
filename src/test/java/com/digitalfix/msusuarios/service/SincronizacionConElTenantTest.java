package com.digitalfix.msusuarios.service;

import com.digitalfix.msusuarios.domain.AppUser;
import com.digitalfix.msusuarios.domain.Company;
import com.digitalfix.msusuarios.messaging.UsuarioEventoPublisher;
import com.digitalfix.msusuarios.repository.AppUserRepository;
import com.digitalfix.msusuarios.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * El rol lo asigna el tenant: al iniciar sesion, la base se pone al dia con lo
 * que trae el token, y no hay que volver a asignarlo a mano.
 */
class SincronizacionConElTenantTest {

    private final AppUserRepository usuarios = mock(AppUserRepository.class);
    private final CompanyRepository empresas = mock(CompanyRepository.class);
    private final UsuarioEventoPublisher eventos = mock(UsuarioEventoPublisher.class);
    private final AppUserService servicio = new AppUserService(usuarios, empresas, eventos);

    private AppUser guardado;

    @BeforeEach
    void usuarioQueEntroComoCliente() {
        guardado = new AppUser();
        guardado.setAzureOid("oid-kevin");
        guardado.setEmail("ke.henriquezr@duocuc.cl");
        guardado.setName("Kevin");
        guardado.setRole("CLIENTE");
        guardado.setActive(true);
        guardado.setCompany(new Company());
        when(usuarios.findByAzureOid("oid-kevin")).thenReturn(Optional.of(guardado));
        when(usuarios.save(any(AppUser.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void siElTenantLeCambioElRolSeActualizaYSeAvisa() {
        AppUser resultado = servicio.autoProvision("oid-kevin", "ke.henriquezr@duocuc.cl", "Kevin", "Tecnico");

        assertEquals("TECNICO", resultado.getRole());
        verify(usuarios).save(guardado);
        verify(eventos).publicarCambioRol(guardado);
    }

    @Test
    void sinRolEnElTokenConservaElGuardado() {
        AppUser resultado = servicio.autoProvision("oid-kevin", "ke.henriquezr@duocuc.cl", "Kevin", null);

        assertEquals("CLIENTE", resultado.getRole());
        verify(usuarios, never()).save(any());
        verify(eventos, never()).publicarCambioRol(any());
    }

    @Test
    void conElMismoRolNoGuardaNada() {
        servicio.autoProvision("oid-kevin", "ke.henriquezr@duocuc.cl", "Kevin", "CLIENTE");

        verify(usuarios, never()).save(any());
    }

    @Test
    void tambienPoneAlDiaNombreYCorreoSinAvisarCambioDeRol() {
        AppUser resultado = servicio.autoProvision("oid-kevin", "kevin@duocuc.cl", "Kevin Henriquez", "CLIENTE");

        assertEquals("kevin@duocuc.cl", resultado.getEmail());
        assertEquals("Kevin Henriquez", resultado.getName());
        verify(usuarios).save(guardado);
        verify(eventos, never()).publicarCambioRol(any());
    }
}
