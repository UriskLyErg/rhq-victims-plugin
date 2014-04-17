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
package org.rhq.plugins.victims;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ManualAddFacet;
import org.rhq.core.pluginapi.inventory.ProcessScanResult;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.core.system.ProcessInfo;

/**
 * @author Caleb House
 * 
 */
public class VictimsDiscoveryComponent implements
		ResourceDiscoveryComponent<ResourceComponent<?>>,
		ManualAddFacet<ResourceComponent<?>> {

	
	/*
	@Override
	public DiscoveredResourceDetails discoverResource(Configuration arg0,
			ResourceDiscoveryContext<ResourceComponent<?>> arg1)
			throws InvalidPluginConfigurationException {
	}

	@Override
	public Set<DiscoveredResourceDetails> discoverResources(
			ResourceDiscoveryContext<ResourceComponent<?>> context)
			throws InvalidPluginConfigurationException, Exception {
		// TODO Auto-generated method stub
		Set<DiscoveredResourceDetails> result = new HashSet<DiscoveredResourceDetails>();
		DiscoveredResourceDetails drd = this.discoverResource(context.getDefaultPluginConfiguration(), context);
		Configuration childConfig = drd.getPluginConfiguration();
		String key = childConfig.getSimpleValue("path", null);
			if (key == null)
				throw new InvalidPluginConfigurationException(
						"No path provided");

			String name = "Victims Check";
			String description = "Vulnerable Jar/War/Sar";
			ResourceType resourceType = context.getResourceType();
			DiscoveredResourceDetails detail = new DiscoveredResourceDetails(
					resourceType, key, name, null, description, childConfig,
					null);

			result.add(detail);
		}

		return result;
	}
*/
	private static final Log LOG = LogFactory.getLog(VictimsDiscoveryComponent.class);
	private static final String VICTIMS_SCAN_PATH = "paths";
	private static final String VICTIMS_VERSION = "version";
	
	public Set<DiscoveredResourceDetails> discoverResources(ResourceDiscoveryContext context) {

		
        if (LOG.isDebugEnabled()) {
            LOG.debug("Resource Discovery Started");
        }
        Set<DiscoveredResourceDetails> servers = new LinkedHashSet<DiscoveredResourceDetails>();

        // Process any auto-discovered resources.
        List<ProcessScanResult> autoDiscoveryResults = context.getAutoDiscoveredProcesses();
        for (ProcessScanResult result : autoDiscoveryResults) {
            LOG.info("Discovered a mysql process: " + result);
            ProcessInfo procInfo = result.getProcessInfo();
            servers.add(createResourceDetails(context, context.getDefaultPluginConfiguration(), procInfo));
        }

        return servers;
    }

    public DiscoveredResourceDetails discoverResource(Configuration pluginConfiguration,
        ResourceDiscoveryContext resourceDiscoveryContext) throws InvalidPluginConfigurationException {
        ProcessInfo processInfo = null;
        DiscoveredResourceDetails resourceDetails = createResourceDetails(resourceDiscoveryContext,
            pluginConfiguration, processInfo);
        return resourceDetails;
    }
    
    protected static DiscoveredResourceDetails createResourceDetails(ResourceDiscoveryContext discoveryContext,
            Configuration pluginConfig, ProcessInfo processInfo) throws InvalidPluginConfigurationException {
            String key = new StringBuilder().append("VictimsEVD:")
                .append(pluginConfig.getSimple(VICTIMS_SCAN_PATH).getStringValue()).toString();
            DiscoveredResourceDetails result = new DiscoveredResourceDetails(discoveryContext.getResourceType(), key, "victims",
            		pluginConfig.getSimple(VICTIMS_VERSION).getStringValue(), "Victims EVD Scanner", pluginConfig, processInfo);
            return result;

        }
}
