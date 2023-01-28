package nl.tudelft.sem.template.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")  // DU Anomaly is a false positive here
public class ContractChangeProposalService {

    @Autowired
    private transient ContractChangeProposalRepository changeProposalRepository;

    @Autowired
    private transient ContractService contractService;

    private static final transient double MAX_HOURS = 20;
    private static final transient double MAX_WEEKS = 26;

    /**
     * Saves a change proposal to the repository.
     *
     * @param changeRequest The request entity with the proposed change parameters.
     * @param contractId    The id of the contract the change is suggested on.
     * @param userId        The id of the user that wants the change.
     * @return The saved proposal entity.
     * @throws InvalidChangeProposalException If the proposal is invalid
     * @throws InactiveContractException      If the contract is not active.
     * @throws ContractNotFoundException      If the contract doesn't exist.
     * @throws AccessDeniedException          If user is not in the contract.
     */
    public ContractChangeProposal submitProposal(ContractChangeRequest changeRequest,
                                                 Long contractId, String userId)
            throws InvalidChangeProposalException, InactiveContractException,
            ContractNotFoundException, AccessDeniedException {
        // Get contract (also marks it as expired if needed):
        Contract contract = contractService.getContract(contractId);

        // Convert to the contractChangeProposalEntity:
        // Checks for authorization as well:
        ContractChangeProposal proposal = changeRequest.toContractChangeProposal(contract, userId);

        // Check if proposal is valid and if contract is active:
        validateContractProposal(proposal);

        return changeProposalRepository.save(proposal);
    }

    /**
     * Accept a proposal and update the contract information.
     *
     * @param proposalId  The id of the proposal.
     * @param participant The user that accepts the proposal.
     * @return The updated contract.
     * @throws ChangeProposalNotFoundException If the proposal doesn't exist.
     * @throws InvalidChangeProposalException  If the proposal's parameters are no longer valid,
     *                                         meaning the contract was changed once before.
     *                                         (not likely to happen but just for caution)
     * @throws AccessDeniedException           If the participant is not in the contract.
     */
    public Contract acceptProposal(Long proposalId, String participant)
            throws ChangeProposalNotFoundException, InvalidChangeProposalException,
            AccessDeniedException {

        // Check if proposal exists and if contract is active:
        ContractChangeProposal proposal = getProposal(proposalId);

        // Check if the participant can reject the proposal:
        validateProposalAction(proposal, participant);

        // Try to update contract:
        Contract updatedContract =
                contractService.updateContract(proposal.getContract(), proposal);

        // If successful accept proposal:
        changeProposalRepository.acceptProposal(proposalId);
        // Delete all past rejected proposals:
        changeProposalRepository.deleteAllRejectedProposalsOfContract(proposal.getContract());

        return updatedContract;
    }

    /**
     * Reject a proposal by its id.
     *
     * @param proposalId  The proposal's id.
     * @param participant The user that rejects the proposal.
     * @throws ChangeProposalNotFoundException If the proposal doesn't exist.
     * @throws AccessDeniedException           If the participant is not in the contract.
     */
    public void rejectProposal(Long proposalId, String participant)
            throws ChangeProposalNotFoundException,
            AccessDeniedException {

        // Check if proposal exists and if contract is active:
        ContractChangeProposal proposal = getProposal(proposalId);
        // Check if the participant can reject the proposal:
        validateProposalAction(proposal, participant);

        changeProposalRepository.rejectProposal(proposalId);
    }

    /**
     * Delete a proposal by its id.
     *
     * @param proposalId The id of the proposal.
     * @param proposer   The user that proposed the change and wants to delete it.
     * @throws ChangeProposalNotFoundException If the proposer is not in the contract
     *                                         or if the proposal doesn't exist.
     * @throws AccessDeniedException           If the proposer is not in the contract.
     */
    public void deleteProposal(Long proposalId, String proposer)
            throws ChangeProposalNotFoundException, AccessDeniedException {
        // Check if proposal exists and if contract is active:
        ContractChangeProposal proposal = getProposal(proposalId);

        // Can't delete proposal if it was reviewed (accepted/rejected):
        if (proposal.getStatus() != ChangeStatus.PENDING) {
            throw new ChangeProposalNotFoundException(
                    "This proposal was already reviewed and cannot be deleted");
        }

        // Check if proposer is owner of the proposal:
        if (!proposal.getProposer().equals(proposer)) {
            throw new AccessDeniedException();
        }
        changeProposalRepository.deleteById(proposalId);
    }

    /**
     * Get all changes proposed for a contract.
     *
     * @param contract The contract the user checks for proposals.
     * @param userId   The id of the user.
     * @return List of all change proposals for that contract.
     * @throws AccessDeniedException     If the user is not in the contract.
     * @throws InactiveContractException If the contract is no longer active.
     */
    public List<ContractChangeProposal> getProposals(Contract contract, String userId)
            throws AccessDeniedException, InactiveContractException {
        // Check if user is a participant in the contract:
        if (!contract.getCompanyId().equals(userId) && !contract.getStudentId().equals(userId)) {
            throw new AccessDeniedException();
        }

        // Check if contract is still active:
        if (!contract.getStatus().equals(ContractStatus.ACTIVE)) {
            throw new InactiveContractException();
        }

        return changeProposalRepository.findAllByContract(contract);
    }

