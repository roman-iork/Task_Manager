package hexlet.code.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskCreateDTO {
    @NotNull
    @Size(min = 1)
    private String title;

    private int index;

    private String content;

    @NotNull
    private String status;

    private int assigneeId;
}
