/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.java.btrace.visualvm.datasources;

import com.sun.btrace.comm.Command;
import com.sun.btrace.comm.ErrorCommand;
import com.sun.btrace.comm.ExitCommand;
import com.sun.btrace.comm.GridDataCommand;
import com.sun.btrace.comm.MessageCommand;
import com.sun.btrace.comm.NumberMapDataCommand;
import com.sun.btrace.comm.StringMapDataCommand;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.datasource.DataSource;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicReference;
import net.java.btrace.visualvm.api.BTraceEngine;
import net.java.btrace.visualvm.api.BTraceTask;

/**
 *
 * @author Jaroslav Bachorik <yardus@netbeans.org>
 */
public class BTraceTaskDS extends DataSource {
    private BTraceTask task;

    final private AtomicReference<PrintWriter> writerRef = new AtomicReference<PrintWriter>();

    final private BTraceTask.CommandListener logger = new BTraceTask.CommandListener() {

        @Override
        public void onCommand(Command cmd) {
            PrintWriter pw = writerRef.get();
            if (pw == null) return;

            switch (cmd.getType()) {
                case Command.MESSAGE: {
                    ((MessageCommand) cmd).print(pw);
                    break;
                }
                case Command.GRID_DATA: {
                    ((GridDataCommand)cmd).print(pw);
                    break;
                }
                case Command.NUMBER_MAP: {
                    ((NumberMapDataCommand)cmd).print(pw);
                    break;
                }
                case Command.STRING_MAP: {
                    ((StringMapDataCommand)cmd).print(pw);
                    break;
                }
                case Command.ERROR: {
                    pw.println("*** Error in BTrace probe");
                    pw.println("===========================================================");
                    ((ErrorCommand) cmd).getCause().printStackTrace(pw);
                    break;
                }
                case Command.EXIT: {
                    pw.println("===========================================================");
                    pw.println("Application exited: " + ((ExitCommand) cmd).getExitCode());
                    break;
                }
            }
        }
    };

    public BTraceTaskDS(Application app) {
        super(app);
        setVisible(false);
        task = BTraceEngine.sharedInstance().createTask(app.getPid());
        task.addCommandListener(logger);
    }

    public BTraceTask getTask() {
        return task;
    }

    public void setWriter(Writer writer) {
        writerRef.set((writer instanceof PrintWriter) ? (PrintWriter)writer : new PrintWriter(writer));
        task.setWriter(writerRef.get());
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
