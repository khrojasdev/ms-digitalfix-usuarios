package com.digitalfix.msusuarios.dto;

import java.time.LocalDateTime;

/**
 * Evento que publica usuarios cuando una cuenta se crea o cambia de estado.
 *
 * tipoEvento: CREADO, ACTIVADO, DESACTIVADO, ROL_CAMBIADO
 */
public record UsuarioEventoDto(
        String tipoEvento,
        String oid,
        String email,
        String rol,
        Long empresaId,
        boolean activo,
        LocalDateTime fecha
) {}
