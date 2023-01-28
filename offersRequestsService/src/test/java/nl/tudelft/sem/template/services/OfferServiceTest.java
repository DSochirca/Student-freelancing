package nl.tudelft.sem.template.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import logger.FileLogger;
import nl.tudelft.sem.template.entities.Offer;
import nl.tudelft.sem.template.entities.StudentOffer;
import nl.tudelft.sem.template.entities.TargetedCompanyOffer;
import nl.tudelft.sem.template.entities.dtos.AverageRatingResponse;
import nl.tudelft.sem.template.entities.dtos.AverageRatingResponseWrapper;
import nl.tudelft.sem.template.entities.dtos.Response;
import nl.tudelft.sem.template.enums.Status;
import nl.tudelft.sem.template.exceptions.LowRatingException;
import nl.tudelft.sem.template.exceptions.UpstreamServiceException;
import nl.tudelft.sem.template.repositories.OfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
class OfferServiceTest {

    @Autowired
    private transient OfferService offerService;

    @MockBean
    private transient OfferRepository offerRepository;

    @MockBean
    private transient RestTemplate restTemplate;

    @MockBean
    private transient FileLogger fileLogger;

    private transient StudentOffer studentOffer;
    private transient TargetedCompanyOffer targetedCompanyOffer;
    private transient String student;

    @BeforeEach
    void setup() {
        student = "Student";
        List<String> expertise = Arrays.asList("Expertise 1", "Expertise 2", "Expertise 3");
        studentOffer = new StudentOffer("This is a title", "This is a description",
            20, 520,
            Arrays.asList("Expertise 1", "Expertise 2", "Expertise 3"), Status.DISABLED,
            32, student);
        targetedCompanyOffer = new TargetedCompanyOffer("This is a company title",
            "This is a company description",
            20, 520, expertise, Status.DISABLED,
            Arrays.asList("Requirement 1", "Requirement 2", "Requirement 3"),
            "Company", null);
    }

    @Test
    void saveOfferValidTest() throws LowRatingException, UpstreamServiceException {
        Mockito.when(offerRepository.save(studentOffer))
            .thenReturn(studentOffer);
        AverageRatingResponse fakeResponse = new AverageRatingResponse(5.0);
        AverageRatingResponseWrapper responseWrapper = new AverageRatingResponseWrapper();
        responseWrapper.setData(fakeResponse);
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.any()))
                .thenReturn(responseWrapper);

        assertEquals(studentOffer, offerService.saveOffer(studentOffer));
    }

    @Test
    void saveOfferPendingTest() throws LowRatingException, UpstreamServiceException {
        AverageRatingResponse fakeResponse = new AverageRatingResponse(5.0);
        AverageRatingResponseWrapper responseWrapper = new AverageRatingResponseWrapper();
        responseWrapper.setData(fakeResponse);
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.any()))
                .thenReturn(responseWrapper);

        studentOffer = Mockito.mock(StudentOffer.class);
        Mockito.when(offerRepository.save(studentOffer))
                        .thenReturn(studentOffer);
        offerService.saveOffer(studentOffer);

        Mockito.verify(studentOffer).setStatus(Status.PENDING);
    }

    @Test
    void saveOfferTooManyHoursTest() {
        studentOffer.setHoursPerWeek(21);
        String errorMessage = "Offer exceeds 20 hours per week";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> offerService.saveOffer(studentOffer));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void saveOfferTooLongDurationTest() {
        studentOffer.setTotalHours(521);
        String errorMessage = "Offer exceeds 6 month duration";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> offerService.saveOffer(studentOffer), errorMessage);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void getAllByUsernameTest() {
        StudentOffer studentOffer2 = new StudentOffer("This is a second student offer title",
            "This is a second student offer description",
            20, 520,
            Arrays.asList("Expertise 1", "Expertise 2", "Expertise 3"), Status.DISABLED,
            32, student);
        targetedCompanyOffer.setStudentOffer(studentOffer);
        Mockito.when(offerRepository.getAllByUsername(student))
            .thenReturn(Arrays.asList(studentOffer, studentOffer2, targetedCompanyOffer));
        Map<String, List<Offer>> expected = new HashMap<>();
        expected.put("studentOffers", Arrays.asList(studentOffer, studentOffer2));
        expected.put("targetedCompanyOffers", List.of(targetedCompanyOffer));

        assertEquals(expected, offerService.getAllByUsername(student));
    }

    @Test
    void saveOfferWithResponseNoErrorTest() {
        Mockito.when(offerRepository.save(studentOffer))
                .thenReturn(studentOffer);

        AverageRatingResponse fakeResponse = new AverageRatingResponse(5.0);
        AverageRatingResponseWrapper responseWrapper = new AverageRatingResponseWrapper();
        responseWrapper.setData(fakeResponse);
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.any()))
                .thenReturn(responseWrapper);

        ResponseEntity<Response<Offer>> response = offerService.saveOfferWithResponse(studentOffer);
        System.out.println(response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(studentOffer, Objects.requireNonNull(response.getBody()).getData());
    }

    @Test
    void saveOfferWithResponseLowRatingTest() {
        Mockito.when(offerRepository.save(studentOffer))
                .thenReturn(studentOffer);

        AverageRatingResponse fakeResponse = new AverageRatingResponse(1.2);
        AverageRatingResponseWrapper responseWrapper = new AverageRatingResponseWrapper();
        responseWrapper.setData(fakeResponse);
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.any()))
                .thenReturn(responseWrapper);

        ResponseEntity<Response<Offer>> response = offerService.saveOfferWithResponse(studentOffer);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(Objects.requireNonNull(response.getBody()).getData());
    }

    @Test
    void saveOfferWithResponseUpstreamUnavailableTest() {
        Mockito.when(offerRepository.save(studentOffer))
                .thenReturn(studentOffer);
        ResponseEntity<Response<Offer>> response = offerService.saveOfferWithResponse(studentOffer);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNull(Objects.requireNonNull(response.getBody()).getData());
    }

    @Test
    void saveOfferWithResponseTooManyHoursTest() {
        studentOffer.setTotalHours(521);
        Mockito.when(offerRepository.save(studentOffer))
                .thenReturn(studentOffer);
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.any()))
                .thenReturn(new AverageRatingResponse(4.5));
        ResponseEntity<Response<Offer>> response = offerService.saveOfferWithResponse(studentOffer);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(Objects.requireNonNull(response.getBody()).getData());
    }


}