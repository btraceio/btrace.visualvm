/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openjdk.btrace.visualvm.impl;

import java.io.PrintWriter;
import java.util.Map;
import java.util.WeakHashMap;
import org.openide.util.lookup.ServiceProvider;
import org.openjdk.btrace.tooling.api.BTraceTask;
import org.openjdk.btrace.tooling.spi.OutputProvider;

/**
 *
 * @author Jaroslav Bachorik <yardus@netbeans.org>
 */
final public class BTraceOutputRegistry implements OutputProvider {
    @ServiceProvider(service=OutputProvider.class)
    final public static class Provider implements OutputProvider {

        @Override
        public PrintWriter getStdErr(BTraceTask btt) {
            return BTraceOutputRegistry.getInstance().getStdErr(btt);
        }

        @Override
        public PrintWriter getStdOut(BTraceTask btt) {
            return BTraceOutputRegistry.getInstance().getStdOut(btt);
        }

    }

    final private static class Singleton {
        final private static BTraceOutputRegistry INSTANCE = new BTraceOutputRegistry();
    }

    final private Map<BTraceTask, PrintWriter> writerMap = new WeakHashMap<BTraceTask, PrintWriter>();

    private BTraceOutputRegistry() {}

    final public static BTraceOutputRegistry getInstance() {
        return Singleton.INSTANCE;
    }

    @Override
    public PrintWriter getStdErr(BTraceTask btt) {
        PrintWriter pw = writerMap.get(btt);
        if (pw == null) return OutputProvider.DEFAULT.getStdErr(btt);
        return pw;
    }

    @Override
    public PrintWriter getStdOut(BTraceTask btt) {
        PrintWriter pw = writerMap.get(btt);
        if (pw == null) return OutputProvider.DEFAULT.getStdOut(btt);
        return pw;
    }

    public void registerOuptut(BTraceTask btt, PrintWriter pw) {
        writerMap.put(btt, pw);
    }
}
