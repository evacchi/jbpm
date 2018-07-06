package org.jbpm.process.instance;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jbpm.process.core.context.variable.VariableInstance;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.kie.api.runtime.rule.RuleUnit;

public abstract class ProcessVariables {
    public static <T extends RuleUnit> ProcessVariables.Typed<T> typed(T value) {
        return new Typed<>(value);
    }
    public static ProcessVariables.Untyped untyped(Map<String, Object> parameters) {
        return new Untyped(parameters);
    }

    public <T> Optional<Typed<T>> asTyped(Class<T> c) { return Optional.empty(); }
    public Optional<Untyped> asUntyped() { return Optional.empty(); }

    public abstract Collection<VariableInstance> variables();

    public void validate(String processName, VariableScope scope, VariableScopeInstance instance) {
        Objects.requireNonNull(scope, "This process does not support parameters!");
        for ( VariableInstance variableInstance:  variables() ) {
            instance.setVariable( variableInstance.name(), variableInstance.get() );
        }
    }



    static class Typed<T> extends ProcessVariables {
        private final T value;
        private final HashMap<String, PropertyDescriptor> propertyDescriptors;

        private Typed(T value) {
            this.value = value;
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(value.getClass());
                this.propertyDescriptors = new HashMap<>();
                for (PropertyDescriptor propertyDescriptor : beanInfo.getPropertyDescriptors()) {
                    propertyDescriptors.put(propertyDescriptor.getName(), propertyDescriptor);
                }
            } catch (IntrospectionException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public T value() {
            return value;
        }

        public <T> Optional<Typed<T>> asTyped(Class<T> c) { return Optional.of((Typed<T>)this); }

        public Collection<VariableInstance> variables() {
            return propertyDescriptors.values()
                    .stream()
                    .map(pd -> VariableInstance.of(
                            pd.getName(),
                            () -> getPropertyValue(pd),
                            v -> setPropertyValue(pd, v)))
                    .collect(Collectors.toList());
        }

        private Object getPropertyValue(PropertyDescriptor propertyDescriptor) {
            try {
                return propertyDescriptor.getReadMethod().invoke(value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
        }

        private void setPropertyValue(PropertyDescriptor propertyDescriptor, Object v) {
            try {
                propertyDescriptor.getWriteMethod().invoke(value, v);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    static class Untyped extends ProcessVariables {

        private final Map<String, Object> parameters;

        private Untyped(Map<String, Object> parameters) {
            this.parameters = parameters;
        }

        public Map<String, Object> parameters() {
            return parameters;
        }

        public Optional<Untyped> asUntyped() { return Optional.of(this); }

        public Collection<VariableInstance> variables() {
            return parameters.entrySet()
                    .stream()
                    .map(el -> VariableInstance.of(el.getKey(), el::getValue, el::setValue))
                    .collect(Collectors.toList());
        }


    }

}
