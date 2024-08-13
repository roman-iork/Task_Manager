package hexlet.code.initialization;

import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InitFillingStatusesTable implements ApplicationRunner {

    @Autowired
    private TaskStatusRepository statusRepository;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (statusRepository.findAll().isEmpty()) {
            var slugList = new ArrayList<>(List.of("draft", "to_review", "to_be_fixed", "to_publish", "published"));
            var nameList = new ArrayList<>(List.of("Draft", "toReview", "toBeFixed", "toPublish", "Published"));
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
    }
}
