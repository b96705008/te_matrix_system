package paper.backend;

import java.io.BufferedInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonAPI {
	static String apiURL = "http://140.112.107.1/patentAPI/index.php?fun=getPatentClass";
	
	public static void main(String[] args) throws Exception {
		setAllPatentClass();

	}
	
	static void setAllPatentClass() throws Exception{
		 Database db = Database.getInstance();;
		 Map<String, String> patentYearMap = db.getPatentYearMap();
		 Set<String> patentIDSet = patentYearMap.keySet();
		 
		 for(String patentID:patentIDSet){
			 String year = patentYearMap.get(patentID);
			 setPatentClassAPI(patentID, year, db);
		 }

	}
	
	static void setPatentClassAPI(String patentID, String year, Database db) throws Exception{
	    int chunksize = 4096;
	    byte[] chunk = new byte[chunksize];
	    String jsonStr = "";
	    int count;
	    
	    URL pageUrl = new URL(JsonAPI.apiURL+"&patentID="+patentID+"&year="+year);
	      
	    // read web (binary stream)
	    BufferedInputStream bis = new BufferedInputStream(pageUrl.openStream());
	     
	    while ((count = bis.read(chunk)) != -1) {
	    	String s = new String(chunk, 0, count, "utf-8");
	        jsonStr += s;
	    }
	    
	    JSONObject jsonObject = null;
	    //check if exist this id
	    try{
	    	jsonObject = new JSONObject(jsonStr);
	    }catch(JSONException e){
	    	e.printStackTrace();
	    }
	    
	    JSONArray ipcJsonArray = jsonObject.getJSONArray("IPC");
	    JSONArray uspcJsonArray = jsonObject.getJSONArray("USPC");
	    
	    for(int i=0; i<ipcJsonArray.length(); i++){
	    	String ipc = ipcJsonArray.getString(i);
	    	System.out.println(ipc);
	    	db.insertClass("IPC", patentID, ipc);
	    }
	    
	    for(int i=0; i<uspcJsonArray.length(); i++){
	    	String upsc = uspcJsonArray.getString(i);
	    	System.out.println(upsc);
	    	db.insertClass("USPC", patentID, upsc);
	    }
	}

}
