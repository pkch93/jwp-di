package nextstep.di.factory;

import com.google.common.collect.Maps;
import nextstep.annotation.Bean;
import nextstep.annotation.Configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BeanDefinitionFactory {
    private final Set<Class<?>> preInstantiateClasses;

    public BeanDefinitionFactory(Set<Class<?>> preInstantiateClasses) {
        this.preInstantiateClasses = preInstantiateClasses;
    }

    public Map<Class<?>, BeanDefinition> createBeanDefinition() {
        Map<Class<?>, BeanDefinition> definitions = Maps.newHashMap();

        for (Class<?> preInstantiateClass : preInstantiateClasses) {
            if (preInstantiateClass.isAnnotationPresent(Configuration.class)) {
                Method[] declaredMethods = preInstantiateClass.getDeclaredMethods();
                List<Method> beanCreations = Stream.of(declaredMethods)
                        .filter(method -> method.isAnnotationPresent(Bean.class))
                        .collect(Collectors.toList());

                Object config = createBean(preInstantiateClass);

                for (Method beanCreation : beanCreations) {
                    Class<?> beanType = beanCreation.getReturnType();
                    BeanDefinition beanDefinition = new BeanDefinition(
                            beanType,
                            (objects) -> {
                                beanCreation.getDeclaringClass().newInstance();
                                beanCreation.invoke(config, objects)
                            },
                            Arrays.asList(beanCreation.getParameterTypes())
                    );
                    definitions.put(beanType, beanDefinition);
                }
            } else {
                Constructor<?> injectedConstructor = createInjectedConstructor(preInstantiateClass);

                definitions.put(preInstantiateClass,
                        new BeanDefinition(
                                preInstantiateClass,
                                (parameters) -> {
                                    try {
                                        return injectedConstructor.newInstance(parameters);
                                    } catch (InstantiationException e) {
                                        e.printStackTrace();
                                        throw new BeanCreationFailException(e);
                                    }
                                },
                                Arrays.asList(injectedConstructor.getParameterTypes())
                        )
                );
            }
        }
    }
}
