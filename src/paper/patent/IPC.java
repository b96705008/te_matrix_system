package paper.patent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

public class IPC {

	public static void main(String[] args) {
		IPC ipc = new IPC();
		ipc.addIPC("C03B 037/10");

	}
	
	Set<String> sectionSet = new TreeSet<String>();
	Set<String> classSet = new TreeSet<String>();
	Set<String> subClassSet = new TreeSet<String>();
	Set<String> groupSet = new TreeSet<String>();
	Set<String> subGroupSet = new TreeSet<String>();
	//Map<String,String> mainIpc = new HashMap<String,String>(); //1:section, 2:class, 3:subClass, 4:group, 5:subGroup (5 layer)
	
	public IPC(){		
	}
	
	public IPC(Set<String> ipcSet){
		this.addIPC(ipcSet);
	}
	
	static public Map<String,String> parseIPC(String ipc){
		//C03B 37/10 -> C, C03, C03B, C03B0037100000
		Map<String,String> ipcMap = new HashMap<String,String>();
		
		String[] ipcArr = ipc.split(" ");
		String ipc_section = ipcArr[0].substring(0, 1);
		String ipc_class = ipcArr[0].substring(0, 3);
		String ipc_subClass = ipcArr[0];
		String ipc_group = "";
		String ipc_subGroup = "";
		String[] groupArr = ipcArr[1].split("/");
		
		int prefixLength = groupArr[0].length();
		for(int i=0; i<(4-prefixLength); i++){
			groupArr[0] = "0"+groupArr[0];
		}
		
		int postfixLength = groupArr[1].length();
		for(int i=0; i<(6-postfixLength); i++){
			groupArr[1] = groupArr[1]+"0";
		}
		ipc_group = ipcArr[0]+groupArr[0]+"000000";
		ipc_subGroup = ipcArr[0]+groupArr[0]+groupArr[1];
		
		ipcMap.put("section", ipc_section);
		ipcMap.put("class", ipc_class);
		ipcMap.put("subClass", ipc_subClass);
		ipcMap.put("group", ipc_group);
		if(!ipc_subGroup.equals(ipc_group))
			ipcMap.put("subGroup", ipc_subGroup);
		
		return ipcMap;
	}
	
	public void addIPC(String ipc){
		Map<String,String> ipcMap = IPC.parseIPC(ipc);
		
		this.sectionSet.add(ipcMap.get("section"));
		this.classSet.add(ipcMap.get("class"));
		this.subClassSet.add(ipcMap.get("subClass"));
		this.groupSet.add(ipcMap.get("group"));
		if(ipcMap.containsKey("subGroup"))
			this.subGroupSet.add(ipcMap.get("subGroup"));
	}
	
	public void addIPC(Set<String> ipcSet){
		if(ipcSet.size() > 0){
			//this.mainIpc = IPC.parseIPC(ipcList.get(0));
			for(String ipc:ipcSet){
				this.addIPC(ipc);
			}
		}else{
			System.out.println("ipcArr.length == 0 !!");
		}	
	}
	
	//layserStr = set 1,2,3,4,5 ex:"12345"
	public Set<String> getParseIpcSet(String layerStr){
		Set<String> ipcSet = new TreeSet<String>();
		
		if(layerStr.contains("1"))
			ipcSet.addAll(this.sectionSet);
		if(layerStr.contains("2"))
			ipcSet.addAll(this.classSet);
		if(layerStr.contains("3"))
			ipcSet.addAll(this.subClassSet);
		if(layerStr.contains("4"))
			ipcSet.addAll(this.groupSet);
		if(layerStr.contains("5"))
			ipcSet.addAll(this.subGroupSet);
		
		return ipcSet;
	}
	/*
	public Set<String> getMainIpcSet(String layerStr){
		Set<String> mainIpcSet = new TreeSet<String>();
		
		if(layerStr.contains("1"))
			mainIpcSet.add(this.mainIpc.get("section"));
		if(layerStr.contains("2"))
			mainIpcSet.add(this.mainIpc.get("class"));
		if(layerStr.contains("3"))
			mainIpcSet.add(this.mainIpc.get("subClass"));
		if(layerStr.contains("4"))
			mainIpcSet.add(this.mainIpc.get("group"));
		if(layerStr.contains("5") && this.mainIpc.containsKey("subGroup"))
			mainIpcSet.add(this.mainIpc.get("subGroup"));
		
		return mainIpcSet;
	}*/
}
