package org.rhq.plugins.victims.server;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.enterprise.server.plugin.pc.ControlFacet;
import org.rhq.enterprise.server.plugin.pc.ControlResults;
import org.rhq.enterprise.server.plugin.pc.ServerPluginComponent;
import org.rhq.enterprise.server.plugin.pc.ServerPluginContext;

import com.redhat.victims.VictimsRecord;

import org.rhq.plugins.victims.server.SyncJSONPCMap;
import org.rhq.plugins.victims.server.ServerListener;

public class VictimsServerComponent implements ServerPluginComponent, ControlFacet {

	private ResourceContext<ResourceComponent<?>> resourceContext;
	private Configuration pluginConfiguration;
    private ServerPluginContext context;
    private SyncJSONPCMap tempDB = new SyncJSONPCMap();
    private ServerListener serverSide = null;
    private PropertyMap tempRecord = null;
    private PropertyList tempInfected = null;
    
    private static String INFECTED = "infected";
    private static String RECORD = "record";
    private static String PC = "pc";
    private static String CVE = "cve";
    private static String PATH = "path";

    public ControlResults checkCVE(ControlResults controlResults) {
    	/* Actual method for getting CVE results
    	* Works by running through the SyncMap and slamming results
    	* Appropriate shape. This is so far beyond usable. It really needs
    	* DB access but this is basically proof of concept
    	*/
    	tempInfected = new PropertyList(INFECTED);
    	for (VictimsRecord record: tempDB.getRecords()) {
    		for (String name : tempDB.pcNames(record)) {
    			for (String path : tempDB.getPaths(record)) {
    				tempRecord = new PropertyMap(RECORD);
        			for (String cve : record.cves){
        				tempRecord.put(new PropertySimple(PC, name));
        				tempRecord.put(new PropertySimple(PATH, path));
        				tempRecord.put(new PropertySimple(CVE, cve));
        			}    				
    			}

    		}
    	}
    	controlResults.getComplexResults().put(tempInfected);
    	return controlResults;
    }
    
    public void initialize(ServerPluginContext context) throws Exception {
        this.context = context;
    }

    public void start() { //Setup, starts up the listener
        serverSide = new ServerListener(Integer.parseInt((context.getPluginConfiguration().getSimpleValue("portnumber"))));
        serverSide.run();
    }

    public void stop() {
    	serverSide.running = false;
    }

    public void shutdown() {

    }

    public ControlResults invoke(String name, Configuration parameters) {
        ControlResults controlResults = new ControlResults(); //Gets any results stored in the SyncJSONMap
        if (name.equals("checkCVE")) {
        	controlResults = checkCVE(controlResults);
        } else {
            controlResults.setError("Unknown operation name: " + name);
        }
        return controlResults;
    }
    
    /*
    @Override
    public String toString() {
        if (this.context == null) {
            return "<no context>";
        }

        StringBuilder str = new StringBuilder();
        str.append("plugin-key=").append(this.context.getPluginEnvironment().getPluginKey()).append(",");
        str.append("plugin-url=").append(this.context.getPluginEnvironment().getPluginUrl()).append(",");
        str.append("plugin-config=[").append(getPluginConfigurationString()).append(']'); // do not append ,
        return str.toString();
    }
    
    private String getPluginConfigurationString() {
        String results = "";
        Configuration config = this.context.getPluginConfiguration();
        for (PropertySimple prop : config.getSimpleProperties().values()) {
            if (results.length() > 0) {
                results += ", ";
            }
            results = results + prop.getName() + "=" + prop.getStringValue();
        }
        return results;
    } */
}