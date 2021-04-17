/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openjdk.btrace.tooling.spi.impl;

import org.openjdk.btrace.tooling.api.BTraceCompiler;
import org.openjdk.btrace.tooling.api.BTraceTask;
import org.openjdk.btrace.tooling.spi.BTraceCompilerFactory;
import org.openjdk.btrace.tooling.spi.BaseBTraceCompiler;

/**
 *
 * @author Jaroslav Bachorik yardus@netbeans.org
 */
final public class BTraceCompilerFactoryImpl implements BTraceCompilerFactory {
    @Override
    public BTraceCompiler newCompiler(final BTraceTask task) {
        return new BaseBTraceCompiler(task);
    }
}
