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
		ResourceComponent<ResourceComponent<?>>, OperationFacet {

	private static final Log LOG = LogFactory.getLog(VictimsComponent.class);
	private static String PATH_CONFIGURATION = "paths";
	private static String SCAN_LOCAL_OPERATION = "scanLocal";
	private static String URL_CONFIGURATION = "hostname";
	private static String SALT = "TESTSALT";
	private static String PORT = "port";
	private int portNumber = 632154;
		
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
		hostName = this.pluginConfiguration.getSimple(URL_CONFIGURATION).getStringValue();
		pcName = this.resourceContext.getSystemInformation().getHostname();
		portNumber = this.pluginConfiguration.getSimple(PORT).getIntegerValue();
	}

	// Useless, we never want to stop, keep on keeping on
	// I changed my mind this would be a good way to drop stuff
	// from the server
	public void stop() {

	}

	public OperationResult invokeOperation(String name, Configuration parameters)
			throws InterruptedException, Exception {
		
		Socket echoSocket = new Socket(hostName, portNumber);
		PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
		String toSend = "";

		if (name != null && name.equals(SCAN_LOCAL_OPERATION)) {
			for (String path : paths) {
				if (new File(path).exists()) {
					for (VictimsRecord vr : VictimsScanner.getRecords(path)) {
						toSend = pcName + SALT + vr.toString() + SALT + path;
						out.println(toSend);
						out.println();
					}
				}
			}
			
			OperationResult result = new OperationResult("complete");
			echoSocket.close();
			return result;
		}
		echoSocket.close();
		throw new UnsupportedOperationException("Operation " + name + " is not valid");
	}

	@Override
	public AvailabilityType getAvailability() {
		return AvailabilityType.UP;
	}
}
