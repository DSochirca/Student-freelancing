package nl.tudelft.sem.template.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import logger.FileLogger;
import nl.tudelft.sem.template.dtos.requests.ContractRequest;
import nl.tudelft.sem.template.entities.Contract;
import nl.tudelft.sem.template.entities.ContractChangeProposal;
import nl.tudelft.sem.template.enums.ChangeStatus;
import nl.tudelft.sem.template.enums.ContractStatus;
import nl.tudelft.sem.template.exceptions.AccessDeniedException;
import nl.tudelft.sem.template.exceptions.ContractNotFoundException;
import nl.tudelft.sem.template.exceptions.InactiveContractException;
import nl.tudelft.sem.template.exceptions.InvalidChangeProposalException;
import nl.tudelft.sem.template.exceptions.InvalidContractException;
import nl.tudelft.sem.template.repositories.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("PMD.AvoidDuplicateLiterals")  // Repeating strings in @Tag are not an issue
class ContractServiceTest {

    @Autowired
    private transient ContractService contractService;

    @MockBean
    private transient ContractRepository contractRepository;

    @MockBean
    private transient FileLogger fileLogger;

    private transient Contract contract;
    private transient Contract contractToBeSaved;
    private final transient String companyId = "TUDelft";
    private final transient String studentId = "JohnDoe";

    private transient ContractChangeProposal proposal;

    private final transient ArgumentCaptor<Contract> contractArgumentCaptor =
            ArgumentCaptor.forClass(Contract.class);

    @BeforeEach
    void setUp() {
        LocalDate startDate = LocalDate.of(2021, 12, 25);
        LocalDate endDate = startDate.plusWeeks(3);
        contract = new Contract(1L, companyId, studentId, startDate, endDate, 14,
                42, 15, ContractStatus.ACTIVE, null);

        // Contract passed with the request body:
        ContractRequest contractRequest = new ContractRequest(companyId, studentId, 14d, 42d, 15d);
        contractToBeSaved = contractRequest.toContract();

        // Student suggested the change:
        proposal = new ContractChangeProposal(1L, contract, studentId, companyId,
                null, null, null, null, ChangeStatus.PENDING);
    }

    @Test
    @Tag("saveContract")
    void saveValidContractSuccess() throws InvalidContractException {
        when(contractRepository
                .findActiveContract(companyId, studentId))
                .thenReturn(null);
        when(contractRepository.save(contractToBeSaved)).thenReturn(contract);

        final Contract actual = contractService.saveContract(contractToBeSaved);

        verify(contractRepository).save(contractArgumentCaptor.capture());

        // Get current date from the captured contract value:
        LocalDate today = contractArgumentCaptor.getValue().getStartDate();
        contract.setStartDate(today);
        contract.setEndDate(today.plusWeeks(3));

        // Assert that the contract params are correct before saving:
        contract.setId(null);
        assertEquals(contract, contractArgumentCaptor.getValue());

        // After saving the id is updated:
        contract.setId(1L);
        assertEquals(contract, actual);

        // File Logger:
        verify(fileLogger).log("Contract " + contract.getId() + " has been saved between company "
                + companyId + " and student " + studentId);
    }

    @Test
    @Tag("saveContract")
    void saveInvalidContractException() {
        // Contract between the same person
        contract.setStudentId(contract.getCompanyId());
        when(contractRepository
                .findActiveContract(companyId, studentId))
                .thenReturn(null);

        assertThrows(InvalidContractException.class, () -> contractService.saveContract(contract));
    }

    @Test
    @Tag("getContractByCompanyAndStudent")
    void getContractAccessDenied() {
        assertThrows(AccessDeniedException.class,
                () -> contractService.getContract(companyId, studentId, true, "BillGates"));
    }

    @Test
    @Tag("getContractByCompanyAndStudent")
    void getActiveContractSuccess() throws ContractNotFoundException, AccessDeniedException {
        // Just to be sure the end date doesn't pass:
        contract.setEndDate(LocalDate.of(4000, 1, 1));

        when(contractRepository
                .findActiveContract(companyId, studentId))
                .thenReturn(contract);

        assertEquals(contract, contractService.getContract(companyId, studentId, true, studentId));
    }

