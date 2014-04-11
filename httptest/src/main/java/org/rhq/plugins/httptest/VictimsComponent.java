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

package org.rhq.plugins.httptest;

import java.io.File;
import java.io.IOException;
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

import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;
import com.redhat.victims.VictimsException;
import com.redhat.victims.VictimsRecord;
import com.redhat.victims.VictimsScanner;

/**
 * @author Caleb House
 *
 */
public class VictimsComponent implements ResourceComponent, MeasurementFacet {

    private static final String[] EXTENSIONS = new String[]{"jar","war","sar"};
    String path;
    
    /* (non-Javadoc)
     * @see org.rhq.core.pluginapi.inventory.ResourceComponent#start(org.rhq.core.pluginapi.inventory.ResourceContext)
     */
    public void start(ResourceContext context) throws InvalidPluginConfigurationException, Exception {
        path = new String(context.getResourceKey());
    }

    /* (non-Javadoc)
     * @see org.rhq.core.pluginapi.inventory.ResourceComponent#stop()
     */
    public void stop() {
        // TODO Auto-generated method stub
    }

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> metrics)
    		throws IOException, Exception, VictimsException {

    	Collection<File> fileList = null;
    	VictimsDBInterface vdb = VictimsDB.db();
    	
    	for (String arg : path.split(";")){
    		File dir = new File(arg);
    		if (fileList == null){
    			fileList = FileUtils.listFiles(dir, EXTENSIONS, true);
    		} else {
    			fileList.addAll(FileUtils.listFiles(dir, EXTENSIONS, true));	
    		}
    	}
    	
    	for (File javaFile : fileList) {
        	for (VictimsRecord vr : VictimsScanner.getRecords(javaFile.getAbsolutePath())){
        		for (String cve : vdb.getVulnerabilities(vr)) {
        			for (MeasurementScheduleRequest request : metrics) {
            			if (request.getName().equals("vulnerability")) {
                            report.addData(new MeasurementDataTrait(request, cve));
                        } 
        			}
        		}
        	}               
    	}
    }

	@Override
	public AvailabilityType getAvailability() {
		return null;
	}
}
