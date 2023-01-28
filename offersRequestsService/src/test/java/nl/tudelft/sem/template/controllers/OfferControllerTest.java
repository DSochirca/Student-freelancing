package nl.tudelft.sem.template.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import nl.tudelft.sem.template.entities.NonTargetedCompanyOffer;
import nl.tudelft.sem.template.entities.Offer;
import nl.tudelft.sem.template.entities.StudentOffer;
import nl.tudelft.sem.template.entities.TargetedCompanyOffer;
import nl.tudelft.sem.template.entities.dtos.ContractDto;
import nl.tudelft.sem.template.entities.dtos.OfferDto;
import nl.tudelft.sem.template.entities.dtos.Response;
import nl.tudelft.sem.template.enums.Status;
import nl.tudelft.sem.template.services.OfferService;
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
class OfferControllerTest {

    @Autowired
    private transient OfferController offerController;

    @MockBean
    private transient OfferService offerService;
    @MockBean
    private transient StudentOfferController studentController;
    @MockBean
    private transient TargetedCompanyOfferController targetedController;
    @MockBean
    private transient NonTargetedCompanyOfferController nonTargetedController;

    private transient StudentOffer studentOffer;
    private transient TargetedCompanyOffer targetedCompanyOffer;
    private transient String student;
    private transient String company;
    private transient String studentRole;
    private transient String companyRole;
    private transient String unauthenticatedMessage;
    private transient String invalidRole;
    private transient OfferDto offerDto;

    @BeforeEach
    void setup() {
        companyRole = "COMPANY";
        unauthenticatedMessage = "User is not authenticated";
        invalidRole = "User has invalid Role";
        student = "Student";
        company = "Company";
        studentRole = "STUDENT";
        List<String> expertise = Arrays.asList("Expertise 1", "Expertise 2", "Expertise 3");
        offerDto = new OfferDto("This is a title",
                "This is a description", 20, 520,
                expertise, null, null, 0, null);
        studentOffer = new StudentOffer("This is a title",
            "This is a description", 20, 520,
            expertise, Status.DISABLED,
            32, student);
        targetedCompanyOffer = new TargetedCompanyOffer("This is a company title",
            "This is a company description",
            20, 520, expertise, Status.DISABLED,
            Arrays.asList("Requirement 1", "Requirement 2", "Requirement 3"),
            company, null);
    }

