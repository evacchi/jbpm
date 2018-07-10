package org.jbpm.process.core.context.variable;

public class SimpleVariableReference<T> implements ValueReference<T> {

    private T value;

    public SimpleVariableReference(T value) {
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
