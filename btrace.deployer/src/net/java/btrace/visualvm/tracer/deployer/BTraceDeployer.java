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

package net.java.btrace.visualvm.tracer.deployer;

import com.sun.btrace.api.BTraceEngine;
import com.sun.btrace.api.BTraceTask;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.modules.tracer.TracerProgressObject;
import com.sun.tools.visualvm.modules.tracer.dynamic.spi.DeployerImpl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jaroslav Bachorik
 */
public class BTraceDeployer implements DeployerImpl {

    final private static Logger LOGGER = Logger.getLogger(BTraceDeployer.class.getName());

    final private static String ALL_FRAGMENTS = "<all>"; // NOI18N
    final private static Pattern FRAGMENT_PATTERN = Pattern.compile("//\\s*<fragment\\s+name\\s*=\\s*\"(.*?)\">(.*?)//\\s*</fragment>", Pattern.DOTALL | Pattern.MULTILINE);

    private static class Singleton {
        final private static BTraceDeployer INSTANCE = new BTraceDeployer();
    }

    final private BTraceEngine engine = BTraceEngine.newInstance();

    final private Map<Application, Map<URL, Collection<String>>> fragmentMap = new HashMap<Application, Map<URL, Collection<String>>>();
    final private Map<Application, Set<BTraceTask>> tasks = new HashMap<Application, Set<BTraceTask>>();
    final private AtomicBoolean deployedFlag  = new AtomicBoolean(false);

    private BTraceDeployer() {}

    public static BTraceDeployer instance() {
        return Singleton.INSTANCE;
    }

    @Override
    public void applyConfig(Application app, Map<String, Object> config) {
        Object urlObj = config.get("script");
        if (urlObj == null) {
            LOGGER.log(Level.WARNING, "BTrace deployment with no valid script file URL found");
            return;
        }
        URL url = null;
        if (urlObj instanceof URL) {
            url = (URL)config.get("script");
        } else {
            try {
                url = new URL(urlObj.toString());
            } catch (MalformedURLException e) {
                LOGGER.log(Level.WARNING, "Invalid URL", e);
                return;
            }
        }

        synchronized(fragmentMap) {
            Map<URL, Collection<String>> appFragments = fragmentMap.get(app);
            if (appFragments == null) {
                appFragments = new HashMap<URL, Collection<String>>();
                fragmentMap.put(app, appFragments);
            }
            Collection<String> fragments = appFragments.get(url);
            if (fragments == null) {
                fragments = new HashSet<String>();
                appFragments.put(url, fragments);
            }
            fragments.add((String)config.get("fragment"));
        }
    }

    @Override
    public boolean deploy(Application app, final TracerProgressObject progress, int availableSteps) {
        if (deployedFlag.compareAndSet(false, true)) {
            Set<String> probeSet = new HashSet<String>();
            Map<URL, Collection<String>> appFragments = fragmentMap.get(app);
            for(Map.Entry<URL, Collection<String>> entry : appFragments.entrySet()) {
                URL probeUrl = entry.getKey();
                StringBuilder sb = loadUrl(probeUrl);
                String probeSrc = sb.toString();

                Collection<String> fragments = entry.getValue();
                Matcher m = FRAGMENT_PATTERN.matcher(probeSrc);
                int offset = 0;
                while (m.find()) {
                    if ((fragments.size() == 1 && fragments.iterator().next().equals(ALL_FRAGMENTS)) || !fragments.contains(m.group(1))) {
                        sb.replace(m.start() - offset, m.end() - offset, ""); // NOI18N
                        offset += (m.end() - m.start());
                    }
                }
                probeSet.add(sb.toString());
                    }
            final CountDownLatch latch = new CountDownLatch(probeSet.size());
            final float stepsPerProbe = (float)availableSteps / probeSet.size();
            final AtomicBoolean result = new AtomicBoolean(true);

            for(String s : probeSet) {
                final BTraceTask task = engine.createTask(app.getPid());
                task.setScript(s);
                task.addStateListener(new BTraceTask.StateListener() {
                    private BTraceTask.MessageDispatcher retrCounter = null;

                    @Override
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
                                retrCounter = new BTraceTask.MessageDispatcher() {
                                    private float stepMultiplier = (stepsPerProbe - 2) / (float)task.getInstrClasses();
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

                                };
                                task.addMessageDispatcher(retrCounter);
                                break;
                            }
                            case FINISHED:
                            case RUNNING: {
                                if (retrCounter != null) {
                                    task.removeMessageDispatcher(retrCounter);
                                }
                                latch.countDown();
                                break;
                            }
                            case FAILED: {
                                result.set(false);
                                latch.countDown();
                                break;
                            }
                        }
                    }
                });
                task.start();
                synchronized(tasks) {
                    Set<BTraceTask> ts = tasks.get(app);
                    if (ts == null) {
                        ts = new HashSet<BTraceTask>();
                        tasks.put(app, ts);
                    }
                    ts.add(task);
                }
            }
            try {
                latch.await();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            if (!result.get()) return false;
        }
        return true;
    }

    @Override
    public void undeploy(Application app) {
        if (deployedFlag.compareAndSet(true, false)) {
            Set<BTraceTask> undeploying  = null;
            synchronized(tasks) {
                undeploying = tasks.remove(app);
            }
            if (undeploying != null) {
                for(BTraceTask task : undeploying) {
                    task.stop();
                }
            }
            fragmentMap.clear();
        }
    }

    private static StringBuilder loadUrl(URL url) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(url.openStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, null, e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
        return sb;
    }
}
