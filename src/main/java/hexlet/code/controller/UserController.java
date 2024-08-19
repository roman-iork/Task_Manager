package hexlet.code.controller;

import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.exception.ForeignKeyConstraintException;
import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static java.lang.String.format;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> index() {
        var users = userRepository.findAll();
        var usersInDTOFormat = users.stream().map(u -> userMapper.map(u)).toList();
        return ResponseEntity.ok()
                .header("x-total-count", String.valueOf(users.size()))
                .body(usersInDTOFormat);
    }

    @GetMapping("/users/{id}")
    public UserDTO show(@PathVariable long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchResourceException(format("(CtrShw)No user with id %o!", id)));
        return userMapper.map(user);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO create(@Valid @RequestBody UserCreateDTO userData) {
        var user = userMapper.map(userData);
        user.setPasswordHashed(hashPassword(user));
        user.setRole("ROLE_USER");
        userRepository.save(user);
        return userMapper.map(user);
    }

    @PutMapping("/users/{id}")
    public UserDTO update(@PathVariable long id,
                          @Valid @RequestBody UserUpdateDTO userData) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchResourceException(format("(CtrUpd)No user with id %o!", id)));
        userMapper.update(userData, user);
        if (userData.getPassword() != null && userData.getPassword().orElse(null) != null) {
            user.setPasswordHashed(hashPassword(user));
        }
        userRepository.save(user);
        return userMapper.map(user);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        try {
            userRepository.deleteById(id);
        } catch (Exception exception) {
            throw new ForeignKeyConstraintException("Can't delete user since it's connected with task");
        }
    }

    private String hashPassword(User user) {
        var password = user.getPasswordHashed();
        return passwordEncoder.encode(password);
    }
}
