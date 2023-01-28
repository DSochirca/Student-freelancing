package nl.tudelft.sem.template.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.naming.NoPermissionException;
import nl.tudelft.sem.template.entities.Offer;
import nl.tudelft.sem.template.entities.StudentOffer;
import nl.tudelft.sem.template.entities.TargetedCompanyOffer;
import nl.tudelft.sem.template.entities.dtos.ContractDto;
import nl.tudelft.sem.template.entities.dtos.Response;
import nl.tudelft.sem.template.enums.Status;
import nl.tudelft.sem.template.exceptions.ContractCreationException;
import nl.tudelft.sem.template.exceptions.LowRatingException;
import nl.tudelft.sem.template.exceptions.UpstreamServiceException;
import nl.tudelft.sem.template.exceptions.UserDoesNotExistException;
import nl.tudelft.sem.template.exceptions.UserServiceUnvanvailableException;
import nl.tudelft.sem.template.services.StudentOfferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
@AutoConfigureMockMvc
class StudentOfferControllerTest {

    @Autowired
    private transient StudentOfferController studentOfferController;

    @MockBean
    private transient StudentOfferService studentOfferService;

    private transient StudentOffer studentOffer;
    private transient String student;
    private transient String studentRole;
    private transient String company;
    private transient String companyRole;
    private transient String unauthenticated;
    private transient List<String> expertise;
    private transient TargetedCompanyOffer targetedCompanyOffer;
    private transient ContractDto contract;

    @BeforeEach
    void setup() {
        student = "Student";
        studentRole = "STUDENT";
        company = "Our Company";
        companyRole = "COMPANY";
        unauthenticated = "User has not been authenticated";
        expertise = Arrays.asList("Expertise 1", "Expertise 2", "Expertise 3");
        studentOffer = new StudentOffer("This is a title",
            "This is a description", 20, 520,
            expertise, Status.DISABLED,
            32, student);
        targetedCompanyOffer = new TargetedCompanyOffer("Ben's services", "Hey I'm Ben",
                15, 150,
                Arrays.asList("Singing", "Web Dev", "Care-taking"), Status.PENDING,
                Arrays.asList("Singing", "Web Dev", "Care-taking"),
                company, studentOffer);

        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = startDate.plusWeeks(
                (long) Math.ceil(studentOffer.getTotalHours() / studentOffer.getHoursPerWeek()));

        contract = new ContractDto(1L, targetedCompanyOffer.getCompanyId(), student,
                startDate, endDate, targetedCompanyOffer.getHoursPerWeek(),
                targetedCompanyOffer.getTotalHours(),
                studentOffer.getPricePerHour(), "ACTIVE");
    }


