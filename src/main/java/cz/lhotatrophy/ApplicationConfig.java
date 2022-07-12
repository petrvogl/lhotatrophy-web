package cz.lhotatrophy;

import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author Petr Vogl
 */
@Log4j2
@Configuration
@EnableTransactionManagement(proxyTargetClass = false)
@PropertySources({
	@PropertySource("classpath:application.properties"),
	@PropertySource("file:${WEB_LOCAL_CONFIG}")})
public class ApplicationConfig {

	/**
	 * Default application {@link ZoneId}.
	 */
	public static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("Europe/Prague");
	/**
	 * Default application {@link TimeZone}.
	 */
	public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone(DEFAULT_ZONE_ID);
	
	@PostConstruct
	public void init() {
		TimeZone.setDefault(DEFAULT_TIME_ZONE);
		log.info("TimeZone set to {}: {}", DEFAULT_ZONE_ID.getId(), new Date().toString());
	}

	@Bean
	@Autowired
	public JpaTransactionManager transactionManager(final SessionFactory sessionFactory) {
		final JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(sessionFactory);
		return txManager;
	}
}
