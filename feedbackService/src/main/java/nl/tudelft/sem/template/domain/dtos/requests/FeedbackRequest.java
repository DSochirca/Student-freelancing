package nl.tudelft.sem.template.domain.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackRequest {
    private String review;
    private int rating;
    private String from;
    private String to;
    private Long contractId;
}
