package org.rhq.plugins.victims.server;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.enterprise.server.plugin.pc.ControlFacet;
import org.rhq.enterprise.server.plugin.pc.ControlResults;
import org.rhq.enterprise.server.plugin.pc.ServerPluginComponent;
import org.rhq.enterprise.server.plugin.pc.ServerPluginContext;

import com.redhat.victims.VictimsRecord;
import org.rhq.plugins.victims.server.SyncJSONPCMap;

public class VictimsServerComponent implements ServerPluginComponent, ControlFacet {

	private ResourceContext<ResourceComponent<?>> resourceContext;
	private Configuration pluginConfiguration;
    private ServerPluginContext context;
    private SyncJSONPCMap tempDB = new SyncJSONPCMap();
    private ServerListener serverSide = null;
    private PropertyList tempRecord = null;
    private PropertyList tempInfected = null;
    
    private static String INFECTED = "infected";
    private static String RECORD = "record";
    private static String PC = "pc";
    private static String CVE = "cve";

    public void checkCVE(ControlResults controlResults) {
    	tempInfected = new PropertyList(INFECTED);
    	for (VictimsRecord record: tempDB.getRecords()) {
    		for (String cve : record.cves){
    			tempRecord = new PropertyList(RECORD);
    			for (String name : tempDB.pcNames(record)) {
    				tempRecord.add(new PropertySimple(name, name));
    			}
    		}
    	}
    	controlResults.getComplexResults().put(tempInfected);
    }
    /*
     * <serverplugin:results>
				<c:list-property name="paths">
					<c:list-property name="cves">
						<c:simple-property name="pc" type="string" readOnly="true" />
						<c:simple-property name="cve" type="string" readOnly="true" />
					</c:list-property>
			</c:list-property>
     */
    
    public void initialize(ServerPluginContext context) throws Exception {
        this.context = context;
    }

    public void start() {
        serverSide = new ServerListener(Integer.parseInt((context.getPluginConfiguration().getSimpleValue("portnumber"))));
    }

    public void stop() {

    }

    public void shutdown() {

    }

    public ControlResults invoke(String name, Configuration parameters) {
        ControlResults controlResults = new ControlResults();
        if (name.equals("checkCVE")) {
        	
        } else {
            controlResults.setError("Unknown operation name: " + name);
        }
        return controlResults;
    }
}