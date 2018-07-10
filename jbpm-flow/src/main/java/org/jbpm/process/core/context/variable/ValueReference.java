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
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ValueReference<T> extends Serializable {

    T get();

    void set(T value);

    static <T> ValueReference<T> of(
            Supplier<T> getter,
            Consumer<T> setter) {
        return new LambdaVariableReference<>(getter, setter);
    }

}

