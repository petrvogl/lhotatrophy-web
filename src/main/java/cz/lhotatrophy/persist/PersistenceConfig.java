package cz.lhotatrophy.persist;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author Petr Vogl
 */
@Configuration
@EnableTransactionManagement
public class PersistenceConfig {

	@Bean
	@Autowired
	public PlatformTransactionManager transactionManager(final SessionFactory sessionFactory) {
		final JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(sessionFactory);
		return txManager;
	}
}
