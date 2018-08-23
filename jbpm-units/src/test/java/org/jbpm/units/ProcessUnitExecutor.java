package org.jbpm.units;

import java.util.Collection;
import java.util.stream.Collectors;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.impl.AbstractUnitExecutor;
import org.jbpm.process.instance.ProcessVariables;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.units.support.ProcessUnitInstance;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.RuleUnit;
import org.kie.api.runtime.rule.RuleUnitExecutor;

public class ProcessUnitExecutor extends AbstractUnitExecutor {

    Collection<ProcessUnitInstance> processes;

    public ProcessUnitExecutor(KieSession session) {
        super(session);
    }

    @Override
    public RuleUnitExecutor bind(KieBase kiebase) {
        this.processes =
                getKieSession().getKieBase().getProcesses()
                        .stream()
                        .map(RuleFlowProcess.class::cast)
                        .map(p -> new ProcessUnitInstance((InternalKnowledgeRuntime) getKieSession(), p))
                        .collect(Collectors.toList());

        return super.bind(kiebase);
    }

    @Override
    protected int internalExecuteUnit(RuleUnit ruleUnit) {
        return (int) processes.stream()
                .filter(p -> p.isContained(ruleUnit))
                .map(p -> p.start(ruleUnit))
                .count();
    }

    @Override
    public void runUntilHalt(Class<? extends RuleUnit> ruleUnitClass) {

    }

    @Override
    public void runUntilHalt(RuleUnit ruleUnit) {

    }

    @Override
    public void halt() {

    }
}
