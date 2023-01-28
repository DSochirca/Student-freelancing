package nl.tudelft.sem.template.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response<E> {
    private E data;
    private String errorMessage;

    public Response(E data) {
        this.data = data;
    }
}
