package hexlet.code.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Content {
    @Autowired
    private ObjectMapper objectMapper;

    private Map<String, Object> content = new HashMap<>();

    public Content add(String key, Object value) {
        content.put(key, value);
        return this;
    }

    public String build() throws JsonProcessingException {
        var result = objectMapper.writeValueAsString(content);
        content.clear();
        return result;
    }
}
