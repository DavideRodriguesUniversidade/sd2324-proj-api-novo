package tukano.persistence;

import java.io.File;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import tukano.api.User;
import tukano.api.Short;

import java.util.logging.Logger;


public class Hibernate {
    private static final String HIBERNATE_CFG_FILE = "hibernate.cfg.xml";
    private static final Logger log = Logger.getLogger(Hibernate.class.getName());

    private static final Hibernate instance = new Hibernate();

    private final SessionFactory sessionFactory;

    private Hibernate() {
        try {
            sessionFactory = new Configuration()
                    .configure(new File(HIBERNATE_CFG_FILE))
                    .buildSessionFactory();
        } catch (Exception e) {
            log.severe("Error initializing Hibernate session factory: " + e.getMessage());
            throw new RuntimeException("Error initializing Hibernate session factory", e);
        }
    }

    /**
     * Returns the Hibernate instance, initializing if necessary.
     * Requires a configuration file (hibernate.cfg.xml)
     *
     * @return the Hibernate instance
     */
    public static Hibernate getInstance() {
        return instance;
    }

    public void persist(Object... objects) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            for (Object o : objects) {
                session.persist(o);
            }
            tx.commit();
        } catch (Exception e) {
            log.severe("Error persisting objects: " + e.getMessage());
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void update(Object... objects) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            for (Object o : objects) {
                session.merge(o);
            }
            tx.commit();
        } catch (Exception e) {
            log.severe("Error updating objects: " + e.getMessage());
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public void delete(Object... objects) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            for (Object o : objects) {
                session.remove(o);
            }
            tx.commit();
        } catch (Exception e) {
            log.severe("Error deleting objects: " + e.getMessage());
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public <T> List<T> jpql(String jpqlStatement, Class<T> clazz) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(jpqlStatement, clazz).list();
        } catch (Exception e) {
            log.severe("Error executing JPQL query: " + e.getMessage());
            throw e;
        }
    }

    public <T> List<T> sql(String sqlStatement, Class<T> clazz) {
        try (Session session = sessionFactory.openSession()) {
            return session.createNativeQuery(sqlStatement, clazz).list();
        } catch (Exception e) {
            log.severe("Error executing SQL query: " + e.getMessage());
            throw e;
        }
    }

    public void persistShort(Short shortObj) {
        persist(shortObj);
    }

    public void updateShort(Short shortObj) {
        update(shortObj);
    }

    public void deleteShort(Short shortObj) {
        delete(shortObj);
    }

    public void persistUser(User user) {
        persist(user);
    }

    public void updateUser(User user) {
        update(user);
    }

    public void deleteUser(User user) {
        delete(user);
    }

    public List<Short> getAllShorts() {
        return jpql("SELECT s FROM Short s", Short.class);
    }

    public List<User> getAllUsers() {
        return jpql("SELECT u FROM User u", User.class);
    }

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
}
