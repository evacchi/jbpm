package org.jbpm.units;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.impl.AbstractUnitExecutorSession;
import org.drools.core.spi.Activation;
import org.jbpm.process.instance.ProcessVariables;
import org.jbpm.units.support.ProcessRuntime;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.RuleUnit;

public class ProcessUnitExecutorSession extends AbstractUnitExecutorSession {

    public ProcessUnitExecutorSession() {
        super();
    }

    public ProcessUnitExecutorSession(KieSession session) {
        super(session);
    }

    @Override
    protected int internalExecuteUnit(RuleUnit ruleUnit) {
        RuleUnit injected = this.getRuleUnitFactory()
                .injectUnitVariables(this, ruleUnit);
        InternalKnowledgeRuntime kieSession = (InternalKnowledgeRuntime) getKieSession();
        ProcessRuntime processRuntime = new ProcessRuntime(kieSession);
        return (int) kieSession.getKieBase().getProcesses().stream()
                .map(p -> processRuntime.startProcess(
                        p.getId(),
                        ProcessVariables.typed(injected),
                        null)).count();
    }

    @Override
    public void runUntilHalt(RuleUnit ruleUnit) {

    }

    @Override
    public void switchToRuleUnit(RuleUnit ruleUnit, Activation activation) {

    }

    @Override
    public void cancelActivation(Activation activation) {

    }

    @Override
    public void guardRuleUnit(Class<? extends RuleUnit> ruleUnitClass, Activation activation) {

    }

    @Override
    public void guardRuleUnit(RuleUnit ruleUnit, Activation activation) {

    }


}
