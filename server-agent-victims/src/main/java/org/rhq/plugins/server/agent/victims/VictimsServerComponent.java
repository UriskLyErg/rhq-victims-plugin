package org.rhq.plugins.server.agent.victims;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.AvailabilityType;

import com.redhat.victims.VictimsRecord;

import org.rhq.plugins.server.agent.victims.SyncJSONPCMap;
import org.rhq.plugins.server.agent.victims.ServerListener;

public class VictimsServerComponent implements ResourceComponent<ResourceComponent<?>>, OperationFacet {

	private ResourceContext<ResourceComponent<?>> resourceContext;
    private SyncJSONPCMap tempDB = new SyncJSONPCMap();
    private ServerListener serverSide = null;
    private PropertyMap tempRecord = null;
    private PropertyList tempInfected = null;
    
    private static String INFECTED = "infected";
    private static String RECORD = "record";
    private static String PC = "pc";
    private static String CVE = "cve";
    private static String PATH = "path";
    private static String RETURN_RESULTS = "checkCVE";
    private static String PORT_NUMBER = "portnumber";
    private static String WIPE_DB = "cleanDB";
    
    /*<c:list-property name="infected">
	<c:list-property name="host">
		<c:simple-property name="pc" />
	</c:list-property>
	<c:list-property name="dir">
		<c:simple-property name="path" />
	</c:list-property>
	<c:list-property name="record">
		<c:simple-property name="cve" />
	</c:list-property>
	
    /* Actual method for getting CVE results
    * Works by running through the SyncMap and slamming results
    * Appropriate shape. This is so far beyond usable. It really needs
    * DB access but this is basically proof of concept
    */
    public OperationResult checkCVE(Configuration controlResults, OperationResult results) {
    	
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
        			tempInfected.add(tempRecord);
    			}
    		}
    	}
    	results.getComplexResults().put(tempInfected);
    	return results;
    }
    
    public void clearDB(){
    	for (VictimsRecord record: tempDB.getRecords()){
    		for (String host : tempDB.pcNames(record)) {
    			for (String path : tempDB.getPaths(record)){
    				tempDB.deleteName(record, host, path);
    			}
    		}
    	}
    }

	public AvailabilityType getAvailability() {
		return AvailabilityType.UP;
	}

	public OperationResult invokeOperation(String name, Configuration parameters)
			throws InterruptedException, Exception {
		OperationResult results = new OperationResult();
		
		if (name != null && name.equals(RETURN_RESULTS)) {
			results = checkCVE(parameters, results);
        } else if (name != null && name.equals(WIPE_DB)) {
        	clearDB();
        	results.setSimpleResult("deleted");
        } else {
        	results.setErrorMessage("Unknown operation name: " + name);
        }
		return results;
	}

	public void start(ResourceContext<ResourceComponent<?>> context)
			throws InvalidPluginConfigurationException, Exception {
		this.resourceContext = context;
		serverSide = new ServerListener(Integer.parseInt((resourceContext.getPluginConfiguration().getSimpleValue(PORT_NUMBER))));
        serverSide.run();	
	}

	public void stop() {
		serverSide.running = false;
	}
}