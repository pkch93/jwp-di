package nextstep.di.factory;

import java.util.List;

public class BeanDefinition {
    private Class<?> type;
    private Creation creation;
    private List<Class<?>> parameters;

    public BeanDefinition(Class<?> type, Creation creation, List<Class<?>> parameters) {
        this.type = type;
        this.creation = creation;
        this.parameters = parameters;
    }

    public Class<?> getType() {
        return type;
    }

    public Creation getCreation() {
        return creation;
    }

    public List<Class<?>> getParameters() {
        return parameters;
    }
}
