/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.process.instance.timer;

import java.util.Map;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.ProcessRuntimeImpl;
import org.jbpm.process.instance.event.SignalManager;
import org.kie.api.time.SessionClock;
import org.kie.internal.runtime.StatefulKnowledgeSession;

public class TimerManagerRuntimeAdaptor implements TimerManageRuntime {
    private final InternalKnowledgeRuntime kruntime;

    public TimerManagerRuntimeAdaptor(InternalKnowledgeRuntime kruntime) {
        this.kruntime = kruntime;
    }

    @Override
    public void startOperation() {
        kruntime.startOperation();
    }

    @Override
    public void endOperation() {
        kruntime.endOperation();
    }

    @Override
    public SessionClock getSessionClock() {
        return kruntime.getSessionClock();
    }

    @Override
    public SignalManager getSignalManager() {
        return getProcessRuntime().getSignalManager();
    }

    private InternalProcessRuntime getProcessRuntime() {
        return (InternalProcessRuntime) kruntime.getProcessRuntime();
    }

    @Override
    public TimerManager getTimerManager() {
        return getProcessRuntime().getTimerManager();
    }

    @Override
    public void startProcess(String processId, Map<String, Object> parameters, String timer) {
        ((ProcessRuntimeImpl) getProcessRuntime()).startProcess(processId, parameters, timer);
    }

    @Override
    public long getIdentifier() {
        return ((StatefulKnowledgeSession)kruntime).getIdentifier();
    }
}
