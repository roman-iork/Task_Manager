package hexlet.code.controller;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.repository.TaskStatusRepository;
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
public class TaskStatusController {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    @GetMapping("/task_statuses/{id}")
    public TaskStatusDTO show(@PathVariable long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchResourceException(format("(CtrShw)No taskStatus with id %o", id)));
        return taskStatusMapper.map(taskStatus);
    }

    @GetMapping("/task_statuses")
    public ResponseEntity<List<TaskStatusDTO>> index() {
        var taskStatuses = taskStatusRepository.findAll();
        return ResponseEntity.ok()
                .header("x-total-count", String.valueOf(taskStatuses.size()))
                .body(taskStatuses.stream().map(st -> taskStatusMapper.map(st)).toList());
    }

    @PostMapping("/task_statuses")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskStatusDTO create(@Valid @RequestBody TaskStatusCreateDTO taskStatusData) {
        var status = taskStatusMapper.map(taskStatusData);
        taskStatusRepository.save(status);
        return taskStatusMapper.map(status);
    }

    @PutMapping("/task_statuses/{id}")
    public TaskStatusDTO update(@Valid @RequestBody TaskStatusUpdateDTO taskStatusData,
                                @PathVariable long id) {
        var status = taskStatusRepository.findById(id)
                .orElseThrow(() -> new NoSuchResourceException(format("(CtrUpd)No taskStatus with id %o", id)));
        taskStatusMapper.update(taskStatusData, status);
        taskStatusRepository.save(status);
        return taskStatusMapper.map(status);
    }

    @DeleteMapping("/task_statuses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        taskStatusRepository.deleteById(id);
    }
}
