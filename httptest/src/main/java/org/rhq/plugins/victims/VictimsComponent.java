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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.InvalidPluginConfigurationException;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;

import com.redhat.victims.VictimsException;
import com.redhat.victims.VictimsRecord;
import com.redhat.victims.VictimsScanner;
import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Caleb House
 */
public class VictimsComponent implements
		ResourceComponent<ResourceComponent<?>>, MeasurementFacet {

	private static final Log LOG = LogFactory.getLog(VictimsComponent.class);
	private static final String[] EXTENSIONS = new String[] { "jar", "war", "sar" };
	private ArrayList<String> paths = new ArrayList<String>();
	
	private ResourceContext<ResourceComponent<?>> resourceContext;
	private Configuration pluginConfiguration;

	public void start(ResourceContext<ResourceComponent<?>> resourceContext)
			throws InvalidPluginConfigurationException, Exception {
		this.resourceContext = resourceContext;
		this.pluginConfiguration = this.resourceContext.getPluginConfiguration();
		for (int i = 0; i < pluginConfiguration.getList("paths").getList().size(); i++){
			paths.add(pluginConfiguration.getList("paths").getList().get(i).getName());
		}
	}

	public void stop() {
	}

	// Inherited from MeasurementFacet
	public AvailabilityType getAvailability() {
		if (paths != null) {
			return AvailabilityType.UP;
		} else {
			return AvailabilityType.DOWN;
		}
	}

	public void getValues(MeasurementReport report,
			Set<MeasurementScheduleRequest> requests) throws IOException,
			Exception, VictimsException {
		
		Collection<File> fileList = null;
		VictimsDBInterface vdb = VictimsDB.db();
		
		for (String arg : paths) {
			File dir = new File(arg);
			if (fileList == null) {
				fileList = FileUtils.listFiles(dir, EXTENSIONS, true);
			} else {
				fileList.addAll(FileUtils.listFiles(dir, EXTENSIONS, true));
			}
		}

		for (File javaFile : fileList) {
			for (VictimsRecord vr : VictimsScanner.getRecords(javaFile
					.getAbsolutePath())) {
				for (String cve : vdb.getVulnerabilities(vr)) {
					for (MeasurementScheduleRequest request : requests) {
						if (request.getName().equals("vulnerability")) {
							MeasurementDataTrait result = new MeasurementDataTrait(request, cve);
							report.addData(result);
						}
					}
				}
			}
		}
		
		/*boolean sendVictimsRecord(String host; int portNumber; String victimsRecord) throws IOException {

	        String hostName = args[0];
	        int portNumber = Integer.parseInt(args[1]);

	        try (
	            Socket echoSocket = new Socket(hostName, portNumber);
	            PrintWriter out =
	                new PrintWriter(echoSocket.getOutputStream(), true);
	            BufferedReader in =
	                new BufferedReader(
	                    new InputStreamReader(echoSocket.getInputStream()));
	            BufferedReader stdIn =
	                new BufferedReader(
	                    new InputStreamReader(System.in))
	        ) {
	            String userInput;
	            while ((userInput = stdIn.readLine()) != null) {
	                out.println(userInput);
	                System.out.println("echo: " + in.readLine());
	            }
	        } catch (UnknownHostException e) {
	            System.err.println("Don't know about host " + hostName);
	        } catch (IOException e) {
	            System.err.println("Couldn't get I/O for the connection to " +
	                hostName);
	        } 
	        */
	}
}
