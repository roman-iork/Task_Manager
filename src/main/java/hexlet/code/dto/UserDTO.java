package hexlet.code.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class UserDTO {
    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private String createdAt;
}
