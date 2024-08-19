package hexlet.code.configuration;

import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.repository.UserRepository;
import hexlet.code.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.DefaultHttpSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

import static java.lang.String.format;
import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasRole;
import static org.springframework.security.authorization.AuthorizationManagers.anyOf;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService userService;

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ApplicationContext context) throws Exception {
        var expressionHandler = new DefaultHttpSecurityExpressionHandler();
        expressionHandler.setApplicationContext(context);

        var hasTheSameUserId = new WebExpressionAuthorizationManager(
                "@webSecurityConfig.checkUserId(authentication, #userId)"
        );
        hasTheSameUserId.setExpressionHandler(expressionHandler);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                        .requestMatchers("/welcome").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/swagger-ui/*").permitAll()
                        .requestMatchers("/v3/**").permitAll()
                        .requestMatchers("/index.html").permitAll()
                        .requestMatchers("/assets/**").permitAll()
                        .requestMatchers("/api/pages/*").permitAll()
                        .requestMatchers("/api/pages").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/login").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/{userId}")
                            .access(anyOf(hasTheSameUserId, hasRole("ADMIN")))
                        .requestMatchers(HttpMethod.DELETE, "/api/users/{userId}")
                            .access(anyOf(hasTheSameUserId, hasRole("ADMIN")))
//                        .requestMatchers(HttpMethod.PUT, "/api/users/{userId}")
//                            .access(anyOf(allOf(hasTheSameUserId, hasRole("USER")), hasRole("ADMIN")))
//                        .requestMatchers(HttpMethod.DELETE, "/api/users/{userId}")
//                            .access(anyOf(allOf(hasTheSameUserId, hasRole("USER")), hasRole("ADMIN")))
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer((rs) -> rs.jwt((jwt) -> jwt.decoder(jwtDecoder)))
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .build();
    }

    @Bean
    public AuthenticationProvider daoAuthProvider(AuthenticationManagerBuilder auth) {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

//    public boolean checkUserId(Authentication authentication, long id) {
//        var authUserId = (Long) ((Jwt) authentication.getPrincipal()).getClaims().get("userId");
//        if (authUserId == null) {
//            throw new NoSuchResourceException("(WbSc)No userId by token");
//        }
//        return authUserId == id;
//    }

    public boolean checkUserId(Authentication authentication, long id) {
        var authUserEmail = authentication.getName();
        var user = userRepository.findByEmail(authUserEmail)
                .orElseThrow(() -> new NoSuchResourceException(format("(WbSc)No user by email %s", authUserEmail)));
        return user.getId() == id;
    }
}
