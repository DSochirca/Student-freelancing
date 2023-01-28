package nl.tudelft.sem.template.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.naming.NoPermissionException;
import logger.FileLogger;
import nl.tudelft.sem.template.entities.Offer;
import nl.tudelft.sem.template.entities.StudentOffer;
import nl.tudelft.sem.template.entities.TargetedCompanyOffer;
import nl.tudelft.sem.template.entities.dtos.AverageRatingResponse;
import nl.tudelft.sem.template.entities.dtos.AverageRatingResponseWrapper;
import nl.tudelft.sem.template.entities.dtos.ContractDto;
import nl.tudelft.sem.template.enums.Status;
import nl.tudelft.sem.template.exceptions.ContractCreationException;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentOfferServiceTest {

    @Autowired
    private transient StudentOfferService studentOfferService;

    @MockBean
    private transient StudentOfferRepository studentOfferRepository;
    @MockBean
    private transient TargetedCompanyOfferRepository targetedCompanyOfferRepository;
    @MockBean
    private transient OfferRepository offerRepository;

    @MockBean
    private transient Utility utility;

    @MockBean
    private transient FileLogger fileLogger;

    private transient StudentOffer offerTwo;
    private transient StudentOffer offerThree;
    private transient String student;
    private transient TargetedCompanyOffer accepted;
    private transient ContractDto contract;

    @BeforeEach
    void setUp() {
        student = "Student";
        offerTwo = new StudentOffer("Rado's services", "Hey I'm Rado",
            10, 100,
            Arrays.asList("Drawing", "Swimming", "Running"),
            Status.PENDING,
            50, student);
        offerThree = new StudentOffer("Ben's services", "Hey I'm Ben",
            15, 150,
            Arrays.asList("Singing", "Web Dev", "Care-taking"), Status.ACCEPTED,
            50, student);
        accepted = new TargetedCompanyOffer("Ben's services", "Hey I'm Ben",
                15, 150,
                Arrays.asList("Singing", "Web Dev", "Care-taking"), Status.PENDING,
                Arrays.asList("Singing", "Web Dev", "Care-taking"),
                "Our Company", offerTwo);
        offerTwo.setTargetedCompanyOffers(new ArrayList<>());
        offerThree.setTargetedCompanyOffers(new ArrayList<>());

        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = startDate.plusWeeks(
                (long) Math.ceil(accepted.getTotalHours() / accepted.getHoursPerWeek()));

        contract = new ContractDto(1L, accepted.getCompanyId(), student, startDate, endDate,
                accepted.getHoursPerWeek(), accepted.getTotalHours(),
                accepted.getStudentOffer().getPricePerHour(), "ACTIVE");
    }

    @Test
    void getOffersTest() {
        List<StudentOffer> returned = new ArrayList<>();
        returned.add(offerTwo);


        Mockito.when(studentOfferRepository.findAllActive())
                .thenReturn(returned);

        assertEquals(returned, studentOfferService.getOffers());
    }

    @Test
    void getOffersByIdTestPass() {
        List<Offer> returned = new ArrayList<>();
        returned.add(offerThree);
        try {
            Mockito.doNothing().when(utility).userExists(any());
            Mockito.when(studentOfferRepository.findAllByStudentId(student))
                    .thenReturn(returned);

            assertEquals(returned, studentOfferService.getOffersById(student));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void getOffersByIdTestFailEmpty() {
        Mockito.when(studentOfferRepository.findAllByStudentId(student))
                .thenReturn(new ArrayList<>());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> studentOfferService.getOffersById(student));
        String message = "No such student has made offers!";
        assertEquals(message, exception.getMessage());
    }

    @Test
    void acceptOfferTest() throws NoPermissionException, ContractCreationException {
        TargetedCompanyOffer declined = new TargetedCompanyOffer();
        offerTwo.setTargetedCompanyOffers(List.of(declined, accepted));

        Mockito.when(targetedCompanyOfferRepository.findById(accepted.getId()))
                        .thenReturn(Optional.of(accepted));

        Mockito.when(utility.createContract(any(), any(), any(), any(), any()))
                .thenReturn(contract);

        final ContractDto actual = studentOfferService.acceptOffer(student, accepted.getId());

        Mockito.verify(studentOfferRepository, times(1)).save(any());
        Mockito.verify(targetedCompanyOfferRepository,
                times(2)).save(any());
        assertSame(accepted.getStatus(), Status.ACCEPTED);
        assertSame(offerTwo.getStatus(), Status.DISABLED);
        assertSame(declined.getStatus(), Status.DECLINED);
        assertEquals(contract, actual);
    }

    @Test
    void acceptOfferTestFailInvalid() {
        Mockito.when(targetedCompanyOfferRepository.findById(accepted.getId()))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception
                = assertThrows(IllegalArgumentException.class,
                    () -> studentOfferService.acceptOffer(student, accepted.getId()));
        String errorMessage = "ID is not valid!";
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void acceptOfferTestFailStatusStudent() {
        offerTwo.setStatus(Status.DISABLED);
        Mockito.when(targetedCompanyOfferRepository.findById(accepted.getId()))
                .thenReturn(Optional.of(accepted));

        IllegalArgumentException exception
                = assertThrows(IllegalArgumentException.class,
                    () -> studentOfferService.acceptOffer(student, accepted.getId()));
        String errorMessage = "The StudentOffer or TargetedRequest is not active anymore!";
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void acceptOfferTestFailStatusTargeted() {
        accepted.setStatus(Status.DECLINED);
        Mockito.when(targetedCompanyOfferRepository.findById(accepted.getId()))
                .thenReturn(Optional.of(accepted));

        IllegalArgumentException exception
                = assertThrows(IllegalArgumentException.class,
                    () -> studentOfferService.acceptOffer(student, accepted.getId()));
        String errorMessage = "The StudentOffer or TargetedRequest is not active anymore!";
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void acceptOfferTestFailRole() {
        accepted.setStatus(Status.DECLINED);
        Mockito.when(targetedCompanyOfferRepository.findById(accepted.getId()))
                .thenReturn(Optional.of(accepted));

        NoPermissionException exception
                = assertThrows(NoPermissionException.class,
                    () -> studentOfferService.acceptOffer("different", accepted.getId()));
        String errorMessage = "User not allowed to accept this TargetedOffer";
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void updateStudentOfferTest() {
        AverageRatingResponse fakeResponse = new AverageRatingResponse(5.0);
        AverageRatingResponseWrapper responseWrapper = new AverageRatingResponseWrapper();
        responseWrapper.setData(fakeResponse);
        try {
            Mockito.when(utility.getAverageRating(Mockito.anyString()))
                    .thenReturn(5.0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        StudentOffer edited = offerTwo;
        edited.setDescription("New Description, last one was awful");
        edited.setPricePerHour(100.0);

        Mockito.when(studentOfferRepository.getById(offerTwo.getId()))
                .thenReturn(offerTwo);
        Mockito.when(offerRepository.save(offerTwo))
                        .thenReturn(offerTwo);

        studentOfferService.updateStudentOffer(edited);
        Mockito.verify(offerRepository).save(edited);
    }

    @Test
    void updateStudentOfferTestFailStatus() {
        StudentOffer edited = offerThree;
        edited.setId(offerTwo.getId());
        Mockito.when(studentOfferRepository.getById(offerTwo.getId()))
                .thenReturn(offerTwo);

        String message = "You are not allowed to edit the Status";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> studentOfferService.updateStudentOffer(edited));
        assertEquals(message, exception.getMessage());
    }


    @Test
    void updateStudentOfferTestFailId() {
        Mockito.when(studentOfferRepository.getById(offerTwo.getId()))
                .thenReturn(null);

        String message = "This StudentOffer does not exist!";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> studentOfferService.updateStudentOffer(offerTwo));
        assertEquals(message, exception.getMessage());
    }

    @Test
    void getByKeyWordTest() throws UnsupportedEncodingException {
        String keyWord = "Hey I'm Rado";
        Mockito.when(studentOfferRepository.getAllByKeyWord(keyWord))
                .thenReturn(List.of(offerTwo));

        assertEquals(List.of(offerTwo), studentOfferService.getByKeyWord(keyWord));
    }

    @Test
    void getByExpertisesTest() throws UnsupportedEncodingException {
        List<StudentOffer> asd = new ArrayList<>();
        asd.add(offerTwo);
        Mockito.when(studentOfferRepository.findAllActive())
                .thenReturn(asd);

        List<String> expertises = new ArrayList<>();
        expertises.add("Swimming");
        assertEquals(List.of(offerTwo),
                studentOfferService.getByExpertises(expertises));
    }
}
