package nl.tudelft.sem.template.domain.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.domain.dtos.enums.UserRole;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String username;
    private String password;
    private UserRole role;
}
