package org.jbpm.process.core.context.variable;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jbpm.process.instance.context.variable.VariableScopeInstance;

public interface VariableInstance<T> {

    T get();

    void set(T value);

    static <T> VariableInstance<T> of(
            Supplier<T> getter,
            Consumer<T> setter) {
        return new LambdaVariableInstance<>(getter, setter);
    }

    static <T> VariableInstance<T> of(
            VariableScopeInstance parentScope,
            Variable variable) {
        return new RootVariableInstance<>(parentScope, variable);
    }

    class RootVariableInstance<T> implements VariableInstance<T> {

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
    }
}

class LambdaVariableInstance<T> implements VariableInstance<T> {

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



class SimpleVariableInstance<T> implements VariableInstance<T> {

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

//class ValidatedVariableInstanceDecorator<T> implements VariableInstance<T> {
//
//    ProcessInstance processInstance;
//    VariableScope variableScope;
//    VariableInstance<T> delegate;
//    private transient String variableIdPrefix = null;
//    private transient String variableInstanceIdPrefix = null;
//
//    ProcessEventSupport processEventSupport =
//            ((InternalProcessRuntime) processInstance
//                    .getKnowledgeRuntime()
//                    .getProcessRuntime())
//                    .getProcessEventSupport();
//
//    @Override
//    public T get() {
//        return delegate.get();
//    }
//
//    @Override
//    public void set(T value) {
//        beforeSetter(value);
//        T oldValue = get();
//        delegate.set(value);
//        afterSetter(oldValue, value);
//    }
//
//    private void beforeSetter(T value) {
//        variableScope
//                .validateVariable(
//                        ((Process) variableScope.getContextContainer()).getName(),
//                        name(),
//                        value);
//
//        if (name() == null) {
//            throw new IllegalArgumentException(
//                    "The name of a variable may not be null!");
//        }
//        Object oldValue = get();
//        if (oldValue == null) {
//            if (value == null) {
//                return;
//            }
//        }
//        processEventSupport.fireBeforeVariableChanged(
//                prefixed(name()),
//                instancePrefixed(name()),
//                oldValue,
//                value,
//                processInstance,
//                processInstance.getKnowledgeRuntime());
//
//        if (name().startsWith(VariableScope.CASE_FILE_PREFIX)) {
//            String nameInCaseFile = name().replaceFirst(VariableScope.CASE_FILE_PREFIX, "");
//            // store it under case file rather regular variables
//            @SuppressWarnings("unchecked")
//            Collection<CaseData> caseFiles = (Collection<CaseData>) processInstance.getKnowledgeRuntime().getObjects(new ClassObjectFilter(CaseData.class));
//            if (caseFiles.size() == 1) {
//                CaseData caseFile = (CaseData) caseFiles.iterator().next();
//                FactHandle factHandle = processInstance.getKnowledgeRuntime().getFactHandle(caseFile);
//
//                caseFile.add(nameInCaseFile, value);
//                processInstance.getKnowledgeRuntime().update(factHandle, caseFile);
//                ((KieSession) processInstance.getKnowledgeRuntime()).fireAllRules();
//                return;
//            }
//        }
//    }
//
//    public void afterSetter(T oldValue, T value) {
//        processEventSupport.fireAfterVariableChanged(
//                prefixed(name()),
//                instancePrefixed(name()),
//                oldValue,
//                value,
//                processInstance,
//                processInstance.getKnowledgeRuntime());
//    }
//
//    private String instancePrefixed(String name) {
//        return (variableInstanceIdPrefix == null ? "" : variableInstanceIdPrefix + ":") + name;
//    }
//
//    private String prefixed(String name) {
//        return (variableIdPrefix == null ? "" : variableIdPrefix + ":") + name;
//    }
//}

