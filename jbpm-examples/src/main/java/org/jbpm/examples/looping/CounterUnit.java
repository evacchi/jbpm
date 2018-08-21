package org.jbpm.examples.looping;

import org.kie.api.definition.rule.UnitVar;
import org.kie.api.runtime.rule.RuleUnit;

public class CounterUnit implements RuleUnit {
    @UnitVar("counter")
    int counter;

    public int getCounter() {
        return counter;
    }

    public void setCounter(int value) {
        counter = value;
    }
}
