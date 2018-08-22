package org.jbpm.units.support;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.process.instance.ProcessRuntimeImpl;
import org.jbpm.process.instance.ProcessVariables;
import org.kie.api.runtime.process.ProcessInstance;

public class ProcessRuntime {

    final ProcessRuntimeImpl processRuntime;

    public ProcessRuntime(InternalKnowledgeRuntime kieSession) {
        this.processRuntime =
                (ProcessRuntimeImpl) kieSession.getProcessRuntime();
    }

    public ProcessInstance startProcess(String processId,
                                         ProcessVariables parameters,
                                         String trigger) {
        ProcessInstance processInstance =
                processRuntime.createProcessInstance(processId, null, parameters);
        if (processInstance != null) {
            // start process instance
            return processRuntime.startProcessInstance(processInstance.getId(), trigger);
        }
        return null;
    }
}