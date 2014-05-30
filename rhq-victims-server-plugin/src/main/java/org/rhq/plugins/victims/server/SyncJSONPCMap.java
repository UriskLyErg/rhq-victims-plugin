package org.rhq.plugins.victims.server;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.redhat.victims.VictimsRecord;

public class SyncJSONPCMap {
    
	//Design is Victims Record (Path, PCName) to allow you to find exactly where you stuff is
	//This will be replaced by DB when physically possible
	LinkedHashMap<VictimsRecord, LinkedHashMap<String, String>> victimsHash = new LinkedHashMap<VictimsRecord, LinkedHashMap<String, String>>();
	
	public synchronized boolean contains(VictimsRecord vr){
		return victimsHash.containsKey(vr); //Checks if the VR already exists in list
	}
	
	//returns PCs with a specific Victims Record
	public synchronized ArrayList<String> pcNames(VictimsRecord vr){
		ArrayList<String> names = new ArrayList<String>();
		for (String name : victimsHash.get(vr).keySet()){
			names.add(name);
		}
		return names;
	}
	
	//Returns paths for a specific Victims Record
	public synchronized ArrayList<String> getPaths(VictimsRecord vr){
		ArrayList<String> paths = new ArrayList<String>();
		for (String path : victimsHash.get(vr).values()){
			paths.add(path);
		}
		return paths;
	}
	
	//Returns list of all Victims Records (gets big)
	public synchronized ArrayList<VictimsRecord> getRecords() {
		ArrayList<VictimsRecord> victims = new ArrayList<VictimsRecord>();
		for (VictimsRecord key : victimsHash.keySet()){
			victims.add(key);
		}
		return victims;
	}
	
	//Adds a new record, or just the new PC and path if it already exists
	public synchronized void put(VictimsRecord vr, String name, String path){
		if (victimsHash.containsKey(vr)){
			victimsHash.get(vr).put(name, path);
		} else {
			LinkedHashMap<String, String> pcNames = new LinkedHashMap<String, String>();
			pcNames.put(name, path);
			victimsHash.put(vr, pcNames);
		}
	}
	
	//Removes a PC and path from this list, also removes the record if it is the last one
	public synchronized void deleteName(VictimsRecord vr, String name, String path){
		if (victimsHash.containsKey(vr)){
			victimsHash.get(vr).remove(name);
			if (victimsHash.get(vr).size() == 0) {
				victimsHash.remove(vr);
			}
		}
	}
}