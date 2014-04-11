/*
 * RHQ Management Platform
 * Copyright (C) 2005-2008 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.rhq.plugins.httptest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

/**
 * @author Caleb House
 *
 */
public class VictimsDiscoveryComponent implements ResourceDiscoveryComponent {

    /* (non-Javadoc)
     * @see org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent#discoverResources(org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext)
     */
    public Set discoverResources(ResourceDiscoveryContext context) throws InvalidPluginConfigurationException,
        Exception {

        Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
        
        
        List<Configuration> childConfigs = context.getPluginConfigurations();
            for (Configuration childConfig : childConfigs) {
                String key = childConfig.getSimpleValue("path", null);
                if (key == null)
                    throw new InvalidPluginConfigurationException(
                           "No path provided");
                
                
        String name = "Victims Check";
        String description = "Vulnerable Jar/War/Sar";
        ResourceType resourceType = context.getResourceType();
        DiscoveredResourceDetails detail = new DiscoveredResourceDetails(resourceType, key, name, null,
        																description, childConfig, null);

        result.add(detail);
            }

        return result;
    }

}
