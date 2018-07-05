package org.jbpm.ruleunits;

import org.kie.api.definition.rule.UnitVar;
import org.kie.api.runtime.rule.RuleUnit;

public class CounterUnit implements RuleUnit {
    @UnitVar("count") int count;
    public int getCount() { return count; }
}
