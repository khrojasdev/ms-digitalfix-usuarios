package com.digitalfix.msusuarios.messaging;

import com.digitalfix.msusuarios.domain.AppUser;
import com.digitalfix.msusuarios.dto.UsuarioEventoDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Publica eventos de usuario en el exchange topic.
 *
 * El envio es resiliente: si el broker no responde se registra el problema
 * pero NO se cae la operacion de negocio (crear usuario o cambiar su estado).
 */
@Service
public class UsuarioEventoPublisher {

    private static final Logger log = LoggerFactory.getLogger(UsuarioEventoPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${amqp.exchange.usuarios}")
    private String exchange;

    @Value("${amqp.routing-key.usuarios-evento}")
    private String routingKeyBase;

    public UsuarioEventoPublisher(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publicarCreado(AppUser usuario) {
        publicar("CREADO", usuario);
    }

    public void publicarCambioRol(AppUser usuario) {
        publicar("ROL_CAMBIADO", usuario);
    }

    public void publicarCambioEstado(AppUser usuario) {
        publicar(Boolean.TRUE.equals(usuario.getActive()) ? "ACTIVADO" : "DESACTIVADO", usuario);
    }

    private void publicar(String tipoEvento, AppUser usuario) {
        Long empresaId = usuario.getCompany() != null ? usuario.getCompany().getId() : null;
        UsuarioEventoDto evento = new UsuarioEventoDto(
                tipoEvento,
                usuario.getAzureOid(),
                usuario.getEmail(),
                usuario.getRole(),
                empresaId,
                Boolean.TRUE.equals(usuario.getActive()),
                LocalDateTime.now()
        );
        // Routing key por tipo, p.ej. usuarios.eventos.creado
        String routingKey = routingKeyBase + "." + tipoEvento.toLowerCase();
        try {
            String json = objectMapper.writeValueAsString(evento);
            rabbitTemplate.convertAndSend(exchange, routingKey, json);
            log.info("Evento de usuario publicado: {} ({})", tipoEvento, usuario.getAzureOid());
        } catch (JsonProcessingException e) {
            log.error("No se pudo serializar el evento de usuario {}.", usuario.getAzureOid(), e);
        } catch (AmqpException e) {
            log.error("No se pudo publicar el evento de usuario {}: {}",
                    usuario.getAzureOid(), e.getMessage());
        }
    }
}
