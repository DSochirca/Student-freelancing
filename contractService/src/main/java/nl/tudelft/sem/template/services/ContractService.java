package nl.tudelft.sem.template.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import logger.FileLogger;
import nl.tudelft.sem.template.entities.Contract;
import nl.tudelft.sem.template.entities.ContractChangeProposal;
import nl.tudelft.sem.template.enums.ContractStatus;
import nl.tudelft.sem.template.exceptions.AccessDeniedException;
import nl.tudelft.sem.template.exceptions.ContractNotFoundException;
import nl.tudelft.sem.template.exceptions.InactiveContractException;
import nl.tudelft.sem.template.exceptions.InvalidChangeProposalException;
import nl.tudelft.sem.template.exceptions.InvalidContractException;
import nl.tudelft.sem.template.interfaces.ContractServiceInterface;
import nl.tudelft.sem.template.repositories.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContractService implements ContractServiceInterface {

    @Autowired
    private transient ContractRepository contractRepository;
    @Autowired
    private transient FileLogger logger;

    private static final transient double MAX_HOURS = 20;
    private static final transient double MAX_WEEKS = 26;


    /**
     * Saves a contract in the repository.
     *
     * @param contract The contract to be saved.
     * @return The saved contract.
     * @throws InvalidContractException If the contract has invalid parameters
     *                                  or if one already exists.
     */
    public Contract saveContract(Contract contract) throws InvalidContractException {
        // Check contract parameters:
        validateContract(contract);

        // Set startDate:
        contract.setStartDate(LocalDate.now());

        // Set endDate:
        int weeks = (int) Math.ceil(contract.getTotalHours() / contract.getHoursPerWeek());
        LocalDate date = contract.getStartDate().plusWeeks(weeks);
        contract.setEndDate(date);    //LocalDate is immutable, so different memory address here

        // Set as active
        contract.setStatus(ContractStatus.ACTIVE);

        contract = contractRepository.save(contract);
        logger.log("Contract "
            + contract.getId()
            + " has been saved between company "
            + contract.getCompanyId()
            + " and student "
            + contract.getStudentId());

        return contract;
    }

    /**
     * Gets the contract (active or not) between 2 parties from the repository.
     *
     * @param companyId The id of the company.
     * @param studentId The id of the student.
     * @param active    If the query needs an active contract or not.
     * @param userId    The id of the user that wants to get the contract.
     * @return The found contract or null if not found.
     * @throws ContractNotFoundException Thrown if a contract doesn't exist.
     * @throws AccessDeniedException     If the user doesn't have rights to access the contract.
     */
    public Contract getContract(String companyId, String studentId, boolean active, String userId)
            throws ContractNotFoundException, AccessDeniedException {
        // Check authorization:
        if (!companyId.equals(userId) && !studentId.equals(userId)) {
            throw new AccessDeniedException();
        }

        Contract contract;
        // If we need the current active contract:
        if (active) {
            // Retrieve current active contract:
            contract = getActive(companyId, studentId);
        } else {
            // Retrieve most recent (active or not) contract:
            contract = getNewest(companyId, studentId);
        }

        return contract;
    }

    /**
     * Get a contract by the passed id.
     * Also used to check if a contract exists.
     * No authorization checks here because it's not used by the controllers.
     *
     * @param contractId The id of the contract.
     * @throws ContractNotFoundException If the contract doesn't exist.
     */
    public Contract getContract(Long contractId) throws ContractNotFoundException {
        Optional<Contract> c = contractRepository.findById(contractId);

        // If contract doesn't exist throw exception:
        if (c.isEmpty()) {
            throw new ContractNotFoundException(contractId);
        }

        Contract contract = c.get();

        // Check if it should expire (updates it in the repository as well):
        if (shouldExpire(contract)) {
            contract.setStatus(ContractStatus.EXPIRED);
        }

        return contract;
    }

    /**
     * Gets an active Contract.
     *
     * @param companyId - the id of the company.
     * @param studentId - the id of the student.
     * @return - An active Contract.
     * @throws ContractNotFoundException - May be thrown,
     *      if contract doesn't exist or should Expire.
     */
    private Contract getActive(String companyId, String studentId)
            throws ContractNotFoundException {
        Contract contract = contractRepository.findActiveContract(companyId, studentId);

        // Check if it should expire (updates it in the repository as well):
        if (contract == null || shouldExpire(contract)) {
            throw new ContractNotFoundException(companyId, studentId);
        }

        return contract;
    }

    /**
     * Gets the most recent Contract.
     *
     * @param companyId - the id of the company.
     * @param studentId - the id of the student.
     * @return - The most recent Contract.
     * @throws ContractNotFoundException - May be thrown,
     *      if contract doesn't exist.
     */
    private Contract getNewest(String companyId, String studentId)
            throws ContractNotFoundException {
        Contract contract = contractRepository
                .findFirstByCompanyIdEqualsAndStudentIdEqualsOrderByStartDateDesc(
                        companyId, studentId
                );

        if (contract == null) {
            throw new ContractNotFoundException(companyId, studentId);
        }

        // Check if it should expire (updates it in the repository as well):
        if (shouldExpire(contract)) {
            contract.setStatus(ContractStatus.EXPIRED);
        }
        return contract;
    }

    /**
     * Terminates a contract.
     *
     * @param contractId The contract that is to be terminated.
     * @param userId     The id of the user that wants the contract terminated.
     * @throws ContractNotFoundException If the contract doesn't exist.
     * @throws InactiveContractException If the contract was already cancelled or expired.
     * @throws AccessDeniedException     If the user doesn't have rights to terminate the contract.
     */
    public void terminateContract(Long contractId, String userId)
            throws ContractNotFoundException, InactiveContractException, AccessDeniedException {
        // Check if contract exists and get contract:
        Contract contract = getContract(contractId);

        // Check if user is a participant in the contract:
        checkAuthorization(contract, userId);

        // Terminate contract only if it is active:
        if (!contract.getStatus().equals(ContractStatus.ACTIVE)) {
            throw new InactiveContractException();
        }
        logger.log("Contract "
            + contractId
            + " has been terminated");
        contractRepository.terminateContract(contractId);
    }

    /**
     * Updates a contract when a proposed change is accepted.
     * <br><br>
     * This method is called only if the contract is active, so no checks for that are needed.
     * No authorization checks because they are done before the call to this method.
     * <br>
     *
     * @param contract The contract that should be changed.
     * @param proposal The proposed changes.
     * @return The updated contract entity.
     * @throws InvalidChangeProposalException If the proposal exceeds the max no. of hours/weeks
     *                                        allowed or the end date is invalid.
     */
    public Contract updateContract(Contract contract, ContractChangeProposal proposal)
            throws InvalidChangeProposalException {
        // Update contract with values from proposal:
        if (proposal.getHoursPerWeek() != null) {
            contract.setHoursPerWeek(proposal.getHoursPerWeek());
        }

        if (proposal.getTotalHours() != null) {
            contract.setTotalHours(proposal.getTotalHours());
        }

        if (proposal.getPricePerHour() != null) {
            contract.setPricePerHour(proposal.getPricePerHour());
        }

        // Set computed end date:
        double weeks = contract.getTotalHours() / contract.getHoursPerWeek();
        LocalDate endDate = contract.getStartDate().plusWeeks((int) Math.ceil(weeks));
        contract.setEndDate(endDate);

        // If proposed end date isn't null set as new end date:
        if (proposal.getEndDate() != null) {
            LocalDate proposedEndDate = proposal.getEndDate();

            // Check if proposal end date is after the minimum required end date:
            if (proposedEndDate.isBefore(endDate)) {
                throw new InvalidChangeProposalException(
                        "The new end date of contract is too soon.");
            }

            weeks = ChronoUnit.WEEKS.between(contract.getStartDate(), proposedEndDate);
            contract.setEndDate(proposedEndDate);
        }


        // Check if too many hours per week or too many weeks:
        if (contract.getHoursPerWeek() > MAX_HOURS || weeks > MAX_WEEKS) {
            throw new InvalidChangeProposalException("This change proposal is not valid anymore, "
                    + "due to past modifications to the contract.");
        }

        return contractRepository.save(contract);
    }

    //----------------------------------------
    //      HELPER METHODS:
    //----------------------------------------

    /**
     * PRIVATE HELPER METHOD which validates a contract's parameters.
     *
     * @param contract The contract to be validated.
     * @throws InvalidContractException Thrown when the contract is not valid
     *                                  e.g. exceeds 20 hours per week, 6 month duration
     *                                  or the company's id and student's id are the same
     *                                  or if there is an existing active contract already.
     */
    public void validateContract(Contract contract)
            throws InvalidContractException {
        // Contract between the same user:
        if (contract.getCompanyId().equals(contract.getStudentId())

                // Max no of hours exceeded:
                || contract.getHoursPerWeek() > MAX_HOURS

                // No of weeks exceeded:
                || contract.getTotalHours() / contract.getHoursPerWeek() > MAX_WEEKS) {

            throw new InvalidContractException();
        }
        Contract existingContract = contractRepository.findActiveContract(
                contract.getCompanyId(), contract.getStudentId());
        if (existingContract != null) {
            throw new InvalidContractException(
                    "Please cancel the existing contract (#" + contract.getId()
                            + ") with this party.");
        }
    }

    /**
     * Expires a contract if the end date passed already.
     *
     * @param contract The contract to expire.
     * @return true if the contract should expire,
     *         false if it shouldn't or was already expired/terminated.
     */
    public boolean shouldExpire(Contract contract) {
        if (contract == null) {
            return false;
        }

        // Contract already expired or terminated so no further checks:
        if (contract.getStatus() != ContractStatus.ACTIVE) {
            return false;
        }

        LocalDate today = LocalDate.now();
        if (contract.getEndDate().isBefore(today)) {
            // Set contract as expired:
            contractRepository.setExpiredContract(contract.getId());
            return true;
        }
        return false;
    }

    /**
     * Checks if the user can access the contract entity,
     * which is if the userId is in the contract (as companyId or studentId).
     *
     * @param contract The contract the user wants to access.
     * @param userId   The userId of the user we check.
     * @throws AccessDeniedException If the user isn't in the contract.
     */
    public void checkAuthorization(Contract contract, String userId)
            throws AccessDeniedException {
        if (!contract.getStudentId().equals(userId) && !contract.getCompanyId().equals(userId)) {
            throw new AccessDeniedException();
        }
    }

}
