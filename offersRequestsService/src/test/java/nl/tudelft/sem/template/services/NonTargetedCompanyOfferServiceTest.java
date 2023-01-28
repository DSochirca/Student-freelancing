package nl.tudelft.sem.template.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.naming.NoPermissionException;
import logger.FileLogger;
import nl.tudelft.sem.template.entities.Application;
import nl.tudelft.sem.template.entities.NonTargetedCompanyOffer;
import nl.tudelft.sem.template.entities.dtos.ContractDto;
import nl.tudelft.sem.template.enums.Status;
import nl.tudelft.sem.template.exceptions.ContractCreationException;
import nl.tudelft.sem.template.repositories.ApplicationRepository;
import nl.tudelft.sem.template.repositories.NonTargetedCompanyOfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@AutoConfigureMockMvc
class NonTargetedCompanyOfferServiceTest {

    @Autowired
    private transient NonTargetedCompanyOfferService service;

    @MockBean
    private transient NonTargetedCompanyOfferRepository offerRepository;

    @MockBean
    private transient ApplicationRepository applicationRepository;

    @MockBean
    private transient Utility utility;

    @MockBean
    private transient FileLogger fileLogger;

    private transient NonTargetedCompanyOffer offer;
    private transient Application application;
    private transient String student;
    private transient String companyId;
    private transient ContractDto contract;

    @BeforeEach
    void setup() {
        student = "student";
        companyId = "facebook";
        List<String> expertise = List.of("e1", "e2", "e3");
        List<String> requirements = List.of("r1", "r2", "r3");
        offer = new NonTargetedCompanyOffer("title", "description",
                20, 520, expertise,
                Status.PENDING, requirements, companyId);
        application = new Application(student, 5, Status.PENDING, offer);

        LocalDate startDate = LocalDate.of(2022, 1, 1);
        LocalDate endDate = startDate.plusWeeks(
                (long) Math.ceil(offer.getTotalHours() / offer.getHoursPerWeek()));

        contract = new ContractDto(1L, companyId, student, startDate, endDate,
                offer.getHoursPerWeek(), offer.getTotalHours(),
                application.getPricePerHour(), "ACTIVE");
    }

    @Test
    void applyNonExistantTest() {
        Mockito.when(offerRepository.getOfferById(3L))
                .thenReturn(null);
        String errorMessage = "There is no offer associated with this id";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.apply(application, 3L));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void applyNotActiveTest() {
        offer.setStatus(Status.DISABLED);
        Mockito.when(offerRepository.getOfferById(3L))
                .thenReturn(offer);
        String errorMessage = "This offer is not active anymore";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.apply(application, 3L));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void applyAlreadyAppliedTest() {
        Mockito.when(offerRepository.getOfferById(3L))
                .thenReturn(offer);
        Mockito.when(applicationRepository
                        .existsByStudentIdAndNonTargetedCompanyOffer(student, offer))
                .thenReturn(true);
        String errorMessage = "Student already applied to this offer";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.apply(application, 3L));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void applyValidTest() {
        Mockito.when(offerRepository.getOfferById(3L))
                .thenReturn(offer);
        Mockito.when(applicationRepository
                        .existsByStudentIdAndNonTargetedCompanyOffer(student, offer))
                .thenReturn(false);
        Mockito.when(applicationRepository.save(application))
                .thenReturn(application);

        assertEquals(application, service.apply(application, 3L));
    }

    @Test
    void applySetterTest() {
        Application app = Mockito.mock(Application.class);
        app.setStudentId(student);
        Mockito.when(offerRepository.getOfferById(3L))
                .thenReturn(offer);
        Mockito.when(applicationRepository
                        .existsByStudentIdAndNonTargetedCompanyOffer(student, offer))
                .thenReturn(false);
        Mockito.when(applicationRepository.save(application))
                .thenReturn(application);
        service.apply(app, 3L);
        Mockito.verify(app).setNonTargetedCompanyOffer(offer);
    }

    @Test
    void acceptTest() throws NoPermissionException, ContractCreationException {
        Application declined = new Application(student, 10, Status.PENDING, offer);
        offer.setApplications(List.of(application, declined));
        Mockito.when(offerRepository.getOfferById(offer.getId()))
                .thenReturn(offer);
        Mockito.when(applicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));

        Mockito.when(utility.createContract(any(), any(), any(), any(), any()))
                .thenReturn(contract);

        final ContractDto actual = service.accept(companyId, application.getId());

        Mockito.verify(offerRepository, times(1)).save(any());
        Mockito.verify(applicationRepository,
                times(2)).save(any());
        assertSame(application.getStatus(), Status.ACCEPTED);
        assertSame(offer.getStatus(), Status.DISABLED);
        assertSame(declined.getStatus(), Status.DECLINED);
        assertEquals(contract, actual);
    }

    @Test
    void acceptTestFailStatusApplication() {
        application.setStatus(Status.DECLINED);
        Mockito.when(offerRepository.getOfferById(offer.getId()))
                .thenReturn(offer);
        Mockito.when(applicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));

        IllegalArgumentException exception
                = assertThrows(IllegalArgumentException.class,
                    () -> service.accept(companyId, application.getId()));
        String errorMessage = "The offer or application is not active anymore!";
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void acceptTestFailStatusOffer() {
        application.setStatus(Status.DISABLED);
        Mockito.when(offerRepository.getOfferById(offer.getId()))
                .thenReturn(offer);
        Mockito.when(applicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));

        IllegalArgumentException exception
                = assertThrows(IllegalArgumentException.class,
                    () -> service.accept(companyId, application.getId()));
        String errorMessage = "The offer or application is not active anymore!";
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void acceptTestFailRole() {
        application.setStatus(Status.DISABLED);
        Mockito.when(offerRepository.getOfferById(offer.getId()))
                .thenReturn(offer);
        Mockito.when(applicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));

        NoPermissionException exception
                = assertThrows(NoPermissionException.class,
                    () -> service.accept("different", application.getId()));
        String errorMessage = "User can not accept this application!";
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void acceptTestEmpty() {
        String errorMessage = "The application does not exist";
        Mockito.when(applicationRepository.findById(3L))
                .thenReturn(Optional.empty());
        IllegalArgumentException exception
                = assertThrows(IllegalArgumentException.class,
                    () -> service.accept(companyId, 3L));
        assertEquals(errorMessage, exception.getMessage());
    }

}