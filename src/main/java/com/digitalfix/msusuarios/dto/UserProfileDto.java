package com.digitalfix.msusuarios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * El perfil que consume el BFF para resolver el contexto de quien llama.
 *
 * companyId es lo que sostiene la multi-tenencia: el token dice quien eres,
 * pero de que empresa eres solo lo sabe esta tabla. companyName es para que
 * la cabecera del frontend pueda mostrarla sin una segunda llamada.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    private String name;
    private String email;
    private String role;
    private Long companyId;
    private String companyName;
    private Boolean active;
}
