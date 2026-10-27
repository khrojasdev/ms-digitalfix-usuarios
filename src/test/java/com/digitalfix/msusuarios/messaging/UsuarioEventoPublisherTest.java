package com.digitalfix.msusuarios.messaging;

import com.digitalfix.msusuarios.domain.AppUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Verifica que se publica un evento al crear o cambiar el estado de un usuario,
 * sin necesidad de un broker real.
 */
class UsuarioEventoPublisherTest {

    private UsuarioEventoPublisher nuevoPublisher(RabbitTemplate template) {
        UsuarioEventoPublisher publisher = new UsuarioEventoPublisher(template, new ObjectMapper().findAndRegisterModules());
        ReflectionTestUtils.setField(publisher, "exchange", "usuarios.negocio.exchange");
        ReflectionTestUtils.setField(publisher, "routingKeyBase", "usuarios.eventos");
        return publisher;
    }

    private AppUser usuario(boolean activo) {
        AppUser u = new AppUser();
        u.setAzureOid("oid-123");
        u.setEmail("persona@duocuc.cl");
        u.setRole("ADMIN");
        u.setActive(activo);
        return u;
    }

    @Test
    void publicaAlCrearUsuario() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        UsuarioEventoPublisher publisher = nuevoPublisher(template);

        publisher.publicarCreado(usuario(true));

        verify(template, times(1)).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void publicaAlCambiarEstado() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        UsuarioEventoPublisher publisher = nuevoPublisher(template);

        publisher.publicarCambioEstado(usuario(false));

        verify(template, times(1)).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void publicaAlCambiarRol() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        UsuarioEventoPublisher publisher = nuevoPublisher(template);

        publisher.publicarCambioRol(usuario(true));

        verify(template, times(1)).convertAndSend(
                org.mockito.ArgumentMatchers.eq("usuarios.negocio.exchange"),
                org.mockito.ArgumentMatchers.eq("usuarios.eventos.rol_cambiado"),
                anyString());
    }
}
