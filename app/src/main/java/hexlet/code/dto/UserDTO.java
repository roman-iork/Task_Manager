package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class UserDTO {
    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate createdAt;
}
