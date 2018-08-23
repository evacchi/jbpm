package org.jbpm.units.support;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.process.instance.ProcessRuntimeImpl;
import org.jbpm.process.instance.ProcessVariables;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.RuleUnit;

public class ProcessUnitInstance {

    private final ProcessRuntimeImpl processRuntime;
    private final RuleFlowProcess ruleFlowProcess;
    private ProcessInstance processInstance;

    public ProcessUnitInstance(InternalKnowledgeRuntime kieSession, RuleFlowProcess ruleFlowProcess) {
        this.processRuntime =
                (ProcessRuntimeImpl) kieSession.getProcessRuntime();
        this.ruleFlowProcess = ruleFlowProcess;
    }

    public boolean isContained(RuleUnit unit) {
        return ruleFlowProcess.getUnit().equals(unit.getClass().getName());
    }

    public ProcessInstance start(RuleUnit unit) {
        processInstance = processRuntime.createProcessInstance(
                ruleFlowProcess.getId(),
                null,
                ProcessVariables.typed(unit));

        if (processInstance != null) {
            // start process instance
            return processRuntime.startProcessInstance(processInstance.getId(), null);
        }
        return null;
    }
}