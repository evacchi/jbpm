package org.jbpm.process.core.context.variable;

import java.util.Collection;

import org.drools.core.ClassObjectFilter;
import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.CaseData;
import org.kie.api.runtime.rule.FactHandle;

public class CaseVariableInstance<T> extends ReferenceVariableInstance<T> {

    private final InternalKnowledgeRuntime knowledgeRuntime;

    public CaseVariableInstance(VariableScopeInstance parentScopeInstance, Variable variable) {
        super(parentScopeInstance, variable);
        this.knowledgeRuntime = parentScopeInstance.getProcessInstance().getKnowledgeRuntime();
    }

    @Override
    public T get() {
        Collection<CaseData> caseFiles = (Collection<CaseData>)
                knowledgeRuntime.getObjects(new ClassObjectFilter(CaseData.class));
        if (caseFiles.size() == 1) {
            CaseData caseFile = caseFiles.iterator().next();
            // check if there is case file prefix and if so remove it before checking case file data
            final String lookUpName =
                    name().startsWith(VariableScope.CASE_FILE_PREFIX) ?
                            name().replaceFirst(VariableScope.CASE_FILE_PREFIX, "") :
                            name();
            if (caseFile != null) {
                return (T) caseFile.getData(lookUpName);
            }
        }
        return null;
    }

    @Override
    public void set(T value) {
        super.set(value);

        Collection<CaseData> caseFiles = (Collection<CaseData>)
                knowledgeRuntime.getObjects(new ClassObjectFilter(CaseData.class));
        if (caseFiles.size() == 1) {
            CaseData caseFile = caseFiles.iterator().next();
            FactHandle factHandle = knowledgeRuntime.getFactHandle(caseFile);
            caseFile.add(name(), value);
            knowledgeRuntime.update(factHandle, caseFile);
            ((KieSession) knowledgeRuntime).fireAllRules();
        }
    }

}
