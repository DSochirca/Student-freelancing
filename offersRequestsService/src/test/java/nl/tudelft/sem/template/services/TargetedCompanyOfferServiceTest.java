package nl.tudelft.sem.template.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import nl.tudelft.sem.template.entities.Offer;
import nl.tudelft.sem.template.entities.StudentOffer;
import nl.tudelft.sem.template.entities.TargetedCompanyOffer;
import nl.tudelft.sem.template.entities.dtos.AverageRatingResponse;
import nl.tudelft.sem.template.entities.dtos.AverageRatingResponseWrapper;
import nl.tudelft.sem.template.entities.dtos.Response;
import nl.tudelft.sem.template.enums.Status;
import nl.tudelft.sem.template.exceptions.UserNotAuthorException;
import nl.tudelft.sem.template.repositories.OfferRepository;
import nl.tudelft.sem.template.repositories.StudentOfferRepository;
import nl.tudelft.sem.template.repositories.TargetedCompanyOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class TargetedCompanyOfferServiceTest {

    @Autowired
    private transient TargetedCompanyOfferService targetedCompanyOfferService;

    @MockBean
    private transient StudentOfferRepository studentOfferRepository;

    @MockBean
    private transient OfferRepository offerRepository;

    @MockBean
    private transient TargetedCompanyOfferRepository targetedCompanyOfferRepository;

    @MockBean
    private transient RestTemplate restTemplate;

    private transient StudentOffer studentOffer;
    private transient TargetedCompanyOffer targetedCompanyOffer;
    private transient List<String> expertise;
    private transient String student;
    private transient String company;

    @BeforeEach
    void setup() {
        expertise = Arrays.asList("Expertise 1", "Expertise 2", "Expertise 3");
        student = "Student";
        company = "Company";
        studentOffer = new StudentOffer("This is a title", "This is a description", 20, 520,
            expertise, Status.DISABLED,
            32, student);

        targetedCompanyOffer = new TargetedCompanyOffer("This is a company title",
            "This is a company description",
            20, 520, expertise, Status.DISABLED,
            Arrays.asList("Requirement 1", "Requirement 2", "Requirement 3"),
            company, null);
    }

    TargetedCompanyOffer createCompanyOffer2(StudentOffer studentOffer) {
        return new TargetedCompanyOffer(
                "This is a company title", "This is a company description",
                20, 520, expertise, Status.DISABLED,
                Arrays.asList("Requirement 1", "Requirement 2", "Requirement 3"),
                company, studentOffer);
    }

    @Test
    void getOffersByIdTestFail() {
        Mockito.when(targetedCompanyOfferRepository.findAllByCompanyId(any()))
            .thenReturn(new ArrayList<>());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> targetedCompanyOfferService.getOffersById("CompanyOne"));
        String message = "No such company has made offers!";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void getOffersByIdTestPass() {
        List<Offer> returned = new ArrayList<>();
        returned.add(targetedCompanyOffer);
        Mockito.when(targetedCompanyOfferRepository.findAllByCompanyId("MyCompany"))
            .thenReturn(returned);

        assertEquals(returned, targetedCompanyOfferService.getOffersById("MyCompany"));
    }

    @Test
    void getOffersByStudentTestPass() {
        List<Offer> returned = new ArrayList<>();
        returned.add(targetedCompanyOffer);

        Mockito.when(studentOfferRepository.getById(any()))
            .thenReturn(studentOffer);

        Mockito.when(targetedCompanyOfferRepository.findAllByStudentOffer(studentOffer))
            .thenReturn(returned);
        try {
            assertEquals(returned,
                targetedCompanyOfferService.getOffersByStudentOffer(studentOffer.getId(), student));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void getOffersByStudentTestFailNull() {
        Mockito.when(studentOfferRepository.getById(any()))
            .thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> targetedCompanyOfferService
                .getOffersByStudentOffer(studentOffer.getId(), student));
        String message = "Student offer does not exist";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void getTargetedByStudentTest() {
        Mockito.when(targetedCompanyOfferRepository.getAllByStudent(student))
            .thenReturn(List.of(targetedCompanyOffer));
        List<TargetedCompanyOffer> result = targetedCompanyOfferService
            .getAllByStudent(student);
        assertEquals(List.of(targetedCompanyOffer), result);

    }

    @Test
    void getOffersByStudentOfferNotAuthorTest() {
        Mockito.when(studentOfferRepository.getById(3L))
                .thenReturn(studentOffer);
        UserNotAuthorException exception = assertThrows(UserNotAuthorException.class,
                () -> targetedCompanyOfferService
                        .getOffersByStudentOffer(3L, "fake"));
        String message = "User with id fake is not the author of this offer";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void saveTargetedCompanyOfferWithResponseValidTest() {
        AverageRatingResponse fakeResponse = new AverageRatingResponse(5.0);
        AverageRatingResponseWrapper responseWrapper = new AverageRatingResponseWrapper();
        responseWrapper.setData(fakeResponse);
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.any()))
                .thenReturn(responseWrapper);

        TargetedCompanyOffer targetedCompanyOffer2 = createCompanyOffer2(studentOffer);
        studentOffer.setId(33L);
        Mockito.when(studentOfferRepository.getById(33L))
                .thenReturn(studentOffer);
        Mockito.when(offerRepository.save(targetedCompanyOffer))
                .thenReturn(targetedCompanyOffer2);

        ResponseEntity<Response<Offer>> response = targetedCompanyOfferService
                .saveOfferWithResponse(targetedCompanyOffer, 33L);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(targetedCompanyOffer2, Objects.requireNonNull(response.getBody()).getData());
    }

    @Test
    void saveTargetedCompanyOfferWithResponseNoStudentOfferTest() {
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.any()))
                .thenReturn(new AverageRatingResponse(5.0));

        TargetedCompanyOffer targetedCompanyOffer2 = createCompanyOffer2(studentOffer);
        Mockito.when(offerRepository.save(targetedCompanyOffer))
                .thenReturn(targetedCompanyOffer2);

        ResponseEntity<Response<Offer>> response = targetedCompanyOfferService
                .saveOfferWithResponse(targetedCompanyOffer, 69L);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(Objects.requireNonNull(response.getBody()).getData());
    }

    @Test
    void saveTargetedCompanyOfferWithResponseLowRatingTest() {
        AverageRatingResponse fakeResponse = new AverageRatingResponse(1.0);
        AverageRatingResponseWrapper responseWrapper = new AverageRatingResponseWrapper();
        responseWrapper.setData(fakeResponse);
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.any()))
                .thenReturn(responseWrapper);

        TargetedCompanyOffer targetedCompanyOffer2 = createCompanyOffer2(studentOffer);
        studentOffer.setId(33L);
        Mockito.when(studentOfferRepository.getById(33L))
                .thenReturn(studentOffer);
        Mockito.when(offerRepository.save(targetedCompanyOffer))
                .thenReturn(targetedCompanyOffer2);

        ResponseEntity<Response<Offer>> response = targetedCompanyOfferService
                .saveOfferWithResponse(targetedCompanyOffer, 33L);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(Objects.requireNonNull(response.getBody()).getData());
    }

    @Test
    void saveTargetedCompanyOfferWithResponseServiceUnavailableTest() {
        TargetedCompanyOffer targetedCompanyOffer2 = createCompanyOffer2(studentOffer);
        studentOffer.setId(33L);
        Mockito.when(studentOfferRepository.getById(33L))
                .thenReturn(studentOffer);
        Mockito.when(offerRepository.save(targetedCompanyOffer))
                .thenReturn(targetedCompanyOffer2);

        ResponseEntity<Response<Offer>> response = targetedCompanyOfferService
                .saveOfferWithResponse(targetedCompanyOffer, 33L);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNull(Objects.requireNonNull(response.getBody()).getData());
    }

}