package org.jbpm.process.core.context.variable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LambdaVariableReference<T> implements ValueReference<T> {

    private final Supplier<T> getter;
    private final Consumer<T> setter;

    public LambdaVariableReference(Supplier<T> getter, Consumer<T> setter) {
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
