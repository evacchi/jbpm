package org.jbpm.process.core.context.variable;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.drools.core.ClassObjectFilter;
import org.drools.core.event.ProcessEventSupport;
import org.jbpm.process.core.Process;
import org.jbpm.process.core.impl.ProcessImpl;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.ProcessInstance;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.CaseData;
import org.kie.api.runtime.rule.FactHandle;

public interface VariableInstance<T> {

    String name();

    T get();

    void set(T value);

    static <T> VariableInstance<T> of(ProcessInstance processInstance,
                                      VariableScope variableScope,
                                      String name,
                                      Supplier<T> getter,
                                      Consumer<T> setter) {
        return new VariableInstance<T>() {
            private transient String variableIdPrefix = null;
            private transient String variableInstanceIdPrefix = null;

            ProcessEventSupport processEventSupport =
                    ((InternalProcessRuntime) processInstance
                            .getKnowledgeRuntime()
                            .getProcessRuntime())
                            .getProcessEventSupport();



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
                beforeSetter(value);
                T oldValue = get();
                setter.accept(value);
                afterSetter(oldValue, value);
            }

            private void beforeSetter(T value) {
                variableScope
                        .validateVariable(
                                ((Process) variableScope.getContextContainer()).getName(),
                                name,
                                value);

                if (name == null) {
                    throw new IllegalArgumentException(
                            "The name of a variable may not be null!");
                }
                Object oldValue = get();
                if (oldValue == null) {
                    if (value == null) {
                        return;
                    }
                }
                processEventSupport.fireBeforeVariableChanged(
                        prefixed(name),
                        instancePrefixed(name),
                        oldValue,
                        value,
                        processInstance,
                        processInstance.getKnowledgeRuntime());

                if (name.startsWith(VariableScope.CASE_FILE_PREFIX)) {
                    String nameInCaseFile = name.replaceFirst(VariableScope.CASE_FILE_PREFIX, "");
                    // store it under case file rather regular variables
                    @SuppressWarnings("unchecked")
                    Collection<CaseData> caseFiles = (Collection<CaseData>) processInstance.getKnowledgeRuntime().getObjects(new ClassObjectFilter(CaseData.class));
                    if (caseFiles.size() == 1) {
                        CaseData caseFile = (CaseData) caseFiles.iterator().next();
                        FactHandle factHandle = processInstance.getKnowledgeRuntime().getFactHandle(caseFile);

                        caseFile.add(nameInCaseFile, value);
                        processInstance.getKnowledgeRuntime().update(factHandle, caseFile);
                        ((KieSession)processInstance.getKnowledgeRuntime()).fireAllRules();
                        return;
                    }

                }
            }

            public void afterSetter(T oldValue, T value) {
                processEventSupport.fireAfterVariableChanged(
                        prefixed(name),
                        instancePrefixed(name),
                        oldValue,
                        value,
                        processInstance,
                        processInstance.getKnowledgeRuntime());

            }


            private String instancePrefixed(String name) {
                return (variableInstanceIdPrefix == null? "" : variableInstanceIdPrefix + ":") + name;
            }

            private String prefixed(String name) {
                return (variableIdPrefix == null ? "" : variableIdPrefix + ":") + name;
            }



        };
    }
}


