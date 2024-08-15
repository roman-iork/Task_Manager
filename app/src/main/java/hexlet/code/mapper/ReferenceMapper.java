package hexlet.code.mapper;

import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.model.BaseEntity;
import hexlet.code.repository.TaskStatusRepository;
import jakarta.persistence.EntityManager;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.TargetType;
import org.springframework.beans.factory.annotation.Autowired;

import static java.lang.String.format;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class ReferenceMapper {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TaskStatusRepository statusRepository;

    public <T extends BaseEntity> T toEntity(Long id, @TargetType Class<T> entityClass) {
        return id != null ? entityManager.find(entityClass, id) : null;
    }

    public <T extends BaseEntity> T toEntity(String slug, @TargetType Class<T> entityClass) {
        var status = statusRepository.findBySlug(slug)
                .orElseThrow(() -> new NoSuchResourceException(format("(RefMap)No status with slug %s", slug)));
        Long statusId = status.getId();
        return entityManager.find(entityClass, statusId);
    }
}
