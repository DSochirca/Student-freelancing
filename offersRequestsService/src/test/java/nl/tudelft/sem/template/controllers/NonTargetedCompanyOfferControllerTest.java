package nl.tudelft.sem.template.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import javax.naming.NoPermissionException;
import nl.tudelft.sem.template.entities.Application;
import nl.tudelft.sem.template.entities.NonTargetedCompanyOffer;
import nl.tudelft.sem.template.entities.Offer;
import nl.tudelft.sem.template.entities.dtos.ContractDto;
import nl.tudelft.sem.template.entities.dtos.Response;
import nl.tudelft.sem.template.enums.Status;
import nl.tudelft.sem.template.exceptions.ContractCreationException;
import nl.tudelft.sem.template.exceptions.LowRatingException;
import nl.tudelft.sem.template.exceptions.UpstreamServiceException;
import nl.tudelft.sem.template.services.NonTargetedCompanyOfferService;
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


@SpringBootTest
@AutoConfigureMockMvc
class NonTargetedCompanyOfferControllerTest {

    @Autowired
    private transient NonTargetedCompanyOfferController offerController;

    @MockBean
    private transient NonTargetedCompanyOfferService offerService;

    private transient NonTargetedCompanyOffer offer;
    private transient Application application;
    private transient String student;
    private transient String company;
    private transient String studentRole;
    private transient String companyRole;
    private transient ContractDto contract;


    @BeforeEach
    void setup() {
        final List<String> expertise = List.of("e1", "e2", "e3");
        final List<String> requirements = List.of("r1", "r2", "r3");
        student = "student";
        company = "facebook";
        studentRole = "STUDENT";
        companyRole = "COMPANY";
        offer = new NonTargetedCompanyOffer("title", "description",
                20, 520, expertise,
                null, requirements, company);
        application = new Application(student, 5, Status.PENDING, offer);

        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = startDate.plusWeeks(
                (long) Math.ceil(offer.getTotalHours() / offer.getHoursPerWeek()));

        contract = new ContractDto(1L, company, student, startDate, endDate,
                offer.getHoursPerWeek(), offer.getTotalHours(),
                application.getPricePerHour(), "ACTIVE");
    }

    @Test
    void saveOfferNotSameAuthorTest() {
        String errorMessage = "User can not make this offer";
        ResponseEntity<Response<Offer>> response = offerController
                .createNonTargetedCompanyOffer("google", offer);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(errorMessage,
                Objects.requireNonNull(response.getBody()).getErrorMessage());
    }

    @Test
    void saveOfferValidTest() throws LowRatingException, UpstreamServiceException {
        Mockito.when(offerService.saveOfferWithResponse(offer)).thenReturn(
                new ResponseEntity<>(new Response<>(offer), HttpStatus.CREATED)
        );
        ResponseEntity<Response<Offer>> response = offerController
                .createNonTargetedCompanyOffer(company, offer);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(new Response<>(offer), response.getBody());
    }

    @Test
    void saveOfferIllegalTest() throws LowRatingException, UpstreamServiceException {
        String error = "error";
        Mockito.when(offerService.saveOfferWithResponse(offer)).thenReturn(
                new ResponseEntity<>(new Response<>(null, error), HttpStatus.BAD_REQUEST)
        );

        ResponseEntity<Response<Offer>> response = offerController
                .createNonTargetedCompanyOffer(company, offer);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(error,
                Objects.requireNonNull(response.getBody()).getErrorMessage());
    }

    @Test
    void applyNotAuthenticatedTest() {
        String errorMessage = "User has not been authenticated";
        ResponseEntity<Response<Application>> response = offerController
                .apply("", "", application, 3L);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(errorMessage,
                Objects.requireNonNull(response.getBody()).getErrorMessage());
    }

    @Test
    void applyNotAuthorTest() {
        String errorMessage = "User can not make this application";
        ResponseEntity<Response<Application>> response = offerController
                .apply("fake", studentRole, application, 3L);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(errorMessage,
                Objects.requireNonNull(response.getBody()).getErrorMessage());
    }

    @Test
    void applyNotStudentTest() {
        String errorMessage = "User can not make this application";
        ResponseEntity<Response<Application>> response = offerController
                .apply(student, companyRole, application, 3L);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(errorMessage,
                Objects.requireNonNull(response.getBody()).getErrorMessage());
    }

    @Test
    void applyValidTest() {
        Mockito.when(offerService.apply(application, 3L))
                .thenReturn(application);
        ResponseEntity<Response<Application>> response = offerController
                .apply(student, studentRole, application, 3L);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(new Response<>(application), response.getBody());
    }

    @Test
    void applyIllegalTest() {
        String error = "error";
        Mockito.when(offerService.apply(application, 3L))
                .thenThrow(new IllegalArgumentException(error));

        ResponseEntity<Response<Application>> response = offerController
                .apply(student, studentRole, application, 3L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(error,
                Objects.requireNonNull(response.getBody()).getErrorMessage());
    }

    @Test
    void acceptApplicationTest() throws NoPermissionException, ContractCreationException {
        Mockito.when(offerService.accept(company, application.getId()))
                .thenReturn(contract);

        ResponseEntity<Response<ContractDto>> res
                = offerController
                .acceptApplication(company, application.getId());
        Response<ContractDto> response =
                new Response<>(contract, "Application has been accepted successfully!");

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void acceptApplicationTestFailIllegalArgument()
            throws NoPermissionException, ContractCreationException {
        String message = "There is no offer associated with this application!";
        Mockito
                .doThrow(new IllegalArgumentException(
                        message))
                .when(offerService).accept(company, application.getId());

        ResponseEntity<Response<ContractDto>> res
                = offerController
                .acceptApplication(company, application.getId());
        Response<ContractDto> response =
                new Response<>(null, message);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

    @Test
    void acceptApplicationTestFailNoPermission()
            throws NoPermissionException, ContractCreationException {
        String message = "User can not accept this application!";
        Mockito
                .doThrow(new NoPermissionException(
                        message))
                .when(offerService).accept(company, application.getId());

        ResponseEntity<Response<ContractDto>> res
                = offerController
                .acceptApplication(company, application.getId());
        Response<ContractDto> response =
                new Response<>(null, message);

        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        assertEquals(response, res.getBody());
    }

}