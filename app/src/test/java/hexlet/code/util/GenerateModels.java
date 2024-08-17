package hexlet.code.util;

import hexlet.code.exception.NoSuchResourceException;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class GenerateModels {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private Faker faker;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TaskStatusRepository taskStatusRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private LabelRepository labelRepository;

    public User generateUser(String password) {
        var user = new User();
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setEmail(faker.internet().emailAddress());
        user.setPasswordHashed(passwordEncoder.encode(password));
        user.setRole("ROLE_USER");
        userRepository.save(user);
        return user;
    }

    public User generateUser() {
        var password = faker.internet().domainWord();
        return generateUser(password);
    }

    public TaskStatus generateStatus() {
        var status = new TaskStatus();
        status.setName("Archived");
        status.setSlug("archived");
        taskStatusRepository.save(status);
        return status;
    }

    public Task generateTask() {
        long statusId = 2;
        long userId = 3;
        var task = new Task();
        task.setName("Walk");
        task.setIndex(135);
        task.setDescription("Make a stroll");
        task.setTaskStatus(taskStatusRepository.findById(statusId)
                .orElseThrow(() -> new NoSuchResourceException("(TestTask)No such status")));
        task.setAssignee(userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchResourceException("(TestTask)No such user")));
        taskRepository.save(task);
        return task;
    }

    public Label generateLabel(String name) {
        var label = new Label();
        label.setName(name);
        labelRepository.save(label);
        return label;
    }
}