    @Test
    @Tag("getContractByCompanyAndStudent")
    void getActiveContractNotFound() {
        when(contractRepository
                .findActiveContract(companyId, studentId))
                .thenReturn(null);

        assertThrows(ContractNotFoundException.class,
                () -> contractService.getContract(companyId, studentId, true, companyId));
    }

    @Test
    @Tag("getContractByCompanyAndStudent")
    void getActiveContractThatExpired() {
        // The end date passed, so it should be set to expired and throw exception:
        contract.setEndDate(LocalDate.of(2021, 12, 1));

        when(contractRepository
                .findActiveContract(companyId, studentId))
                .thenReturn(contract);

        assertThrows(ContractNotFoundException.class,
                () -> contractService.getContract(companyId, studentId, true, studentId));
    }

    @Test
    @Tag("getContractByCompanyAndStudent")
    void getMostRecentContractSuccess() throws ContractNotFoundException, AccessDeniedException {
        // Just to be sure the end date doesn't pass:
        contract.setEndDate(LocalDate.of(4000, 1, 1));

        when(contractRepository
                .findFirstByCompanyIdEqualsAndStudentIdEqualsOrderByStartDateDesc(
                        companyId, studentId))
                .thenReturn(contract);

        assertEquals(contract, contractService.getContract(companyId, studentId, false, studentId));
    }

    @Test
    @Tag("getContractByCompanyAndStudent")
    void getMostRecentContractNotFound() throws ContractNotFoundException, AccessDeniedException {
        when(contractRepository
                .findFirstByCompanyIdEqualsAndStudentIdEqualsOrderByStartDateDesc(
                        companyId, studentId))
                .thenReturn(null);

        assertThrows(ContractNotFoundException.class,
                () -> contractService.getContract(companyId, studentId, false, studentId));
    }

    @Test
    @Tag("getContractByCompanyAndStudent")
    void getMostRecentContractExpired() throws ContractNotFoundException, AccessDeniedException {
        contract.setEndDate(LocalDate.of(2021, 12, 1));
        // Contract still active:
        when(contractRepository
                .findFirstByCompanyIdEqualsAndStudentIdEqualsOrderByStartDateDesc(
                        companyId, studentId))
                .thenReturn(contract);

        Contract actual = contractService.getContract(companyId, studentId, false, studentId);

        // Status should be set to expired:
        contract.setStatus(ContractStatus.EXPIRED);

        assertEquals(contract, actual);
    }

    @Test
    @Tag("getContractById")
    void getContractByIdNotFound() {
        Exception e = new ContractNotFoundException(1L);
        when(contractRepository
                .findById(1L)).thenReturn(Optional.empty());

        assertThrows(ContractNotFoundException.class,
                () -> contractService.getContract(companyId, studentId, true, studentId),
                e.getMessage());
    }

    @Test
    @Tag("getContractById")
    void getContractByIdShouldExpire() throws ContractNotFoundException {
        contract.setEndDate(LocalDate.of(2021, 12, 1));

        when(contractRepository
                .findById(1L)).thenReturn(Optional.of(contract));  // Still active when retrieved

        contract.setStatus(ContractStatus.EXPIRED);
        assertEquals(contract, contractService.getContract(1L));
    }

    @Test
    @Tag("getContractById")
    void getContractByIdShouldNotExpire() throws ContractNotFoundException {
        // Just to be sure the end date doesn't pass:
        contract.setEndDate(LocalDate.of(4000, 1, 1));

        when(contractRepository
                .findById(1L)).thenReturn(Optional.of(contract));

        assertEquals(contract, contractService.getContract(1L));
    }

    @Test
    @Tag("terminateContract")
    void terminateContractSuccess() throws ContractNotFoundException,
            InactiveContractException, AccessDeniedException {
        // Just to be sure the end date doesn't pass:
        contract.setEndDate(LocalDate.of(4000, 1, 1));

        when(contractRepository.findById(contract.getId()))
                .thenReturn(Optional.of(contract));

        contractService.terminateContract(contract.getId(), studentId);

        verify(contractRepository).terminateContract(contract.getId());

        // File Logger:
        verify(fileLogger).log("Contract " + contract.getId() + " has been terminated");
    }

