/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.tools.visualvm.modules.tracer.javafx.impl;

import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.application.jvm.Jvm;
import com.sun.tools.visualvm.application.jvm.JvmFactory;
import com.sun.tools.visualvm.modules.tracer.dynamic.spi.ApplicationValidator;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JFXValidator implements ApplicationValidator {
    private static final class Singleton {
        final private static JFXValidator INSTANCE = new JFXValidator();
    }

    public static final JFXValidator getInstance() {
        return Singleton.INSTANCE;
    }

    @Override
    public boolean isPackageApplicable(Application app) {
        Jvm jvm = JvmFactory.getJVMFor(app);
        if (jvm != null) {
            return "com.sun.javafx.runtime.Main".equals(jvm.getMainClass()); // NOI18N
        }
        return false;
    }

}
