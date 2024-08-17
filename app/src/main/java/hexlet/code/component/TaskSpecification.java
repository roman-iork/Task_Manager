package hexlet.code.component;

import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.model.Task;
import hexlet.code.repository.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Component
public class TaskSpecification {
    @Autowired
    private LabelRepository labelRepository;


    public Specification<Task> build(TaskParamsDTO parameters) {
        return titleCont(parameters.getTitleCont())
                .and(status(parameters.getStatus()))
                .and(assignee(parameters.getAssigneeId()))
                .and(label(parameters.getLabelId()));
    }

    public Specification<Task> titleCont(String title) {
        return (taskRoot, criteriaQuery, builder) ->
                title == null ? builder.conjunction()
                        : builder.like(builder.lower(taskRoot.get("name")), "%" + title.toLowerCase() + "%");
    }

    public Specification<Task> status(String status) {
        return (taskRoot, criteriaQuery, builder) ->
                status == null ? builder.conjunction()
                        : builder.equal(taskRoot.get("taskStatus").get("slug"), status);
    }

    public Specification<Task> assignee(Long assigneeId) {
        return (taskRoot, criteriaQuery, builder) ->
                assigneeId == null ? builder.conjunction()
                        : builder.equal(taskRoot.get("assignee").get("id"), assigneeId);
    }

    public Specification<Task> label(Long labelId) {
        return (taskRoot, criteriaQuery, builder) -> {
            if (labelId == null) {
                return builder.conjunction();
            }
            var label = labelRepository.findById(labelId)
                    .orElseThrow(() -> new NoSuchResourceException(format("(SpecTask)No label with id %o", labelId)));
            return builder.isMember(label, taskRoot.get("labels"));
        };
    }
}