    @Test
    @Tag("terminateContract")
    void terminateContractNotActive() {
        contract.setStatus(ContractStatus.TERMINATED);

        when(contractRepository.findById(contract.getId())).thenReturn(Optional.of(contract));

        assertThrows(InactiveContractException.class,
                () -> contractService.terminateContract(contract.getId(), studentId));
    }

    // -----------------------
    //  UPDATING CONTRACTS:
    // -----------------------

    @Test
    @Tag("updateContract")
    void updateOnlyPrice() throws InvalidChangeProposalException {
        double price = 16d;
        proposal.setPricePerHour(price);

        contractService.updateContract(contract, proposal);

        verify(contractRepository).save(contractArgumentCaptor.capture());

        // Assert that the contract params are correct before updating it:
        contract.setPricePerHour(price);
        assertEquals(contract, contractArgumentCaptor.getValue());
    }

    /**
     * ON-POINT (also an OUT-POINT) boundary test (max hours per week > 20 throws exception).
     */
    @Test
    @Tag("updateContract")
    @Tag("BoundaryTest")
    void updateHoursPerWeekValid() throws InvalidChangeProposalException {
        double hours = 20d;
        proposal.setHoursPerWeek(hours);

        contractService.updateContract(contract, proposal);

        verify(contractRepository).save(contractArgumentCaptor.capture());

        // Assert that the contract params are correct before updating it:
        contract.setPricePerHour(hours);
        assertEquals(contract, contractArgumentCaptor.getValue());
    }

    /**
     * OFF-POINT (also an IN-POINT) boundary test (max hours per week > 20 throws exception).
     */
    @Test
    @Tag("updateContract")
    @Tag("BoundaryTest")
    void updateHoursPerWeekInvalid() {
        double hours = 21d;
        proposal.setHoursPerWeek(hours);

        assertThrows(InvalidChangeProposalException.class,
                () -> contractService.updateContract(contract, proposal));
    }

    /**
     * ON-POINT (also an OUT-POINT) boundary test (total weeks > 26 throws exception).
     */
    @Test
    @Tag("updateContract")
    @Tag("BoundaryTest")
    void updateTotalHoursValid() throws InvalidChangeProposalException {
        contract.setHoursPerWeek(2);
        contract.setTotalHours(50);  // 25 weeks total

        // 52 total hours will be exactly 26 weeks:
        double hours = 52d;
        proposal.setTotalHours(hours);

        contractService.updateContract(contract, proposal);

        verify(contractRepository).save(contractArgumentCaptor.capture());

        // Assert that the contract params are correct before updating it:
        contract.setTotalHours(hours);
        contract.setEndDate(contract.getStartDate().plusWeeks(26));
        assertEquals(contract, contractArgumentCaptor.getValue());
    }

    /**
     * OFF-POINT (also an IN-POINT) boundary test (total weeks > 26 throws exception).
     */
    @Test
    @Tag("updateContract")
    @Tag("BoundaryTest")
    void updateTotalHoursInvalid() {
        contract.setHoursPerWeek(2);
        contract.setTotalHours(50);  // 25 weeks total

        // 52 total hours will be exactly 26 weeks:
        double hours = 52.01;
        proposal.setTotalHours(hours);

        assertThrows(InvalidChangeProposalException.class,
                () -> contractService.updateContract(contract, proposal));
    }

    /**
     * 20h per week and 26 total weeks is an ON-POINT (also an OUT-POINT).
     */
    @Test
    @Tag("updateContract")
    void updateHoursPerWeekAndTotalHoursValid() throws InvalidChangeProposalException {
        // Exactly 26 weeks and 20h per week:
        double hoursPerWeek = 20d;
        double totalHours = 520d;
        proposal.setHoursPerWeek(hoursPerWeek);
        proposal.setTotalHours(totalHours);

        contractService.updateContract(contract, proposal);

        verify(contractRepository).save(contractArgumentCaptor.capture());

        // Assert that the contract params are correct before updating it:
        contract.setHoursPerWeek(hoursPerWeek);
        contract.setTotalHours(totalHours);
        contract.setEndDate(contract.getStartDate().plusWeeks(26));
        assertEquals(contract, contractArgumentCaptor.getValue());
    }

