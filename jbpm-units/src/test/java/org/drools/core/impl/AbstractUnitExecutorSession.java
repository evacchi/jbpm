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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.drools.core.SessionConfiguration;
import org.drools.core.SessionConfigurationImpl;
import org.drools.core.common.InternalAgenda;
import org.drools.core.common.InternalAgendaGroup;
import org.drools.core.datasources.CursoredDataSource;
import org.drools.core.datasources.InternalDataSource;
import org.drools.core.event.AgendaEventSupport;
import org.drools.core.event.RuleEventListenerSupport;
import org.drools.core.event.RuleRuntimeEventSupport;
import org.drools.core.ruleunit.RuleUnitDescr;
import org.drools.core.ruleunit.RuleUnitFactory;
import org.drools.core.spi.Activation;
import org.drools.core.spi.FactHandleFactory;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.Globals;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.rule.DataSource;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.RuleUnit;
import org.kie.api.runtime.rule.RuleUnitExecutor;

import static org.drools.core.ruleunit.RuleUnitUtil.RULE_UNIT_ENTRY_POINT;

// this should not extend RuleUnit directly, we should create an abstract class
public abstract class AbstractUnitExecutorSession implements InternalRuleUnitExecutor {

    private final StatefulKnowledgeSessionImpl session;

    private RuleUnitFactory ruleUnitFactory;
    private RuleUnit currentRuleUnit;

    private AtomicBoolean suspended = new AtomicBoolean(false);

    private LinkedList<RuleUnit> unitsStack = new LinkedList<>();

    public AbstractUnitExecutorSession() {
        session = new StatefulKnowledgeSessionImpl();
        initSession(new SessionConfigurationImpl(), EnvironmentFactory.newEnvironment());
        session.agendaEventSupport = new AgendaEventSupport();
        session.ruleRuntimeEventSupport = new RuleRuntimeEventSupport();
        session.ruleEventListenerSupport = new RuleEventListenerSupport();
    }

    public AbstractUnitExecutorSession(KieSession session) {
        this.session = ((StatefulKnowledgeSessionImpl) session);
        bind(session.getKieBase());
    }

    public AbstractUnitExecutorSession(final long id,
                                       boolean initInitFactHandle,
                                       final SessionConfiguration config,
                                       final Environment environment) {
        session = new StatefulKnowledgeSessionImpl(id, null, initInitFactHandle, config, environment);
        initSession(config, environment);
    }

    public AbstractUnitExecutorSession(final long id,
                                       final FactHandleFactory handleFactory,
                                       final long propagationContext,
                                       final SessionConfiguration config,
                                       final InternalAgenda agenda,
                                       final Environment environment) {
        session = new StatefulKnowledgeSessionImpl(id, null, handleFactory, propagationContext, config, agenda, environment);
        initSession(config, environment);
    }

    private void initSession(SessionConfiguration config, Environment environment) {
        session.init(config, environment);
//        session.ruleUnitExecutor = this;
    }

    @Override
    public RuleUnitExecutor bind(KieBase kiebase) {
        InternalKnowledgeBase kbase = (InternalKnowledgeBase) kiebase;
        if (!kbase.hasUnits()) {
            throw new IllegalStateException("Cannot create a RuleUnitExecutor against a KieBase without units");
        }

        session.handleFactory = kbase.newFactHandleFactory();
        session.bindRuleBase(kbase, null, false);

        // this.ruleUnitGuardSystem = new RuleUnitGuardSystem(this);
        return this;
    }

    @Override
    public <T> DataSource<T> newDataSource(String name, T... items) {
        DataSource<T> dataSource = new CursoredDataSource(session);
        for (T item : items) {
            dataSource.insert(item);
        }
        getRuleUnitFactory().bindVariable(name, dataSource);
        return dataSource;
    }

    @Override
    public int run(Class<? extends RuleUnit> ruleUnitClass) {
        return internalRun(getRuleUnitFactory().getOrCreateRuleUnit(this, ruleUnitClass));
    }

    @Override
    public int run(RuleUnit ruleUnit) {
        return internalRun(getRuleUnitFactory().injectUnitVariables(this, ruleUnit));
    }

    protected int internalRun(RuleUnit ruleUnit) {
        int fired = 0;
        for (RuleUnit evaluatedUnit = ruleUnit; evaluatedUnit != null; evaluatedUnit = unitsStack.poll()) {
            fired += internalExecuteUnit(evaluatedUnit) ;//+ ruleUnitGuardSystem.fireActiveUnits(evaluatedUnit);
        }
        return fired;
    }

    protected abstract int internalExecuteUnit(RuleUnit ruleUnit) ;

