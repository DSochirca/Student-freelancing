package nl.tudelft.sem.template.domain.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response<E> {
    private E data;
    private String errorMessage;
}

