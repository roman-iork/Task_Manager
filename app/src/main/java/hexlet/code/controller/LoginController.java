package hexlet.code.controller;

import hexlet.code.dto.AuthRequest;
import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.repository.UserRepository;
import hexlet.code.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.lang.String.format;

@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/login")
    public String authenticate(@RequestBody AuthRequest authRequest) {
        var user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new NoSuchResourceException(format("No user by email: %s", authRequest.getEmail())));
        var tokenBase = new UsernamePasswordAuthenticationToken(
                authRequest.getEmail(),
                authRequest.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().name())));

        authenticationManager.authenticate(tokenBase);

        var token = jwtUtils.generateToken(authRequest.getEmail());
        return token;
    }

    @GetMapping("/admin")
    public String checkAuthorization() {
        return "Authorized";
    }
}
