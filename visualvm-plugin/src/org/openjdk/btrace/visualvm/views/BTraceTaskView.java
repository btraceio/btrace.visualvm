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

package org.openjdk.btrace.visualvm.views;

import javax.swing.JSplitPane;
import org.openjdk.btrace.visualvm.datasources.BTraceTaskDS;
import org.openjdk.btrace.visualvm.views.classpath.BTraceClassPathPanel;
import org.graalvm.visualvm.core.datasource.descriptor.DataSourceDescriptorFactory;
import org.graalvm.visualvm.core.ui.DataSourceView;
import org.graalvm.visualvm.core.ui.components.DataViewComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openjdk.btrace.tooling.api.BTraceTask;

/**
 *
 * @author Jaroslav Bachorik
 */
public class BTraceTaskView extends DataSourceView {
    public BTraceTaskView(BTraceTaskDS task) {
        super(task, "BTrace", DataSourceDescriptorFactory.getDescriptor(task).getIcon(), DataSourceView.POSITION_LAST, true);
    }

    @Override
    protected DataViewComponent createComponent() {
        final OutputPane output = new OutputPane();
        final BTraceTaskDS taskDS = (BTraceTaskDS)getDataSource();
        taskDS.setWriter(output.getWriter());

        final BTraceTask task = taskDS.getTask();
        BTraceTaskEditorPanel panel = new BTraceTaskEditorPanel(task);
        
        DataViewComponent.MasterView mv = new DataViewComponent.MasterView("BTrace", "", panel);
        DataViewComponent.MasterViewConfiguration mvc = new DataViewComponent.MasterViewConfiguration(true);
        final DataViewComponent dvc = new DataViewComponent(mv, mvc);


        final BTraceClassPathPanel cpPanel = new BTraceClassPathPanel(task);

        dvc.addDetailsView(new DataViewComponent.DetailsView("Class-Path", "Class-Path", POSITION_AT_THE_END, cpPanel, null), DataViewComponent.BOTTOM_RIGHT);
        dvc.addDetailsView(new DataViewComponent.DetailsView("Output", "Output", POSITION_LAST, output, null), DataViewComponent.BOTTOM_LEFT);

        dvc.hideDetailsArea(DataViewComponent.BOTTOM_LEFT);
        dvc.hideDetailsArea(DataViewComponent.BOTTOM_RIGHT);

        // Workaround to force correct initial appearance
        try {
            JSplitPane splitPane = (JSplitPane)panel.getParent().getParent().getParent().getParent(); // get the master/details splitter
            splitPane.getBottomComponent().setVisible(false); // hide details container (the actual workaround - should work automatically)
        } catch (Exception e) {} // DataViewComponent implementation probably changed, no way to apply the workaround

        task.addStateListener(new BTraceTask.StateListener() {
            private ProgressHandle ph = null;
            public void stateChanged(BTraceTask.State state) {
                if (state == BTraceTask.State.STARTING) {
                    ph = ProgressHandleFactory.createHandle("Starting BTrace task...");
                    ph.start();
                    output.clear();
                    dvc.showDetailsArea(DataViewComponent.BOTTOM_LEFT);
                } else if (state == BTraceTask.State.RUNNING) {
                    if (ph != null) ph.finish();
                    dvc.showDetailsArea(DataViewComponent.BOTTOM_LEFT);
                } else if (state.ordinal() >= BTraceTask.State.FINISHED.ordinal()) {
                    if (ph != null) ph.finish();
                }
            }
        });
        
        return dvc;
    }
}
