/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.java.btrace.visualvm.tracer.j2se.impl;

import com.sun.tools.visualvm.modules.tracer.ItemValueFormatter;
import com.sun.tools.visualvm.modules.tracer.ProbeItemDescriptor;
import com.sun.tools.visualvm.modules.tracer.dynamic.spi.ItemDescriptorProvider;
import java.util.Map;

/**
 *
 * @author Jaroslav Bachorik
 */
public class DescriptorProviders {
    final private static ItemDescriptorProvider awtDescriptorProvider = new ItemDescriptorProvider() {
        public ProbeItemDescriptor create(String itemName, Map<String, Object> attributes) {
            return ProbeItemDescriptor.continuousLineFillItem((String)attributes.get(ATTR_DISPLAY_NAME), (String)attributes.get(ATTR_DESCRIPTION), ItemValueFormatter.DEFAULT_PERCENT, 1d, 0L, 1000L);
        }
    };

    final private static ItemDescriptorProvider ioDescriptorProvider = new ItemDescriptorProvider() {

        public ProbeItemDescriptor create(String itemName, Map<String, Object> attributes) {
            return ProbeItemDescriptor.continuousLineItem((String)attributes.get(ATTR_DISPLAY_NAME), (String)attributes.get(ATTR_DESCRIPTION), new ItemValueFormatter.Decimal(1, "kbps"));
        }
    };

    public static ItemDescriptorProvider awt() {
        return awtDescriptorProvider;
    }

    public static ItemDescriptorProvider io() {
        return ioDescriptorProvider;
    }
}
