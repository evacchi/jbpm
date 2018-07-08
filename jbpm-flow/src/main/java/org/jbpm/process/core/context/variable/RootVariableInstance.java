package org.jbpm.process.core.context.variable;

import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;

public final class RootVariableInstance<T> implements VariableInstance.Named<T> {

    private final Variable variableDescriptor;
    private final VariableScopeInstance parentScopeInstance;
    private VariableInstance<T> delegate;

    public RootVariableInstance(VariableScopeInstance parentScopeInstance, Variable variableDescriptor) {
        this.parentScopeInstance = parentScopeInstance;
        this.variableDescriptor = variableDescriptor;
        this.delegate = new SimpleVariableInstance<T>((T) variableDescriptor.getValue());
    }

    public String name() {
        return variableDescriptor.getName();
    }

    public VariableScopeInstance getParentScopeInstance() {
        return parentScopeInstance;
    }

    @Override
    public T get() {
        return delegate.get();
    }

    @Override
    public void set(T value) {
        delegate.set(value);
    }

    public void setDelegate(VariableInstance<T> delegate) {
        this.delegate = delegate;
    }

    public VariableInstance<T> getDelegate() {
        return delegate;
    }

    public VariableInstance.Named<T> validated(String idPrefix, String instanceIdPrefix, ProcessInstance processInstance) {
        return new ValidatedVariableInstanceDecorator<>(this, idPrefix, instanceIdPrefix, processInstance);
    }
}
