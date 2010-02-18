/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.java.btrace.visualvm.impl;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.jvm.JvmFactory;
import com.sun.tools.visualvm.core.datasource.DataSourceRepository;
import java.util.Properties;
import net.java.btrace.visualvm.spi.ProcessDetailsProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Bachorik <yardus@netbeans.org>
 */
@ServiceProvider(service=ProcessDetailsProvider.class)
public class ApplicationDetailsProvider extends ProcessDetailsProvider {
    final private static Properties nullProperties = new Properties();

    @Override
    public boolean canBeTraced(int pid) {
        for(Application app : DataSourceRepository.sharedInstance().getDataSources(Application.class)) {
            if (app.isLocalApplication() && app.getPid() == pid) {
                Jvm jvm = JvmFactory.getJVMFor(app);
                if (jvm.isAttachable() && jvm.isGetSystemPropertiesSupported()) return true;
            }
        }
        return false;
    }

    @Override
    public Properties getSystemProperties(int pid) {
        for(Application app : DataSourceRepository.sharedInstance().getDataSources(Application.class)) {
            if (app.isLocalApplication() && app.getPid() == pid) {
                Jvm jvm = JvmFactory.getJVMFor(app);
                if (jvm != null) {
                    return jvm.getSystemProperties();
                }
            }
        }
        return nullProperties;
    }

}
