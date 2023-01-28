package nl.tudelft.sem.template.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AverageRatingResponse {
    private double averageRating;
}
