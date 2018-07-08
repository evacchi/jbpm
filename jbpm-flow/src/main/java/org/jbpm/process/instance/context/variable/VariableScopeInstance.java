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

package org.jbpm.process.instance.context.variable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.jbpm.process.core.context.variable.RootVariableInstance;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableInstance;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.ContextInstanceContainer;
import org.jbpm.process.instance.context.AbstractContextInstance;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;

/**
 *
 */
public class VariableScopeInstance extends AbstractContextInstance {

    private static final long serialVersionUID = 510l;

    private Map<String, VariableInstance.Named> variables = new HashMap<>();

    public String getContextType() {
        return VariableScope.VARIABLE_SCOPE;
    }

    public Object getVariable(String name) {
        return getVariableInstance(name).get();
    }

    public Map<String, Object> getVariables() {
        return Collections.unmodifiableMap(variables);
    }

    public void setVariable(String name, Object value) {
        System.out.println(name);
        variables.get(name).set(value);
    }

    public void internalSetVariable(String name, Object value) {
        getVariableInstance(name).getDelegate().set(value);
    }

    public RootVariableInstance<Object> newInstanceOf(Variable variable) {
        RootVariableInstance<Object> variableInstance =
                VariableInstance.root(this, variable);
        return variableInstance;
    }

    public <T> VariableInstance.Named<T> getVariableInstance(String name) {
        return variables.get(name);
    }

    public VariableScope getVariableScope() {
        return (VariableScope) getContext();
    }

    public void setContextInstanceContainer(ContextInstanceContainer contextInstanceContainer) {
        super.setContextInstanceContainer(contextInstanceContainer);
        final String variableIdPrefix;
        final String variableInstanceIdPrefix;
        if (contextInstanceContainer instanceof CompositeContextNodeInstance) {
            variableIdPrefix = ((Node) ((CompositeContextNodeInstance) contextInstanceContainer).getNode()).getUniqueId();
            variableInstanceIdPrefix = ((CompositeContextNodeInstance) contextInstanceContainer).getUniqueId();
        } else {
            variableIdPrefix = null;
            variableInstanceIdPrefix = null;
        }

        getVariableScope().getVariables().stream()
                .map(v -> VariableInstance.root(this, v).validated(variableIdPrefix, variableInstanceIdPrefix, getProcessInstance()))
                .forEach(v -> variables.put(v.name(), v));
    }
}
