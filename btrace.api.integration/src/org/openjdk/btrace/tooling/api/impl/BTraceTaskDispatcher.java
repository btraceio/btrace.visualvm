package org.openjdk.btrace.tooling.api.impl;

import org.openjdk.btrace.core.comm.Command;
import org.openjdk.btrace.core.comm.ErrorCommand;
import org.openjdk.btrace.core.comm.GridDataCommand;
import org.openjdk.btrace.core.comm.MessageCommand;
import org.openjdk.btrace.core.comm.NumberDataCommand;
import org.openjdk.btrace.core.comm.NumberMapDataCommand;
import org.openjdk.btrace.core.comm.RetransformClassNotification;
import org.openjdk.btrace.core.comm.StringMapDataCommand;
import org.openjdk.btrace.tooling.api.BTraceTask;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class BTraceTaskDispatcher {
    private final static ExecutorService dispatcher = Executors.newSingleThreadExecutor();
    private final Set<BTraceTask.MessageDispatcher> messageDispatchers = new HashSet<>();

    BTraceTaskDispatcher() {
    }

    /**
     * Dispatcher management
     *
     * @param dispatcher {@linkplain BTraceTask.MessageDispatcher} instance to add
     */
    void addMessageDispatcher(BTraceTask.MessageDispatcher dispatcher) {
        synchronized (messageDispatchers) {
            messageDispatchers.add(dispatcher);
        }
    }

    /**
     * Dispatcher management
     *
     * @param dispatcher {@linkplain BTraceTask.MessageDispatcher} instance to remove
     */
    void removeMessageDispatcher(BTraceTask.MessageDispatcher dispatcher) {
        synchronized (messageDispatchers) {
            messageDispatchers.remove(dispatcher);
        }
    }

    @SuppressWarnings("FutureReturnValueIgnored")
    void dispatchCommand(final Command cmd) {
        final Set<BTraceTask.MessageDispatcher> dispatchingSet;
        synchronized (messageDispatchers) {
            dispatchingSet = new HashSet<>(messageDispatchers);
        }
        dispatcher.submit(new Runnable() {
            @Override
            public void run() {
                for (BTraceTask.MessageDispatcher listener : dispatchingSet) {
                    switch (cmd.getType()) {
                        case Command.MESSAGE: {
                            listener.onPrintMessage(((MessageCommand) cmd).getMessage());
                            break;
                        }
                        case Command.RETRANSFORM_CLASS: {
                            listener.onClassInstrumented(((RetransformClassNotification) cmd).getClassName());
                            break;
                        }
                        case Command.NUMBER: {
                            NumberDataCommand ndc = (NumberDataCommand) cmd;
                            listener.onNumberMessage(ndc.getName(), ndc.getValue());
                            break;
                        }
                        case Command.NUMBER_MAP: {
                            NumberMapDataCommand nmdc = (NumberMapDataCommand) cmd;
                            listener.onNumberMap(nmdc.getName(), nmdc.getData());
                            break;
                        }
                        case Command.STRING_MAP: {
                            StringMapDataCommand smdc = (StringMapDataCommand) cmd;
                            listener.onStringMap(smdc.getName(), smdc.getData());
                            break;
                        }
                        case Command.GRID_DATA: {
                            GridDataCommand gdc = (GridDataCommand) cmd;
                            listener.onGrid(gdc.getName(), gdc.getData());
                            break;
                        }
                        case Command.ERROR: {
                            ErrorCommand ec = (ErrorCommand) cmd;
                            listener.onError(ec.getCause());
                            break;
                        }
                    }
                }
            }
        });
    }
}