package hexlet.code.controller;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.exception.ForeignKeyConstraintException;
import hexlet.code.exception.NoSuchResourceException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.repository.LabelRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class LabelController {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private LabelMapper labelMapper;

    @GetMapping("/labels/{id}")
    public LabelDTO show(@PathVariable long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new NoSuchResourceException(format("(CtrShw)No label with id %o", id)));
        return labelMapper.map(label);
    }

    @GetMapping("/labels")
    public ResponseEntity<List<LabelDTO>> index() {
        var labels = labelRepository.findAll();
        return ResponseEntity.ok()
                .header("x-total-count", String.valueOf(labels.size()))
                .body(labels.stream().map(l -> labelMapper.map(l)).toList());
    }

    @PostMapping("/labels")
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO create(@Valid @RequestBody LabelCreateDTO labelData) {
        var label = labelMapper.map(labelData);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    @PutMapping("/labels/{id}")
    public LabelDTO update(@PathVariable long id,
                           @Valid @RequestBody LabelUpdateDTO labelData) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new NoSuchResourceException(format("(CtrUpd)No label with id %o", id)));
        labelMapper.update(labelData, label);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    @DeleteMapping("/labels/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        try {
            labelRepository.deleteById(id);
        } catch (Exception exception) {
            throw new ForeignKeyConstraintException("Can't delete this label since it is connected with some task");
        }
    }
}
