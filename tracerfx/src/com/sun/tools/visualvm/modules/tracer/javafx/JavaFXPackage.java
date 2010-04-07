/*
 *  Copyright 2007-2010 Sun Microsystems, Inc.  All Rights Reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Sun designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Sun in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 *  CA 95054 USA or visit www.sun.com if you need additional information or
 *  have any questions.
 */

package com.sun.tools.visualvm.modules.tracer.javafx;

import com.sun.btrace.api.BTraceEngine;
import com.sun.btrace.api.BTraceTask;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.modules.tracer.SessionInitializationException;
import com.sun.tools.visualvm.modules.tracer.TracerPackage;
import com.sun.tools.visualvm.modules.tracer.TracerProbe;
import com.sun.tools.visualvm.modules.tracer.TracerProbeDescriptor;
import com.sun.tools.visualvm.modules.tracer.TracerProgressObject;
import com.sun.tools.visualvm.tools.jmx.JmxModel;
import com.sun.tools.visualvm.tools.jmx.JmxModelFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Jiri Sedlacek
 */
class JavaFXPackage extends TracerPackage.SessionAware<Application> {

    private static final Logger LOGGER = Logger.getLogger(JavaFXPackage.class.getName());

    private static final int CONN_CYCLES = Integer.getInteger("fxtracer.connCycles", 5); // NOI18N
    private static final String PROBE_NAME = "FxBtraceTracker"; // NOI18N
    private static final String TRACKER_NAME = "btrace:name=" + PROBE_NAME; // NOI18N
    private static final ObjectName BTRACE_BEAN;
    static {
        ObjectName btraceBean = null;
        try { btraceBean = new ObjectName(TRACKER_NAME); } catch (Exception e) {}
        BTRACE_BEAN = btraceBean;
    }

    private static final String SCRIPT_FILE =
            "com/sun/tools/visualvm/modules/tracer/javafx/resources/JavaFXTracer.probe"; // NOI18N

    static final Icon ICON = new ImageIcon(ImageUtilities.loadImage(
            "com/sun/tools/visualvm/modules/tracer/javafx/resources/fx.png", true)); // NOI18N
    private static final String NAME = "JavaFX Metrics";
    private static final String DESCR = "Provides various metrics for monitoring runtime behavior of a JavaFX application.";
    private static final int POSITION = 100;

    private BTraceTask task;

    private TracerProbeDescriptor metricsDescriptor;
    private TracerProbeDescriptor objectsDescriptor;
    private TracerProbeDescriptor fpsDescriptor;
    private TracerProbeDescriptor pulseCountDescriptor;
    private TracerProbeDescriptor pulseTimeCountDescriptor;
    private TracerProbeDescriptor syncCallsDescriptor;
    private TracerProbeDescriptor sgTimingDescriptor;
    private TracerProbeDescriptor sgNodeDescriptor;
    private TracerProbeDescriptor sgCssDescriptor;
    private JavaFXProbe metricsProbe;
    private JavaFXProbe objectsProbe;
    private JavaFXProbe fpsProbe;
    private JavaFXProbe pulseCountProbe;
    private JavaFXProbe pulseTimeCountProbe;
    private JavaFXProbe syncCallProbe;
    private JavaFXProbe sgTimingProbe;
    private JavaFXProbe sgNodeProbe;
    private JavaFXProbe sgCssProbe;

    private TracerProgressObject progress;

    private final boolean available;
    private PrintWriter writer;
    private BTraceEngine engine;

    JavaFXPackage(boolean available) {
        super(NAME, DESCR, ICON, POSITION);
        this.available = available;
        engine = BTraceEngine.newInstance();
    }


    public TracerProbeDescriptor[] getProbeDescriptors() {
        metricsDescriptor = JavaFXMetricsProbe.createDescriptor(ICON, available);
        objectsDescriptor = JavaFXObjectsProbe.createDescriptor(ICON, available);
        fpsDescriptor = JavaFXFpsProbe.createDescriptor(ICON, available);
        pulseCountDescriptor = JavaFXPulseCountProbe.createDescriptor(ICON, available);
        pulseTimeCountDescriptor = JavaFXPulseTimingProbe.createDescriptor(ICON, available);
        syncCallsDescriptor = JavaFXSyncCallsProbe.createDescriptor(ICON, available);
        sgTimingDescriptor = JavaFXSGTimingProbe.createDescriptor(ICON, available);
        sgNodeDescriptor = JavaFXSGNodeProbe.createDescriptor(ICON, available);
        sgCssDescriptor = JavaFXSGCssProbe.createDescriptor(ICON, available);
        return new TracerProbeDescriptor[] { metricsDescriptor, objectsDescriptor, fpsDescriptor, pulseCountDescriptor, pulseTimeCountDescriptor, syncCallsDescriptor, sgTimingDescriptor, sgNodeDescriptor, sgCssDescriptor };
    }

