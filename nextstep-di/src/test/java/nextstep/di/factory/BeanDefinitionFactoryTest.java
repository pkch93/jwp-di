package nextstep.di.factory;

import nextstep.di.factory.example.config.ExampleConfig;
import nextstep.di.factory.example.controller.QnaController;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class BeanDefinitionFactoryTest {
    @Test
    @DisplayName("")
    void createBeanDefinition() {
        Set<Class<?>> preInstantiateClazz = Sets.newHashSet(Arrays.asList(ExampleConfig.class, QnaController.class));
        BeanDefinitionFactory beanDefinitionFactory = new BeanDefinitionFactory(preInstantiateClazz);
        Map<Class<?>, BeanDefinition> definitions = beanDefinitionFactory.createBeanDefinition();


    }
}