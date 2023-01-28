package nl.tudelft.sem.template.repositories;

import java.util.List;
import javax.transaction.Transactional;
import nl.tudelft.sem.template.entities.Offer;
import nl.tudelft.sem.template.entities.StudentOffer;
import nl.tudelft.sem.template.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentOfferRepository extends JpaRepository<StudentOffer, Long> {


    //------------
    // UPDATES:
    //------------

    @Transactional
    @Modifying
    @Query("UPDATE StudentOffer o SET o.hoursPerWeek = ?2 WHERE o.id = ?1")
    void updateHoursPerWeek(Long studentOfferId, double hoursPerWeek);

    @Transactional
    @Modifying
    @Query("UPDATE StudentOffer o SET o.totalHours = ?2 WHERE o.id = ?1")
    void updateTotalHours(Long studentOfferId, double totalHours);

    @Transactional
    @Modifying
    @Query("UPDATE StudentOffer o SET o.expertise = ?2 WHERE o.id = ?1")
    void updateExpertise(Long studentOfferId, List<String> expertise);

    @Transactional
    @Modifying
    @Query("UPDATE StudentOffer o SET o.status = ?2 WHERE o.id = ?1")
    void updateStatus(Long studentOfferId, Status status);

    @Transactional
    @Modifying
    @Query("UPDATE StudentOffer o SET o.pricePerHour = ?2 WHERE o.id = ?1")
    void updatePricePerHour(Long studentOfferId, double pricePerHour);
    
    @Query("SELECT s FROM StudentOffer s WHERE s.studentId = ?1")
    List<Offer> findAllByStudentId(String studentId);

    @Query("select s FROM StudentOffer s WHERE s.status = 'PENDING'")
    List<StudentOffer> findAllActive();

    @Query(value = "SELECT * FROM offer WHERE dtype = 'StudentOffer' AND id = ?1",
            nativeQuery = true)
    StudentOffer getById(Long id);

    @Query("SELECT s FROM StudentOffer s WHERE s.description LIKE %?1% "
            + "OR s.title LIKE %?1% OR concat(s.hoursPerWeek, '') LIKE %?1% "
            + "OR concat(s.pricePerHour, '') LIKE %?1% "
            + "OR concat(s.totalHours, '') LIKE %?1%"
            + "AND s.status = 'PENDING'")
    List<StudentOffer> getAllByKeyWord(String keyWord);
}