    //----------------------------------------
    //      HELPER METHODS:
    //----------------------------------------

    /**
     * HELPER METHOD which validates a contract change proposal's parameters.
     *
     * @param proposal The proposal to be validated.
     * @throws InvalidChangeProposalException Thrown when the proposal is not valid
     *                                        e.g. exceeds 20 hours per week, 6 month duration
     *                                        or the company's id and student's id are the same
     *                                        or if the contract is expired or cancelled
     *                                        or if the previous proposal wasn't reviewed.
     * @throws InactiveContractException      Thrown if the contract has expired or was terminated.
     */
    public void validateContractProposal(ContractChangeProposal proposal)
            throws InvalidChangeProposalException, InactiveContractException {
        // When creating the proposal from a request there are
        // already checks to see if the 'proposer' is in the contract
        // also at least one suggested change field is not null.

        Contract contract = proposal.getContract();
        ContractStatus contractStatus = contract.getStatus();

        // Contract expired or terminated:
        if (contractStatus != ContractStatus.ACTIVE) {
            throw new InactiveContractException();
        }

        // If proposal didn't include hoursPerWeek or totalHours, use values from contract:
        double hoursPerWeek = proposal.getHoursPerWeek() != null
                ? proposal.getHoursPerWeek() : contract.getHoursPerWeek();
        double totalHours = proposal.getTotalHours() != null
                ? proposal.getTotalHours() : contract.getTotalHours();

        // Hours per week exceeded:
        if (hoursPerWeek > MAX_HOURS) {
            throw new InvalidChangeProposalException();
        }

        // Check if number of weeks is exceeded:
        validateNumberOfWeeks(totalHours / hoursPerWeek, contract, proposal);

        // If there already is a pending change proposal by this user:
        if (changeProposalRepository.findPendingChange(proposal.getContract(),
                proposal.getProposer()) != null) {
            throw new InvalidChangeProposalException(
                    "Your previous proposal hasn't been reviewed yet");
        }
    }

    /**
     * Checks if number of weeks proposed are beyond the 6-month limit.
     *
     * @param totalWeeks The computed total weeks (totalHours / hoursPerWeek)
     * @param contract The contract that needs change.
     * @param proposal The proposal that is created.
     * @throws InvalidChangeProposalException If the #weeks > 26 (6 months).
     */
    private void validateNumberOfWeeks(double totalWeeks, Contract contract,
                                       ContractChangeProposal proposal)
            throws InvalidChangeProposalException {

        // If there is a new, proposed end date (should be later than totalWeeks):
        if (proposal.getEndDate() != null) {
            LocalDate computedEndDate =
                    contract.getStartDate().plusWeeks((int) Math.ceil(totalWeeks));
            LocalDate proposedEndDate = proposal.getEndDate();

            // Check if proposal end date is after the minimum required end date:
            if (proposedEndDate.isBefore(computedEndDate)) {
                throw new InvalidChangeProposalException(
                        "The new end date of contract is too soon.");
            }

            totalWeeks = ChronoUnit.WEEKS.between(contract.getStartDate(), proposedEndDate);
        }

        // No of weeks exceeded:
        if (totalWeeks > MAX_WEEKS) {
            throw new InvalidChangeProposalException();
        }
    }

    /**
     * HELPER METHOD which checks if a change proposal can be accepted / rejected.
     *
     * @param proposal    The proposal to be validated.
     * @param participant The id of the user that wants to accept / reject the proposal.
     * @throws IllegalArgumentException Thrown when the participant isn't in the proposal
     *                                  (only the contract participant can accept/reject a proposal)
     *                                  or if the contract is expired or cancelled.
     */
    public void validateProposalAction(ContractChangeProposal proposal, String participant)
            throws AccessDeniedException {

        // Participant is not in the contract:
        if (!proposal.getParticipant().equals(participant)) {
            throw new AccessDeniedException();
        }
    }

    /**
     * Get a proposal by the passed id.
     * Also used to check if a proposal exists.
     *
     * @param proposalId The id of the proposal.
     * @throws ChangeProposalNotFoundException If the proposal doesn't exist.
     */
    public ContractChangeProposal getProposal(Long proposalId)
            throws ChangeProposalNotFoundException {

        Optional<ContractChangeProposal> p = changeProposalRepository.findById(proposalId);

        if (p.isEmpty()) {
            throw new ChangeProposalNotFoundException(proposalId);
        } else {
            // Get the proposal only if the contract is active:

            ContractChangeProposal proposal = p.get();
            Contract contract = proposal.getContract();

            // Set contract as expired if needed (also updates it in the repository):
            if (contractService.shouldExpire(contract)
                    || contract.getStatus() != ContractStatus.ACTIVE) {

                // Set locally as well for the next repository call:
                if (contract.getStatus() == ContractStatus.ACTIVE) {
                    contract.setStatus(ContractStatus.EXPIRED);
                }
                // Get rid of the proposals if the contract is not active:
                changeProposalRepository.deleteAllProposalsOfContract(contract);

                // Not found because it was deleted:
                throw new ChangeProposalNotFoundException(proposalId);
            }

            return proposal;
        }
    }
}
