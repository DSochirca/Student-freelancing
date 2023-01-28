package nl.tudelft.sem.template.domain.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponse {
    private String review;
    private int rating;
    private String from;
    private String to;
    private Long contractId;
}
