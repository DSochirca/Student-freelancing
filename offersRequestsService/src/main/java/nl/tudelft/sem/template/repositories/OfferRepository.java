package nl.tudelft.sem.template.repositories;

import java.util.List;
import nl.tudelft.sem.template.entities.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OfferRepository extends JpaRepository<Offer, Long> {


    @Query(value = "SELECT * "
        + "FROM offer t "
        + "LEFT JOIN (SELECT id, student_id AS s_id FROM offer WHERE dtype = 'StudentOffer') s "
        + "ON t.student_offer_id = s.id "
        + "WHERE t.student_id = ?1 OR t.company_id = ?1 OR s.s_id = ?1 ", nativeQuery = true)
    List<Offer> getAllByUsername(String username);

}
