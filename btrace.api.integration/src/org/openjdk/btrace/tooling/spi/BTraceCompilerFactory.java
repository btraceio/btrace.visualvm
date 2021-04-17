/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openjdk.btrace.tooling.spi;

import org.openjdk.btrace.tooling.api.BTraceCompiler;
import org.openjdk.btrace.tooling.api.BTraceTask;

/**
 *
 * @author Jaroslav Bachorik yardus@netbeans.org
 */
public interface BTraceCompilerFactory {
    BTraceCompiler newCompiler(BTraceTask task);
}
