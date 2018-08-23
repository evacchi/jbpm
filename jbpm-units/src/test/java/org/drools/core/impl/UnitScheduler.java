package org.drools.core.impl;

import java.util.LinkedList;

import org.kie.api.runtime.rule.RuleUnit;

public class UnitScheduler {

    final private LinkedList<RuleUnit> units = new LinkedList<>();

    public void schedule(RuleUnit unit) {
        units.push(unit);
    }

    public RuleUnit current() {
        return units.peek();
    }

    public RuleUnit next() {
        return units.poll();
    }
}
