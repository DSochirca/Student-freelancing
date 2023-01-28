package nl.tudelft.sem.template.interfaces;

import nl.tudelft.sem.template.entities.Contract;
import nl.tudelft.sem.template.entities.ContractChangeProposal;
import nl.tudelft.sem.template.exceptions.AccessDeniedException;
import nl.tudelft.sem.template.exceptions.ContractNotFoundException;
import nl.tudelft.sem.template.exceptions.InactiveContractException;
import nl.tudelft.sem.template.exceptions.InvalidChangeProposalException;
import nl.tudelft.sem.template.exceptions.InvalidContractException;

public interface ContractServiceInterface {
    public Contract saveContract(Contract contract) throws InvalidContractException;

    public Contract getContract(String companyId, String studentId, boolean active, String userId)
            throws ContractNotFoundException, AccessDeniedException;

    public Contract getContract(Long contractId) throws ContractNotFoundException;

    public void terminateContract(Long contractId, String userId)
            throws ContractNotFoundException, InactiveContractException, AccessDeniedException;

    public Contract updateContract(Contract contract, ContractChangeProposal proposal)
            throws InvalidChangeProposalException;
}
