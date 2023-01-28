package nl.tudelft.sem.template.controllers;

import java.util.List;
import nl.tudelft.sem.template.dtos.requests.ContractChangeRequest;
import nl.tudelft.sem.template.entities.Contract;
import nl.tudelft.sem.template.entities.ContractChangeProposal;
import nl.tudelft.sem.template.exceptions.AccessDeniedException;
import nl.tudelft.sem.template.exceptions.ChangeProposalNotFoundException;
import nl.tudelft.sem.template.exceptions.ContractNotFoundException;
import nl.tudelft.sem.template.exceptions.InactiveContractException;
import nl.tudelft.sem.template.exceptions.InvalidChangeProposalException;
import nl.tudelft.sem.template.services.ContractChangeProposalService;
import nl.tudelft.sem.template.services.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContractChangeProposalController {

    @Autowired
    private transient ContractChangeProposalService changeProposalService;

    @Autowired
    private transient ContractService contractService;

    private final transient String nameHeader = "x-user-name";
    private final transient String unauthenticatedMessage
            = "User has not been authenticated";

    /**
     * Submit a contract change proposal.
     *
     * @param userName      The id of the user making the request.
     * @param contractId    The id of the contract the user wants to change.
     * @param changeRequest The request containing the new contract parameters.
     * @return 201 CREATED with the saved proposal if the proposal is valid;
     *         400 BAD REQUEST if the contract is inactive
     *         or if the change proposal parameters are invalid,
     *         401 UNAUTHORIZED if user is not authenticated,
     *         403 FORBIDDEN if the user is not in the contract,
     *         404 NOT FOUND if the contract is not found.
     */
    @PostMapping("/{contractId}/changeProposals")
    public ResponseEntity<Object> proposeChange(
            @RequestHeader(nameHeader) String userName,
            @PathVariable Long contractId,
            @RequestBody ContractChangeRequest changeRequest) {

        // Check if authenticated:
        if (userName.isBlank()) {
            return new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED);
        }

        try {
            ContractChangeProposal p
                    = changeProposalService.submitProposal(changeRequest, contractId, userName);

            return new ResponseEntity<>(p, HttpStatus.CREATED);

        } catch (ContractNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InvalidChangeProposalException | InactiveContractException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Accept a contract change proposal.
     *
     * @param userName   The id of the user making the request.
     * @param proposalId The id of the proposal that will be accepted.
     * @return 200 OK with the updated contract if everything is valid;
     *         400 BAD REQUEST if the proposal is invalid
     *         or if the contract is inactive;
     *         401 UNAUTHORIZED if user is not authenticated,
     *         403 FORBIDDEN if the user is not the one that should review the proposal,
     *         404 NOT FOUND if the proposal is not found.
     */
    @PutMapping("/changeProposals/{proposalId}/accept")
    public ResponseEntity<Object> acceptProposal(
            @RequestHeader(nameHeader) String userName,
            @PathVariable(name = "proposalId") Long proposalId) {

        // Check if authenticated:
        if (userName.isBlank()) {
            return new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED);
        }

        try {
            Contract contract = changeProposalService.acceptProposal(proposalId, userName);
            return new ResponseEntity<>(contract, HttpStatus.OK);
        } catch (ChangeProposalNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InvalidChangeProposalException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Reject a contract change proposal.
     *
     * @param userName   The id of the user making the request.
     * @param proposalId The id of the proposal that will be rejected.
     * @return 200 OK if successful,
     *         401 UNAUTHORIZED if user is not authenticated,
     *         403 FORBIDDEN if the user is not the one that should review the proposal,
     *         404 NOT FOUND if the proposal is not found.
     */
    @PutMapping("/changeProposals/{proposalId}/reject")
    public ResponseEntity<String> rejectProposal(
            @RequestHeader(nameHeader) String userName,
            @PathVariable(name = "proposalId") Long proposalId) {

        // Check if authenticated:
        if (userName.isBlank()) {
            return new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED);
        }

        try {
            changeProposalService.rejectProposal(proposalId, userName);
            return ResponseEntity.ok(null);
        } catch (ChangeProposalNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Delete a contract change proposal.
     *
     * @param userName   The id of the user making the request.
     * @param proposalId The id of the proposal that will be deleted.
     * @return 200 OK if successful,
     *         401 UNAUTHORIZED if user is not authenticated,
     *         403 FORBIDDEN if the user is not the one that submitted the proposal,
     *         404 NOT FOUND if the proposal is not found.
     */
    @DeleteMapping("/changeProposals/{proposalId}")
    public ResponseEntity<String> deleteProposal(
            @RequestHeader(nameHeader) String userName,
            @PathVariable(name = "proposalId") Long proposalId) {

        // Check if authenticated:
        if (userName.isBlank()) {
            return new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED);
        }

        try {
            changeProposalService.deleteProposal(proposalId, userName);
            return ResponseEntity.ok(null);
        } catch (ChangeProposalNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Get all proposed changes on a contract.
     *
     * @param userName   The id of the user making the request.
     * @param contractId The id of the contract.
     * @return 200 OK if successful,
     *         400 BAD REQUEST if the contract is inactive,
     *         401 UNAUTHORIZED if user is not authenticated,
     *         403 FORBIDDEN if the user is not in the contract,
     *         404 NOT FOUND if the contract was not found.
     */
    @GetMapping("/{contractId}/changeProposals")
    public ResponseEntity<Object> getProposalsOfContract(
            @RequestHeader(nameHeader) String userName,
            @PathVariable(name = "contractId") Long contractId) {

        // Check if authenticated:
        if (userName.isBlank()) {
            return new ResponseEntity<>(unauthenticatedMessage, HttpStatus.UNAUTHORIZED);
        }

        try {
            // Get contract and mark it expired if needed:
            Contract contract = contractService.getContract(contractId);
            List<ContractChangeProposal> proposals =
                    changeProposalService.getProposals(contract, userName);
            return ResponseEntity.ok().body(proposals);
        } catch (ContractNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InactiveContractException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

}
