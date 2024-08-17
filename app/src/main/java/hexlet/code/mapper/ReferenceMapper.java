package hexlet.code.mapper;

import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.model.BaseEntity;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import jakarta.persistence.EntityManager;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.TargetType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class ReferenceMapper {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TaskStatusRepository statusRepository;

    @Autowired
    private LabelRepository labelRepository;

    public <T extends BaseEntity> T toEntity(Long id, @TargetType Class<T> entityClass) {
        return id != null ? entityManager.find(entityClass, id) : null;
    }

    public <T extends BaseEntity> T toEntity(String slug, @TargetType Class<T> entityClass) {
        var status = statusRepository.findBySlug(slug)
                .orElseThrow(() -> new NoSuchResourceException(format("(RefMap)No status with slug %s", slug)));
        Long statusId = status.getId();
        return entityManager.find(entityClass, statusId);
    }

    public List<Integer> toDTO(List<Label> labels) {
        return labels.stream().map(l -> Math.toIntExact(l.getId())).toList();
    }

    public List<Label> fromDTO(List<Integer> labelsIds) {
        List<Label> labels = new ArrayList<>();
        labelsIds.forEach(lId -> {
            var labelId = Long.valueOf(lId);
            var label = labelRepository.findById(labelId)
                    .orElseThrow(() -> new NoSuchResourceException(format("(RefMap)No label with id %o", labelId)));
            labels.add(label);
        });
        return labels;
    }
}
