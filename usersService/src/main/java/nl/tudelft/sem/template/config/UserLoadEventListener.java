package nl.tudelft.sem.template.config;

import nl.tudelft.sem.template.entities.Admin;
import nl.tudelft.sem.template.entities.Company;
import nl.tudelft.sem.template.entities.Student;
import nl.tudelft.sem.template.entities.User;
import nl.tudelft.sem.template.enums.Role;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.springframework.stereotype.Component;

/**
 * Custom event handler for whenever an instance is loaded by Hibernate. In this case the
 * <code>Role</code> transient attribute is populated by checking which subclass the entity
 * belongs to.
 */
@Component
public class UserLoadEventListener implements PostLoadEventListener {

    public static final long serialVersionUID = 40414244;

    public static final UserLoadEventListener INSTANCE = new UserLoadEventListener();

    @Override
    public void onPostLoad(PostLoadEvent event) {
        final Object entity = event.getEntity();
        if (entity instanceof User) {
            User user = (User) entity;

            if (user instanceof Student) {
                user.setRole(Role.STUDENT);
            } else if (user instanceof Company) {
                user.setRole(Role.COMPANY);
            } else if (user instanceof Admin) {
                user.setRole(Role.ADMIN);
            }
        }
    }
}
