package hexlet.code.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusCreateDTO {
    @NotNull
    @Size(min = 1)
    @Column(unique = true)

    private String name;

    @NotNull
    @Size(min = 1)
    @Column(unique = true)
    private String slug;
}
