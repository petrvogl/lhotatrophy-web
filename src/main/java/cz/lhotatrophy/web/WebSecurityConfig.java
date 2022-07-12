package cz.lhotatrophy.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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
				.antMatchers("/").permitAll()
				.antMatchers("/js/**").permitAll()
				.antMatchers("/css/**").permitAll()
				.antMatchers("/doc/**").permitAll()
				.antMatchers("/img/**").permitAll()
				.antMatchers("/favicon/**").permitAll()
				.antMatchers("/register").permitAll()
				.antMatchers("/prihlasene-tymy").permitAll()
				.antMatchers("/admin/register").permitAll()
				// testing
//				.antMatchers("/testing").permitAll()
//				.antMatchers("/testing/v2").permitAll()
//				.antMatchers("/testing/v2/prihlasene-tymy").permitAll()
//				.antMatchers("/testing/v2/register").permitAll()
				// the rest
				.anyRequest().authenticated()
				.and()
				.formLogin()
				.loginPage("/login").permitAll()
				.and()
				.logout().permitAll().logoutSuccessUrl("/");
		return http.build();
	}
	
	@Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/templates/**");
    }
	
	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
