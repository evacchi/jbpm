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

package org.jbpm.marshalling.impl;

import java.util.Date;

import org.drools.core.marshalling.impl.MarshallerReaderContext;
import org.drools.core.marshalling.impl.MarshallerWriteContext;
import org.drools.core.marshalling.impl.ProtobufInputMarshaller;
import org.drools.core.marshalling.impl.ProtobufMessages;
import org.drools.core.marshalling.impl.ProtobufMessages.Timers.Timer;
import org.drools.core.marshalling.impl.ProtobufOutputMarshaller;
import org.drools.core.marshalling.impl.TimersInputMarshaller;
import org.drools.core.marshalling.impl.TimersOutputMarshaller;
import org.drools.core.time.JobContext;
import org.drools.core.time.JobHandle;
import org.drools.core.time.TimerService;
import org.drools.core.time.Trigger;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.timer.ProcessJobContext;
import org.jbpm.process.instance.timer.StartProcessJobContext;
import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.process.instance.timer.TimerManager;
import org.jbpm.process.instance.timer.TimerManagerRuntimeAdaptor;

public class TimerManagerMarshallers {

    public static class ProcessTimerOutputMarshaller implements TimersOutputMarshaller {

        public Timer serialize(JobContext jobCtx, MarshallerWriteContext outputCtx) {
            // do not store StartProcess timers as they are registered whenever session starts
            if (jobCtx instanceof StartProcessJobContext) {
                return null;
            }
            ProcessJobContext pctx = (ProcessJobContext) jobCtx;

            return ProtobufMessages.Timers.Timer
                    .newBuilder()
                    .setType(ProtobufMessages.Timers.TimerType.PROCESS)
                    .setExtension(
                            JBPMMessages.procTimer,
                            JBPMMessages.ProcessTimer.newBuilder()
                                    .setTimer(ProtobufProcessMarshaller.writeTimer(outputCtx, pctx.getTimer()))
                                    .setTrigger(ProtobufOutputMarshaller.writeTrigger(pctx.getTrigger(), outputCtx)).build())
                    .build();
        }
    }

    public static class ProcessTimerInputMarshaller implements TimersInputMarshaller {

        public void deserialize(MarshallerReaderContext inCtx, Timer timer) throws ClassNotFoundException {
            JBPMMessages.ProcessTimer ptimer = timer.getExtension(JBPMMessages.procTimer);

            TimerService ts = inCtx.wm.getTimerService();

            long processInstanceId = ptimer.getTimer().getProcessInstanceId();

            Trigger trigger = ProtobufInputMarshaller.readTrigger(inCtx, ptimer.getTrigger());

            TimerInstance timerInstance = ProtobufProcessMarshaller.readTimer(inCtx, ptimer.getTimer());

            TimerManager tm = ((InternalProcessRuntime) inCtx.wm.getProcessRuntime()).getTimerManager();

            // check if the timer instance is not already registered to avoid duplicated timers
            if (!tm.getTimerMap().containsKey(timerInstance.getId())) {
                TimerManagerRuntimeAdaptor rt = new TimerManagerRuntimeAdaptor(inCtx.wm.getKnowledgeRuntime());
                ProcessJobContext pctx = new ProcessJobContext(timerInstance, trigger, processInstanceId,
                                                               rt, false);
                Date date = trigger.hasNextFireTime();

                if (date != null) {
                    long then = date.getTime();
                    long now = pctx.getKnowledgeRuntime().getSessionClock().getCurrentTime();
                    // overdue timer
                    if (then < now) {
                        trigger = new TimerManager.OverdueTrigger(trigger, pctx.getKnowledgeRuntime());
                    }
                }
                JobHandle jobHandle = ts.scheduleJob(TimerManager.processJob, pctx, trigger);
                timerInstance.setJobHandle(jobHandle);
                pctx.setJobHandle(jobHandle);

                tm.getTimerMap().put(timerInstance.getId(), timerInstance);
            }
        }
    }
}
