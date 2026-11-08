package dto;

import domain.Role;

public record MeResponse(String username, Role role) {
}
