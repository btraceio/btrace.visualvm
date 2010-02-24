/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.java.btrace.visualvm.datasources;


import com.sun.btrace.api.BTraceEngine;
import com.sun.btrace.api.BTraceTask;
import com.sun.btrace.api.BTraceTask.State;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.datasource.DataSource;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicReference;
import net.java.btrace.visualvm.impl.BTraceOutputRegistry;

/**
 *
 * @author Jaroslav Bachorik <yardus@netbeans.org>
 */
public class BTraceTaskDS extends DataSource {
    private BTraceTask task;

    final private AtomicReference<PrintWriter> writerRef = new AtomicReference<PrintWriter>();

    final private static BTraceEngine engine = BTraceEngine.newInstance();

    public BTraceTaskDS(Application app) {
        super(app);
        setVisible(false);
        task = engine.createTask(app.getPid());
        task.addMessageDispatcher(new BTraceTask.MessageDispatcher() {
            @Override
            public void onPrintMessage(String message) {
                getWriter().print(message);

            }
        });
        task.addStateListener(new BTraceTask.StateListener() {

            @Override
            public void stateChanged(State state) {
                switch (state) {
                    case STARTING: {
                        getWriter().println("* Starting BTrace task");
                        break;
                    }
                    case COMPILING: {
                        getWriter().println("** Compiling the BTrace script ...");
                        getWriter().flush();
                        break;
                    }
                    case FAILED: {
                        getWriter().println("!!! Error occured");
                        break;
                    }
                    case COMPILED: {
                        getWriter().println("*** Compiled");
                        break;
                    }
                    case INSTRUMENTING: {
                        getWriter().println("** Instrumenting " + task.getInstrClasses() + " classes ...");
                        break;
                    }
                    case RUNNING: {
                        getWriter().println("*** Done");
                        getWriter().println("** BTrace up&running\n");
                        break;
                    }
                    case FINISHED: {
                        getWriter().println("** BTrace has stopped");
                        break;
                    }
                }
            }
        });
    }

    public BTraceTask getTask() {
        return task;
    }

    public void setWriter(Writer writer) {
        writerRef.set((writer instanceof PrintWriter) ? (PrintWriter)writer : new PrintWriter(writer));
        BTraceOutputRegistry.getInstance().registerOuptut(task, writerRef.get());
    }

    private PrintWriter getWriter() {
        return writerRef.get();
    }

    @Override
    public String toString() {
        return task.toString();
    }

    @Override
    public int hashCode() {
        return task.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return task.equals(obj);
    }
}
