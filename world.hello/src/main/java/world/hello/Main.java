package world.hello;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.redhat.victims.database.VictimsDB;
import com.redhat.victims.database.VictimsDBInterface;
import com.redhat.victims.VictimsException;
import com.redhat.victims.VictimsRecord;
import com.redhat.victims.VictimsScanner;

import org.apache.commons.io.FileUtils;

public class Main {
	
	private static final String[] EXTENSIONS = new String[]{"jar","war","sar"};
	
    public static void testRunningJars(Collection<File> testJars) throws IOException, VictimsException{
    	
    	VictimsDBInterface vdb = VictimsDB.db();
                
        for (File javaFile : testJars) {
        	for (VictimsRecord vr : VictimsScanner.getRecords(javaFile.getAbsolutePath())){
        		System.out.println(javaFile.getName());
        		for (String cve : vdb.getVulnerabilities(vr)) {
	        		System.out.println(cve);
	        	}
        	}
        }
    }
    
    public static void main(String[] args) throws IOException, VictimsException {
    	
    	Collection<File> fileList = null;
    	
    	for (String arg : args){
    		File rootdir = new File(arg);
    		if (fileList == null){
    			fileList = FileUtils.listFiles(rootdir, EXTENSIONS, true);
    		} else {
    			fileList.addAll(FileUtils.listFiles(rootdir, EXTENSIONS, true));	
    		}
    	}
    	
    	testRunningJars(fileList);
    }
}