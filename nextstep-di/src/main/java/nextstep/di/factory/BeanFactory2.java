package nextstep.di.factory;

import com.google.common.collect.Maps;
import nextstep.annotation.Bean;
import nextstep.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BeanFactory2 {
    private static final Logger logger = LoggerFactory.getLogger(BeanFactory2.class);

    private Set<Class<?>> preInstantiateBeans;

    private Map<Class<?>, BeanDefinition> definitions = Maps.newHashMap();
    private Map<Class<?>, Object> beans = Maps.newHashMap();

    public BeanFactory2(Set<Class<?>> preInstantiateBeans) {
        this.preInstantiateBeans = preInstantiateBeans;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        return (T) beans.get(requiredType);
    }

    public void initialize() throws InvocationTargetException, IllegalAccessException {
        for (Class<?> preInstantiateBean : preInstantiateBeans) {
            if (preInstantiateBean.isAnnotationPresent(Configuration.class)) {
                Method[] declaredMethods = preInstantiateBean.getDeclaredMethods();
                List<Method> beanCreations = Stream.of(declaredMethods)
                        .filter(method -> method.isAnnotationPresent(Bean.class))
                        .collect(Collectors.toList());

                Object config = createBean(preInstantiateBean);

                for (Method beanCreation : beanCreations) {
                    Class<?> beanType = beanCreation.getReturnType();
                    BeanDefinition beanDefinition = new BeanDefinition(
                            beanType,
                            (objects) -> beanCreation.invoke(config, objects),
                            Arrays.asList(beanCreation.getParameterTypes())
                    );
                    definitions.put(beanType, beanDefinition);
                }
            } else {
                Constructor<?> injectedConstructor = createInjectedConstructor(preInstantiateBean);

                definitions.put(preInstantiateBean,
                        new BeanDefinition(
                                preInstantiateBean,
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

        for (BeanDefinition beanDefinition : definitions.values()) {
            beans.put(beanDefinition.getType(), createBean(beanDefinition));
        }
    }

    private Object createBean(BeanDefinition beanDefinition) throws InvocationTargetException, IllegalAccessException {
        List<Object> parameters = new ArrayList<>();

        for (Class<?> parameter : beanDefinition.getParameters()) {

            if (beans.containsKey(parameter)) {
                parameters.add(beans.get(parameter));
            } else {
                BeanDefinition beanDefinition1 = BeanFactoryUtils.findConcreteDefinition(parameter, definitions);
                parameters.add(createBean(beanDefinition1));
            }
        }

        return beanDefinition.getCreation().create(parameters.toArray());
    }

    private Object createBean(Class clazz) {
        if (beans.containsKey(clazz)) {
            return beans.get(clazz);
        }

        return createInjectedInstance(clazz);
    }

    private Object createInjectedInstance(Class concreteClass) {
        Constructor<?> injectedConstructor = BeanFactoryUtils.getInjectedConstructor(concreteClass);

        if (injectedConstructor == null) {
            return createInstance(getDefaultConstructor(concreteClass));
        }

        List<Object> parameters = prepareParameterBeans(injectedConstructor);
        return createInstance(injectedConstructor, parameters.toArray());
    }

    private Constructor<?> createInjectedConstructor(Class concreteClass) {
        Constructor<?> injectedConstructor = BeanFactoryUtils.getInjectedConstructor(concreteClass);

        if (injectedConstructor == null) {
            return getDefaultConstructor(concreteClass);
        }

        return injectedConstructor;
    }

    private Object createInstance(Constructor constructor, Object... parameters) {
        try {
            return constructor.newInstance(parameters);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
            throw new BeanCreationFailException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Constructor getDefaultConstructor(Class concreteClass) {
        try {
            return concreteClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
            throw new BeanCreationFailException(e);
        }
    }

    private List<Object> prepareParameterBeans(Constructor<?> injectedConstructor) {
        List<Object> parameters = new ArrayList<>();
        for (Class<?> parameterType : injectedConstructor.getParameterTypes()) {
            Class parameter = findConcreteClass(parameterType);
            Object bean = createBean(parameter);
            parameters.add(bean);
        }

        return parameters;
    }

    private Class findConcreteClass(Class<?> clazz) {
        return BeanFactoryUtils.findConcreteClass(clazz, preInstantiateBeans);
    }
}
