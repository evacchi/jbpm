/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.process.assembler;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.kie.api.io.ResourceType;

public class DRFAssemblerService extends AbstractProcessAssembler {

    @Override
    public ResourceType getResourceType() {
        return ResourceType.DRF;
    }

    @Override
    protected void configurePackageBuilder(KnowledgeBuilderImpl kb) {
    }
}
