package hexlet.code.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class UserUpdateDTO {
    @Email
    @Column(unique = true)
    private JsonNullable<String> email;

    private JsonNullable<String> password;

    private JsonNullable<String> firstName;

    private JsonNullable<String> lastName;
}
