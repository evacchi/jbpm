package org.jbpm.process.instance;

import java.beans.BeanInfo;
import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.jbpm.process.core.context.variable.VariableInstance;
import org.kie.api.runtime.rule.RuleUnit;

public abstract class ProcessVariables {

    public static <T extends RuleUnit> ProcessVariables.Typed<T> typed(T value) {
        return new Typed<>(value);
    }

    public static ProcessVariables.Untyped untyped(Map<String, Object> parameters) {
        return new Untyped(parameters);
    }

    public abstract Map<String, VariableInstance> variables(ProcessInstance processInstance);

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

        public Map<String, VariableInstance> variables(ProcessInstance processInstance) {
            return propertyDescriptors.values()
                    .stream()
                    .collect(Collectors.toMap(
                            FeatureDescriptor::getName,
                            pd -> VariableInstance.of(
                                    () -> getPropertyValue(pd),
                                    v -> setPropertyValue(pd, v))));
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

        public Map<String, VariableInstance> variables(ProcessInstance processInstance) {
            return parameters.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            el -> el.getKey(),
                            el -> VariableInstance.of(
                                    el::getValue,
                                    el::setValue)));
        }
    }
}
