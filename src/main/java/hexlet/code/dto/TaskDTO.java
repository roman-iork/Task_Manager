package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class TaskDTO {
    private int id;
    private String title;
    private int index;
    private String content;
    private String status;
    @JsonProperty("assignee_id")
    private int assigneeId;
    private String createdAt;
    private List<Integer> taskLabelIds;
}
