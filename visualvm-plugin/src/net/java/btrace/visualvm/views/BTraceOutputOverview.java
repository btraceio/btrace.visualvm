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

package net.java.btrace.visualvm.views;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.jvm.JvmFactory;
import com.sun.tools.visualvm.core.ui.DataSourceViewPlugin;
import com.sun.tools.visualvm.core.ui.DataSourceViewPluginProvider;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent.DetailsView;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import javax.swing.JLabel;

/**
 *
 * @author Jaroslav Bachorik <jaroslav.bachorik@sun.com>
 */
public class BTraceOutputOverview extends DataSourceViewPlugin {
    private static String getBTraceOutput(Application app) {
        if (app.getState() != Application.STATE_AVAILABLE) return null;
        
        Jvm jvm = JvmFactory.getJVMFor(app);
        return (jvm != null && jvm.isGetSystemPropertiesSupported()) ? jvm.getSystemProperties().getProperty("btrace.output") : null;
    }

    public BTraceOutputOverview(Application app) {
        super(app);
    }

    public static class Provider extends DataSourceViewPluginProvider<Application> {       
        @Override
        protected DataSourceViewPlugin createPlugin(Application app) {
            return new BTraceOutputOverview(app);
        }

        @Override
        protected boolean supportsPluginFor(Application app) {
            return getBTraceOutput(app) != null;
        }
    }

    @Override
    public DetailsView createView(int i) {
        if (i != DataViewComponent.TOP_RIGHT) return null;
        try {
            Reader r = new FileReader(new File(getBTraceOutput((Application)getDataSource())));
            return new DetailsView("BTrace Output", "BTrace Output", 99, new OutputPane(r), null);
        } catch (IOException e) {
            return new DetailsView("BTrace Output", "BTrace Output", 99, new JLabel(e.getLocalizedMessage()), null);
        }
    }
}
