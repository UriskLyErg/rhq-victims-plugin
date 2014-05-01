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

import java.util.Set;
import java.util.Collections;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ManualAddFacet;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;

/**
* 
* @author Caleb House
*/
public class VictimsDiscoveryComponent implements ManualAddFacet<VictimsComponent>, ResourceDiscoveryComponent<VictimsComponent> {


    @Override
    public DiscoveredResourceDetails discoverResource(Configuration pluginConfiguration,
                                                      ResourceDiscoveryContext<VictimsComponent> context) throws InvalidPluginConfigurationException {

        String paths = ""; 
        
        for (int i = 0; i < pluginConfiguration.getList("paths").getList().size(); i++){
        	paths = pluginConfiguration.getList("paths").getList().get(i).getName();
	        if (paths==null || paths.isEmpty()) {
	            throw new InvalidPluginConfigurationException("Path must not be empty");
	        }
	        if (paths.equals("/")) {
	            throw new InvalidPluginConfigurationException("/ is forbidden");
	        }
        }

        DiscoveredResourceDetails result = new DiscoveredResourceDetails(
            context.getResourceType(),
            "victims",
            "Victims",
            null,
            "Victims Paths",
            pluginConfiguration,
            null);
        return result;
    }

    @Override
    public Set<DiscoveredResourceDetails> discoverResources(
        ResourceDiscoveryContext<VictimsComponent> context) throws InvalidPluginConfigurationException, Exception {
        return Collections.emptySet();
    }
}
/*
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
*/