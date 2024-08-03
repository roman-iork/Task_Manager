package hexlet.code;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@Getter
public class CustomeBeanPostProcessor implements BeanPostProcessor {
    private Set<String> beanSet = new HashSet<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        beanSet.add(beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
//        if (beanName.contains("userRepository")) {
//            System.out.println(bean.getClass() + "  " + beanName + "bean___bean___bean___bean___bean___");
//        }
        return bean;
    }
}
