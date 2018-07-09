package org.jbpm.process.core.context.variable;

import java.util.function.BiConsumer;

import org.jbpm.process.instance.context.variable.VariableScopeInstance;

public class ReferenceVariableInstance<T> implements VariableInstance<T> {

    private final Variable variableDescriptor;
    private final VariableScopeInstance parentScopeInstance;
    private ValueReference<T> delegate;
    private BiConsumer<T, T> beforeSetHandler;
    private BiConsumer<T, T> afterSetHandler;

    public ReferenceVariableInstance(VariableScopeInstance parentScopeInstance, Variable variableDescriptor) {
        this.parentScopeInstance = parentScopeInstance;
        this.variableDescriptor = variableDescriptor;
        this.delegate = new SimpleVariableInstance<T>((T) variableDescriptor.getValue());
    }

    public String name() {
        return variableDescriptor.getName();
    }

    @Override
    public T get() {
        return delegate.get();
    }

    @Override
    public void set(T value) {
        T oldValue = delegate.get();
        if (beforeSetHandler != null) beforeSetHandler.accept(oldValue, value);
        delegate.set(value);
        if (afterSetHandler != null) afterSetHandler.accept(oldValue, value);
    }

    public void setReference(ValueReference<T> delegate) {
        this.delegate = delegate;
    }

    public ValueReference<T> getDelegate() {
        return delegate;
    }

    /**
     *
     * @param beforeSetHandler (oldValue, newValue) -> {}
     */
    public ReferenceVariableInstance<T> beforeSet(BiConsumer<T, T> beforeSetHandler) {
        this.beforeSetHandler = beforeSetHandler;
        return this;
    }

    public ReferenceVariableInstance<T> afterSet(BiConsumer<T, T> afterSetHandler) {
        this.afterSetHandler = afterSetHandler;
        return this;
    }
}
