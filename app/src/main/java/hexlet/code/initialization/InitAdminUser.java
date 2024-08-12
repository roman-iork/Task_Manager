package hexlet.code.initialization;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitAdminUser implements ApplicationRunner {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Faker faker;

    @Override
    public void run(ApplicationArguments arguments) {
        if (userRepository.findByEmail("hexlet@example.com").orElse(null) == null) {
            var user = new User();
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            user.setEmail("hexlet@example.com");
            user.setPasswordHashed(passwordEncoder.encode("qwerty"));
            user.setRole("ROLE_ADMIN");
            userRepository.save(user);
        }
    }
}
