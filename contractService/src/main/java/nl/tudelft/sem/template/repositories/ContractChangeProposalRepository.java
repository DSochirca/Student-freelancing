package nl.tudelft.sem.template.repositories;

import java.util.List;
import nl.tudelft.sem.template.entities.Contract;
import nl.tudelft.sem.template.entities.ContractChangeProposal;
import nl.tudelft.sem.template.enums.ChangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ContractChangeProposalRepository extends
        JpaRepository<ContractChangeProposal, Long> {

    List<ContractChangeProposal> findAllByContract(Contract c);

    @Query("SELECT p FROM ContractChangeProposal p WHERE p.contract = ?1 "
            + "AND p.proposer= ?2 AND p.status = 'PENDING'")
    ContractChangeProposal findPendingChange(Contract c, String proposer);

    @Transactional
    @Modifying
    @Query("UPDATE ContractChangeProposal p SET p.status = 'REJECTED' WHERE p.id = ?1")
    void rejectProposal(Long proposalId);

    @Transactional
    @Modifying
    @Query("UPDATE ContractChangeProposal p SET p.status = 'ACCEPTED' WHERE p.id = ?1")
    void acceptProposal(Long proposalId);

    @Transactional
    @Modifying
    @Query("DELETE FROM ContractChangeProposal p WHERE p.contract = ?1 AND p.status = 'REJECTED'")
    void deleteAllRejectedProposalsOfContract(Contract c);

    @Transactional
    @Modifying
    @Query("DELETE FROM ContractChangeProposal p WHERE p.contract = ?1")
    void deleteAllProposalsOfContract(Contract c);
}
