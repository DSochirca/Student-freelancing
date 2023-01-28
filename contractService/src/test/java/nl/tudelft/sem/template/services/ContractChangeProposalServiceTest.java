package nl.tudelft.sem.template.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import nl.tudelft.sem.template.dtos.requests.ContractChangeRequest;
import nl.tudelft.sem.template.entities.Contract;
import nl.tudelft.sem.template.entities.ContractChangeProposal;
import nl.tudelft.sem.template.enums.ChangeStatus;
import nl.tudelft.sem.template.enums.ContractStatus;
import nl.tudelft.sem.template.exceptions.AccessDeniedException;
import nl.tudelft.sem.template.exceptions.ChangeProposalNotFoundException;
import nl.tudelft.sem.template.exceptions.ContractNotFoundException;
import nl.tudelft.sem.template.exceptions.InactiveContractException;
import nl.tudelft.sem.template.exceptions.InvalidChangeProposalException;
import nl.tudelft.sem.template.repositories.ContractChangeProposalRepository;
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
class ContractChangeProposalServiceTest {

    @Autowired
    private transient ContractChangeProposalService proposalService;

    @MockBean
    private transient ContractChangeProposalRepository proposalRepository;

    @MockBean
    private transient ContractService contractService;

    private final transient String companyId = "TUDelft";
    private final transient String studentId = "JohnDoe";
    private transient Contract contract;
    private transient ContractChangeProposal proposal;
    private transient ContractChangeRequest changeRequest;

    private final transient ArgumentCaptor<ContractChangeProposal> proposalArgumentCaptor
            = ArgumentCaptor.forClass(ContractChangeProposal.class);

    private final transient ArgumentCaptor<Contract> contractArgumentCaptor =
            ArgumentCaptor.forClass(Contract.class);

    @BeforeEach
    void setUp() {
        LocalDate startDate = LocalDate.of(2021, 12, 25);
        LocalDate endDate = startDate.plusWeeks(3);
        contract = new Contract(1L, companyId, studentId, startDate, endDate, 14,
                42, 15, ContractStatus.ACTIVE, null);

        // Student suggested the change:
        proposal = new ContractChangeProposal(1L, contract, studentId, companyId,
                null, null, null, null, ChangeStatus.PENDING);
    }

    @Test
    @Tag("submitProposal")
    void submitProposalSuccess() throws ContractNotFoundException, AccessDeniedException,
            InvalidChangeProposalException, InactiveContractException {
        changeRequest = new ContractChangeRequest(20d, null, null, null);
        proposal.setHoursPerWeek(20d);

        when(contractService.getContract(contract.getId())).thenReturn(contract);

        proposalService.submitProposal(changeRequest, contract.getId(), studentId);

        proposal.setId(null);
        verify(proposalRepository).save(proposal);
    }

    @Test
    @Tag("submitProposal")
    void submitProposalFailed() throws ContractNotFoundException {
        changeRequest = new ContractChangeRequest(20d, null, null, null);
        proposal.setHoursPerWeek(20d);

        when(contractService.getContract(contract.getId())).thenReturn(contract);

        assertThrows(AccessDeniedException.class,
                () -> proposalService.submitProposal(changeRequest, contract.getId(), "BillGates"));
    }

    @Test
    @Tag("acceptProposal")
    void acceptProposalSuccess() throws AccessDeniedException,
            ChangeProposalNotFoundException, InvalidChangeProposalException {
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(contractService.shouldExpire(contract)).thenReturn(false);

        proposalService.acceptProposal(1L, companyId);

        verify(contractService).updateContract(contract, proposal);
        verify(proposalRepository).acceptProposal(1L);
        verify(proposalRepository).deleteAllRejectedProposalsOfContract(contract);
    }

    @Test
    @Tag("acceptProposal")
    void acceptProposalFailed() {
        when(proposalRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ChangeProposalNotFoundException.class,
                () -> proposalService.acceptProposal(1L, companyId));
    }

    @Test
    @Tag("rejectProposal")
    void rejectProposalSuccess() throws AccessDeniedException,
            ChangeProposalNotFoundException {
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(contractService.shouldExpire(contract)).thenReturn(false);

        proposalService.rejectProposal(1L, companyId);

        verify(proposalRepository).rejectProposal(1L);
    }

    @Test
    @Tag("rejectProposal")
    void rejectProposalFailed() {
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(contractService.shouldExpire(contract)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> proposalService.rejectProposal(1L, "BillGates"));
    }

