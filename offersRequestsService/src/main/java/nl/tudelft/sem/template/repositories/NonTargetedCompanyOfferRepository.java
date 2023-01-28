package nl.tudelft.sem.template.repositories;

import java.util.List;
import javax.transaction.Transactional;
import lombok.NonNull;
import nl.tudelft.sem.template.entities.NonTargetedCompanyOffer;
import nl.tudelft.sem.template.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NonTargetedCompanyOfferRepository extends
        JpaRepository<NonTargetedCompanyOffer, Long> {


    @Query(value = "SELECT * FROM offer "
            + "WHERE id = ?1 AND dtype = 'NonTargetedCompanyOffer'", nativeQuery = true)
    NonTargetedCompanyOffer getOfferById(Long offerId);
}
