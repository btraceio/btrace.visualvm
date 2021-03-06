/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.btrace.visualvm.api;

import org.graalvm.visualvm.application.Application;
import org.graalvm.visualvm.application.jvm.Jvm;
import org.graalvm.visualvm.application.jvm.JvmFactory;
import org.graalvm.visualvm.core.datasource.DataSourceRepository;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openjdk.btrace.tooling.api.BTraceTask;
import org.openjdk.btrace.tooling.spi.ClasspathProvider;

/**
 *
 * @author Jaroslav Bachorik <yardus@netbeans.org>
 */
@ServiceProvider(service=ClasspathProvider.class)
final public class BTraceClasspathProvider implements ClasspathProvider {
    final private Map<WeakReference<BTraceTask>, Collection<String>> classpaths = new HashMap<WeakReference<BTraceTask>, Collection<String>>();

    final public static BTraceClasspathProvider getInstance() {
        return Lookup.getDefault().lookup(BTraceClasspathProvider.class);
    }

    public void addCpEntry(BTraceTask btt, String cpEntry) {
        synchronized(classpaths) {
            Collection<String> paths = findClasspath(btt);
            paths.add(cpEntry);
        }
    }

    public void removeCpEntry(BTraceTask btt, String cpEntry) {
        synchronized(classpaths) {
            Collection<String> paths = findClasspath(btt);
            paths.remove(cpEntry);
        }
    }

    public void load(BTraceTask btt, Properties metadata) {
        String classPath = metadata.getProperty("class-path", "");
        StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
        while (st.hasMoreTokens()) {
            addCpEntry(btt, st.nextToken());
        }
    }

    public void save(BTraceTask btt, Properties metadata) {
        Set<String> paths = new HashSet<String>(findClasspath(btt));
        StringBuilder sb = new StringBuilder();
        for(String path : paths) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            sb.append(path);
        }
        metadata.setProperty("class-path", sb.toString());
    }

    @Override
    public Collection<String> getClasspath(BTraceTask btt) {
        Set<String> cp = new HashSet<String>(getInitClassPath(btt));
        cp.addAll(findClasspath(btt));
        return cp;
    }

    private Collection<String> findClasspath(BTraceTask btt) {
        synchronized(classpaths) {
            Collection<String> paths = null;
            for(Map.Entry<WeakReference<BTraceTask>, Collection<String>> entry : classpaths.entrySet()) {
                BTraceTask t = entry.getKey().get();
                if (t != null && t.equals(btt)) {
                    paths = entry.getValue();
                    break;
                }
            }
            if (paths == null) {
                paths = new HashSet<String>();
                classpaths.put(new WeakReference<BTraceTask>(btt), paths);
            }
            return paths;
        }
    }

    private Collection<String> getInitClassPath(BTraceTask btt) {
        Collection<String> paths = new HashSet<String>();

        Properties props = getSystemProperties(btt);
        String userDir = props.getProperty("user.dir", null); // NOI18N
        String cp = props.getProperty("java.class.path", ""); // NOI18N

        if (userDir != null) {
            paths.add(userDir);
        }
        StringTokenizer st = new StringTokenizer(cp, File.pathSeparator);
        while (st.hasMoreTokens()) {
            String pathElement = st.nextToken();
            paths.add(pathElement);
        }

        return paths;
    }

    private Properties getSystemProperties(BTraceTask btt) {
        for(Application app : DataSourceRepository.sharedInstance().getDataSources(Application.class)) {
            if (app.isLocalApplication() && app.getPid() == btt.getPid()) {
                Jvm jvm =JvmFactory.getJVMFor(app);
                return jvm.getSystemProperties();
            }
        }
        return new Properties();
    }
}

