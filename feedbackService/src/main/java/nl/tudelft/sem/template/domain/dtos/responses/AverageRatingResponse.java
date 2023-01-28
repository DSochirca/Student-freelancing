package nl.tudelft.sem.template.domain.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AverageRatingResponse {
    private double averageRating;
}
