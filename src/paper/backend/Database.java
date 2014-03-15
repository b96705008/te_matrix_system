package paper.backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import paper.clustering.cluster.ClusterGroup;
import paper.nlp.NLPTool;
import paper.nlp.phrase.PhraseGroup;
import paper.nlp.phrase.object.NounPhrase;
import paper.nlp.phrase.object.PrepPhrase;
import paper.nlp.phrase.object.VerbPhrase;
import paper.patent.Patent;

import com.mysql.jdbc.PreparedStatement;

import edu.stanford.nlp.ling.HasWord;

public class Database {
	//資料庫連結設定
	static String URL = "jdbc:mysql://localhost:3306/patent_value";
	static String USER = "root";
	static String PASSWORD = "root";
	
	private static Database instance = null; 
	
	public static Database getInstance() { 
        if (instance == null) {
            instance = new Database(); 
        }
        return instance; 
    } 

	//property
	Connection conn;
	Statement stmt;
	
	private Database(){
		try {
			this.conn = DriverManager.getConnection(URL, USER, PASSWORD);
			this.stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public void closeDB() throws Exception{
		this.stmt.close();
		this.conn.close();
	}
	
	//======INSERT, UPDATE========
	public void insertIpcTitle(String ipc, String title) throws SQLException{
		PreparedStatement pstmt;
		pstmt = (PreparedStatement) this.conn.prepareStatement("INSERT INTO `IPC_TITLE` VALUES(?, ?)");
		pstmt.setString(1, ipc);
		pstmt.setString(2, title);
		pstmt.executeUpdate(); 
		pstmt.clearParameters(); 
		pstmt.close(); 
	}
	
	public void insertIpcTitle(String filePath) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line = null;
		
		while((line = br.readLine()) != null){
			System.out.println(line);
			String[] ipc_title = line.split("\t");
			String ipc = ipc_title[0].trim();
			String title = ipc_title[1].trim();		
			try {
				this.insertIpcTitle(ipc, title);
			} catch (SQLException e) {
				System.out.println("Duplicate entry for key 'PRIMARY'");
			}
		}
		br.close();	
	}
	
	public void insertIpcTitle() throws IOException{
		this.insertIpcTitle("data/EN_ipc_section_A_title_list_20120101.txt");
		this.insertIpcTitle("data/EN_ipc_section_B_title_list_20120101.txt");
		this.insertIpcTitle("data/EN_ipc_section_C_title_list_20120101.txt");
		this.insertIpcTitle("data/EN_ipc_section_D_title_list_20120101.txt");
		this.insertIpcTitle("data/EN_ipc_section_E_title_list_20120101.txt");
		this.insertIpcTitle("data/EN_ipc_section_F_title_list_20120101.txt");
		this.insertIpcTitle("data/EN_ipc_section_G_title_list_20120101.txt");
		this.insertIpcTitle("data/EN_ipc_section_H_title_list_20120101.txt");
	}
	
	//儲存專利資訊的接口
	public void insertPatentProfile(String patentID, String year, String title, String title_dwpi, String ipc, 
			String citing_ref, String forwardCiteNum, String ab_dwpi, String novelty_dwpi, String use_dwpi, 
			String adv_dwpi, String tech_dwpi, String first_claim, String terms_dwpi, String ab){
		String insertSQL = "INSERT INTO `PATENT_PROFILE` VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		PreparedStatement pstmt;
		try{
			pstmt = (PreparedStatement) this.conn.prepareStatement(insertSQL);
			pstmt.setString(1, patentID);
			pstmt.setString(2, year);
			pstmt.setString(3, title);
			pstmt.setString(4, title_dwpi);
			pstmt.setString(5, ipc);
			pstmt.setString(6, citing_ref);
			pstmt.setString(7, forwardCiteNum);
			pstmt.setString(8, ab_dwpi);
			pstmt.setString(9, novelty_dwpi);
			pstmt.setString(10, use_dwpi);
			pstmt.setString(11, adv_dwpi);
			pstmt.setString(12, tech_dwpi);
			pstmt.setString(13, first_claim);
			pstmt.setString(14, terms_dwpi);
			pstmt.setString(15, ab);
			pstmt.executeUpdate(); 
			pstmt.clearParameters(); 
			pstmt.close(); 
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void updatePatentProfile(String patentID, String contentStr){
		//String updateSQL = "UPDATE `PATENT_PROFILE` SET `abstract`='"+contentStr+"' WHERE `patentID`='"+patentID+"'";
		String updateSQL = "UPDATE `PATENT_PROFILE` SET `first_claim`='"+contentStr+"' WHERE `patentID`='"+patentID+"'";
		try {
			this.stmt.executeUpdate(updateSQL);		
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void insertTFMatrixData(String field, String patentID, String year, String tech, String fun){
		String insertSQL = "INSERT INTO `TFMatix_Content` VALUES(?, ?, ?, ?, ?)";
		PreparedStatement pstmt;
		try{
			pstmt = (PreparedStatement) this.conn.prepareStatement(insertSQL);
			pstmt.setString(1, field);
			pstmt.setString(2, patentID);
			pstmt.setString(3, year);
			pstmt.setString(4, tech);
			pstmt.setString(5, fun);
			pstmt.executeUpdate(); 
			pstmt.clearParameters(); 
			pstmt.close(); 
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//儲存技術功效矩陣資料的接口 (tech, effect)
	public void insertTFDimeData(String dime, String patentID, String field,  
			String dime_level1 , String dime_level2){
		
		String insertSQL = "INSERT INTO `"+dime+"` VALUES(?, ?, ?, ?)";
		PreparedStatement pstmt;
		try{
			pstmt = (PreparedStatement) this.conn.prepareStatement(insertSQL);
			pstmt.setString(1, patentID);
			pstmt.setString(2, field);
			pstmt.setString(3, dime_level1);
			pstmt.setString(4, dime_level2);
			pstmt.executeUpdate(); 
			pstmt.clearParameters(); 
			pstmt.close(); 
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//儲存General Inquirer資訊的接口
	public void insertEffectTerms(String term, String number, String semantic, 
			String tag, String defined, String phrase){
		String insertSQL = "INSERT INTO `EFFECT_TERMS` VALUES(?, ?, ?, ?, ?, ?)";
		PreparedStatement pstmt;
		try{
			pstmt = (PreparedStatement) this.conn.prepareStatement(insertSQL);
			pstmt.setString(1, term);
			pstmt.setString(2, number);
			pstmt.setString(3, semantic);
			pstmt.setString(4, tag);
			pstmt.setString(5, defined);
			pstmt.setString(6, phrase);
			pstmt.executeUpdate(); 
			pstmt.clearParameters(); 
			pstmt.close(); 
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//parse General Inquirer record到其他欄位 (ex: percent)
	public void updateEffectTerms(){
		String sql = "SELECT * FROM `EFFECT_TERMS`";
		Pattern pattern = Pattern.compile("(\\d)+%");
		ResultSet result = null;
		
		try {
			result = this.stmt.executeQuery(sql);
			while(result.next()){
				String defined = result.getString("defined");
				Matcher matcher = pattern.matcher(defined);
				if(matcher.find()) {
					float percent = Float.parseFloat(matcher.group().replace("%", ""))/100;
					System.out.println(percent);
					result.updateFloat("percent", percent);
					result.updateRow();
				}
			}
			
			result.close();
		} catch (SQLException e) {

		}	
	}
	
	public void insertClass(String type, String patentID, String classStr){
		
		String insertSQL = "INSERT INTO `PATENT_"+(type.toUpperCase())+"` VALUES(?, ?)";
		PreparedStatement pstmt;
		try{
			pstmt = (PreparedStatement) this.conn.prepareStatement(insertSQL);
			pstmt.setString(1, patentID);
			pstmt.setString(2, classStr);
			pstmt.executeUpdate(); 
			pstmt.clearParameters(); 
			pstmt.close(); 
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//======SELECT========
	public Map<String, String> getPatentYearMap() throws SQLException{
		Map<String, String> patentYearMap = new TreeMap<String, String>();
		
		String sql = "SELECT `patentID` , `year` FROM `PATENT_PROFILE`";
		ResultSet result = this.stmt.executeQuery(sql);;
		while(result.next()){
			String patentID = result.getString(1).substring(2, 9);
			String year = result.getString(2);
			
			System.out.print(patentID+"\t");
			System.out.println(year+"\t");
			patentYearMap.put(patentID, year);
		}	
		
		result.close();
		return patentYearMap;
	}
	
	public String getTitleByIpcSet(Set<String> ipcSet) throws SQLException{
		boolean isFirst = true;
		String sql = "SELECT * FROM `IPC_TITLE` WHERE ";		
		for(String ipc:ipcSet){
			if(isFirst){
				sql += "`Ipc`='"+ipc+"'  ";
				isFirst = false;
			}else{
				sql += "OR `Ipc`='"+ipc+"' ";
			}
		}
		
		String ipcResult = "";
		ResultSet result = this.stmt.executeQuery(sql);;
		while(result.next()){
			String str = result.getString(2);
			str = str.replaceAll("\\(.*\\)", "");
			str = str.replaceAll("[A-Z]{1}\\d{2}[A-Z]{1}\\d{10}", "");
			str = str.trim();
			ipcResult += str+". ";
			//System.out.println(str);
		}	
		result.close();
		//System.out.println(ipcResult);
		return ipcResult.trim();
	}
	
	//根據領域和level給與真實矩陣的技術群和功效群到clustersMap, 
	//並回傳這領域所有的distinct patentID SET
	//field: DDS, LEDS, QDGP, SRD, CN 	
	public Set<String> processTFData(String field, Map<String, ClusterGroup> clustersMap, int level){
		Set<String> patentIDs = new TreeSet<String>();
		//String[] dimeArr = {"tech", "effect", "use"};
		String[] dimeArr = {"tech", "effect"};
		if(level != 1 && level != 2) level=2;
		
		for(String dime : dimeArr){
			ClusterGroup clusters = clustersMap.get(dime);
			dime = dime.toUpperCase();
			String sql = "SELECT * FROM `"+dime+"` WHERE `field` = '"+field+"'";
			try {
				ResultSet result = this.stmt.executeQuery(sql);	
				while(result.next()){
					String patentID = result.getString("patentID");
					String dimeTitle = result.getString("level"+level);
					clusters.addElement(dimeTitle, patentID);
					patentIDs.add(patentID);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return patentIDs;
	}
	
	//給定某一領域, 並回傳這領域所有的distinct patentID SET
	public Set<String> getPatentIDsByField(String field, String dime){
		Set<String> patentIDs = new TreeSet<String>();
		String sql = "";
		if(dime.equals("all"))
			sql = "SELECT DISTINCT(`patentID`) FROM `TFMatix_Content` WHERE `field`='"+field+"'";
		else{
			sql = "SELECT DISTINCT(`patentID`) FROM `"+dime.toUpperCase()+"` WHERE `field`='"+field+"'";
		}
		
		try {
			ResultSet result = this.stmt.executeQuery(sql);	
			while(result.next()){
				String patentID = result.getString(1);
				patentIDs.add(patentID);
				//System.out.println(patentID);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return patentIDs;
	}
	
	public Set<String> getIpcSetByPatentID(String patentID){
		Set<String> ipcSet = new TreeSet<String>();
		String sql = "SELECT `ipc` FROM `PATENT_IPC` WHERE `patentID` = '"+patentID+"'";
		
		try {
			ResultSet result = this.stmt.executeQuery(sql);		
			while(result.next()){
				String ipc = result.getString(1);
				ipcSet.add(ipc);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ipcSet;
	}
	
	//給定一個patentID, 並回傳一個專利物件
	public Patent setPatentProfile(String patentID){
		Patent patent = null;
		String sql = "SELECT * FROM `PATENT_PROFILE` WHERE `patentID`='"+patentID+"'";	
		try {
			ResultSet result = this.stmt.executeQuery(sql);
			if(result.next()){
				patent = new Patent(patentID);
				patent.addPatentContent("title", result.getString("title_dwpi"));
				//patent.addPatentContent("novelty", result.getString("novelty_dwpi"));
				patent.addPatentContent("use", result.getString("use_dwpi"));
				patent.addPatentContent("adv", result.getString("adv_dwpi"));
				patent.addPatentContent("ab", result.getString("abstract"));
				patent.addPatentContent("claim", NLPTool.removeNoise(result.getString("first_claim")));	
			}		
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return patent;
	}
	
	//給定一個term和其詞性，回傳功效分數
	public double queryEffectTerms(String term, String tag){
		double score = 0;
		double probScore = 0;
		double semanticScore = 0;
		int count = 0;

		String sql = "SELECT * FROM `EFFECT_TERMS` WHERE (`term` = '" + term + "'";
		if(tag != "Noun") 
			sql += " OR MATCH (`defined`) AGAINST ('" + term + "' IN BOOLEAN MODE))";
		else 
			sql += ")";	
		
		if(tag != null && !tag.equals("")){
			sql += " AND MATCH (`tag`) AGAINST ('"+tag+"' IN BOOLEAN MODE)";
		}
		
		try {
			ResultSet result = this.stmt.executeQuery(sql);
			while(result.next()){
				String semantic = result.getString("semantic");
			
				float percent = result.getFloat("percent");
				if(semantic.equals("positive"))
					semanticScore += percent; //semanticScore++;
				else 
					semanticScore -= percent; //semanticScore--;
				count++;
				probScore += percent;
			}		
		} catch (SQLException e) {
			System.out.println(term+" "+tag+" -> error term!!");
		}
		
		if(count > 0){
			semanticScore = (Math.abs(semanticScore) + 1) / count;
			score = probScore * semanticScore;
			//score = semanticScore; 
		}
		
		return score;	
	}
	
	public static void main(String[] args) throws Exception{		
		Database db = Database.getInstance();
		//Patent patent = db.setPatentProfile("5613140");
		//patent.showPhrasesByMap();
		//patent.showPhrasesByMap();
		//db.queryEffectTerms("high", "Modif");
		//db.queryEffectTerms("sensitivity", "Noun");
		System.out.println(db.queryEffectTerms("yield", "SUPV"));
		System.out.println(db.queryEffectTerms("high", "Modif"));
		System.out.println(db.queryEffectTerms("efficiency", "Noun"));
		//db.getPatentIDsByField("DDS", "tech");
	}

}
