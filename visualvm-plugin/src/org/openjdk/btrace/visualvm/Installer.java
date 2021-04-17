/*
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package org.openjdk.btrace.visualvm;


import org.openjdk.btrace.visualvm.impl.BTraceTaskDescriptorProvider;
import org.openjdk.btrace.visualvm.views.BTraceOutputOverview;
import org.openjdk.btrace.visualvm.views.BTraceTaskViewProvider;
import org.graalvm.visualvm.application.views.ApplicationViewsSupport;
import org.openide.modules.ModuleInstall;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {
    @Override
    public void restored() {
        BTraceTaskViewProvider.initialize();
        BTraceTaskDescriptorProvider.initialize();
        ApplicationViewsSupport.sharedInstance().getOverviewView().registerPluginProvider(new BTraceOutputOverview.Provider());
    }

    @Override
    public void uninstalled() {
        BTraceTaskViewProvider.shutdown();
        BTraceTaskDescriptorProvider.shutdown();
        ApplicationViewsSupport.sharedInstance().getOverviewView().unregisterPluginProvider(new BTraceOutputOverview.Provider());
        super.uninstalled();
    }
}