    @Test
    void getAllByUsernameTest() {
        targetedCompanyOffer.setStudentOffer(studentOffer);
        Map<String, List<Offer>> expectedMap = new HashMap<>();
        expectedMap.put("studentOffers", List.of(studentOffer));
        expectedMap.put("targetedCompanyOffers", List.of(targetedCompanyOffer));
        Mockito.when(offerService.getAllByUsername(student))
            .thenReturn(expectedMap);

        ResponseEntity<Response<Map<String, List<Offer>>>> response
                = offerController
            .getAllByUsername("Student");
        Response<Map<String, List<Offer>>> res
                = new Response<>(expectedMap, null);

        assertEquals(res, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getAllByUsernameNotAuthenticatedTest() {
        ResponseEntity<Response<Map<String, List<Offer>>>> response = offerController
                .getAllByUsername("");
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("User is not authenticated",
                Objects.requireNonNull(response.getBody()).getErrorMessage());
    }

    @Test
    void getOffersByCreatorTestStudent() {
        ResponseEntity<Response<List<Offer>>> res = new ResponseEntity<>(
                new Response<>(List.of(studentOffer)),
                HttpStatus.OK);
        Mockito.when(studentController.getStudentOffersById(student))
                .thenReturn(res);

        assertEquals(res, offerController
                .getOffersByCreator(student, studentRole, Optional.empty()));
    }

    @Test
    void getOffersByCreatorTestInvalidRole() {
        ResponseEntity<Response<List<Offer>>> res = new ResponseEntity<>(
                new Response<>(null, invalidRole),
                HttpStatus.BAD_REQUEST);

        assertEquals(res, offerController
                .getOffersByCreator(student, "", Optional.empty()));
    }

    @Test
    void getOffersByCreatorTestInvalidUserName() {
        ResponseEntity<Response<List<Offer>>> res = new ResponseEntity<>(
                new Response<>(null, unauthenticatedMessage),
                HttpStatus.UNAUTHORIZED);

        assertEquals(res, offerController
                .getOffersByCreator("", studentRole, Optional.empty()));
    }

    @Test
    void getOffersByCreatorTestTargeted() {
        ResponseEntity<Response<List<Offer>>> res = new ResponseEntity<>(
                new Response<>(List.of(targetedCompanyOffer)),
                HttpStatus.OK);
        Mockito.when(targetedController.getCompanyOffersById(company))
                .thenReturn(res);

        assertEquals(res, offerController
                .getOffersByCreator(company, companyRole, Optional.empty()));
    }

    @Test
    void getOffersByCreatorTestTargetedWithParameterCompany() {
        ResponseEntity<Response<List<Offer>>> res = new ResponseEntity<>(
                new Response<>(List.of(targetedCompanyOffer)),
                HttpStatus.OK);
        Mockito.when(targetedController
                        .getCompanyOffersByStudentOffer(company, 1L))
                .thenReturn(res);

        assertEquals(res, offerController
                .getOffersByCreator(company, companyRole, Optional.of(1L)));
    }

    @Test
    void getOffersByCreatorTestTargetedWithParameterStudent() {
        ResponseEntity<Response<List<Offer>>> res = new ResponseEntity<>(
                new Response<>(List.of(targetedCompanyOffer)),
                HttpStatus.OK);
        Mockito.when(targetedController
                        .getCompanyOffersByStudentOffer(student, 1L))
                .thenReturn(res);

        assertEquals(res, offerController
                .getOffersByCreator(student, studentRole, Optional.of(1L)));
    }

    @Test
    void acceptTestEmptyUserName() {
        ResponseEntity<Response<ContractDto>> res = new ResponseEntity<>(
                new Response<>(null, unauthenticatedMessage),
                HttpStatus.UNAUTHORIZED);

        assertEquals(res, offerController.accept("", studentRole, 2L));
    }

    @Test
    void acceptTestInvalidRole() {
        ResponseEntity<Response<ContractDto>> res = new ResponseEntity<>(
                new Response<>(null, invalidRole),
                HttpStatus.BAD_REQUEST);

        assertEquals(res, offerController.accept(student, "", 2L));
    }

    @Test
    void acceptTestStudent() {
        ResponseEntity<Response<ContractDto>> res = new ResponseEntity<>(
                new Response<>(new ContractDto()),
                HttpStatus.OK);

        Mockito.when(studentController.acceptTargetedOffer(student, 1L))
                        .thenReturn(res);

        assertEquals(res, offerController.accept(student, studentRole, 1L));
    }

    @Test
    void acceptTestCompany() {
        ResponseEntity<Response<ContractDto>> res = new ResponseEntity<>(
                new Response<>(new ContractDto()),
                HttpStatus.OK);

        Mockito.when(nonTargetedController.acceptApplication(company, 1L))
                .thenReturn(res);

        assertEquals(res, offerController.accept(company, companyRole, 1L));
    }

    @Test
    void createOfferTestEmptyUserName() {
        ResponseEntity<Response<Offer>> res = new ResponseEntity<>(
                new Response<>(null, unauthenticatedMessage),
                HttpStatus.UNAUTHORIZED);

        assertEquals(res, offerController
                .createOffer("", studentRole, Optional.of(1L), null));
    }

    @Test
    void createOfferTestInvalidRole() {
        ResponseEntity<Response<Offer>> res = new ResponseEntity<>(
                new Response<>(null, "The entered offer or credentials are invalid"),
                HttpStatus.BAD_REQUEST);

        assertEquals(res, offerController
                .createOffer(student, "", Optional.of(1L), null));
    }

    @Test
    void createOfferTestStudent() {
        ResponseEntity<Response<Offer>> res = new ResponseEntity<>(
                new Response<>(studentOffer),
                HttpStatus.OK);
        offerDto.setPricePerHour(32);
        offerDto.setStudentId(student);
        StudentOffer response = offerDto.toStudentOffer();

        Mockito.when(studentController.saveStudentOffer(student, response))
                        .thenReturn(res);
        assertEquals(res, offerController
                .createOffer(student, studentRole, Optional.empty(), offerDto));
    }

    @Test
    void createOfferTestTargeted() {
        offerDto.setCompanyId(company);
        offerDto.setTitle("This is a company title");
        offerDto.setDescription("This is a company description");

        ResponseEntity<Response<Offer>> res = new ResponseEntity<>(
                new Response<>(targetedCompanyOffer),
                HttpStatus.OK);

        TargetedCompanyOffer response = offerDto.toTargetedCompanyOffer();

        Mockito.when(targetedController.saveTargetedCompanyOffer(company, response, 1L))
                .thenReturn(res);
        assertEquals(res, offerController
                .createOffer(company, companyRole, Optional.of(1L), offerDto));
    }

    @Test
    void createOfferTestNonTargeted() {
        NonTargetedCompanyOffer non
                = new NonTargetedCompanyOffer("This is a title",
                "This is a description", 20, 520,
                Arrays.asList("Expertise 1", "Expertise 2", "Expertise 3"),
                Status.PENDING, null, company);
        ResponseEntity<Response<Offer>> res = new ResponseEntity<>(
                new Response<>(non),
                HttpStatus.OK);
        offerDto.setCompanyId(company);

        NonTargetedCompanyOffer response = offerDto.toNonTargetedCompanyOffer();

        Mockito.when(nonTargetedController.createNonTargetedCompanyOffer(company, response))
                .thenReturn(res);
        assertEquals(res, offerController
                .createOffer(company, companyRole, Optional.empty(), offerDto));
    }
}