    @Test
    @Tag("updateContract")
    void updateOnlyEndDate() throws InvalidChangeProposalException {
        LocalDate newEndDate = contract.getEndDate().plusWeeks(1); // extend by 1 week
        proposal.setEndDate(newEndDate);

        contractService.updateContract(contract, proposal);

        verify(contractRepository).save(contractArgumentCaptor.capture());

        // Assert that the contract params are correct before updating it:
        contract.setEndDate(newEndDate);
        assertEquals(contract, contractArgumentCaptor.getValue());
    }

    @Test
    @Tag("updateContract")
    void updateTotalHoursAndEndDate() throws InvalidChangeProposalException {
        // Current contract: 14h per week, 42h total, 3 weeks
        double totalHours = 43d;    // will add one more week to the endDate
        LocalDate newEndDate = contract.getEndDate().plusWeeks(2);  // add 2 weeks (1 extra)
        proposal.setTotalHours(totalHours);
        proposal.setEndDate(newEndDate);

        contractService.updateContract(contract, proposal);

        verify(contractRepository).save(contractArgumentCaptor.capture());

        // Assert that the contract params are correct before updating it:
        contract.setTotalHours(totalHours);
        contract.setEndDate(newEndDate);
        assertEquals(contract, contractArgumentCaptor.getValue());
    }

    @Test
    @Tag("updateContract")
    void updateEndDateButItsTooManyWeeks() {
        proposal.setEndDate(contract.getStartDate().plusWeeks(27));

        assertThrows(InvalidChangeProposalException.class,
                () -> contractService.updateContract(contract, proposal));
    }

    @Test
    @Tag("updateContract")
    void updateEndDateButItsTooSoon() throws InvalidChangeProposalException {
        // Current contract: 14h per week, 42h total, 3 weeks
        double totalHours = 43d;    // will add one more week to the endDate
        LocalDate newEndDate = contract.getEndDate();  // keep the previous endDate
        proposal.setTotalHours(totalHours);
        proposal.setEndDate(newEndDate);

        // The endDate should be one week or more after the current one, so it fails
        assertThrows(InvalidChangeProposalException.class,
                () -> contractService.updateContract(contract, proposal));
    }

    @Test
    @Tag("updateContract")
    void updateHoursPerWeekAndTotalHoursAndExtendEndDate() throws InvalidChangeProposalException {
        // Current contract: 14h per week, 42h total, 3 weeks
        double hoursPerWeek = 10d;
        double totalHours = 100d;    //10 weeks
        LocalDate newEndDate = contract.getStartDate().plusWeeks(13);  // add 3 weeks extra
        proposal.setHoursPerWeek(hoursPerWeek);
        proposal.setTotalHours(totalHours);
        proposal.setEndDate(newEndDate);

        contractService.updateContract(contract, proposal);

        verify(contractRepository).save(contractArgumentCaptor.capture());

        // Assert that the contract params are correct before updating it:
        contract.setHoursPerWeek(hoursPerWeek);
        contract.setTotalHours(totalHours);
        contract.setEndDate(newEndDate);
        assertEquals(contract, contractArgumentCaptor.getValue());
    }

    // -----------------------
    //  HELPER METHODS TESTS:
    // -----------------------

    @Test
    @Tag("ValidateContract")
    void validateContractSuccess() {
        assertDoesNotThrow(() -> contractService.validateContract(contractToBeSaved));
    }

    @Test
    void validateContractSamePartiesFailed() {
        contractToBeSaved.setCompanyId(studentId);

        assertThrows(InvalidContractException.class,
                () -> contractService.validateContract(contractToBeSaved));
    }

    /**
     * ON-POINT (also OUT-POINT) boundary test (max hours per week > 20 throws exception).
     */
    @Test
    @Tag("ValidateContract")
    @Tag("BoundaryTest")
    void validateContractHoursPerWeekCorrect() {
        contractToBeSaved.setHoursPerWeek(20d);

        assertDoesNotThrow(() -> contractService.validateContract(contractToBeSaved));
    }