    @Test
    void saveStudentOfferValid() throws LowRatingException, UpstreamServiceException {
        Offer studentOffer2 = new StudentOffer("This is a title",
            "This is a description", 20,
            520, Arrays.asList("Expertise 1", "Expertise 2", "Expertise 3"), Status.PENDING,
            32, "Student");
        Mockito.when(studentOfferService.saveOfferWithResponse(studentOffer))
            .thenReturn(new ResponseEntity<>(new Response<>(studentOffer2), HttpStatus.CREATED));

        Response<Offer> res = new Response<>(studentOffer2, null);

        ResponseEntity<Response<Offer>> response
                = studentOfferController.saveStudentOffer(student, studentOffer);
        assertEquals(res, response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void saveStudentOfferIllegal() throws LowRatingException, UpstreamServiceException {
        studentOffer.setHoursPerWeek(21);
        String errorMessage = "Offer exceeds 20 hours per week";
        Mockito.when(studentOfferService.saveOfferWithResponse(studentOffer)).thenReturn(
            new ResponseEntity<>(new Response<>(null, errorMessage), HttpStatus.BAD_REQUEST)
        );

        ResponseEntity<Response<Offer>> response
                = studentOfferController.saveStudentOffer(student, studentOffer);

        Response<Offer> errorResponse = new Response<>(null, errorMessage);
        assertEquals(errorResponse, response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getAllStudentOffersTest() {
        List<StudentOffer> list = List.of(studentOffer);
        Mockito.when(studentOfferService.getOffers())
            .thenReturn(list);

        ResponseEntity<Response<List<StudentOffer>>> response
                = studentOfferController.getAllStudentOffers();
        Response<List<StudentOffer>> offersRespond =
                new Response<>(list, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(offersRespond, response.getBody());
    }

    @Test
    void getStudentOffersByIdValidTest() {
        List<Offer> studentOffers = List.of(studentOffer);
        try {
            Mockito.when(studentOfferService.getOffersById(student))
                    .thenReturn(studentOffers);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ResponseEntity<Response<List<Offer>>> response
                = studentOfferController.getStudentOffersById(student);
        Response<List<Offer>> offersRespond =
                new Response<>(studentOffers, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(offersRespond, response.getBody());
    }

    @Test
    void getStudentOffersByIdIllegalTest() {
        String errorMessage = "Error";
        try {
            Mockito.when(studentOfferService.getOffersById(student))
                .thenThrow(new IllegalArgumentException(errorMessage));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ResponseEntity<Response<List<Offer>>> response
                = studentOfferController.getStudentOffersById(student);
        Response<List<Offer>> responseError =
                new Response<>(null, errorMessage);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(responseError, response.getBody());
    }

    @Test
    void editStudentOfferTest() {
        Mockito.doNothing()
                .when(studentOfferService)
                .updateStudentOffer(studentOffer);

        ResponseEntity<Response<String>> res
                = studentOfferController.editStudentOffer(studentOffer, student, studentRole);
        Response<String> response =
                new Response<>("Student Offer has been updated successfully!", null);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void editStudentOfferTestFailUserName() {
        student = "";
        ResponseEntity<Response<String>> res
                = studentOfferController.editStudentOffer(studentOffer, student, studentRole);
        Response<String> response =
                new Response<>(null, "User has not been authenticated");

        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void editStudentOfferTestFailRole() {
        ResponseEntity<Response<String>> res
                = studentOfferController.editStudentOffer(studentOffer, student, companyRole);
        Response<String> response =
                new Response<>(null, "User is not allowed to edit this offer");

        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void editStudentOfferTestFailIllegalArgument() {
        Mockito
                .doThrow(new IllegalArgumentException("You are not allowed to edit the Status"))
                .when(studentOfferService).updateStudentOffer(studentOffer);

        ResponseEntity<Response<String>> res
                = studentOfferController.editStudentOffer(studentOffer, student, studentRole);
        Response<String> response =
                new Response<>(null, "You are not allowed to edit the Status");

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void saveStudentOfferNotAuthorTest() {
        ResponseEntity<Response<Offer>> response = studentOfferController
                .saveStudentOffer("fake", studentOffer);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("User not allowed to post this StudentOffer",
                Objects.requireNonNull(response.getBody()).getErrorMessage());
    }

    @Test
    void saveStudentOfferNotStudentTest() {
        ResponseEntity<Response<Offer>> response = studentOfferController
                .saveStudentOffer("fake", studentOffer);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("User not allowed to post this StudentOffer",
                Objects.requireNonNull(response.getBody()).getErrorMessage());
    }

    @Test
    void acceptTargetedOfferTest() throws NoPermissionException, ContractCreationException {
        Mockito.when(studentOfferService
                .acceptOffer(student, targetedCompanyOffer.getId()))
                .thenReturn(contract);

        ResponseEntity<Response<ContractDto>> res
                = studentOfferController
                .acceptTargetedOffer(student, targetedCompanyOffer.getId());
        Response<ContractDto> response =
                new Response<>(contract, "The Company Offer was accepted successfully!");

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void acceptTargetedOfferTestFailIllegalArgument()
            throws NoPermissionException, ContractCreationException {
        String message = "The StudentOffer or TargetedRequest is not active anymore!";
        Mockito
                .doThrow(new IllegalArgumentException(
                        message))
                .when(studentOfferService)
                .acceptOffer(student, targetedCompanyOffer.getId());

        ResponseEntity<Response<ContractDto>> res
                = studentOfferController
                .acceptTargetedOffer(student, targetedCompanyOffer.getId());
        Response<ContractDto> response =
                new Response<>(null, message);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void acceptTargetedOfferTestFailNoPermission()
            throws NoPermissionException, ContractCreationException {
        String message = "User not allowed to accept this TargetedOffer";
        Mockito
                .doThrow(new NoPermissionException(
                        message))
                .when(studentOfferService)
                .acceptOffer(company, targetedCompanyOffer.getId());

        ResponseEntity<Response<ContractDto>> res
                = studentOfferController
                .acceptTargetedOffer(company, targetedCompanyOffer.getId());
        Response<ContractDto> response =
                new Response<>(null, message);

        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void getStudentOffersByIdUnavailableTest() {
        try {
            Mockito.when(studentOfferService.getOffersById(student))
                    .thenThrow(new UserServiceUnvanvailableException("test"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ResponseEntity<Response<List<Offer>>> response =
                studentOfferController.getStudentOffersById(student);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("test",
                Objects.requireNonNull(response.getBody()).getErrorMessage());
    }

    @Test
    void getStudentOffersByIdUserNotExistTest() {
        try {
            Mockito.when(studentOfferService.getOffersById(student))
                    .thenThrow(new UserDoesNotExistException("error"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        ResponseEntity<Response<List<Offer>>> response =
                studentOfferController.getStudentOffersById(student);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("error",
                Objects.requireNonNull(response.getBody()).getErrorMessage());
    }

    @Test
    void getOffersByKeyWordTestFailUserName() {
        ResponseEntity<Response<List<StudentOffer>>> res
                = studentOfferController
                .getOffersByKeyWord("Word", "", companyRole);
        Response<List<StudentOffer>> response =
                new Response<>(null, unauthenticated);

        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void getOffersByKeyWordTestRole() {
        ResponseEntity<Response<List<StudentOffer>>> res
                = studentOfferController
                .getOffersByKeyWord("title", company, studentRole);
        Response<List<StudentOffer>> response =
                new Response<>(null, "User is not allowed to see Student Offers!");

        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void getOffersByKeyWordTest() throws UnsupportedEncodingException {
        Mockito.when(studentOfferService.getByKeyWord("student"))
                .thenReturn(List.of(studentOffer));

        ResponseEntity<Response<List<StudentOffer>>> res
                = studentOfferController
                .getOffersByKeyWord("student", company, companyRole);
        Response<List<StudentOffer>> response =
                new Response<>(List.of(studentOffer), null);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void getOffersByKeyWordTestFailEncoding() throws UnsupportedEncodingException {
        Mockito
                .doThrow(new UnsupportedEncodingException())
                .when(studentOfferService)
                .getByKeyWord(any());
        ResponseEntity<Response<List<StudentOffer>>> res
                = studentOfferController
                .getOffersByKeyWord("explore", company, companyRole);
        Response<List<StudentOffer>> response =
                new Response<>(null, "Keyword is invalid!");

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void getOffersByExpertisesTestFailUserName() {
        ResponseEntity<Response<List<StudentOffer>>> res
                = studentOfferController
                .getOffersByExpertises(expertise, "", companyRole);
        Response<List<StudentOffer>> response =
                new Response<>(null, unauthenticated);

        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void getOffersByExpertisesTestRole() {
        ResponseEntity<Response<List<StudentOffer>>> res
                = studentOfferController
                .getOffersByExpertises(expertise, company, studentRole);
        Response<List<StudentOffer>> response =
                new Response<>(null, "User is not allowed to see Student Offers!");

        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void getOffersByExpertisesTest() throws UnsupportedEncodingException {
        Mockito.when(studentOfferService.getByExpertises(expertise))
                .thenReturn(List.of(studentOffer));

        ResponseEntity<Response<List<StudentOffer>>> res
                = studentOfferController
                .getOffersByExpertises(expertise, company, companyRole);
        Response<List<StudentOffer>> response =
                new Response<>(List.of(studentOffer), null);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void getOffersByExpertisesTestFailEncoding() throws UnsupportedEncodingException {
        Mockito
                .doThrow(new UnsupportedEncodingException())
                .when(studentOfferService)
                .getByExpertises(any());
        ResponseEntity<Response<List<StudentOffer>>> res
                = studentOfferController
                .getOffersByExpertises(expertise, company, companyRole);
        Response<List<StudentOffer>> response =
                new Response<>(null, "An expertise is invalid!");

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

}