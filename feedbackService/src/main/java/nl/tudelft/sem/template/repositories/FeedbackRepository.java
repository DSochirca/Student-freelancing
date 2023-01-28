package nl.tudelft.sem.template.repositories;

import java.util.List;
import nl.tudelft.sem.template.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    @Query("SELECT f FROM Feedback f WHERE f.author = ?1 "
        + "AND f.recipient = ?2 AND f.contractId = ?3")
    List<Feedback> hasReviewedBefore(String authorId, String recipientId, Long contractId);

    @Query("SELECT COALESCE(AVG(f.rating), -1) FROM Feedback f WHERE f.recipient = ?1")
    public double getAverageRatingByUser(String username);

}
