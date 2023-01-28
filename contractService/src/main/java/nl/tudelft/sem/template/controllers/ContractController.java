package nl.tudelft.sem.template.controllers;

import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import nl.tudelft.sem.template.dtos.requests.ContractRequest;
import nl.tudelft.sem.template.entities.Contract;
import nl.tudelft.sem.template.exceptions.AccessDeniedException;
import nl.tudelft.sem.template.exceptions.ContractNotFoundException;
import nl.tudelft.sem.template.exceptions.InactiveContractException;
import nl.tudelft.sem.template.exceptions.InvalidContractException;
import nl.tudelft.sem.template.interfaces.ContractServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContractController {

    @Autowired
    private transient ContractServiceInterface contractService;

    private final transient String nameHeader = "x-user-name";
    private final transient String roleHeader = "x-user-role";
    private final transient String unauthenticatedMessage
            = "User has not been authenticated";

    /**
     * Validates a ContractRequest when creating a contract.
     * If a parameter in contractRequest is null,
     * a MethodArgumentNotValidException is thrown (from the @NotNull annotation),
     * which automatically triggers this method.
     *
     * <p>Method from: baeldung.com/spring-boot-bean-validation
     *
     * @param e The thrown exception containing the error message.
     * @return 400 BAD_REQUEST with all the fields that have been omitted.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException e) {

        // map <Field, Error> explaining which fields were null/empty
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    /**
     * Create a contract. Only available for internal requests.
     *
     * @param contractRequest The contract to create.
     * @return 201 CREATED along with the created contract entity if successful,
     *         400 BAD REQUEST if there already exists a contract between the 2 parties
     *         or if the contract has invalid parameters,
     *         403 FORBIDDEN if the request didn't come from an internal service.
     */
    @PostMapping("/")
    public ResponseEntity<Object> createContract(
            @RequestHeader(roleHeader) String role,
            @Valid @RequestBody ContractRequest contractRequest
    ) {
        if (!role.equals("INTERNAL_SERVICE")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        try {
            Contract c = contractService.saveContract(contractRequest.toContract());
            return new ResponseEntity<>(c, HttpStatus.CREATED);
        } catch (Exception e) {
            return getResponseEntityForException(e);
        }
    }

    /**
     * Terminate an existing contract.
     *
     * @param userName The id of the user making the request.
     * @param id       The id of the contract that should be terminated.
     * @return 200 OK if successful,
     *         400 BAD REQUEST if the contract is not active,
     *         401 UNAUTHORIZED if user is not authenticated,
     *         403 FORBIDDEN if the contract doesn't belong to the user,
     *         404 NOT FOUND if the contract is not found.
     */
    @PutMapping("/{id}/terminate")
    public ResponseEntity<Object> terminateContract(
            @RequestHeader(nameHeader) String userName,
            @PathVariable Long id) {

        // Check if authenticated:
        if (userName.isBlank()) {
            return new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED);
        }

        try {
            contractService.terminateContract(id, userName);
            return ResponseEntity.ok().body(null);
        } catch (Exception e) {
            return getResponseEntityForException(e);
        }
    }

    /**
     * Get the current existing ACTIVE contract between 2 parties.
     *
     * @param userName  The id of the user making the request.
     * @param companyId The id of the company.
     * @param studentId The id of the student.
     * @return 200 OK along with the contract between the company and student,
     *         401 UNAUTHORIZED if user is not authenticated,
     *         403 FORBIDDEN if the contract doesn't belong to the user,
     *         404 NOT FOUND if there is no current active contract.
     */
    @GetMapping("/{companyId}/{studentId}/current")
    public ResponseEntity<Object> getContract(
            @RequestHeader(nameHeader) String userName,
            @PathVariable(name = "companyId") String companyId,
            @PathVariable(name = "studentId") String studentId) {

        // Check if authenticated:
        if (userName.isBlank()) {
            return new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED);
        }

        try {
            Contract contract = contractService.getContract(companyId, studentId, true, userName);
            return ResponseEntity.ok().body(contract);
        } catch (Exception e) {
            return getResponseEntityForException(e);
        }
    }

    /**
     * Get the most recent contract (active or not) between 2 parties.
     *
     * @param userName The id of the user making the request.
     * @param companyId The id of the company.
     * @param studentId The id of the student.
     * @return 200 OK along with the contract between the company and student,
     *         401 UNAUTHORIZED if user is not authenticated,
     *         403 FORBIDDEN if the contract doesn't belong to the user,
     *         404 NOT FOUND if there is no contract between the parties.
     */
    @GetMapping("/{companyId}/{studentId}/mostRecent")
    public ResponseEntity<Object> getMostRecentContract(
            @RequestHeader(nameHeader) String userName,
            @PathVariable(name = "companyId") String companyId,
            @PathVariable(name = "studentId") String studentId) {

        // Check if authenticated:
        if (userName.isBlank()) {
            return new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED);
        }

        try {
            Contract contract = contractService.getContract(companyId, studentId, false, userName);
            return ResponseEntity.ok().body(contract);
        } catch (Exception e) {
            return getResponseEntityForException(e);
        }
    }

    /**
     * Returns a response entity with an error message and a different status
     * for different exceptions.
     *
     * @param e The exception that was thrown.
     * @return A response entity with different statuses for different exceptions.
     */
    private ResponseEntity<Object> getResponseEntityForException(Exception e) {
        if (e instanceof ContractNotFoundException) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

        if (e instanceof AccessDeniedException) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }

        if (e instanceof InvalidContractException) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        if (e instanceof InactiveContractException) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
