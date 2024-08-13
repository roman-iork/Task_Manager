package hexlet.code.utils;

import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static java.lang.String.format;

@Component
public class JWTUtils {

    @Autowired
    private JwtEncoder encoder;

    @Autowired
    private UserRepository userRepository;

    public String generateToken(String userEmail) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NoSuchResourceException(format("(Jwt)No user with email: %s", userEmail)));
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject(userEmail)
                .claim("scope", user.getRole())
                .claim("userId", user.getId())
                .build();
        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
