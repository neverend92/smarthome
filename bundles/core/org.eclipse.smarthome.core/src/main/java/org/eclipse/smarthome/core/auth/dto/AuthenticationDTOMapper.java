package org.eclipse.smarthome.core.auth.dto;

import org.eclipse.smarthome.core.auth.Authentication;

public class AuthenticationDTOMapper {

    /**
     * Maps authentication into DTO object.
     *
     * @param authentication Present authentication.
     * @return Authentication DTO object.
     */
    public static AuthenticationDTO map(Authentication authentication) {
        AuthenticationDTO dto = new AuthenticationDTO();
        dto.username = authentication.getUsername();
        dto.roles = authentication.getRoles();
        return dto;
    }

}
