package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class TaskCreateDTO {
    @NotNull
    @Size(min = 1)
    private String title;

    private int index;

    private String content;

    @NotNull
    private String status;

    @JsonProperty("assignee_id")
    private int assigneeId;

    @NotNull
    private List<Integer> taskLabelIds;
}
