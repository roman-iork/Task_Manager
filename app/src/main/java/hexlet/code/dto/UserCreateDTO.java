package hexlet.code.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDTO {

    private String firstName;

    private String lastName;

    @Email
    @Column(unique = true)
    private String email;

    private String password;
}
