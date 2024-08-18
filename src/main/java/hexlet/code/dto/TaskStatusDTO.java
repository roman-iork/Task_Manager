package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusDTO {
    private int id;
    private String name;
    private String slug;
    private String createdAt;
}
