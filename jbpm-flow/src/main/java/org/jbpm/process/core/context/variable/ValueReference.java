package org.jbpm.process.core.context.variable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ValueReference<T> {

    T get();

    void set(T value);

    static <T> ValueReference<T> of(
            Supplier<T> getter,
            Consumer<T> setter) {
        return new LambdaVariableInstance<>(getter, setter);
    }

}

class LambdaVariableInstance<T> implements ValueReference<T> {

    private final Supplier<T> getter;
    private final Consumer<T> setter;

    public LambdaVariableInstance(Supplier<T> getter, Consumer<T> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public T get() {
        return getter.get();
    }

    @Override
    public void set(T value) {
        setter.accept(value);
    }
}

class SimpleVariableInstance<T> implements ValueReference<T> {

    private T value;

    public SimpleVariableInstance(T value) {
        this.value = value;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }
}
