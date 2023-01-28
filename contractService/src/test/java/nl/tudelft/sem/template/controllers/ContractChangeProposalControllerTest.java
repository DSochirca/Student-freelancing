package nl.tudelft.sem.template.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
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
import nl.tudelft.sem.template.services.ContractChangeProposalService;
import nl.tudelft.sem.template.services.ContractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("PMD.AvoidDuplicateLiterals")  // Repeating strings in @Tag are not an issue
class ContractChangeProposalControllerTest {

    @Autowired
    private transient ContractChangeProposalController proposalController;

    @MockBean
    private transient ContractChangeProposalService proposalService;

    @MockBean
    private transient ContractService contractService;

    private final transient String companyId = "TUDelft";
    private final transient String studentId = "JohnDoe";
    private transient Contract contract;
    private transient ContractChangeProposal proposal;
    private transient ContractChangeRequest changeRequest;

    private final transient String unauthenticatedMessage
            = "User has not been authenticated";

    @BeforeEach
    void setUp() {
        LocalDate startDate = LocalDate.of(2021, 12, 25);
        LocalDate endDate = startDate.plusWeeks(3);
        contract = new Contract(1L, companyId, studentId, startDate, endDate, 14,
                42, 15, ContractStatus.ACTIVE, null);

        // Student suggested the change:
        proposal = new ContractChangeProposal(1L, contract, studentId, companyId,
                16d, 50d, 20d, null, ChangeStatus.PENDING);
        //4 weeks
        changeRequest = new ContractChangeRequest(16d, 50d, 20d, null);
    }


    @Test
    @Tag("proposeChange")
    void proposeChangeSuccess() throws AccessDeniedException, InvalidChangeProposalException,
            InactiveContractException, ContractNotFoundException {
        when(proposalService.submitProposal(changeRequest, contract.getId(), studentId))
                .thenReturn(proposal);

        assertEquals(new ResponseEntity<>(proposal, HttpStatus.CREATED),
                proposalController.proposeChange(studentId, contract.getId(), changeRequest));
    }

