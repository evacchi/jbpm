/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.process.core.context.variable;

import java.io.Serializable;
import java.util.function.BiConsumer;

import org.jbpm.process.instance.context.variable.VariableScopeInstance;

public class ReferenceVariableInstance<T> implements VariableInstance<T> {

    transient private final Variable variableDescriptor;
    transient private final VariableScopeInstance parentScopeInstance;
    private ValueReference<T> delegate;
    private OnSetHandler<T> onSet = OnSetHandler.empty();

    public ReferenceVariableInstance(VariableScopeInstance parentScopeInstance, Variable variableDescriptor) {
        this.parentScopeInstance = parentScopeInstance;
        this.variableDescriptor = variableDescriptor;
        this.delegate = new SimpleVariableReference<>((T) variableDescriptor.getValue());
    }

    public String name() {
        return variableDescriptor.getName();
    }

    @Override
    public T get() {
        return delegate.get();
    }

    @Override
    public void set(T value) {
        T oldValue = delegate.get();
        onSet.before(oldValue, value);
        delegate.set(value);
        onSet.after(oldValue, value);
    }

    public void setReference(ValueReference<T> delegate) {
        // if (this.delegate != null) throw new IllegalStateException("Cannot setReference more than once");
        T value = delegate.get();
        onSet.before(null, value);
        this.delegate = delegate;
        onSet.after(null, value);
    }

    public ValueReference<T> getReference() {
        return delegate;
    }

    public ReferenceVariableInstance<T> onSetHandler(OnSetHandler<T> onSetHandler) {
        this.onSet = onSetHandler;
        return this;
    }

    public interface OnSetHandler<T> extends Serializable {
        static <T> Empty<T> empty() { return Empty.instance; }
        class Empty<T> implements OnSetHandler<T> {
            static Empty instance = new Empty();

            @Override
            public void before(T oldValue, T newValue) {

            }

            @Override
            public void after(T oldValue, T newValue) {

            }
        }
        void before(T oldValue, T newValue);
        void after(T oldValue, T newValue);
    }
}
