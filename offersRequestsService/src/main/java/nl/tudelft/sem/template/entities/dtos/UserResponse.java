package nl.tudelft.sem.template.entities.dtos;

import lombok.Data;

@Data
public class UserResponse {
    private String username;
    private String password;
    private Role role;
}


