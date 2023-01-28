package nl.tudelft.sem.template.repositories;

import nl.tudelft.sem.template.entities.Application;
import nl.tudelft.sem.template.entities.NonTargetedCompanyOffer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends
        JpaRepository<Application, Long> {

    boolean existsByStudentIdAndNonTargetedCompanyOffer(String studentId,
                                                           NonTargetedCompanyOffer
                                                                   nonTargetedCompanyOffer);
}
