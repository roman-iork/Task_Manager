package hexlet.code.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class UserUpdateDTO {
    @Email
    @Column(unique = true)
    @NotNull
    private JsonNullable<String> email;
    @NotNull
    private JsonNullable<String> password;
}
