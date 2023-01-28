package nl.tudelft.sem.template.services;

import java.util.List;
import java.util.Optional;
import javax.naming.NoPermissionException;
import javax.transaction.Transactional;
import logger.FileLogger;
import nl.tudelft.sem.template.entities.Application;
import nl.tudelft.sem.template.entities.NonTargetedCompanyOffer;
import nl.tudelft.sem.template.entities.dtos.ContractDto;
import nl.tudelft.sem.template.enums.Status;
import nl.tudelft.sem.template.exceptions.ContractCreationException;
import nl.tudelft.sem.template.repositories.ApplicationRepository;
import nl.tudelft.sem.template.repositories.NonTargetedCompanyOfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NonTargetedCompanyOfferService extends OfferService {

    @Autowired
    private transient NonTargetedCompanyOfferRepository nonTargetedCompanyOfferRepository;

    @Autowired
    private transient ApplicationRepository applicationRepository;

    @Autowired
    private transient Utility utility;

    @Autowired
    private transient FileLogger logger;

    /** Method for applying to a NonTargetedCompanyOffer.
     *
     * @param application Application that we want to save.
     * @param offerId Offer the application targets.
     * @return Application if saved successfully.
     */
    public Application apply(Application application, Long offerId) {
        NonTargetedCompanyOffer nonTargetedCompanyOffer = nonTargetedCompanyOfferRepository
                .getOfferById(offerId);
        if (nonTargetedCompanyOffer == null) {
            throw new IllegalArgumentException("There is no offer associated with this id");
        }
        if (nonTargetedCompanyOffer.getStatus() != Status.PENDING) {
            throw new IllegalArgumentException("This offer is not active anymore");
        }
        if (applicationRepository
                .existsByStudentIdAndNonTargetedCompanyOffer(
                        application.getStudentId(),
                        nonTargetedCompanyOffer)) {
            throw new IllegalArgumentException("Student already applied to this offer");
        }
        logger.log(application.getStudentId()
                    + " has applied for "
                    + offerId);

        application.setNonTargetedCompanyOffer(nonTargetedCompanyOffer);
        application.setStatus(Status.PENDING);
        return applicationRepository.save(application);
    }

    /**
     /**
     * Service, which accepts an application and declines all others.
     *
     * @param userName - the potential company's id.
     * @param id - the id of the application.
     * @throws NoPermissionException - is thrown
     *      if the user doesn't have permission to accept the application.
     * @throws ContractCreationException - if the request wasn't successful.
     */
    @Transactional
    public ContractDto accept(String userName, Long id)
            throws NoPermissionException, ContractCreationException {
        Optional<Application> application = applicationRepository.findById(id);
        if (application.isEmpty()) {
            throw new IllegalArgumentException(
                    "The application does not exist");
        }
        NonTargetedCompanyOffer nonTargetedCompanyOffer =
                application.get().getNonTargetedCompanyOffer();

        if (!userName.equals(nonTargetedCompanyOffer.getCompanyId())) {
            throw new NoPermissionException("User can not accept this application!");
        }
        if (nonTargetedCompanyOffer.getStatus() != Status.PENDING
            || application.get().getStatus() != Status.PENDING) {
            throw new IllegalArgumentException("The offer or application is not active anymore!");
        }

        ContractDto contract;
        // First try to create the contract between the 2 parties.
        // If the contract creation doesn't succeed then the application isn't accepted.
        // Throws exception if error:
        contract = utility.createContract(userName, application.get().getStudentId(),
                nonTargetedCompanyOffer.getHoursPerWeek(),
                nonTargetedCompanyOffer.getTotalHours(),
                application.get().getPricePerHour());

        List<Application> applications = nonTargetedCompanyOffer.getApplications();

        for (Application app : applications) {
            if (app.equals(application.get())) {
                app.setStatus(Status.ACCEPTED);
            } else {
                app.setStatus(Status.DECLINED);
            }
            applicationRepository.save(app);
        }
        logger.log(nonTargetedCompanyOffer.getCreatorUsername()
                    + " has accepted "
                    + application.get().getStudentId()
                    + " for offer "
                    + nonTargetedCompanyOffer.getId());
        nonTargetedCompanyOffer.setStatus(Status.DISABLED);
        nonTargetedCompanyOfferRepository.save(nonTargetedCompanyOffer);

        return contract;
    }
}
