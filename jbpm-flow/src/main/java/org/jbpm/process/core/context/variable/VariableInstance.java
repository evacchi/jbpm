package org.jbpm.process.core.context.variable;

public interface VariableInstance<T> extends ValueReference<T>  {
    String name();

    void setReference(ValueReference<T> value);
}
