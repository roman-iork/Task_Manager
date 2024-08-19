package hexlet.code.initialization;

import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private LabelRepository labelRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        //adding Admin user
        if (userRepository.findByEmail("hexlet@example.com").orElse(null) == null) {
            var user = new User();
            user.setFirstName(faker.name().firstName());
            user.setLastName(faker.name().lastName());
            user.setEmail("hexlet@example.com");
            user.setPasswordHashed(passwordEncoder.encode("qwerty"));
            user.setRole("ROLE_ADMIN");
            userRepository.save(user);
        }
        //adding users
        if (userRepository.findAll().isEmpty() || userRepository.findAll().size() == 1) {
            for (int i = 0; i < 3; i++) {
                var password = faker.internet().domainWord();
                var user = new User();
                user.setFirstName(faker.name().firstName());
                user.setLastName(faker.name().lastName());
                user.setEmail(faker.internet().emailAddress());
                user.setRole("ROLE_USER");
                user.setPasswordHashed(passwordEncoder.encode(password));
                userRepository.save(user);
            }
        }
        //adding statuses
        if (statusRepository.findAll().isEmpty()) {
            var slugList = new ArrayList<>(List.of("draft", "to_review", "to_be_fixed", "to_publish", "published"));
            var nameList = new ArrayList<>(List.of("Draft", "ToReview", "ToBeFixed", "ToPublish", "Published"));
            for (int i = 0; i < 5; i++) {
                var taskStatus = new TaskStatus();
                taskStatus.setName(nameList.removeFirst());
                taskStatus.setSlug(slugList.removeFirst());
                statusRepository.save(taskStatus);
            }
        }
        //adding labels
        if (labelRepository.findAll().isEmpty()) {
            var labelBug = new Label();
            labelBug.setName("bug");
            labelRepository.save(labelBug);

            var labelFeature = new Label();
            labelFeature.setName("feature");
            labelRepository.save(labelFeature);
        }
        //adding tasks
        if (taskRepository.findAll().isEmpty()) {
            for (int i = 0; i < 5; i++) {
                var taskStatus = taskStatusRepository.findById(faker.number().numberBetween(1L, 6L)).get();
                var user = userRepository.findById(faker.number().numberBetween(1L, 5L)).get();
                var task = new Task();
                task.setName(faker.food().ingredient());
                task.setIndex(faker.number().randomDigit());
                task.setDescription(faker.book().title());
                task.setTaskStatus(taskStatus);
                task.setAssignee(user);
                task.addLabel(labelRepository.findById(1L).get());
                task.addLabel(labelRepository.findById(2L).get());
                taskRepository.save(task);
            }
        }
    }
}
