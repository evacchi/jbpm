package org.jbpm.examples.looping;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Set;

import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import org.drools.core.impl.RuleUnitExecutorSession;
import org.kie.api.runtime.rule.RuleUnit;

// this should not extend RuleUnit directly, we should create an abstract class
public class JBPMRuleUnitExecutorSession extends RuleUnitExecutorSession {

    @Override
    public int run(RuleUnit ruleUnit) {
        RuleUnit injected = this.getRuleUnitFactory().injectUnitVariables(this, ruleUnit);

        getKieSession().startProcess("com.sample.looping", new HashMap<String, Object>() {
            private final CounterUnit counterUnit = (CounterUnit)injected;

            {
                put("counter", counterUnit.getCounter());
            }

            @Override
            public Object get(Object key) {
                if (key.equals("counter")) {
                    return counterUnit.getCounter();
                }
                return super.get(key);
            }
            @Override
            public Object put(String key, Object value) {
                if (key.equals("counter")) {
                    counterUnit.setCounter((int)value);
                }
                return super.put(key, value);
            }



        });
        return 0;
    }



}
