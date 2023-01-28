package nl.tudelft.sem.template.controllers;

import java.net.URI;
import nl.tudelft.sem.template.domain.Response;
import nl.tudelft.sem.template.domain.dtos.requests.FeedbackRequest;
import nl.tudelft.sem.template.domain.dtos.responses.AverageRatingResponse;
import nl.tudelft.sem.template.domain.dtos.responses.FeedbackResponse;
import nl.tudelft.sem.template.services.FeedbackService;
import nl.tudelft.sem.template.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeedbackController {
    @Autowired
    private transient FeedbackService feedbackService;

    @Autowired
    private transient UserService userService;

    /**
     * Get Feedback by id.
     *
     * @param id The id of the feedback.
     * @return The feedback.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Response<FeedbackResponse>> getById(@PathVariable Long id) {
        try {
            FeedbackResponse res = feedbackService.getById(id);

            return ResponseEntity
                .ok(new Response<>(res, null));
        } catch (Exception e) {
            return ResponseEntity
                .badRequest()
                .body(new Response<>(null, e.getMessage()));
        }
    }

    /**
     * Create new feedback for user.
     *
     * @param feedbackRequest The request containing the feedback.
     * @param userName The name of the user making the feedback, inserted by Eureka.
     * @param userRole The role of the user making the feedback, inserted by Eureka.
     * @return Created feedback.
     */
    @PostMapping("/create")
    public ResponseEntity<Response<FeedbackResponse>> create(
        @RequestBody FeedbackRequest feedbackRequest,
        @RequestHeader("x-user-name") String userName,
        @RequestHeader("x-user-role") String userRole
    ) {
        if (userRole == null || userRole.isEmpty()) {
            return ResponseEntity
                .badRequest()
                .body(new Response<>(null, "Missing user-role header"));
        }

        try {
            Pair<FeedbackResponse, Long> res =
                feedbackService.create(feedbackRequest, userName, userRole);

            String uri = "http://feedback-service/" + res.getSecond();

            return ResponseEntity
                .created(URI.create(uri))
                .body(new Response<>(res.getFirst(), null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response<>(null, e.getMessage()));
        }
    }

    /**
     * Get the average rating of a given user.
     *
     * @param userName The name of the user.
     * @return The average rating of the user.
     */
    @GetMapping("/user/{userName}")
    public ResponseEntity<Response<AverageRatingResponse>> getUserFeedback(
            @PathVariable String userName) {
        // First check if the user actually exists
        if (!userService.userExists(userName)) {
            return ResponseEntity.notFound().build();
        }

        double avgRating = feedbackService.getAverageRatingByUser(userName);
        return ResponseEntity.ok(new Response<>(new AverageRatingResponse(avgRating), null));
    }
}
