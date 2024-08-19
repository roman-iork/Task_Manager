package hexlet.code;

import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AppApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void testAdminUser() {
        var user = userRepository.findByEmail("hexlet@example.com").get();
        assertThat(user).isNotEqualTo(null);
        assertTrue(passwordEncoder.matches("qwerty", user.getPasswordHashed()));
    }
}
