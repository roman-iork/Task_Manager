package hexlet.code.initialization;

import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InitFillingTables implements ApplicationRunner {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Faker faker;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TaskStatusRepository statusRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (userRepository.findByEmail("hexlet@example.com").orElse(null) == null) {
            var user = new User();
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            user.setEmail("hexlet@example.com");
            user.setPasswordHashed(passwordEncoder.encode("qwerty"));
            user.setRole("ROLE_ADMIN");
            userRepository.save(user);
        }

        if (userRepository.findAll().isEmpty() || userRepository.findAll().size() == 1) {
            for (int i = 0; i < 3; i++) {
                var password = faker.internet().domainWord();
                var user = Instancio.of(User.class)
                        .ignore(Select.field(User::getId))
                        .ignore(Select.field(User::getCreatedAt))
                        .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                        .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                        .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                        .supply(Select.field(User::getRole), () -> "ROLE_USER")
                        .supply(Select.field(User::getPasswordHashed), () -> passwordEncoder.encode(password))
                        .create();
                userRepository.save(user);
            }
        }

        if (statusRepository.findAll().isEmpty()) {
            var slugList = new ArrayList<>(List.of("draft", "to_review", "to_be_fixed", "to_publish", "published"));
            var nameList = new ArrayList<>(List.of("Draft", "ToReview", "ToBeFixed", "ToPublish", "Published"));
            for (int i = 0; i < 5; i++) {
                var taskStatus = Instancio.of(TaskStatus.class)
                        .ignore(Select.field(TaskStatus::getId))
                        .ignore(Select.field(TaskStatus::getCreatedAt))
                        .supply(Select.field(TaskStatus::getName), nameList::removeFirst)
                        .supply(Select.field(TaskStatus::getSlug), slugList::removeFirst)
                        .create();
                statusRepository.save(taskStatus);
            }
        }

        if (taskRepository.findAll().isEmpty()) {
            for (int i = 0; i < 5; i++) {
                var taskStatus = taskStatusRepository.findById(faker.number().numberBetween(1L, 6L)).get();
                var user = userRepository.findById(faker.number().numberBetween(1L, 5L)).get();
                var task = Instancio.of(Task.class)
                        .ignore(Select.field(Task::getId))
                        .ignore(Select.field(Task::getCreatedAt))
                        .supply(Select.field(Task::getName), () -> faker.food().ingredient())
                        .supply(Select.field(Task::getIndex), () -> faker.number().randomDigit())
                        .supply(Select.field(Task::getDescription), () -> faker.book().title())
                        .supply(Select.field(Task::getTaskStatus), () -> taskStatus)
                        .supply(Select.field(Task::getAssignee), () -> user)
                        .create();
                taskRepository.save(task);
            }
        }
    }
}
