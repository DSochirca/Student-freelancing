package nl.tudelft.sem.template.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import nl.tudelft.sem.template.dtos.requests.ContractRequest;
import nl.tudelft.sem.template.entities.Contract;
import nl.tudelft.sem.template.enums.ContractStatus;
import nl.tudelft.sem.template.exceptions.AccessDeniedException;
import nl.tudelft.sem.template.exceptions.ContractNotFoundException;
import nl.tudelft.sem.template.exceptions.InactiveContractException;
import nl.tudelft.sem.template.exceptions.InvalidContractException;
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
class ContractControllerTest {

    @Autowired
    private transient ContractController contractController;

    @MockBean
    private transient ContractService contractService;

    private transient Contract contract;
    private final transient String companyId = "TUDelft";
    private final transient String studentId = "JohnDoe";

    private transient ContractRequest contractRequest;
    private transient Contract contractFromRequest;

    private final transient String unauthenticatedMessage
            = "User has not been authenticated";

    @BeforeEach
    void setUp() {
        LocalDate startDate = LocalDate.of(2021, 12, 25);
        LocalDate endDate = startDate.plusWeeks(3);
        contract = new Contract(1L, companyId, studentId, startDate, endDate, 14,
                42, 15, ContractStatus.ACTIVE, null);

        // Contract passed with the request body:
        contractRequest = new ContractRequest(companyId, studentId, 14d, 42d, 15d);
        contractFromRequest = contractRequest.toContract();
    }

    @Test
    @Tag("createContract")
    void createContractSuccess() throws InvalidContractException {
        when(contractService.saveContract(contractFromRequest)).thenReturn(contract);

        assertEquals(new ResponseEntity<>(contract, HttpStatus.CREATED),
                contractController.createContract("INTERNAL_SERVICE", contractRequest));
        verify(contractService).saveContract(contractFromRequest);
    }

    @Test
    @Tag("createContract")
    void createContractFailed() throws InvalidContractException {
        InvalidContractException e = new InvalidContractException();

        when(contractService.saveContract(contractFromRequest)).thenThrow(e);

        assertEquals(ResponseEntity.badRequest().body(e.getMessage()),
                contractController.createContract("INTERNAL_SERVICE", contractRequest));
    }

    @Test
    @Tag("createContract")
    void createContractForbidden() {
        assertEquals(new ResponseEntity<>(HttpStatus.FORBIDDEN),
                contractController.createContract("student", contractRequest));
    }

    @Test
    @Tag("terminateContract")
    void terminateContractSuccess() throws AccessDeniedException,
            InactiveContractException, ContractNotFoundException {
        assertEquals(ResponseEntity.ok().body(null),
                contractController.terminateContract(studentId, contract.getId()));

        verify(contractService).terminateContract(contract.getId(), studentId);
    }

    @Test
    @Tag("terminateContract")
    void terminateContractUnauthorized() {
        assertEquals(new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED),
                contractController.terminateContract("", contract.getId()));
    }

    @Test
    @Tag("terminateContract")
    void terminateContractNotFound() throws ContractNotFoundException,
            InactiveContractException, AccessDeniedException {
        Exception e = new ContractNotFoundException(contract.getId());

        doThrow(e).when(contractService).terminateContract(contract.getId(), studentId);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND),
                contractController.terminateContract(studentId, contract.getId()));
    }

    @Test
    @Tag("terminateContract")
    void terminateContractNotActive() throws ContractNotFoundException,
            InactiveContractException, AccessDeniedException {
        Exception e = new InactiveContractException();

        doThrow(e).when(contractService).terminateContract(contract.getId(), studentId);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST),
                contractController.terminateContract(studentId, contract.getId()));
    }

    @Test
    @Tag("terminateContract")
    void terminateContractForbidden() throws ContractNotFoundException,
            InactiveContractException, AccessDeniedException {
        Exception e = new AccessDeniedException();

        doThrow(e).when(contractService).terminateContract(contract.getId(), studentId);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN),
                contractController.terminateContract(studentId, contract.getId()));
    }

    @Test
    @Tag("getContract")
    void getContractSuccess() throws ContractNotFoundException, AccessDeniedException {
        when(contractService.getContract(companyId, studentId, true, studentId))
                .thenReturn(contract);

        assertEquals(ResponseEntity.ok().body(contract),
                contractController.getContract(studentId, companyId, studentId));
    }

    @Test
    @Tag("getContract")
    void getContractUnauthorized() {
        assertEquals(new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED),
                contractController.getContract("", companyId, studentId));
    }

    @Test
    @Tag("getContract")
    void getContractNotFound() throws AccessDeniedException, ContractNotFoundException {
        Exception e = new ContractNotFoundException(companyId, studentId);

        when(contractService.getContract(companyId, studentId, true, studentId)).thenThrow(e);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND),
                contractController.getContract(studentId, companyId, studentId));
    }

    @Test
    @Tag("getContract")
    void getContractAccessDenied() throws AccessDeniedException, ContractNotFoundException {
        Exception e = new AccessDeniedException();

        when(contractService.getContract(companyId, studentId, true, studentId)).thenThrow(e);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN),
                contractController.getContract(studentId, companyId, studentId));
    }

    @Test
    @Tag("getMostRecentContract")
    void getMostRecentContractSuccess() throws ContractNotFoundException, AccessDeniedException {
        when(contractService.getContract(companyId, studentId, false, studentId))
                .thenReturn(contract);

        assertEquals(ResponseEntity.ok().body(contract),
                contractController.getMostRecentContract(studentId, companyId, studentId));
    }

    @Test
    @Tag("getMostRecentContract")
    void getMostRecentContractUnauthorized() {
        assertEquals(new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED),
                contractController.getMostRecentContract("", companyId, studentId));
    }

    @Test
    @Tag("getMostRecentContract")
    void getMostRecentContractNotFound() throws AccessDeniedException, ContractNotFoundException {
        Exception e = new ContractNotFoundException(companyId, studentId);

        when(contractService.getContract(companyId, studentId, false, studentId)).thenThrow(e);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND),
                contractController.getMostRecentContract(studentId, companyId, studentId));
    }

    @Test
    @Tag("getMostRecentContract")
    void getMostRecentContractAccessDenied() throws AccessDeniedException,
            ContractNotFoundException {
        Exception e = new AccessDeniedException();

        when(contractService.getContract(companyId, studentId, false, studentId)).thenThrow(e);

        assertEquals(new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN),
                contractController.getMostRecentContract(studentId, companyId, studentId));
    }
}