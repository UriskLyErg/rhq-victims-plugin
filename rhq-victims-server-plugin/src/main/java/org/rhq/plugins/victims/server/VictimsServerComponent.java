package org.rhq.plugins.victims.server;

import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.enterprise.server.plugin.pc.ControlFacet;
import org.rhq.enterprise.server.plugin.pc.ControlResults;
import org.rhq.enterprise.server.plugin.pc.ScheduledJobInvocationContext;
import org.rhq.enterprise.server.plugin.pc.ServerPluginComponent;
import org.rhq.enterprise.server.plugin.pc.ServerPluginContext;

import com.redhat.victims.VictimsRecord;

public class VictimsServerComponent implements ServerPluginComponent, ControlFacet {

	private static String SALT = "TESTSALT";
	
	private ResourceContext<ResourceComponent<?>> resourceContext;
	private Configuration pluginConfiguration;
    private ServerPluginContext context;
    private SyncJSONPCMap tempDB = new SyncJSONPCMap();
    private ServerListener serverSide = null;
    private PropertyList tempCVE = null;

    public void checkCVE(ControlResults controlResults) {
    	for (VictimsRecord record: tempDB.getRecords()) {
    		for (String cve : record.cves){
    			tempCVE = new PropertyList(cve);
    			for (String name : tempDB.pcNames(record)) {
    				tempCVE.add(new PropertySimple(name, name));
    			}
    			controlResults.getComplexResults().put(tempCVE);
    		}
    	}
    }
    
    public void initialize(ServerPluginContext context) throws Exception {
        this.context = context;
    }

    public void start() {
        serverSide = new ServerListener(Integer.parseInt((context.getPluginConfiguration().get("portnumber").getName())));
    }

    public void stop() {

    }

    public void shutdown() {

    }

    public ControlResults invoke(String name, Configuration parameters) {
        ControlResults controlResults = new ControlResults();
        if (name.equals("testControl")) {
            String paramProp = parameters.getSimple("paramProp").getStringValue();
            if (paramProp.equals("fail")) {
                controlResults.setError("simulated failure!");
            } else {
                controlResults.getComplexResults().put(
                    new PropertySimple("resultProp", "the param was [" + paramProp + "]"));
            }
            System.out.println("Invoked 'testControl'!!! : " + this);
        } else if (name.equals("testControlWithNoParams")) {
            controlResults.getComplexResults().put(new PropertySimple("result", "results value"));
            System.out.println("Invoked 'testControlWithNoParams'!!! : " + this);
        } else if (name.equals("testControlWithNoParamsOrResults")) {
            System.out.println("Invoked 'testControlWithNoParamsOrResults'!!! : " + this);
        } else {
            controlResults.setError("Unknown operation name: " + name);
        }
        return controlResults;
    }

    public void myScheduledJobMethod1() throws Exception {
        System.out.println("The sample plugin scheduled job [myScheduledJobMethod1] has triggered!!! : " + this);
    }

    public void myScheduledJobMethod2(ScheduledJobInvocationContext invocation) throws Exception {
        System.out.println("The sample plugin scheduled job [myScheduledJobMethod2] has triggered!!! : " + this
            + " - CALLBACK DATA=" + invocation.getJobDefinition().getCallbackData());
    }

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
    }
}