    /**
     * OFF-POINT (also an IN-POINT) boundary test (max hours per week > 20 throws exception).
     */
    @Test
    @Tag("ValidateContract")
    @Tag("BoundaryTest")
    void validateContractHoursPerWeekExceeded() {
        contractToBeSaved.setHoursPerWeek(21d);

        assertThrows(InvalidContractException.class,
                () -> contractService.validateContract(contractToBeSaved));
    }

    /**
     * ON-POINT (also an OUT-POINT) boundary test (total weeks > 26 throws exception).
     */
    @Test
    @Tag("ValidateContract")
    @Tag("BoundaryTest")
    void validateContractTotalWeeksCorrect() {
        contractToBeSaved.setHoursPerWeek(2);
        contractToBeSaved.setTotalHours(52);  //52 total hours will be exactly 26 weeks

        assertDoesNotThrow(() -> contractService.validateContract(contractToBeSaved));
    }

    /**
     * OFF-POINT (also an IN-POINT) boundary test (total weeks > 26 throws exception).
     */
    @Test
    @Tag("ValidateContract")
    @Tag("BoundaryTest")
    void validateContractTotalWeeksExceeded() {
        contractToBeSaved.setHoursPerWeek(2);
        contractToBeSaved.setTotalHours(52.1);  //52 total hours will be exactly 26 weeks

        assertThrows(InvalidContractException.class,
                () -> contractService.validateContract(contractToBeSaved));
    }

    @Test
    @Tag("ValidateContract")
    void validateContractAlreadyExists() {
        String errorMsg = "Please cancel the existing contract (#" + contract.getId()
                + ") with this party.";

        when(contractRepository.findActiveContract(companyId, studentId)).thenReturn(contract);

        assertThrows(InvalidContractException.class,
                () -> contractService.validateContract(contractToBeSaved), errorMsg);
    }

    @Test
    @Tag("shouldExpire")
    void shouldNotExpireNullContract() {
        assertFalse(contractService.shouldExpire(null));
    }

    /**
     * ON-POINT (OUT POINT as well) -> (checks if status != ACTIVE).
     */
    @Test
    @Tag("shouldExpire")
    @Tag("BoundaryTest")
    void shouldExpireActiveContractSuccess() {
        contract.setEndDate(LocalDate.of(2021, 1, 1));
        contract.setStatus(ContractStatus.ACTIVE);
        assertTrue(contractService.shouldExpire(contract));
    }

    /**
     * OFF-POINT (IN POINT as well) -> (checks if status != ACTIVE).
     */
    @Test
    @Tag("shouldExpire")
    @Tag("BoundaryTest")
    void shouldNotExpireAlreadyExpiredContract() {
        contract.setStatus(ContractStatus.EXPIRED);
        assertFalse(contractService.shouldExpire(contract));
    }

    /**
     * OFF-POINT (IN POINT as well) -> (checks if status != ACTIVE).
     */
    @Test
    @Tag("shouldExpire")
    @Tag("BoundaryTest")
    void shouldNotExpireTerminatedContract() {
        contract.setStatus(ContractStatus.TERMINATED);
        assertFalse(contractService.shouldExpire(contract));
    }

    @Test
    @Tag("shouldExpire")
    void shouldExpireEndDateDidntPass() {
        contract.setEndDate(LocalDate.of(4096, 1, 1));
        assertFalse(contractService.shouldExpire(contract));
    }

    @Test
    @Tag("shouldExpire")
    void shouldExpireSuccess() {
        contract.setEndDate(LocalDate.of(2021, 1, 1));
        assertTrue(contractService.shouldExpire(contract));
    }

    @Test
    @Tag("checkAuthorization")
    void studentCanAccessContract() {
        assertDoesNotThrow(() -> contractService.checkAuthorization(contract, studentId));
    }

    @Test
    @Tag("checkAuthorization")
    void companyCanAccessContract() {
        assertDoesNotThrow(() -> contractService.checkAuthorization(contract, companyId));
    }

    @Test
    @Tag("checkAuthorization")
    void userCannotAccessContract() {
        assertThrows(AccessDeniedException.class,
                () -> contractService.checkAuthorization(contract, "BillGates"));
    }
}