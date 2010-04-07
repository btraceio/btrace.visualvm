/*
 *  Copyright 2007-2010 Sun Microsystems, Inc.  All Rights Reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License version 2 only, as
 *  published by the Free Software Foundation.  Sun designates this
 *  particular file as subject to the "Classpath" exception as provided
 *  by Sun in the LICENSE file that accompanied this code.
 *
 *  This code is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  version 2 for more details (a copy is included in the LICENSE file that
 *  accompanied this code).
 *
 *  You should have received a copy of the GNU General Public License version
 *  2 along with this work; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 *  CA 95054 USA or visit www.sun.com if you need additional information or
 *  have any questions.
 */

package com.sun.tools.visualvm.modules.tracer.javafx;

import com.sun.tools.visualvm.modules.tracer.ProbeItemDescriptor;
import com.sun.tools.visualvm.modules.tracer.TracerProbeDescriptor;
import com.sun.tools.visualvm.modules.tracer.ItemValueFormatter;
import java.util.List;
import javax.management.Attribute;
import javax.swing.Icon;

/**
 *
 * @author Jiri Sedlacek
 */
class JavaFXMetricsProbe extends JavaFXProbe {
    
    private static final String NAME = "FX Metrics";
    private static final String DESCR = "Monitors Invalidation Rate and Replacement Rate";
    private static final int POSITION = 10;


    JavaFXMetricsProbe() {
        super(2, createItemDescriptors(), createAttrs());
    }


    long[] getValues(List<Attribute> metrics) {
        return new long[] {
            Math.max(((Long)metrics.get(0).getValue()).longValue(), 0),
            Math.max(((Long)metrics.get(1).getValue()).longValue(), 0)
        };
    }


    static final TracerProbeDescriptor createDescriptor(Icon icon, boolean available) {
        return new TracerProbeDescriptor(NAME, DESCR, icon, POSITION, available);
    }
    
    private static final ProbeItemDescriptor[] createItemDescriptors() {
        return new ProbeItemDescriptor[] {
            ProbeItemDescriptor.lineItem("Invalidation rate", "Monitors number of invalidations per second", ItemValueFormatter.DEFAULT_DECIMAL, 0, ProbeItemDescriptor.MAX_VALUE_UNDEFINED),
            ProbeItemDescriptor.lineItem("Replacement rate", "Monitors number of replacements per second", ItemValueFormatter.DEFAULT_DECIMAL, 0, ProbeItemDescriptor.MAX_VALUE_UNDEFINED)
        };
    }
    
    private static final String[] createAttrs() {
        return new String[] { "invalidationRate", "replacementRate" }; // NOI18N
    }

}
