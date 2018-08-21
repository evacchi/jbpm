/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.units;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.impl.RuleUnitExecutorSession;
import org.jbpm.process.instance.ProcessVariables;
import org.jbpm.process.instance.ProcessRuntimeImpl;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.RuleUnit;

// this should not extend RuleUnit directly, we should create an abstract class
public class JBPMUnitExecutorSession extends RuleUnitExecutorSession {

    public JBPMUnitExecutorSession() {
        super();
    }

    public JBPMUnitExecutorSession(KieSession session) {
        super(session);
    }

    @Override
    public int run(RuleUnit ruleUnit) {
        RuleUnit injected = this.getRuleUnitFactory()
                .injectUnitVariables(this, ruleUnit);
        //BeanMap beanMap = BeanMap.create(injected);
        InternalKnowledgeRuntime kieSession = (InternalKnowledgeRuntime) getKieSession();
        ProcessRuntime processRuntime = new ProcessRuntime(kieSession);
        kieSession.getKieBase().getProcesses()
                .forEach(p -> processRuntime.startProcess(
                        p.getId(),
                        ProcessVariables.typed(injected),
                        null));
        return 0;
    }

    static class ProcessRuntime {
        final ProcessRuntimeImpl processRuntime;

        ProcessRuntime(InternalKnowledgeRuntime kieSession) {
            this.processRuntime =
                    (ProcessRuntimeImpl) kieSession.getProcessRuntime();
        }

        private ProcessInstance startProcess(String processId,
                                             ProcessVariables parameters,
                                             String trigger) {
            ProcessInstance processInstance =
                    processRuntime.createProcessInstance(processId, null, parameters);
            if ( processInstance != null ) {
                // start process instance
                return processRuntime.startProcessInstance(processInstance.getId(), trigger);
            }
            return null;
        }

    }


}
