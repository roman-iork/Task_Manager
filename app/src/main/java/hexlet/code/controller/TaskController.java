package hexlet.code.controller;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.lang.String.format;

@RestController
@RequestMapping("/api")
public class TaskController {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @GetMapping("/tasks/{id}")
    public TaskDTO show(@PathVariable long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchResourceException(format("(CtrShow)No task with id %o", id)));
        return taskMapper.map(task);
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskDTO>> index() {
        var tasks = taskRepository.findAll();
        return ResponseEntity.ok()
                .header("x-total-count", String.valueOf(tasks.size()))
                .body(tasks.stream().map(tsk -> taskMapper.map(tsk)).toList());
    }

    @PostMapping("/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO create(@Valid @RequestBody TaskCreateDTO taskData) {
        var task = taskMapper.map(taskData);
        var assigneeId = task.getAssignee().getId();
        var assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new NoSuchResourceException(format("(CtrTaskCrt)No user with id %o", assigneeId)));
        var slug = task.getTaskStatus().getSlug();
        var status = statusRepository.findBySlug(slug)
                .orElseThrow(() -> new NoSuchResourceException(format("(CtrCrt)No status with slug %s", slug)));
        task.setAssignee(assignee);
        task.setTaskStatus(status);
        var labelsIds = taskData.getTaskLabelIds();
        for (var labelId : labelsIds) {
            var label = labelRepository.findById((long) labelId)
                    .orElseThrow(() -> new NoSuchResourceException(format("(CtrTaskCrt)No label with id %o", labelId)));
            task.addLabel(label);
        }
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    @PutMapping("/tasks/{id}")
    public TaskDTO update(@PathVariable long id,
                          @Valid @RequestBody TaskUpdateDTO taskData) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchResourceException(format("(CtrUpd)No user with id %o", id)));
        taskMapper.update(taskData, task);

        if (taskData.getTaskLabelIds().isPresent()) {
            List<Long> newLabelsList = taskData.getTaskLabelIds().get().stream().map(Long::valueOf).toList();
            List<Long> oldLabelsList = task.getLabels().stream().map(Label::getId).toList();
            for (var oldLabel : oldLabelsList) {
                if (!newLabelsList.contains(oldLabel)) {
                    task.removeLabel(labelRepository.findById(oldLabel)
                            .orElseThrow(() -> new NoSuchResourceException(
                                    format("(CtrUpd)No label with id %o", oldLabel)
                            )));
                }
            }
            for (var newLabel : newLabelsList) {
                if (!oldLabelsList.contains(newLabel)) {
                    task.addLabel(labelRepository.findById(newLabel)
                            .orElseThrow(() -> new NoSuchResourceException(
                                    format("(CtrUpd)No label with id %o", newLabel)
                            )));
                }
            }
        }
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    @DeleteMapping("/tasks/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        taskRepository.deleteById(id);
    }
}