    public TracerProbe<Application> getProbe(TracerProbeDescriptor descriptor) {
        if (descriptor == metricsDescriptor) {
            if (metricsProbe == null)
                metricsProbe = new JavaFXMetricsProbe();
            return metricsProbe;
        } else if (descriptor == objectsDescriptor) {
            if (objectsProbe == null)
                objectsProbe = new JavaFXObjectsProbe();
            return objectsProbe;
        } else if (descriptor == fpsDescriptor) {
            if (fpsProbe == null)
                fpsProbe = new JavaFXFpsProbe();
            return fpsProbe;
        } else if (descriptor == pulseCountDescriptor) {
            if (pulseCountProbe == null)
                pulseCountProbe = new JavaFXPulseCountProbe();
            return pulseCountProbe;
        } else if (descriptor == pulseTimeCountDescriptor) {
            if (pulseTimeCountProbe == null)
                pulseTimeCountProbe = new JavaFXPulseTimingProbe();
            return pulseTimeCountProbe;
        } else if (descriptor == syncCallsDescriptor) {
            if (syncCallProbe == null)
                syncCallProbe = new JavaFXSyncCallsProbe();
            return syncCallProbe;
        } else if (descriptor == sgTimingDescriptor) {
            if (sgTimingProbe == null)
                sgTimingProbe = new JavaFXSGTimingProbe();
            return sgTimingProbe;
        } else if (descriptor == sgNodeDescriptor) {
            if (sgNodeProbe == null)
                sgNodeProbe = new JavaFXSGNodeProbe();
            return sgNodeProbe;
        } else if (descriptor == sgCssDescriptor) {
            if (sgCssProbe == null)
                sgCssProbe = new JavaFXSGCssProbe();
            return sgCssProbe;
        } else {
            return null;
        }
    }

    protected TracerProgressObject sessionInitializing(TracerProbe<Application>[] probes,
                Application application, int refresh) {
        progress = new TracerProgressObject(30, "");

        task = engine.createTask(application.getPid());
        try {
            task.setScript(readScript(SCRIPT_FILE));
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Exception when initializing script.", e); // NOI18N
            return null;
        }
        task.addStateListener(new BTraceTask.StateListener() {
            private BTraceTask.MessageDispatcher retrCounter = null;

            public void stateChanged(BTraceTask.State newState) {
                switch (newState) {
                    case COMPILING: {
                        progress.addStep("Compiling...");
                        break;
                    }
                    case COMPILED: {
                        progress.addStep("Compiled.");
                        break;
                    }
                    case INSTRUMENTING: {
                        progress.setText("Instrumenting " + task.getInstrClasses() + " classes...");
                        writer = new PrintWriter(System.out);
                        retrCounter = new BTraceTask.MessageDispatcher() {
                            private float stepMultiplier = 20f / (float)task.getInstrClasses();
                            private float cummulatedStep = 0f;
                            private int cntr = 0;

                            @Override
                            public void onClassInstrumented(String name) {
                                cntr++;

                                cummulatedStep += stepMultiplier;

                                if (cummulatedStep >= 1) {
                                    progress.addSteps((int)cummulatedStep, "Instrumented " + cntr + " of " + task.getInstrClasses() + " classes");
                                    cummulatedStep = cummulatedStep - (int)cummulatedStep;
                                }
                            }

                            @Override
                            public void onPrintMessage(String message) {
                                writer.print(message);
                            }
                        };
                        task.addMessageDispatcher(retrCounter);
                        break;
                    }
                    case FINISHED:
                        if (retrCounter != null) {
                            task.removeMessageDispatcher(retrCounter);
                        }
                        writer.close();
                    case RUNNING: {
                    }
                }
            }
        });


        return progress;
    }

    protected void sessionStarting(TracerProbe<Application>[] probes, Application application)
                throws SessionInitializationException {
        progress.setText("Deploying BTrace script");
        task = deployScript(application);
        if (task == null)
            throw new SessionInitializationException("Unable to deploy BTrace script",
                                                     "Unable to deploy BTrace script to " + application); // NOI18N

        ObjectName mbean = BTRACE_BEAN;
        progress.addStep("Initializing JMX connection");
        MBeanServerConnection connection = getConnection(application, mbean);
        if (connection == null)
            throw new SessionInitializationException("Unable to create JMX connection",
                                                     "Unable to create JMX connection to " + application); // NOI18N);

        for (TracerProbe probe : probes) ((JavaFXProbe)probe).setConnection(connection, mbean);
        progress.finish();
    }

    protected void sessionStopping(TracerProbe<Application>[] probes, Application application) {
        for (TracerProbe probe : probes) ((JavaFXProbe)probe).resetConnection();

        undeployScript(task);
        task = null;
    }


    private static MBeanServerConnection getConnection(Application application, ObjectName mbean) {
        MBeanServerConnection connection = null;

        try {
            JmxModel model = JmxModelFactory.getJmxModelFor(application);
            connection = model != null ? model.getMBeanServerConnection() : null;
            if (connection != null) {
                try {
                    boolean ready = false;
                    for (int i = 0; i < CONN_CYCLES; i++) {
                        ready = !connection.queryNames(mbean, null).isEmpty();
                        if (ready) break;
                        Thread.sleep(500);
                    }
                    if (!ready) {
                        connection = null;
                        LOGGER.info("Timeout initializing JMX connection."); // NOI18N
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.INFO, "Exception when querying JMX connection.", e); // NOI18N
                }
            } else {
                LOGGER.info("Failed to resolve JMX connection."); // NOI18N
            }
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Exception when resolving JMX connection.", e); // NOI18N
        }

        return connection;
    }

    private BTraceTask deployScript(Application application) {
        try {
            task.start();
            return task;
        } catch (Throwable t) {
            LOGGER.log(Level.INFO, "Exception when deploying script.", t); // NOI18N
            return null;
        }
    }

    private void undeployScript(BTraceTask task) {
        try {
            // Sometimes throws IllegalStateException from btrace
            task.stop();
        } catch (Throwable t) {
            LOGGER.log(Level.INFO, "Exception when undeploying script.", t); // NOI18N
        }
    }


    private static String readScript(String scriptFile) throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(JavaFXPackage.class.
                getClassLoader().getResourceAsStream(scriptFile)));
        String line = null;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null)
            sb.append(line).append("\n"); // NOI18N
        return sb.toString();
    }

}
