package nl.tudelft.sem.template.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class UserLoginResponse {
    @Getter
    private String token;
}