    private RuleUnitDescr bindRuleUnit( RuleUnit ruleUnit ) {
        suspended.set( false );
        currentRuleUnit = ruleUnit;
        currentRuleUnit.onStart();

//        factHandlesMap.computeIfAbsent( ruleUnit.getClass(), x -> session.getEntryPoint( RULE_UNIT_ENTRY_POINT ).insert( ruleUnit ) );

        RuleUnitDescr ruDescr = session.kBase.getRuleUnitRegistry().getRuleUnitDescr( ruleUnit );
        ( (Globals) session.getGlobalResolver() ).setDelegate( new RuleUnitGlobals(ruDescr, ruleUnit ) );
        ruDescr.bindDataSources( session, ruleUnit );

        InternalAgendaGroup unitGroup = (InternalAgendaGroup)session.getAgenda().getAgendaGroup(ruleUnit.getClass().getName());
        unitGroup.setAutoDeactivate( false );
        unitGroup.setFocus();

        return ruDescr;
    }


    @Override
    public void runUntilHalt(Class<? extends RuleUnit> ruleUnitClass) {
        runUntilHalt(getRuleUnitFactory().getOrCreateRuleUnit(this, ruleUnitClass));
    }

    @Override
    public abstract void runUntilHalt(RuleUnit ruleUnit);
    
    @Override
    public void halt() {
        session.halt();
    }

    @Override
    public void switchToRuleUnit(Class<? extends RuleUnit> ruleUnitClass, Activation activation) {
        switchToRuleUnit(getRuleUnitFactory().getOrCreateRuleUnit(this, ruleUnitClass), activation);
    }

    @Override
    public abstract void switchToRuleUnit(RuleUnit ruleUnit, Activation activation);

    @Override
    public RuleUnit getCurrentRuleUnit() {
        return currentRuleUnit;
    }

    public RuleUnitFactory getRuleUnitFactory() {
        if (ruleUnitFactory == null) {
            ruleUnitFactory = new RuleUnitFactory();
        }
        return ruleUnitFactory;
    }

    @Override
    public RuleUnitExecutor bindVariable(String name, Object value) {
        getRuleUnitFactory().bindVariable(name, value);
        if (value instanceof InternalDataSource) {
            bindDataSource((InternalDataSource) value);
        }
        return this;
    }

    @Override
    public void bindDataSource(InternalDataSource dataSource) {
        dataSource.setWorkingMemory(session);
    }

    @Override
    public void onSuspend() {
        if (!suspended.getAndSet(true)) {
            if (currentRuleUnit != null) {
                currentRuleUnit.onSuspend();
            }
        }
    }

    @Override
    public void onResume() {
        if (suspended.getAndSet(false)) {
            if (currentRuleUnit != null) {
                currentRuleUnit.onResume();
            }
        }
    }

    @Override
    public void dispose() {
        session.dispose();
        // ruleUnitGuardSystem = null;
        ruleUnitFactory = null;
        currentRuleUnit = null;
    }

    // ---------------------------------------
    // SESSION
    // ---------------------------------------

    @Override
    public KieSession getKieSession() {
        return session;
    }

    @Override
    public Collection<?> getSessionObjects() {
        if (session != null) {
            return session.getObjects();
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<?> getSessionObjects(ObjectFilter filter) {
        if (session != null) {
            return session.getObjects(filter);
        }
        return Collections.emptyList();
    }

    // ---------------------------------------
    // LOGGING
    // ---------------------------------------


    @Override
    public KieRuntimeLogger addConsoleLogger() {
        if (this.session != null) {
            return KieServices.Factory.get().getLoggers().newConsoleLogger(session);
        } else {
            throw new IllegalStateException("Cannot add logger to the rule unit when the session is not available");
        }
    }

    @Override
    public KieRuntimeLogger addFileLogger(String fileName) {
        if (this.session != null) {
            return KieServices.Factory.get().getLoggers().newFileLogger(session, fileName);
        } else {
            throw new IllegalStateException("Cannot add logger to the rule unit when the session is not available");
        }
    }

    @Override
    public KieRuntimeLogger addFileLogger(String fileName, int maxEventsInMemory) {
        if (this.session != null) {
            return KieServices.Factory.get().getLoggers().newFileLogger(session, fileName, maxEventsInMemory);
        } else {
            throw new IllegalStateException("Cannot add logger to the rule unit when the session is not available");
        }
    }

    @Override
    public KieRuntimeLogger addThreadedFileLogger(String fileName, int interval) {
        if (this.session != null) {
            return KieServices.Factory.get().getLoggers().newThreadedFileLogger(session, fileName, interval);
        } else {
            throw new IllegalStateException("Cannot add logger to the rule unit when the session is not available");
        }
    }


    public static class RuleUnitGlobals implements Globals {

        private final RuleUnitDescr ruDescr;
        private final RuleUnit ruleUnit;

        private RuleUnitGlobals(RuleUnitDescr ruDescr, RuleUnit ruleUnit) {
            this.ruDescr = ruDescr;
            this.ruleUnit = ruleUnit;
        }

        @Override
        public Object get(String identifier) {
            return ruDescr.getValue(ruleUnit, identifier);
        }

        @Override
        public void set(String identifier, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDelegate(Globals delegate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<String> getGlobalKeys() {
            throw new UnsupportedOperationException();
        }
    }
}
