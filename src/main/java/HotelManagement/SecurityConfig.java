
package HotelManagement;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Qualifier("adminDetailsService")
    public UserDetailsService adminDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${HOTEL_ADMIN_USERNAME:admin}") String adminUsername,
            @Value("${HOTEL_ADMIN_PASSWORD}") String adminPassword) {

        return new InMemoryUserDetailsManager(
                User.withUsername(adminUsername)
                        .password(passwordEncoder.encode(adminPassword))
                        .roles("ADMIN")
                        .build()
        );
    }

    @Bean
    public DaoAuthenticationProvider adminAuthenticationProvider(
            @Qualifier("adminDetailsService")
            UserDetailsService adminDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(adminDetailsService);

        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public DaoAuthenticationProvider customerAuthenticationProvider(
            CustomerDetailsService customerDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customerDetailsService);

        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider adminAuthenticationProvider,
            DaoAuthenticationProvider customerAuthenticationProvider)
            throws Exception {

        http
            .authenticationProvider(adminAuthenticationProvider)
            .authenticationProvider(customerAuthenticationProvider)

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/login", "/register",
                    "/css/**", "/js/**", "/images/**",
                    "/favicon.ico"
                ).permitAll()

                .requestMatchers("/book", "/my-bookings")
                    .hasAuthority("ROLE_CUSTOMER")

                .requestMatchers("/admin", "/admin/**")
                    .hasAuthority("ROLE_ADMIN")

                .anyRequest().permitAll()
            )

            .formLogin(login -> login
                .loginPage("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    boolean isAdmin = authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                            authority.getAuthority().equals("ROLE_ADMIN"));

                    response.sendRedirect(
                        request.getContextPath()
                            + (isAdmin ? "/admin" : "/")
                    );
                })
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}
