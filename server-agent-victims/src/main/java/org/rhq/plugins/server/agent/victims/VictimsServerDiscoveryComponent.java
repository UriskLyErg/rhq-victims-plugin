package org.rhq.plugins.server.agent.victims;

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
public class VictimsServerDiscoveryComponent implements ManualAddFacet, ResourceDiscoveryComponent {

	//Setup for the plugin. Since nothing happens just create a one time Discovered Resource.
    public DiscoveredResourceDetails discoverResource(Configuration pluginConfiguration,
                                                      ResourceDiscoveryContext context) throws InvalidPluginConfigurationException {

        DiscoveredResourceDetails result = new DiscoveredResourceDetails(
            context.getResourceType(),
            "victimsserver",
            "VictimsServer",
            null,
            "Victims CVEs",
            pluginConfiguration,
            null);
        return result;
    }

    //Not used but brought in from interfaces
    public Set<DiscoveredResourceDetails> discoverResources(
        ResourceDiscoveryContext context) throws InvalidPluginConfigurationException, Exception {
        return Collections.emptySet();
    }
}