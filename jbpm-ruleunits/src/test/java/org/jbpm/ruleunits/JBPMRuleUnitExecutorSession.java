package org.jbpm.ruleunits;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.drools.core.RuleBaseConfiguration;
import org.drools.core.SessionConfiguration;
import org.drools.core.base.ClassFieldAccessorCache;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.common.RuleBasePartitionId;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.factmodel.traits.TraitRegistry;
import org.drools.core.impl.InternalKieContainer;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.RuleUnitExecutorSession;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.reteoo.EntryPointNode;
import org.drools.core.reteoo.LeftTupleNode;
import org.drools.core.reteoo.LeftTupleSource;
import org.drools.core.reteoo.Rete;
import org.drools.core.reteoo.ReteooBuilder;
import org.drools.core.reteoo.SegmentMemory;
import org.drools.core.rule.InvalidPatternException;
import org.drools.core.rule.TypeDeclaration;
import org.drools.core.ruleunit.RuleUnitGuardSystem;
import org.drools.core.ruleunit.RuleUnitRegistry;
import org.drools.core.spi.FactHandleFactory;
import org.drools.core.util.TripleStore;
import org.kie.api.KieBase;
import org.kie.api.builder.ReleaseId;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.process.Process;
import org.kie.api.definition.rule.Query;
import org.kie.api.definition.rule.Rule;
import org.kie.api.definition.type.FactType;
import org.kie.api.event.kiebase.KieBaseEventListener;
import org.kie.api.io.Resource;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.RuleUnit;
import org.kie.api.runtime.rule.RuleUnitExecutor;
import org.mockito.cglib.beans.BeanMap;

// this should not extend RuleUnit directly, we should create an abstract class
public class JBPMRuleUnitExecutorSession extends RuleUnitExecutorSession {

    public JBPMRuleUnitExecutorSession() {
        super();
    }

    public JBPMRuleUnitExecutorSession(KieSession session) {
        super(session);
    }

    @Override
    public int run(RuleUnit ruleUnit) {
        RuleUnit injected = this.getRuleUnitFactory()
                .injectUnitVariables(this, ruleUnit);
        BeanMap beanMap = BeanMap.create(injected);
        KieSession kieSession = getKieSession();
        kieSession.getKieBase().getProcesses()
                .forEach(p -> kieSession.startProcess(p.getId(), beanMap));
        return 0;
    }
}
