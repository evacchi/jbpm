package org.jbpm.ruleunits;

import org.kie.api.KieServices;

public class Main {

    public static void main(String[] args) throws Exception {
        CounterUnit counterUnit = new CounterUnit();

        new JBPMRuleUnitExecutorSession()
                .bind(KieServices.Factory.get()
                              .getKieClasspathContainer()
                              .getKieBase())
                .bindVariable("count", 5)
                .run(counterUnit);
    }

}
