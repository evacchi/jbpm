package org.jbpm.ruleunits;

import org.drools.core.impl.RuleUnitExecutorSession;
import org.kie.api.runtime.rule.RuleUnit;
import org.mockito.cglib.beans.BeanMap;

// this should not extend RuleUnit directly, we should create an abstract class
public class JBPMRuleUnitExecutorSession extends RuleUnitExecutorSession {

    @Override
    public int run(RuleUnit ruleUnit) {
        RuleUnit injected = this.getRuleUnitFactory().injectUnitVariables(this, ruleUnit);
        BeanMap beanMap = BeanMap.create(injected);
        getKieSession().startProcess("com.sample.looping", beanMap);
        return 0;
    }



}
