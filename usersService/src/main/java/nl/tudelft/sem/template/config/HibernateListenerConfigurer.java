package nl.tudelft.sem.template.config;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.stereotype.Component;

/**
 * Appends the <code>UserLoadEventListener</code> handler to the registry listener group.
 */
@Component
public class HibernateListenerConfigurer {
    @PersistenceUnit
    private transient EntityManagerFactory emf;

    @PostConstruct
    protected void init() {
        EventListenerRegistry registry = emf
            .unwrap(SessionFactoryImpl.class)
            .getServiceRegistry()
            .getService(EventListenerRegistry.class);

        registry
            .getEventListenerGroup(EventType.POST_LOAD)
            .appendListener(UserLoadEventListener.INSTANCE);
    }
}
