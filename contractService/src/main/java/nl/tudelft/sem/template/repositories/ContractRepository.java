package nl.tudelft.sem.template.repositories;

import nl.tudelft.sem.template.entities.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    Contract findFirstByCompanyIdEqualsAndStudentIdEqualsOrderByStartDateDesc(
            String companyId, String studentId);

    @Query("SELECT c FROM Contract c WHERE c.companyId = ?1 "
            + "AND c.studentId = ?2 AND c.status = 'ACTIVE'")
    Contract findActiveContract(String companyId, String studentId);

    @Transactional
    @Modifying
    @Query("UPDATE Contract c SET c.status = 'TERMINATED' WHERE c.id = ?1")
    void terminateContract(Long contractId);

    @Transactional
    @Modifying
    @Query("UPDATE Contract c SET c.status = 'EXPIRED' WHERE c.id = ?1")
    void setExpiredContract(Long contractId);

}
