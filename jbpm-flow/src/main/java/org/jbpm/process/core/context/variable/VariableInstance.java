package org.jbpm.process.core.context.variable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface VariableInstance<T> {

    String name();

    T get();

    void set(T value);

    static <T> VariableInstance<T> of(String name, Supplier<T> getter, Consumer<T> setter) {
        return new VariableInstance<T>() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public T get() {
                return getter.get();
            }

            @Override
            public void set(T value) {
                setter.accept(value);
            }
        };
    }
}


