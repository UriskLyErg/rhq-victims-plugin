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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.io.FileUtils;
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
import org.hyperic.sigar.DirUsage;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.pluginapi.util.ObjectUtil;
import org.rhq.core.system.FileSystemInfo;
import org.rhq.core.system.SystemInfo;

/**
 * @author Greg Hinkle
 * @author Heiko W. Rupp
 */
public class VictimsComponent implements ResourceComponent<PlatformComponent>, MeasurementFacet {

	private static final Log LOG = LogFactory.getLog(VictimsComponent.class);
	private static final String[] EXTENSIONS = new String[] { "jar", "war", "sar" };
	private ArrayList<String> paths = new ArrayList<String>();

	private ResourceContext<?> resourceContext;

	public void start(ResourceContext<?> resourceContext)
			throws InvalidPluginConfigurationException, Exception {
		this.resourceContext = resourceContext;
	}

	public void stop() {
	}

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

		for (String arg : paths) {// .split(";")){
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
	}
}
