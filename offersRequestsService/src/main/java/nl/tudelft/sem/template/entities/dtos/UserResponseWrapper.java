package nl.tudelft.sem.template.entities.dtos;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserResponseWrapper extends Response<UserResponse> {
    public UserResponseWrapper(UserResponse data, String errorMessage) {
        super(data, errorMessage);
    }
}
