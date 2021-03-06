/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openjdk.btrace.visualvm.actions;

import java.awt.event.ActionEvent;
import java.util.Set;
import org.openjdk.btrace.visualvm.datasources.BTraceTaskDS;
import org.graalvm.visualvm.application.Application;
import org.graalvm.visualvm.application.jvm.Jvm;
import org.graalvm.visualvm.application.jvm.JvmFactory;
import org.graalvm.visualvm.core.ui.DataSourceWindowManager;
import org.graalvm.visualvm.core.ui.actions.SingleDataSourceAction;
import org.openide.util.NbBundle;

/**
 *
 * @author Jaroslav Bachorik
 */
public class TraceApplicationAction extends SingleDataSourceAction<Application> {
    @Override
    protected void actionPerformed(Application dataSource, ActionEvent actionEvent) {
        BTraceTaskDS task = new BTraceTaskDS(dataSource);
        Set<BTraceTaskDS> registered = dataSource.getRepository().getDataSources(BTraceTaskDS.class);
        if (!registered.contains(task)) {
            dataSource.getRepository().addDataSource(task);
        } else {
            task = registered.iterator().next();
        }
        DataSourceWindowManager.sharedInstance().openDataSource(task);
    }

    @Override
    protected boolean isEnabled(Application app) {
        Jvm jvm = JvmFactory.getJVMFor(app);
        return app.isLocalApplication() && jvm.isAttachable() && jvm.isGetSystemPropertiesSupported();
    }

    public static synchronized TraceApplicationAction newInstance() {
        return new TraceApplicationAction();
    }

    private TraceApplicationAction() {
        super(Application.class);
        putValue(NAME, NbBundle.getMessage(TraceApplicationAction.class, "TraceApplicationAction.title")); // NOI18N
    }
}
