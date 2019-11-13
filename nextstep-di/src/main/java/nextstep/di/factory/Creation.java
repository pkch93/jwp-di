package nextstep.di.factory;

import java.lang.reflect.InvocationTargetException;

@FunctionalInterface
public interface Creation {

    Object create(Object... objects) throws InvocationTargetException, IllegalAccessException;
}
