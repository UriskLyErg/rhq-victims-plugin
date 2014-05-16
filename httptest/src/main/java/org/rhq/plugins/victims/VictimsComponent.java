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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.core.pluginapi.operation.OperationFacet;
import org.rhq.core.pluginapi.operation.OperationResult;

import com.redhat.victims.VictimsException;
import com.redhat.victims.VictimsRecord;
import com.redhat.victims.VictimsScanner;

/**
 * @author Caleb House
 */

// Victims plugin. component is what does all the work, discovery just tells me
// its there
// This plugin will report hashes to the server plugin.
public class VictimsComponent implements
		ResourceComponent<ResourceComponent<?>>, OperationFacet,
		MeasurementFacet {

	private static final Log LOG = LogFactory.getLog(VictimsComponent.class);
	private static String PATH_CONFIGURATION = "paths";
	private static String SCAN_LOCAL_OPERATION = "scanLocal";
	private static String URL_CONFIGURATION = "victimsServer";
	private static String SALT = "TESTSALT";
	private int PORT_NUMBER = 632154;
		
	// Consider a Forced port private static String PORT_NUMBER = "port";
	// No longer required as Victims is smart enough to do directories
	// private static final String[] EXTENSIONS = new String[] { "jar", "war",
	// "sar" };
	private ArrayList<String> paths = new ArrayList<String>();
	private String hostName = "";
	private String pcName = "";

	private ResourceContext<ResourceComponent<?>> resourceContext;
	private Configuration pluginConfiguration;

	public void start(ResourceContext<ResourceComponent<?>> resourceContext)
			throws InvalidPluginConfigurationException, Exception {
		// This is all for setting up the plugin so that it has the good details
		// for paths and stuff
		this.resourceContext = resourceContext;
		this.pluginConfiguration = this.resourceContext.getPluginConfiguration();
		// Loops for dealing with the fact its a list, makes sure we get all of
		// the paths
		for (int i = 0; i < pluginConfiguration.getList(PATH_CONFIGURATION).getList().size(); i++) {
			paths.add(pluginConfiguration.getList(PATH_CONFIGURATION).getList().get(i).getName());
		}
		hostName = this.pluginConfiguration.getSimple(URL_CONFIGURATION).getName();
		pcName = this.resourceContext.getSystemInformation().getHostname();
	}

	// Useless, we never want to stop, keep on keeping on
	// I changed my mind this would be a good way to drop stuff
	// from the server
	public void stop() {

	}

	// Inherited from MeasurementFacet
	// Basically says whether or not the plugins avaliable, if theres no paths
	// we aint doin jack.
	public AvailabilityType getAvailability() {
		if (paths != null) {
			return AvailabilityType.UP;
		} else {
			return AvailabilityType.DOWN;
		}
	}

	/*
	 * Gets values for doing stuff. Im not quite sure I want to use this since
	 * it isnt actually reporting to the measurement facet anymore, its being
	 * reported to the server. Got to find out if the server has anything to
	 * show metrics otherwise this could be a waste of time.
	 */
	public void getValues(MeasurementReport report,
			Set<MeasurementScheduleRequest> requests) throws IOException,
			Exception, VictimsException {
	}

	// Old used for testing before server side implemented
	// VictimsDBInterface vdb = VictimsDB.db();

	public OperationResult invokeOperation(String name, Configuration parameters)
			throws InterruptedException, Exception {

		if (name != null && name.equals(SCAN_LOCAL_OPERATION)) {
			for (String path : paths) {
				if (new File(path).exists()) {
					for (VictimsRecord vr : VictimsScanner.getRecords(path)) {
						//LOG.info(vr.hash);
						try (Socket echoSocket = new Socket(hostName, PORT_NUMBER);
								PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
								BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
								BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
							String toSend = pcName + SALT + vr.toString();
							out.println(toSend);
						} catch (UnknownHostException e) {
							System.err.println("Don't know about host " + hostName);
						} catch (IOException e) {
							System.err.println("Couldn't get I/O for the connection to " + hostName);
						}
					}
				}
			}
			
			OperationResult result = new OperationResult("Victims Scan Complete");
			return result;
		}
		throw new UnsupportedOperationException("Operation " + name + " is not valid");
	}
}

/*
 * This is old code that was used to check for vulns before they were sent to a
 * server plugin for (String cve : vdb.getVulnerabilities(vr)) { for
 * (MeasurementScheduleRequest request : requests) { if
 * (request.getName().equals("vulnerability")) { MeasurementDataTrait result =
 * new MeasurementDataTrait(request, cve); report.addData(result); } } }
 */