    @Test
    @Tag("deleteProposal")
    void deleteProposalSuccess() {
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(contractService.shouldExpire(contract)).thenReturn(false);

        assertDoesNotThrow(
                () -> proposalService.deleteProposal(1L, studentId));
        verify(proposalRepository).deleteById(1L);
    }

    @Test
    @Tag("deleteProposal")
    void deleteProposalAccessDenied() {
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(contractService.shouldExpire(contract)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> proposalService.deleteProposal(1L, companyId));
    }

    @Test
    @Tag("deleteProposal")
    void deleteProposalAcceptedFailed() {
        proposal.setStatus(ChangeStatus.ACCEPTED);
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(contractService.shouldExpire(contract)).thenReturn(false);

        assertThrows(ChangeProposalNotFoundException.class,
                () -> proposalService.deleteProposal(1L, studentId),
                "This proposal was already reviewed and cannot be deleted");
    }

    @Test
    @Tag("deleteProposal")
    void deleteProposalRejectedFailed() {
        proposal.setStatus(ChangeStatus.REJECTED);
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(contractService.shouldExpire(contract)).thenReturn(false);

        assertThrows(ChangeProposalNotFoundException.class,
                () -> proposalService.deleteProposal(1L, companyId),
                "This proposal was already reviewed and cannot be deleted");
    }

    @Test
    @Tag("getProposals")
    void getProposalsAsStudent() throws AccessDeniedException,
            InactiveContractException {
        proposalService.getProposals(contract, studentId);

        verify(proposalRepository).findAllByContract(contract);
    }

    @Test
    @Tag("getProposals")
    void getProposalsAsCompany() throws AccessDeniedException,
            InactiveContractException {
        proposalService.getProposals(contract, studentId);

        verify(proposalRepository).findAllByContract(contract);
    }

    @Test
    @Tag("getProposals")
    void getProposalsAccessDenied() {
        assertThrows(AccessDeniedException.class,
                () -> proposalService.getProposals(contract, "BillGates"));
    }

    @Test
    @Tag("getProposals")
    void getProposalsExpiredContract() {
        contract.setStatus(ContractStatus.EXPIRED);
        assertThrows(InactiveContractException.class,
                () -> proposalService.getProposals(contract, studentId));
    }

    @Test
    @Tag("getProposals")
    void getProposalsTerminatedContract() {
        contract.setStatus(ContractStatus.TERMINATED);
        assertThrows(InactiveContractException.class,
                () -> proposalService.getProposals(contract, companyId));
    }

    // ----------------------------------------
    //      HELPER METHODS TESTS:
    // ----------------------------------------

    @Test
    @Tag("validateProposal")
    void validateProposalExpiredContract() {
        contract.setStatus(ContractStatus.EXPIRED);

        assertThrows(InactiveContractException.class,
                () -> proposalService.validateContractProposal(proposal));
    }

    @Test
    @Tag("validateProposal")
    void validateProposalTerminatedContract() {
        contract.setStatus(ContractStatus.TERMINATED);

        assertThrows(InactiveContractException.class,
                () -> proposalService.validateContractProposal(proposal));
    }

    /**
     * ON-POINT (also OUT-POINT) boundary test (max hours per week > 20 throws exception).
     */
    @Test
    @Tag("validateProposal")
    @Tag("BoundaryTest")
    void validateProposalHoursPerWeekValid() {
        double hoursPerWeek = 20d;
        proposal.setHoursPerWeek(hoursPerWeek);

        assertDoesNotThrow(() -> proposalService.validateContractProposal(proposal));
    }

    /**
     * OFF-POINT (also an IN-POINT) boundary test (max hours per week > 20 throws exception).
     */
    @Test
    @Tag("validateProposal")
    @Tag("BoundaryTest")
    void validateProposalHoursPerWeekExceeded() {
        double hoursPerWeek = 21d;
        proposal.setHoursPerWeek(hoursPerWeek);

        assertThrows(InvalidChangeProposalException.class,
                () -> proposalService.validateContractProposal(proposal));
    }

    /**
     * ON-POINT (also an OUT-POINT) boundary test (total weeks > 26 throws exception).
     */
    @Test
    @Tag("validateProposal")
    @Tag("BoundaryTest")
    void validateProposalTotalWeeksCorrect() {
        proposal.setHoursPerWeek(2d);
        proposal.setTotalHours(52d);  //52 total hours will be exactly 26 weeks

        assertDoesNotThrow(() -> proposalService.validateContractProposal(proposal));
    }

