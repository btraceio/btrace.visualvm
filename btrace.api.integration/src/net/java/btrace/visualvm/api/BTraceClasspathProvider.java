/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.java.btrace.visualvm.api;

import com.sun.btrace.api.BTraceTask;
import com.sun.btrace.spi.ClasspathProvider;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.jvm.JvmFactory;
import com.sun.tools.visualvm.core.datasource.DataSourceRepository;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jaroslav Bachorik <yardus@netbeans.org>
 */
@ServiceProvider(service=ClasspathProvider.class)
final public class BTraceClasspathProvider implements ClasspathProvider {
    final private static class Singleton {
        final private static BTraceClasspathProvider INSTANCE = new BTraceClasspathProvider();
    }

    final private Map<WeakReference<BTraceTask>, Collection<String>> classpaths = new HashMap<WeakReference<BTraceTask>, Collection<String>>();

    final public static BTraceClasspathProvider getInstance() {
        return Singleton.INSTANCE;
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
