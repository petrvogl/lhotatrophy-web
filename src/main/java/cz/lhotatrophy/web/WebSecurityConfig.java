package cz.lhotatrophy.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 *
 * @author Petr Vogl
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
		http
				.authorizeRequests()
				// public
				.antMatchers("/").permitAll()
				.antMatchers("/js/**").permitAll()
				.antMatchers("/css/**").permitAll()
				.antMatchers("/doc/**").permitAll()
				.antMatchers("/img/**").permitAll()
				.antMatchers("/favicon/**").permitAll()
				.antMatchers("/vendor/**").permitAll()
				.antMatchers("/register").permitAll()
				.antMatchers("/prihlasene-tymy").permitAll()
				.antMatchers("/zmena-hesla").permitAll()
				.antMatchers("/zmena-hesla/**").permitAll()
				.antMatchers("/admin/register").permitAll()
				// admin
				.antMatchers("/admin/**").access("hasAuthority('ADMIN')")
				.antMatchers("/rest/admin*").access("hasAuthority('ADMIN')")
				// contest
				.antMatchers("/v-terenu/**").access("hasAuthority('ADMIN')")
				// the rest
				.anyRequest().authenticated()
				.and().csrf().ignoringAntMatchers("/rest/**")
				.and()
				.formLogin().loginPage("/login").permitAll()
				.and()
				.logout().permitAll().logoutSuccessUrl("/");
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