    @Test
    @Tag("proposeChange")
    void proposeChangeUnauthorized() {
        assertEquals(new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED),
                proposalController.proposeChange("", contract.getId(), changeRequest));
    }

    @Test
    @Tag("proposeChange")
    void proposeChangeContractNotFound() throws AccessDeniedException, InactiveContractException,
            InvalidChangeProposalException, ContractNotFoundException {
        Exception e = new ContractNotFoundException(contract.getId());

        when(proposalService.submitProposal(changeRequest, contract.getId(), studentId))
                .thenThrow(e);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND),
                proposalController.proposeChange(studentId, contract.getId(), changeRequest));
    }

    @Test
    @Tag("proposeChange")
    void proposeChangeInvalidProposal() throws AccessDeniedException, InactiveContractException,
            InvalidChangeProposalException, ContractNotFoundException {
        Exception e = new InvalidChangeProposalException();

        when(proposalService.submitProposal(changeRequest, contract.getId(), studentId))
                .thenThrow(e);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST),
                proposalController.proposeChange(studentId, contract.getId(), changeRequest));
    }

    @Test
    @Tag("proposeChange")
    void proposeChangeInactiveContract() throws AccessDeniedException, InactiveContractException,
            InvalidChangeProposalException, ContractNotFoundException {
        Exception e = new InactiveContractException();

        when(proposalService.submitProposal(changeRequest, contract.getId(), studentId))
                .thenThrow(e);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST),
                proposalController.proposeChange(studentId, contract.getId(), changeRequest));
    }

    @Test
    @Tag("proposeChange")
    void proposeChangeForbidden() throws AccessDeniedException, InvalidChangeProposalException,
            InactiveContractException, ContractNotFoundException {
        Exception e = new AccessDeniedException();

        when(proposalService.submitProposal(changeRequest, contract.getId(), studentId))
                .thenThrow(e);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN),
                proposalController.proposeChange(studentId, contract.getId(), changeRequest));
    }

    @Test
    @Tag("acceptProposal")
    void acceptProposalSuccess() throws AccessDeniedException, InvalidChangeProposalException,
            ChangeProposalNotFoundException {
        when(proposalService.acceptProposal(proposal.getId(), companyId))
                .thenReturn(contract);

        assertEquals(new ResponseEntity<>(contract, HttpStatus.OK),
                proposalController.acceptProposal(companyId, proposal.getId()));
    }

    @Test
    @Tag("acceptProposal")
    void acceptProposalUnauthorized() {
        assertEquals(new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED),
                proposalController.acceptProposal("", proposal.getId()));
    }

    @Test
    @Tag("acceptProposal")
    void acceptProposalNotFound() throws AccessDeniedException, InvalidChangeProposalException,
            ChangeProposalNotFoundException {
        Exception e = new ChangeProposalNotFoundException(proposal.getId());

        when(proposalService.acceptProposal(proposal.getId(), companyId)).thenThrow(e);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND),
                proposalController.acceptProposal(companyId, proposal.getId()));
    }

    @Test
    @Tag("acceptProposal")
    void acceptProposalInvalidProposal() throws AccessDeniedException,
            InvalidChangeProposalException, ChangeProposalNotFoundException {
        Exception e = new InvalidChangeProposalException("error");

        when(proposalService.acceptProposal(proposal.getId(), companyId)).thenThrow(e);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST),
                proposalController.acceptProposal(companyId, proposal.getId()));
    }

    @Test
    @Tag("acceptProposal")
    void acceptProposalForbidden() throws AccessDeniedException, InvalidChangeProposalException,
            ChangeProposalNotFoundException {
        Exception e = new AccessDeniedException();

        when(proposalService.acceptProposal(proposal.getId(), companyId)).thenThrow(e);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN),
                proposalController.acceptProposal(companyId, proposal.getId()));
    }

    @Test
    @Tag("rejectProposal")
    void rejectProposalSuccess() {
        assertEquals(new ResponseEntity<>(HttpStatus.OK),
                proposalController.rejectProposal(companyId, proposal.getId()));
    }

    @Test
    @Tag("rejectProposal")
    void rejectProposalUnauthorized() {
        assertEquals(new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED),
                proposalController.rejectProposal("", proposal.getId()));
    }

    @Test
    @Tag("rejectProposal")
    void rejectProposalNotFound() throws AccessDeniedException, ChangeProposalNotFoundException {
        Exception e = new ChangeProposalNotFoundException(proposal.getId());

        doThrow(e).when(proposalService).rejectProposal(proposal.getId(), companyId);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND),
                proposalController.rejectProposal(companyId, proposal.getId()));
    }

    @Test
    @Tag("rejectProposal")
    void rejectProposalForbidden() throws AccessDeniedException,
            ChangeProposalNotFoundException {
        Exception e = new AccessDeniedException();

        doThrow(e).when(proposalService).rejectProposal(proposal.getId(), companyId);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN),
                proposalController.rejectProposal(companyId, proposal.getId()));
    }

    @Test
    @Tag("deleteProposal")
    void deleteProposalSuccess() {
        assertEquals(new ResponseEntity<>(HttpStatus.OK),
                proposalController.deleteProposal(companyId, proposal.getId()));
    }

    @Test
    @Tag("deleteProposal")
    void deleteProposalUnauthorized() {
        assertEquals(new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED),
                proposalController.deleteProposal("", proposal.getId()));
    }

    @Test
    @Tag("deleteProposal")
    void deleteProposalNotFound() throws AccessDeniedException, ChangeProposalNotFoundException {
        Exception e = new ChangeProposalNotFoundException(proposal.getId());

        doThrow(e).when(proposalService).deleteProposal(proposal.getId(), companyId);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND),
                proposalController.deleteProposal(companyId, proposal.getId()));
    }

    @Test
    @Tag("deleteProposal")
    void deleteProposalForbidden() throws AccessDeniedException,
            ChangeProposalNotFoundException {
        Exception e = new AccessDeniedException();

        doThrow(e).when(proposalService).deleteProposal(proposal.getId(), companyId);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN),
                proposalController.deleteProposal(companyId, proposal.getId()));
    }

    @Test
    @Tag("getProposalsOfContract")
    void getContractProposalsSuccess() throws ContractNotFoundException,
            AccessDeniedException, InactiveContractException {
        when(contractService.getContract(contract.getId())).thenReturn(contract);
        when(proposalService.getProposals(contract, companyId)).thenReturn(List.of(proposal));

        assertEquals(new ResponseEntity<>(List.of(proposal), HttpStatus.OK),
                proposalController.getProposalsOfContract(companyId, contract.getId()));
    }

    @Test
    @Tag("getProposalsOfContract")
    void getContractProposalsNotAuthenticated() {
        assertEquals(new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED),
                proposalController.getProposalsOfContract("", contract.getId()));
    }

    @Test
    @Tag("getProposalsOfContract")
    void getContractProposalsContractNotFound() throws ContractNotFoundException {
        Exception e = new ContractNotFoundException(contract.getId());

        doThrow(e).when(contractService).getContract(contract.getId());

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND),
                proposalController.getProposalsOfContract(companyId, contract.getId()));
    }

    @Test
    @Tag("getProposalsOfContract")
    void getContractProposalsContractNotActive() throws ContractNotFoundException,
            AccessDeniedException, InactiveContractException {
        Exception e = new InactiveContractException();

        when(contractService.getContract(contract.getId())).thenReturn(contract);
        doThrow(e).when(proposalService).getProposals(contract, companyId);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST),
                proposalController.getProposalsOfContract(companyId, contract.getId()));
    }

    @Test
    @Tag("getProposalsOfContract")
    void getContractProposalsContractAccessDenied() throws ContractNotFoundException,
            AccessDeniedException, InactiveContractException {
        Exception e = new AccessDeniedException();

        when(contractService.getContract(contract.getId())).thenReturn(contract);
        doThrow(e).when(proposalService).getProposals(contract, companyId);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN),
                proposalController.getProposalsOfContract(companyId, contract.getId()));
    }

}