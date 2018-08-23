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

package org.drools.core.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import org.drools.core.datasources.CursoredDataSource;
import org.drools.core.datasources.InternalDataSource;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.DataSource;
import org.kie.api.runtime.rule.RuleUnit;
import org.kie.api.runtime.rule.RuleUnitExecutor;

// this should not extend RuleUnit directly, we should create an abstract class
public abstract class AbstractUnitExecutor implements RuleUnitExecutor {

    private final StatefulKnowledgeSessionImpl session;

    private UnitFactory unitFactory = new UnitFactory();
    private UnitScheduler unitScheduler = new UnitScheduler();

    public AbstractUnitExecutor(KieSession session) {
        this.session = ((StatefulKnowledgeSessionImpl) session);
        bind(session.getKieBase());
    }

    @Override
    public RuleUnitExecutor bind(KieBase kiebase) {
        InternalKnowledgeBase kbase = (InternalKnowledgeBase) kiebase;
        if (!kbase.hasUnits()) {
            throw new IllegalStateException("Cannot create a UnitExecutor against a KieBase without units");
        }

        // session.getKieBase().getProcesses();

        return this;
    }

    @Override
    public <T> DataSource<T> newDataSource(String name, T... items) {
        DataSource<T> dataSource = new CursoredDataSource(session);
        for (T item : items) {
            dataSource.insert(item);
        }
        unitFactory.bindVariable(name, dataSource);
        return dataSource;
    }

    @Override
    public int run(Class<? extends RuleUnit> ruleUnitClass) {
        return internalRun(unitFactory.getOrCreateRuleUnit(this, ruleUnitClass));
    }

    @Override
    public int run(RuleUnit ruleUnit) {
        return internalRun(unitFactory.injectUnitVariables(this, ruleUnit));
    }

    protected int internalRun(RuleUnit ruleUnit) {
        unitScheduler.schedule(ruleUnit);
        int fired = 0;
        RuleUnit nextUnit = unitScheduler.next();
        while (nextUnit != null) {
            fired += internalExecuteUnit(nextUnit);//+ ruleUnitGuardSystem.fireActiveUnits(evaluatedUnit);
            nextUnit = unitScheduler.next();
        }
        return fired;
    }

    protected abstract int internalExecuteUnit(RuleUnit ruleUnit);

    @Override
    public RuleUnitExecutor bindVariable(String name, Object value) {
        unitFactory.bindVariable(name, value);
        if (value instanceof InternalDataSource) {
            bindDataSource((InternalDataSource) value);
        }
        return this;
    }

    public void bindDataSource(InternalDataSource dataSource) {
        dataSource.setWorkingMemory(session);
    }

    @Override
    public void dispose() {
        session.dispose();
        unitFactory = null;
    }

    // ---------------------------------------
    // SESSION
    // ---------------------------------------

    @Override
    public KieSession getKieSession() {
        return session;
    }

    //
}
