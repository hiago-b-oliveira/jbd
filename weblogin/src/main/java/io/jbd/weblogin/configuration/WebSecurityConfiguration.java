package io.jbd.weblogin.configuration;

import io.jbd.weblogin.dao.UserAccountDAO;
import io.jbd.weblogin.domain.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.Objects;

@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserAccountDAO userAccountDAO;

    @Value("${spring.application.name}")
    private String applicationName;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()

                .authorizeRequests()
                .antMatchers("/login", "/access-denied").permitAll()
                .antMatchers("/actuator/*").hasAuthority("ADMIN")
                .anyRequest().authenticated()
                .and()

                .exceptionHandling()
                .accessDeniedPage(buildPagePath("access-denied"))
                .and()

                .formLogin()
                .loginPage(buildPagePath("login"))
                .loginProcessingUrl("/login")
                .usernameParameter("login")
                .passwordParameter("password")
                .failureUrl(buildPagePath("login?error=1"))
                .defaultSuccessUrl(buildPagePath("home"), true)
                .permitAll()
                .and()

                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl(buildPagePath("login?logout"))
                .invalidateHttpSession(true)
                .permitAll();
    }

    /**
     * When using a service behind zuul (reverse proxy), we get the a url like this: {proxy hostname}/{service name}/{service requested path}.
     * Because of it, we have to use the application name (service name) in the paths for redirect
     */
    private String buildPagePath(String path) {
        return String.format("/%s/%s", this.applicationName, path);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                UserAccount userAccount = userAccountDAO.findByLogin(authentication.getPrincipal().toString());

                if (userAccount == null) {
                    throw new UsernameNotFoundException("User does not exists");
                }

                if (Objects.equals(userAccount.getPassword(), String.valueOf(authentication.getCredentials()))) {
                    return new UsernamePasswordAuthenticationToken(userAccount.getLogin(), null, Arrays.asList(new SimpleGrantedAuthority("ADMIN")));
                } else {
                    throw new BadCredentialsException("Wrong password");
                }
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return Objects.equals(UsernamePasswordAuthenticationToken.class, authentication);
            }
        });
    }
}