    /**
     * OFF-POINT (also an IN-POINT) boundary test (total weeks > 26 throws exception).
     */
    @Test
    @Tag("validateProposal")
    @Tag("BoundaryTest")
    void validateProposalTotalWeeksExceeded() {
        proposal.setHoursPerWeek(2d);
        proposal.setTotalHours(52.1);  //52 total hours will be exactly 26 weeks

        assertThrows(InvalidChangeProposalException.class,
                () -> proposalService.validateContractProposal(proposal));
    }

    /**
     * ON-POINT (also an OUT-POINT) boundary test (total weeks > 26 throws exception).
     */
    @Test
    @Tag("validateProposal")
    @Tag("BoundaryTest")
    void validateProposalEndDateValid() {
        proposal.setEndDate(contract.getStartDate().plusWeeks(26));

        assertDoesNotThrow(() -> proposalService.validateContractProposal(proposal));
    }

    /**
     * OFF-POINT (also an IN-POINT) boundary test (total weeks > 26 throws exception).
     */
    @Test
    @Tag("validateProposal")
    @Tag("BoundaryTest")
    void validateProposalEndDateOver26Weeks() {
        proposal.setEndDate(contract.getStartDate().plusWeeks(27));

        assertThrows(InvalidChangeProposalException.class,
                () -> proposalService.validateContractProposal(proposal));
    }

    @Test
    @Tag("validateProposal")
    void validateProposalEndDateTooSoon() {
        proposal.setEndDate(contract.getStartDate().minusWeeks(2));

        assertThrows(InvalidChangeProposalException.class,
                () -> proposalService.validateContractProposal(proposal),
                "The new end date of contract is too soon.");
    }

    @Test
    @Tag("validateProposal")
    void validateProposalPreviousOneWasNotReviewed() {
        when(proposalRepository.findPendingChange(contract, studentId)).thenReturn(proposal);

        assertThrows(InvalidChangeProposalException.class,
                () -> proposalService.validateContractProposal(proposal),
                "Your previous proposal hasn't been reviewed yet");
    }

    @Test
    @Tag("validateProposalAction")
    void validateActionAccessAllowed() {
        assertDoesNotThrow(() -> proposalService.validateProposalAction(proposal, companyId));
    }

    @Test
    @Tag("validateProposalAction")
    void validateActionAccessDenied() {
        assertThrows(AccessDeniedException.class,
                () -> proposalService.validateProposalAction(proposal, "BillGates"));
    }

    @Test
    @Tag("getProposal")
    void getProposalNotFound() {
        when(proposalRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ChangeProposalNotFoundException.class,
                () -> proposalService.getProposal(1L));
    }

    @Test
    @Tag("getProposal")
    void getProposalSuccess() throws ChangeProposalNotFoundException {
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));

        when(contractService.shouldExpire(contract)).thenReturn(false);

        assertEquals(proposal, proposalService.getProposal(1L));
    }

    @Test
    @Tag("getProposal")
    void getProposalContractExpired() {
        contract.setStatus(ContractStatus.EXPIRED);

        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(contractService.shouldExpire(contract)).thenReturn(false);

        assertThrows(ChangeProposalNotFoundException.class,
                () -> proposalService.getProposal(1L));
        verify(proposalRepository).deleteAllProposalsOfContract(contract);
    }

    @Test
    @Tag("getProposal")
    void getProposalContractTerminated() {
        contract.setStatus(ContractStatus.TERMINATED);

        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(contractService.shouldExpire(contract)).thenReturn(false);

        assertThrows(ChangeProposalNotFoundException.class,
                () -> proposalService.getProposal(1L));
        verify(proposalRepository).deleteAllProposalsOfContract(contract);
    }

    @Test
    @Tag("getProposal")
    void getProposalContractActiveButShouldExpire() {
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(contractService.shouldExpire(contract)).thenReturn(true);


        assertThrows(ChangeProposalNotFoundException.class,
                () -> proposalService.getProposal(1L));
        verify(proposalRepository).deleteAllProposalsOfContract(contractArgumentCaptor.capture());

        //Contract status should have changed to expired before repository call:
        contract.setStatus(ContractStatus.EXPIRED);
        assertEquals(contract, contractArgumentCaptor.getValue());
    }
}