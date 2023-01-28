package nl.tudelft.sem.template.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Objects;
import nl.tudelft.sem.template.domain.Response;
import nl.tudelft.sem.template.domain.dtos.requests.FeedbackRequest;
import nl.tudelft.sem.template.domain.dtos.responses.AverageRatingResponse;
import nl.tudelft.sem.template.domain.dtos.responses.FeedbackResponse;
import nl.tudelft.sem.template.services.FeedbackService;
import nl.tudelft.sem.template.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
@AutoConfigureMockMvc
public class FeedbackControllerTest {
    private transient long id = 1;
    private transient String userName = "username";
    private transient String userRole = "STUDENT";
    private transient Long contractId = -1L;

    @Autowired
    private transient FeedbackController feedbackController;
    @MockBean
    private transient FeedbackService feedbackService;
    @MockBean
    private transient UserService userService;
    private transient FeedbackResponse feedbackResponse;
    private transient FeedbackRequest feedbackRequest;

    @BeforeEach
    void setUp() {
        feedbackResponse = new FeedbackResponse("review", 0, "from", "to", contractId);
        feedbackRequest = new FeedbackRequest("review", 0, "from", "to", contractId);
    }

    @Test
    void getByIdTest() {
        when(feedbackService.getById(id)).thenReturn(feedbackResponse);

        ResponseEntity<Response<FeedbackResponse>> expected = feedbackController.getById(id);
        ResponseEntity<Response<FeedbackResponse>> actual = ResponseEntity
            .ok()
            .body(new Response<>(feedbackResponse, null));

        assertEquals(expected, actual);
    }

    @Test
    void createTest() {
        when(feedbackService.create(feedbackRequest, userName, userRole))
            .thenReturn(Pair.of(feedbackResponse, id));

        ResponseEntity<Response<FeedbackResponse>> expected = ResponseEntity
            .created(URI.create("http://feedback-service/" + id))
            .body(new Response<>(feedbackResponse, null));

        ResponseEntity<Response<FeedbackResponse>> actual = feedbackController
            .create(feedbackRequest, userName, userRole);

        assertEquals(expected, actual);
    }

    @Test
    void getRatingTest() {
        when(feedbackService.getAverageRatingByUser(userName)).thenReturn(5.0);
        when(userService.userExists(userName)).thenReturn(true);
        ResponseEntity<Response<AverageRatingResponse>> response;
        response = feedbackController.getUserFeedback(userName);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(5.0, Objects.requireNonNull(response.getBody()).getData().getAverageRating());
    }

    @Test
    void getRatingNonExistentUser() {
        when(feedbackService.getAverageRatingByUser(userName)).thenReturn(5.0);
        when(userService.userExists(userName)).thenReturn(true);
        ResponseEntity<Response<AverageRatingResponse>> response;
        response = feedbackController.getUserFeedback("nonExistentUser");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
