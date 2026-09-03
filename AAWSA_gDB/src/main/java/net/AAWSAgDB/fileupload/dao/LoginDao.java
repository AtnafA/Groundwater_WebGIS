package net.AAWSAgDB.fileupload.dao;
import java.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import net.AAWSAgDB.fileupload.ValuePair;
import net.AAWSAgDB.fileupload.model.File;
public class LoginDao {
	static Connection conn = null;
	static PreparedStatement pst = null;
	public static int User_id;
	public static int UserLo;
	public static long sizecheck=0;
 // validate Elite User
	public static boolean validateElite(String name,String pass) {
		
		boolean status = false;
		// to login into login11 table........
		try {
			conn=connection.dbconnection();	
			pst = conn.prepareStatement("select * from gw_db_schema.db_user where username=? and password=?");
			pst.setString(1, name);
			pst.setString(2, pass);
			ResultSet rs = pst.executeQuery();
				if(status=rs.next()){
               User_id=rs.getInt("id");  
                     
                	}
				rs.close();
				pst.close();
				conn.close();
		} catch (Exception ee) {
			System.out.println("Error connection "+ee.getMessage());
            ee.printStackTrace();
		} 
		return status;
	}
	//validate corporate User
	public static List<File> validatecor(String name,String user_name) {	
		List<File>username=new ArrayList<File>();
		// to login into login11 table........
		try {
			conn=connection.dbconnection();
			pst = conn.prepareStatement("select id from gw_db_schema.db_user where fullname='"+name+"' and "
					+ "username='"+user_name+"'");
			ResultSet rs = pst.executeQuery();
				while(rs.next()){
               UserLo=rs.getInt("id");
               //System.out.println("USer Id from DB to Homepage "+UserLo);
                	}
				//rs.close();
				//pst.close();
				//conn.close();
		} catch (Exception ee) {
			System.out.println("Error connection "+ee.getMessage());
            ee.printStackTrace();
		} 
		
		return username;
	}
	//validate corporate User
		public static List<File> Userdetails(String user_name) {	
			List<File>username=new ArrayList<File>();
			// to login into login11 table........
			try {
				conn=connection.dbconnection();
				pst = conn.prepareStatement("select fullname,usertype from gw_db_schema.db_user where username='"+user_name+"'");
				ResultSet rs = pst.executeQuery();
					while(rs.next()){
	               String name=rs.getString(1);
	               String center=rs.getString(2);
	               String process=null;
	               File filen=new File(name, 0,center, process);
	               username.add(filen);
	                	}
			} catch (Exception ee) {
				System.out.println("Error connection "+ee.getMessage());
	            ee.printStackTrace();
			} 
			
			return username;
		}
		
		//Region City of Water Well
	public static List<File> gwDBParam(String AAWSA_Dname) throws Throwable{
		List<File> files = new ArrayList<File>();
		try {
			conn=connection.dbconnection();
			String query="select gwdata_id,gwdata_name from gw_db_schema.aawsa_gdb where gwdata_name='"+AAWSA_Dname+"'";
				   Statement statmnt = conn.createStatement();
					ResultSet rs = statmnt.executeQuery(query);
					while(rs.next()){
						int folid=rs.getInt(1);	
						String raw_dataava=rs.getString(2);	
						//System.out.println("GW data types form DB "+raw_dataava);
						File ff=new File("",0,folid,raw_dataava,"");
						files.add(ff);
					}	
		String query2="select reg_id,reg_name from gw_db_schema.region_city";
		Statement statrRegion = conn.createStatement();
		ResultSet rsRegion = statrRegion.executeQuery(query2);
		while(rsRegion.next()){
			int regid=rsRegion.getInt("reg_id");
			String regname=rsRegion.getString("reg_name");
			//System.out.println("Region name form DB "+regname);
			File ff=new File(regid, regname,"",0);
			files.add(ff);
	} 
		String query_sup="select wf_id,wf_name from gw_db_schema.well_field";
		   Statement statmnt_pt = conn.createStatement();
			ResultSet rs_pt = statmnt_pt.executeQuery(query_sup);
			while(rs_pt.next()){
				int folid=rs_pt.getInt(1);
				String folname=rs_pt.getString(2);
				//System.out.println("Well field name form DB "+folname);
				File ff=new File(folname,folid);
				files.add(ff);
			}
			Statement statmntaddWellFP=conn.createStatement();
			String queryaddwellFP="select id,parameterp from gw_db_schema.addtionalparm where contentp='Well Field'";
			ResultSet rsaddwellFP = statmntaddWellFP.executeQuery(queryaddwellFP);
			while(rsaddwellFP.next()){
				int wellField_id=rsaddwellFP.getInt(1);
				String wellField_name=rsaddwellFP.getString(2);
				File ff=new File(wellField_id,wellField_name,0);
				files.add(ff);
			}
			Statement statmntaddWellOwP=conn.createStatement();
			String queryaddwellOwP="select id,parameterp from gw_db_schema.addtionalparm where contentp='Well Owner'";
			ResultSet rsaddwellOwP = statmntaddWellOwP.executeQuery(queryaddwellOwP);
			while(rsaddwellOwP.next()){
				int wellOwner_id=rsaddwellOwP.getInt(1);
				String wellOwner_name=rsaddwellOwP.getString(2);
				Object obj=null;
				File ff=new File(wellOwner_id,wellOwner_name,0,obj);
				files.add(ff);
			}
			Statement statmntaddCasingMP=conn.createStatement();
			String queryaddCasingMP="select id,parameterp from gw_db_schema.addtionalparm where contentp='Casing Material'";
			ResultSet rsaddCasingMP = statmntaddCasingMP.executeQuery(queryaddCasingMP);
			while(rsaddCasingMP.next()){
				int casingM_id=rsaddCasingMP.getInt(1);
				String CasingM_name=rsaddCasingMP.getString(2);
				Object obj=null;
				File ff=new File(casingM_id,CasingM_name,obj,0);
				files.add(ff);
			}
			Statement statmntaddWellTP=conn.createStatement();
			String queryaddWellTP="select id,parameterp from gw_db_schema.addtionalparm where contentp='Well Type'";
			ResultSet rsaddWellTP = statmntaddWellTP.executeQuery(queryaddWellTP);
			while(rsaddWellTP.next()){
				int wellType_id=rsaddWellTP.getInt(1);
				String wellType_name=rsaddWellTP.getString(2);
				Object obj=null;
				File ff=new File(wellType_id,wellType_name,obj,0,0);
				files.add(ff);
			}
			Statement statmntaddAbanRP=conn.createStatement();
			String queryaddAbanRP="select id,parameterp from gw_db_schema.addtionalparm where contentp='Abandoned Reason'";
			ResultSet rsaddAbanRP = statmntaddAbanRP.executeQuery(queryaddAbanRP);
			while(rsaddAbanRP.next()){
				int abanR_id=rsaddAbanRP.getInt(1);
				String abanR_name=rsaddAbanRP.getString(2);
				Object obj=null;
				File ff=new File(abanR_id,abanR_name,0,0,obj);
				files.add(ff);
			}
			
	}catch (SQLException e) {
		e.printStackTrace();
	}
	return files;
	}
	//get Project Output
	public static List<File>region_class(int regionCityId){
		List<File> files=new ArrayList<File>();
		try {
			conn= connection.dbconnection();
			Statement ptOstatmnt=conn.createStatement();
			String ptOquery="select rgC.cl_id,rgC.cl_name from gw_db_schema.region_class as rgC,gw_db_schema.region_city as rc "
				+ "where rc.reg_id=rgC.reg_id and rc.reg_id="+regionCityId+"";
			ResultSet ptOrs = ptOstatmnt.executeQuery(ptOquery);
			while(ptOrs.next()){
				int pOut_id=ptOrs.getInt(1);
				String pOut_name=ptOrs.getString(2);
				List kk = null;
				File ff=new File(pOut_name,pOut_id,kk);
				//System.out.println("region class name= "+pOut_name);
				files.add(ff);
			}
		}catch(Exception ee){
		ee.printStackTrace();	
		}
		return files;
	}
	//
	public static List<File> subCityZone(int type_id){
		//System.out.println(" city or zone ID is = "+type_id);
		List<File> files=new ArrayList<File>();
		try {
			conn= connection.dbconnection();
			Statement ptOstatmnt=conn.createStatement();
		String ptOquery="select scz.scz_id,scz.sub_city_zone_name from gw_db_schema.region_city as rc,gw_db_schema.region_class as rgC,"
				+ "gw_db_schema.sub_city_zone as scz where rc.reg_id=rgC.reg_id and scz.cl_id=rgC.cl_id and rgC.cl_id="+type_id+"";
			ResultSet ptOrs = ptOstatmnt.executeQuery(ptOquery);
			while(ptOrs.next()){
				int scz_id=ptOrs.getInt(1);
				String scz_name=ptOrs.getString(2);
				List kk = null;
				List km=null;
				File ff=new File(scz_name,scz_id,kk,km);
				files.add(ff);
			}
		}catch(Exception ee){
		ee.printStackTrace();	
		}
		return files;
	}
	public static List<File> data_investigated(int cat_name){
		List<File> files=new ArrayList<File>();
		try {
			conn= connection.dbconnection();
			Statement statmnt=conn.createStatement();
		String query="select id,parameterp from gw_db_schema.addtionalparm where contentp='Well Field'";
			ResultSet rs = statmnt.executeQuery(query);
			while(rs.next()){
				int basin_id=rs.getInt(1);
				String basin_name=rs.getString(2);
				File ff=new File(basin_id,basin_name,0);
				files.add(ff);
			}
		}catch(Exception ee){
		ee.printStackTrace();	
		}
		return files;
	}
	//access sample category
	public static List<ValuePair> userAccountManagement(int adminID){
		List<ValuePair> files=new ArrayList<ValuePair>();
		String boolValue="";
		try {
			conn= connection.dbconnection();
			Statement statmnt=conn.createStatement();
		String query="select id,fullname,username,usertype,enabled from gw_db_schema.db_user where usertype='Common User' or usertype='Database User' or "
				+ "usertype='Administrator' and id!="+adminID+" order by id asc";
			ResultSet rs = statmnt.executeQuery(query);
			while(rs.next()){
				int user_id=rs.getInt(1);
				String fullName=rs.getString(2);
				String userName=rs.getString(3);
				String userType=rs.getString(4);
				boolean enabled=rs.getBoolean(5);
				if(enabled==true) {
					boolValue="Active";
				}else {
					boolValue="Not Active";
				}
				ValuePair ff=new ValuePair(user_id,fullName,userName,userType,boolValue);
				files.add(ff);
			}
			rs.close();
			statmnt.close();
			conn.close();
		}catch(Exception ee){
		ee.printStackTrace();	
		}
		return files;
	}
	
	public static List<ValuePair> updateUserAccount(int userId,int privilege,String accountStatus, int adminID){
		Boolean  accountSt=Boolean.parseBoolean(accountStatus);
		String userType="";
		String query="";
		String boolValue="";
		List<ValuePair> files=new ArrayList<ValuePair>();
		if(privilege==110) {
			userType="Administrator";	
		}
		else if(privilege==120) {
			userType="Database User";
		}
		else if(privilege==123) {
			userType="Common User";
		}else if(privilege==4444){
			userType="";
		}
		try {
			conn= connection.dbconnection();
			Statement statmnt=conn.createStatement();
			if(privilege!=4444 && (!accountStatus.equals("undefined"))){
				query="update gw_db_schema.db_user set vern_code="+privilege+",usertype='"+userType+"', enabled="+accountSt+" where id="+userId+"";	
			}
			else if(privilege!=4444 && (accountStatus.equals("undefined"))) {
				query="update gw_db_schema.db_user set vern_code="+privilege+",usertype='"+userType+"' where id="+userId+"";	
			}
			else if((!accountStatus.equals("undefined")) && privilege==4444){
				query="update gw_db_schema.db_user set enabled="+accountSt+" where id="+userId+"";	
			}
			try (PreparedStatement preparedStatement = conn.prepareStatement(query)){
        		preparedStatement.executeUpdate();
        	}
			String querySearch="select id,fullname,username,usertype,enabled from gw_db_schema.db_user where usertype='Common User' or usertype='Database User' or "
					+ "usertype='Administrator' and id!="+adminID+" order by id asc";
				ResultSet rs = statmnt.executeQuery(querySearch);
				while(rs.next()){
					int user_id=rs.getInt(1);
					String fullName=rs.getString(2);
					String userName=rs.getString(3);
					String accessuserType=rs.getString(4);
					boolean enabled=rs.getBoolean(5);
					if(enabled==true) {
						boolValue="Active";
					}else {
						boolValue="Not Active";
					}
					ValuePair ff=new ValuePair(user_id,fullName,userName,accessuserType,boolValue,"");
					files.add(ff);
				}		
		}catch(Exception ee){
		ee.printStackTrace();	
		}
		return files;
	}
	public static List<File>usertype(){
		List<File>files=new ArrayList<File>();
		try {
			conn=connection.dbconnection();
			String quercenter="select * from gw_db_schema.region_city";
			 Statement statmnt = conn.createStatement();
			ResultSet rs = statmnt.executeQuery(quercenter);
			while(rs.next()){
				int na_id=rs.getInt(1);
				String na_name=rs.getString(2);
				Integer jj=0;
				File ff=new File(na_id, na_name,jj);
				files.add(ff);
		} 
			rs.close();
			statmnt.close();
			conn.close();
		} catch (Exception e) {
		e.printStackTrace();
		}
		return files;
	}		
static void readWrit(InputStream ios, BufferedOutputStream bos, long numBytes) throws IOException{
	byte[] buff=new byte[(int) numBytes];
	int val=ios.read(buff);
	if(val!=-1){
		bos.write(buff);
	}	
}
public static List<File>headerfile(){
	List<File> files= new ArrayList<File>();
	try {
		conn=connection.dbconnection();	
		Statement statmnt = conn.createStatement();
			String sqlqu="select * from gw_db_schema.region_city";
			ResultSet rs = statmnt.executeQuery(sqlqu);
			while(rs.next()){
				int scata_id=rs.getInt(1);
				String scat_name=rs.getString(2);
				byte []bb=null;
				//File ff=new File(0,scata_id,bb,scat_name);
				//files.add(ff);
			}
			String query="select * from gw_db_schema.region_city as rc where "
					+ "rc.reg_id!=8";
	 rs = statmnt.executeQuery(query);
	while(rs.next()){
		int basin_id=rs.getInt(1);
		String basin_name=rs.getString(2);
		File ff=new File(basin_id,basin_name,0);
		files.add(ff);
	}
		}catch (Exception e) {
			e.printStackTrace();
		}
	return files;
}
	//Download Drilling Pumping Test History Data
	 public static ValuePair downloadDPTHData(int fileID,String DPTHData) throws Throwable{
	 	ValuePair file = null;
	 	     conn = connection.dbconnection();
	 	     Statement statmnt=conn.createStatement();
	 	  String sql = "select filename,filetype,filesize,reportdoc from gw_db_schema.wellreport where repo_id="+fileID+"";
	 	     ResultSet rs=statmnt.executeQuery(sql);
	 	             while (rs.next()) {
	 	                 String name = rs.getString(1);
	 	                 String contentType = rs.getString(2);
	 	                 int size = rs.getInt(3);
	 	                 byte[] data = rs.getBytes(4);
	 	                 file= new ValuePair(data, name, contentType,size);
	 	             }
	 	             conn.close();
	 	     return file;
	 	 }
	//search Drilling Pumping Test History Data
			public static List<ValuePair> Listdetails() throws Throwable {
				List<ValuePair> files = new ArrayList<ValuePair>();
				try{
						conn=connection.dbconnection();		
						Statement stmt = conn.createStatement();
						String sqlfext2 = "select repo_id,LEFT(filename,POSITION('.' IN RIGHT(filename, POSITION('/' IN REVERSE(filename)) - 1))),"
								+ "regexp_matches(filename,'\\.(\\w+)$'),recordate,dataCategory "
								+ "FROM (SELECT *,ROW_NUMBER() OVER (PARTITION BY filename ORDER BY filesize DESC) as rn FROM gw_db_schema.wellreport)"
								+ " AS subquery WHERE rn = 1";
					            ResultSet rs3 = stmt.executeQuery(sqlfext2);
					            while (rs3.next()) {
					            	int repo_id=rs3.getInt(1);
					            	String fileName = rs3.getString(2);
					                String filetype = rs3.getString(3);
					                String recordate=rs3.getString(4);
					                String dataCategory=rs3.getString(5);
					               ValuePair ff= new ValuePair(repo_id,fileName,filetype,recordate,dataCategory,0);
					          files.add(ff); 
					            }
			    }catch(Exception ee){
			    ee.printStackTrace();	
			    }
			    return files;
			}
	 public static List<File> accessCRUDOperationLogByDate(String bydate){
		 int user_code=0;
		 List<File>file= new ArrayList<File>();
		 try{
			 conn=connection.dbconnection();
			 Statement statmnt=conn.createStatement();		
		 String querybyfol="select case when dl.well_id IS NOT NULL THEN ww.well_index ELSE dl.well_index END AS well_Index,dl.dblogid,du.fullname,"
		 		+ "dl.actiontook,TO_CHAR(dl.datehapped, 'YYYY-MM-DD HH24:MI'),dl.datasheetype from gw_db_schema.database_log as dl left join "
		 		+ "gw_db_schema.water_well as ww on dl.well_id=ww.well_id LEFT JOIN gw_db_schema.db_user as du on dl.user_id=du.id where "
		 		+ "TO_CHAR(dl.datehapped, 'DD')='"+bydate+"' order by dl.datehapped desc";
		 ResultSet rs=statmnt.executeQuery(querybyfol);
		 while (rs.next()) {
			 String well_index = rs.getString(1);
			 int log_id=rs.getInt(2);
			 String userName = rs.getString(3);
			 String actionTook = rs.getString(4);
	         String dateActionTook = rs.getString(5);
	         String dataSheet = rs.getString(6);
File ff= new File(log_id,well_index,userName,actionTook,dateActionTook,dataSheet,0);
	         file.add(ff);
		}
		 }catch(Exception ee){
			 ee.printStackTrace();
		 }
		 return file;
		 
	 }
	 public static List<File> accessCRUDOperationLog(){
		 int user_code=0;
		 List<File>file= new ArrayList<File>();
		 try{
			 conn=connection.dbconnection();
			 Statement statmnt=conn.createStatement();		
		 String querybyfol="select case when dl.well_id IS NOT NULL THEN ww.well_index ELSE dl.well_index END AS well_Index,dl.dblogid,du.fullname,"
		 		+ "dl.actiontook,TO_CHAR(dl.datehapped, 'YYYY-MM-DD HH24:MI'),dl.datasheetype from gw_db_schema.database_log as dl left join "
		 		+ "gw_db_schema.water_well as ww on dl.well_id=ww.well_id LEFT JOIN gw_db_schema.db_user as du on dl.user_id=du.id order by "
		 		+ "dl.datehapped desc";
		 ResultSet rs=statmnt.executeQuery(querybyfol);
		 while (rs.next()) {
			 String well_index = rs.getString(1);
			 int log_id=rs.getInt(2);
			 String userName = rs.getString(3);
			 String actionTook = rs.getString(4);
	         String dateActionTook = rs.getString(5);
	         String dataSheet = rs.getString(6);
File ff= new File(log_id,well_index,userName,actionTook,dateActionTook,dataSheet);
	         file.add(ff);
		}
		 }catch(Exception ee){
			 ee.printStackTrace();
		 }
		 return file;
		 
	 }
		//search well Information By function and Status
			 public static List<File> accessByWellFunctionStatus(String wellStatus,String fuctionStatus) throws SQLException{
				 List<File>file= new ArrayList<File>();
				 conn=connection.dbconnection();
				 Statement statmnt=conn.createStatement();
				 ResultSet rs=null;
				 try{
				 String querybyfol="select ww.well_id,gl.lat,gl.longt,ww.well_index,wf.wf_name from gw_db_schema.geo_location as gl,gw_db_schema.well_field"
				 		+ " as wf,gw_db_schema.water_well as ww,gw_db_schema.well_characteristics as ch,gw_db_schema.con_discharge_test as cdt,"
				 		+ "gw_db_schema.constwell_status as cws,gw_db_schema.productive_well as pro,gw_db_schema.productive_well_status as pws "
				 		+ "where gl.geoloc_id=ww.geoloc_id and wf.wf_id=ww.wf_id and ch.well_id=ww.well_id and cdt.ch_id=ch.ch_id and cdt.cdt_id=cws.cdt_id"
				 		+ " and cws.cws_id=pro.cws_id and pro.pro_id=pws.pro_id and cws.data_colln_case='Operational' and "
				 		+ "SIMILARITY(pws.wellfunstatus,'"+wellStatus+"')>0.85 and SIMILARITY(pws.func_condition,'"+fuctionStatus+"')>0.85";
				 rs=statmnt.executeQuery(querybyfol);
				 while (rs.next()) {
					 int well_id=rs.getInt(1);
					 double lat = rs.getDouble(2);
					 double lng = rs.getDouble(3);
					 String wellIndex = rs.getString(4);
			         String wellFieldName = rs.getString(5);
		File ff= new File(well_id,lat,lng, wellIndex,wellFieldName,"");
			       file.add(ff);
				}
				 }catch(Exception ee){
					 ee.printStackTrace();
				 }
				 return file; 
			 }
			//search well Information By function and Status
			 public static List<File> accessByOwnerCategpryname(String OwnercategoryName) throws SQLException{
				 List<File>file= new ArrayList<File>();
				 conn=connection.dbconnection();
				 Statement statmnt=conn.createStatement();
				 ResultSet rs=null;
				 try{
				 String querybyfol="select ww.well_id,gl.lat,gl.longt,ww.well_index,wf.wf_name from gw_db_schema.geo_location as gl,"
				 		+ "gw_db_schema.well_field as wf,gw_db_schema.water_well as ww where gl.geoloc_id=ww.geoloc_id and wf.wf_id=ww.wf_id "
				 		+ "and ww.wellownercatgory='"+OwnercategoryName+"'";
				 rs=statmnt.executeQuery(querybyfol);
				 while (rs.next()) {
					 int well_id=rs.getInt(1);
					 double lat = rs.getDouble(2);
					 double lng = rs.getDouble(3);
					 String wellIndex = rs.getString(4);
			         String wellFieldName = rs.getString(5);
			         int wellCount=0;
		File ff= new File(well_id,lat,lng, wellIndex,wellFieldName,"","",wellCount);
			       file.add(ff);
				}
				 }catch(Exception ee){
					 ee.printStackTrace();
				 }
				 return file; 
			 }
			//search well Information By function and Status
			 public static List<File> accessByWellDepthByRange(int depthRange) throws SQLException{
				 double rangeScaller=0;
				 String querybyfol="";
				 if(depthRange<=100) {
					 rangeScaller=0.1; 
				 }
				 else if(100< depthRange && depthRange <=350) {
					 rangeScaller=101;
				 }
				 else if(350< depthRange && depthRange <=500) {
					 rangeScaller=351;
				 }else if(500 < depthRange){
					 rangeScaller=1500; 
				 }
				 List<File>file= new ArrayList<File>();
				 conn=connection.dbconnection();
				 Statement statmnt=conn.createStatement();
				 ResultSet rs=null;
				 try{
					 if(500 < depthRange) {
						 querybyfol="select ww.well_id,gl.lat,gl.longt,ww.well_index,wf.wf_name from gw_db_schema.geo_location as gl,"
							 	+ "gw_db_schema.well_field as wf,gw_db_schema.water_well as ww where gl.geoloc_id=ww.geoloc_id and wf.wf_id=ww.wf_id and "
							 	+ "("+depthRange+" < ww.well_depth and ww.well_depth < "+rangeScaller+")";	 
					 }else {
						 querybyfol="select ww.well_id,gl.lat,gl.longt,ww.well_index,wf.wf_name from gw_db_schema.geo_location as gl,"
							 	+ "gw_db_schema.well_field as wf,gw_db_schema.water_well as ww where gl.geoloc_id=ww.geoloc_id and wf.wf_id=ww.wf_id and "
							 	+ "("+rangeScaller+" <= ww.well_depth and ww.well_depth <= "+depthRange+")";	 
					 }
				 rs=statmnt.executeQuery(querybyfol);
				 while (rs.next()) {
					 int well_id=rs.getInt(1);
					 double lat = rs.getDouble(2);
					 double lng = rs.getDouble(3);
					 String wellIndex = rs.getString(4);
			         String wellFieldName = rs.getString(5);
		File ff= new File(well_id,lat,lng, wellIndex,wellFieldName,"","","");
			       file.add(ff);
				}
				 }catch(Exception ee){
					 ee.printStackTrace();
				 }
				 return file; 
			 }
			 //Access current discharge
			 public static List<File> accessByCurrentDischargeRange(double dischargeRange) throws SQLException{
				 double rangeScaller=0;
				 String querybyfol="";
				 if(dischargeRange<=15) {
					 rangeScaller=1; 
				 }
				 else if(15< dischargeRange && dischargeRange <=40) {
					 rangeScaller=15;
				 }
				 else if(40< dischargeRange && dischargeRange <=70) {
					 rangeScaller=40;
				 }else if(70 < dischargeRange && dischargeRange<=120){
					 rangeScaller=70; 
				 }
				 List<File>file= new ArrayList<File>();
				 conn=connection.dbconnection();
				 Statement statmnt=conn.createStatement();
				 ResultSet rs=null;
				 try{
						 querybyfol="select ww.well_id,gl.lat,gl.longt,ww.well_index,wf.wf_name from gw_db_schema.well_field as wf,gw_db_schema.geo_location as gl,"
							 	+ "gw_db_schema.water_well as ww,gw_db_schema.well_characteristics as ch,gw_db_schema.pumping_status as ps where wf.wf_id=ww.wf_id "
							 	+ "and ww.well_id=ch.well_id and gl.geoloc_id=ww.geoloc_id and ch.ch_id=ps.ch_id and ps.data_colln_case='Operational' and "
							 	+ "("+rangeScaller+" < ps.abstraction_rate and ps.abstraction_rate <= "+dischargeRange+")";
				 rs=statmnt.executeQuery(querybyfol);
				 while (rs.next()) {
					 int well_id=rs.getInt(1);
					 double lat = rs.getDouble(2);
					 double lng = rs.getDouble(3);
					 String wellIndex = rs.getString(4);
			         String wellFieldName = rs.getString(5);
		File ff= new File(well_id,lat,lng, wellIndex,wellFieldName,"","","","");
			       file.add(ff);
				}
				 }catch(Exception ee){
					 ee.printStackTrace();
				 }
				 return file; 
			 }
			//Access current SWL
			 public static List<File> accessByCurrentSWLRange(double SWLRange) throws SQLException{
				 double rangeScaller=0;
				 String querybyfol="";
				 if(SWLRange<=0.1) {
					 rangeScaller=0.00001; 
				 }
				 else if(1<= SWLRange && SWLRange <=50) {
					 rangeScaller=0.9999;
				 }
				 else if(50< SWLRange && SWLRange <=100) {
					 rangeScaller=50;
				 }else if(100 < SWLRange && SWLRange<=200){
					 rangeScaller=100; 
				 }
				 List<File>file= new ArrayList<File>();
				 conn=connection.dbconnection();
				 Statement statmnt=conn.createStatement();
				 ResultSet rs=null;
				 try{
						 querybyfol="select ww.well_id,gl.lat,gl.longt,ww.well_index,wf.wf_name from gw_db_schema.well_field as wf,gw_db_schema.geo_location as gl,"
							 	+ "gw_db_schema.water_well as ww,gw_db_schema.well_characteristics as ch,gw_db_schema.con_discharge_test as cdt where wf.wf_id="
							 	+ "ww.wf_id and gl.geoloc_id=ww.geoloc_id and ww.well_id=ch.well_id and ch.ch_id=cdt.ch_id and cdt.data_colln_case='Operational'"
							 	+ " and("+rangeScaller+" <= cdt.swl_m and cdt.swl_m <= "+SWLRange+")";
				 rs=statmnt.executeQuery(querybyfol);
				 while (rs.next()) {
					 int well_id=rs.getInt(1);
					 double lat = rs.getDouble(2);
					 double lng = rs.getDouble(3);
					 String wellIndex = rs.getString(4);
			         String wellFieldName = rs.getString(5);
		File ff= new File(well_id,lat,lng, wellIndex,wellFieldName,"","","","","");
			       file.add(ff);
				}
				 }catch(Exception ee){
					 ee.printStackTrace();
				 }
				 return file; 
			 }
			 //Access current DWL
			 public static List<File> accessByCurrentDWLRange(double DWLRange) throws SQLException{
				 double rangeScaller=0;
				 String querybyfol="";
				 if(DWLRange<=50) {
					 rangeScaller=0.0001; 
				 }
				 else if(50< DWLRange && DWLRange <=100) {
					 rangeScaller=50;
				 }
				 else if(100< DWLRange && DWLRange <=150) {
					 rangeScaller=100;
				 }else if(150 < DWLRange && DWLRange<=250){
					 rangeScaller=150; 
				 }
				 List<File>file= new ArrayList<File>();
				 conn=connection.dbconnection();
				 Statement statmnt=conn.createStatement();
				 ResultSet rs=null;
				 try{
			 querybyfol="select ww.well_id,gl.lat,gl.longt,ww.well_index,wf.wf_name from gw_db_schema.well_field as wf,gw_db_schema.geo_location as gl,"
				 	+ "gw_db_schema.water_well as ww,gw_db_schema.well_characteristics as ch,gw_db_schema.con_discharge_test as cdt where wf.wf_id="
				 	+ "ww.wf_id and gl.geoloc_id=ww.geoloc_id and ww.well_id=ch.well_id and ch.ch_id=cdt.ch_id and cdt.data_colln_case='Operational'"
				 	+ " and("+rangeScaller+" < cdt.dwl_m and cdt.dwl_m <= "+DWLRange+")";
				 rs=statmnt.executeQuery(querybyfol);
				 while (rs.next()) {
					 int well_id=rs.getInt(1);
					 double lat = rs.getDouble(2);
					 double lng = rs.getDouble(3);
					 String wellIndex = rs.getString(4);
			         String wellFieldName = rs.getString(5);
		File ff= new File(well_id,lat,lng, wellIndex,wellFieldName,"","","","","","");
			       file.add(ff);
				}
				 }catch(Exception ee){
					 ee.printStackTrace();
				 }
				 return file; 
			 }
			 
			 //Access transmissivity
			 public static List<File> accessByCurrentTransimissivityRange(double TransimissivityRange) throws SQLException{
				 double rangeScaller=0;
				 String querybyfol="";
				 if(TransimissivityRange<=5) {
					 rangeScaller=0.000001; 
				 }
				 else if(5< TransimissivityRange && TransimissivityRange <=50) {
					 rangeScaller=5.000001;
				 }
				 else if(50< TransimissivityRange && TransimissivityRange <=500) {
					 rangeScaller=50.00001;
				 }else if(500 < TransimissivityRange && TransimissivityRange <=1000000){
					 rangeScaller=500.000001; 
				 }
				 List<File>file= new ArrayList<File>();
				 conn=connection.dbconnection();
				 Statement statmnt=conn.createStatement();
				 ResultSet rs=null;
				 try{
			 querybyfol="select ww.well_id,gl.lat,gl.longt,ww.well_index,wf.wf_name from gw_db_schema.well_field as wf,gw_db_schema.geo_location as gl,"
				 	+ "gw_db_schema.water_well as ww,gw_db_schema.well_characteristics as ch where wf.wf_id=ww.wf_id and gl.geoloc_id=ww.geoloc_id "
				 	+ "and ww.well_id=ch.well_id and("+rangeScaller+" < ch.transmissivity and ch.transmissivity <= "+TransimissivityRange+")";
				 rs=statmnt.executeQuery(querybyfol);
				 while (rs.next()) {
					 int well_id=rs.getInt(1);
					 double lat = rs.getDouble(2);
					 double lng = rs.getDouble(3);
					 String wellIndex = rs.getString(4);
			         String wellFieldName = rs.getString(5);
			        // System.out.println("Well index searched by Transsimitity is "+wellIndex+" and Searched Key is "+TransimissivityRange);
		File ff= new File(well_id,lat,lng, wellIndex,wellFieldName,"","","","","","","");
			       file.add(ff);
				}
				 }catch(Exception ee){
					 ee.printStackTrace();
				 }
				 return file; 
			 }		 
		 //search Well by Function 
	 public static List<File> accessByWellStatus(String wellStatus) throws SQLException{
		 int user_code=0;
		 ResultSet rs=null;
		 List<File>file= new ArrayList<File>();
		 conn=connection.dbconnection();
		 Statement statmnt=conn.createStatement();	
		 try{
		 String querybyfol="select ww.well_id,gl.lat,gl.longt,ww.well_index,wf.wf_name from gw_db_schema.geo_location as gl,gw_db_schema.well_field"
		 		+ " as wf,gw_db_schema.water_well as ww,gw_db_schema.well_characteristics as ch,gw_db_schema.con_discharge_test as cdt,"
		 		+ "gw_db_schema.constwell_status as cws,gw_db_schema.productive_well as pro,gw_db_schema.productive_well_status as pws "
		 		+ "where gl.geoloc_id=ww.geoloc_id and wf.wf_id=ww.wf_id and ch.well_id=ww.well_id and cdt.ch_id=ch.ch_id and cdt.cdt_id=cws.cdt_id"
		 		+ " and cws.cws_id=pro.cws_id and pro.pro_id=pws.pro_id and cws.data_colln_case='Operational' and "
		 		+ "SIMILARITY(pws.wellfunstatus,'"+wellStatus+"')>0.85";
		 rs=statmnt.executeQuery(querybyfol);
		 while (rs.next()) {
			 int well_id=rs.getInt(1);
			 double lat = rs.getDouble(2);
			 double lng = rs.getDouble(3);
			 String wellIndex = rs.getString(4);
	         String wellfield = rs.getString(5);
File ff= new File(well_id,lat,lng, wellIndex,wellfield);
	         file.add(ff);
		}
		 }catch(Exception ee){
			 ee.printStackTrace();
		 }
		 conn.close();
		 return file; 
	 }
	 //project details
	 public static List<File> accessGWODbyWellId(int well_id) throws SQLException{
		 List<File>file= new ArrayList<File>();
		 conn=connection.dbconnection();
		 String checkEmail="";
		 Statement statmnt=conn.createStatement();													
		 try{
		 String querybyfol="select ww.well_id,ww.well_index,dbu.orgname,dbu.email,dbu.phone,pro.wateruse_license,pro.pumping_operen_date,dbu.id"
		 		+ " from gw_db_schema.db_user as dbu,gw_db_schema.water_well as ww,gw_db_schema.well_characteristics as ch,"
		 		+ "gw_db_schema.con_discharge_test as cdt,gw_db_schema.constwell_status as cws,gw_db_schema.productive_well as pro where "
		 		+ "pro.cws_id=cws.cws_id and cws.cdt_id=cdt.cdt_id and cws.enumid=dbu.id and cdt.ch_id=ch.ch_id and "
		 		+ "ch.well_id=ww.well_id and cws.data_colln_case='Operational' and dbu.usertype='Enumerator' and ww.well_id="+well_id+"";
		 ResultSet rs=statmnt.executeQuery(querybyfol);
		 while (rs.next()) {
			 int wellid=rs.getInt(1);
			 String wellIndex = rs.getString(2);
             String orgName = rs.getString(3);
             String orgEmail= rs.getString(4);
             String orgPhone=rs.getString(5);
             String waterUsegeLicence = rs.getString(6);
             String OperationStartDate=rs.getString(7); 
             int userid=rs.getInt(8);
             if(orgEmail=="0") {
            	 checkEmail="NR";
             }
             else {
            	 checkEmail=orgEmail;
             }
             if(orgPhone.equals("0")) {
            	 orgPhone="NR"; 
             }
             if(OperationStartDate.equals("1970-01-01")) {
            	 OperationStartDate="NR";
             }
             File ff= new File(wellid,wellIndex,orgName,checkEmail,orgPhone,waterUsegeLicence,OperationStartDate,userid); 
	         file.add(ff);
		}
		 }catch(Exception ee){
			 ee.printStackTrace();
		 }
		 conn.close();
		 return file; 
	 }
	 public static List<File> selectbyWellId(int wellId) throws SQLException{
		 List<File>file= new ArrayList<File>();
		 try{
			 conn=connection.dbconnection();	
			 Statement statmntaccessWCR=conn.createStatement();
		 String querybyWCR="select ww.well_id,ww.wellcode,geo.easting,geo.northing,geo.longt,geo.lat,geo.l_elevation,ww.cntn_year,ww.well_depth,"
		 		+ "cdt.yield_lps,cdt.swl_m,cdt.dwl_m,ch.specific_capacity,dbu.orgname,dbu.email,dbu.phone,ww.well_index,ww.drilling_license,"
		 		+ "geo.geoloc_id,dbu.id,ww.wellownercatgory,CI.largecasing,CI.telescopedcasing,CI.casing_mtrl,ww.main_aquifer,ww.well_type,"
		 		+ "pws.func_condition,pts.pump_status,pts.pumpinstalldate,pts.pump_capacity,pts.pumphead,pts.pump_position,pts.abstraction_rate,"
		 		+ "ps.gen_power,ps.gen_status from gw_db_schema.power_systemc as ps,"
		 		+ "gw_db_schema.geo_location as geo,gw_db_schema.db_user as dbu,gw_db_schema.water_well as ww,gw_db_schema.casing_info CI,"
		 		+ "gw_db_schema.well_characteristics as ch,gw_db_schema.con_discharge_test as cdt,gw_db_schema.pumping_status as pts,"
		 		+ "gw_db_schema.constwell_status as cws,gw_db_schema.productive_well as pro,gw_db_schema.productive_well_status as pws where "
		 		+ "pts.data_colln_case='Well Completion Report' and cdt.data_colln_case='Well Completion Report' and ps.pro_id=pro.pro_id and "
		 		+ "pws.pro_id=pro.pro_id and "
		 		+ "pro.cws_id=cws.cws_id and cws.cdt_id=cdt.cdt_id and cdt.ch_id=ch.ch_id and ww.owned_by=dbu.id and ch.well_id=ww.well_id and "
		 		+ "CI.well_id=ww.well_id and pts.ch_id=ch.ch_id and ww.geoloc_id=geo.geoloc_id and ww.well_id="+wellId+"";
		 ResultSet rsWCR=statmntaccessWCR.executeQuery(querybyWCR);
		 while (rsWCR.next()) {
			 int well_id=rsWCR.getInt(1);
		        String well_code = rsWCR.getString(2);
		        double easting = rsWCR.getDouble(3);
		        double northing=rsWCR.getDouble(4);
		        double longt=rsWCR.getDouble(5);
		        double lat = rsWCR.getDouble(6);
		        double elevation = rsWCR.getDouble(7);
		        String cnYear=rsWCR.getString(8);
		        double wellDepth=rsWCR.getDouble(9);
		        double wellYield=rsWCR.getDouble(10);
		        double SWL=rsWCR.getDouble(11);
		        double DWL=rsWCR.getDouble(12);
		        double specificCapacity=rsWCR.getDouble(13);
		        String Userorgname=rsWCR.getString(14);
		        String userEmail=rsWCR.getString(15);
		        String userPhone=rsWCR.getString(16);
		        String wellIndex=rsWCR.getString(17);
		        String drillingLicence=rsWCR.getString(18);
		        int geolocID=rsWCR.getInt(19);
		        int ownerID=rsWCR.getInt(20);
		        String ownerCat=rsWCR.getString(21);
		        double largCasing=rsWCR.getDouble(22);
		        double telescopCasing=rsWCR.getDouble(23);
		        String casingMaterial=rsWCR.getString(24);
		        String mainAquifer=rsWCR.getString(25);
		        String wellType=rsWCR.getString(26);
		        String abandonedReason="";
		        String sealedYN="";
		        String dateSealed="1970-01-01";
		        String functionWellCon=rsWCR.getString(27);
		        String pumpStatusUpdate=rsWCR.getString(28);
		        String pumpInstalledDate=rsWCR.getString(29);
		        double pumpCapacity=rsWCR.getDouble(30);
		        String pumpheadUpdate=rsWCR.getString(31);
		        double pumpPositionUpdate=rsWCR.getDouble(32);
		        double dischargeRate=rsWCR.getDouble(33);
		        double generatorPower=rsWCR.getDouble(34);
		        String generatorStatus=rsWCR.getString(35);
		        //System.out.println("Owner Category "+pumpPositionUpdate);
		        File ff= new File(well_id,well_code,easting,northing,longt,lat,elevation,cnYear,wellDepth,wellYield,SWL,DWL,
		        		specificCapacity,Userorgname, userEmail,userPhone,wellIndex,drillingLicence,geolocID,ownerID,ownerCat,
		        		largCasing,telescopCasing,casingMaterial,mainAquifer,wellType,abandonedReason,sealedYN,dateSealed,functionWellCon,
		        		pumpStatusUpdate,pumpInstalledDate,pumpCapacity,pumpheadUpdate,pumpPositionUpdate,dischargeRate,generatorPower,
		        		generatorStatus);
	         file.add(ff);
		}
		 }catch(Exception ee){
			 ee.printStackTrace();
		 }
		 conn.close();
		 return file;	 
	 }
	//Access Drilling Pumping Test History Data By WellIndex
		 public static List<ValuePair> accesswellIndex4DPTHData(int userID,String wellIndex){
		 	List<ValuePair>files=new ArrayList<ValuePair>();
		 	String getvalue="";
		 	if(wellIndex.equals("")) {
 		getvalue="select ww.well_id,wf.wf_name,rg.reg_name,ww.wellownercatgory,du.orgname,cd.yield_lps,ww.well_depth,extract(year from ww.cntn_year)"
	 			+ ",ww.well_index,ww.wellcode,cws.well_status,cws.data_colln_case from gw_db_schema.constwell_status as cws,"
		 		+ "gw_db_schema.con_discharge_test as cd,gw_db_schema.well_characteristics as ch,gw_db_schema.water_well as ww,gw_db_schema.well_field "
		 		+ "as wf,gw_db_schema.db_user as du,gw_db_schema.region_city as rg,gw_db_schema.region_class as rc,gw_db_schema.sub_city_zone as scz,"
		 		+ "gw_db_schema.woreda as wr,gw_db_schema.local_name as lnm where cws.cdt_id=cd.cdt_id and cd.ch_id=ch.ch_id and ch.well_id=ww.well_id"
		 		+ " and ww.wf_id=wf.wf_id and ww.local_id=lnm.local_id and lnm.wr_id=wr.wr_id and wr.scz_id=scz.scz_id and scz.cl_id=rc.cl_id and "
		 		+ "rc.reg_id=rg.reg_id and du.id=ww.owned_by and cws.data_colln_case='Well Completion Report' and "
		 		+ "ww.db_userid="+userID+" order by ww.well_id desc";	
		 	}else {
 		getvalue="select ww.well_id,wf.wf_name,rg.reg_name,ww.wellownercatgory,du.orgname,cd.yield_lps,ww.well_depth,extract(year from ww.cntn_year)"
	 			+ ",ww.well_index,ww.wellcode,cws.well_status,cws.data_colln_case from gw_db_schema.constwell_status as cws,"
		 		+ "gw_db_schema.con_discharge_test as cd,gw_db_schema.well_characteristics as ch,gw_db_schema.water_well as ww,gw_db_schema.well_field "
		 		+ "as wf,gw_db_schema.db_user as du,gw_db_schema.region_city as rg,gw_db_schema.region_class as rc,gw_db_schema.sub_city_zone as scz,"
		 		+ "gw_db_schema.woreda as wr,gw_db_schema.local_name as lnm where cws.cdt_id=cd.cdt_id and cd.ch_id=ch.ch_id and ch.well_id=ww.well_id"
		 		+ " and ww.wf_id=wf.wf_id and ww.local_id=lnm.local_id and lnm.wr_id=wr.wr_id and wr.scz_id=scz.scz_id and scz.cl_id=rc.cl_id and "
		 		+ "rc.reg_id=rg.reg_id and du.id=ww.owned_by and cws.data_colln_case='Well Completion Report' and "
		 		+ "ww.db_userid="+userID+" and LOWER(ww.well_index) like LOWER('"+wellIndex+"%') order by ww.well_id desc";	
		 	}
		 	try{
		 conn=connection.dbconnection();
		 Statement stmnt=conn.createStatement();
		 ResultSet rs=stmnt.executeQuery(getvalue);
		 while(rs.next()){
			 int well_id=rs.getInt(1);
			    String wellField = rs.getString(2);
			    String regionName = rs.getString(3);
			    String ownerGroup = rs.getString(4);
			    String OunerName=rs.getString(5);
			    double waterYield=rs.getDouble(6);
			    double wellDepth=rs.getDouble(7);
			    String constYear=rs.getString(8);
			    String DBwellIndex=rs.getString(9);
			    String wellCode=rs.getString(10);
			    String wellStatus=rs.getString(11);
			    String dataCollectCase=rs.getString(12);
			    ValuePair ff= new ValuePair(well_id,wellField,regionName,ownerGroup,OunerName,waterYield,wellDepth,constYear,DBwellIndex,wellCode,wellStatus,
					 dataCollectCase);
			   files.add(ff);
		 }
		 }catch (Exception e) {
			e.printStackTrace();
		}
		 	return files;
		 }
	 //select well completion report data for update or to delete
	 public static List<ValuePair> access_modify_delete(String fname) throws SQLException{
		 List<ValuePair> files=new ArrayList<ValuePair>();
		 conn=connection.dbconnection();
		 String sql ="select ww.well_id,wf.wf_name,rg.reg_name,ww.wellownercatgory,du.orgname,cd.yield_lps,ww.well_depth,extract(year from ww.cntn_year)"
		 		+ ",ww.well_index,ww.wellcode,cws.well_status,cws.data_colln_case from gw_db_schema.constwell_status as cws,"
		 		+ "gw_db_schema.con_discharge_test as cd,gw_db_schema.well_characteristics as ch,gw_db_schema.water_well as ww,gw_db_schema.well_field "
		 		+ "as wf,gw_db_schema.db_user as du,gw_db_schema.region_city as rg,gw_db_schema.region_class as rc,gw_db_schema.sub_city_zone as scz,"
		 		+ "gw_db_schema.woreda as wr,gw_db_schema.local_name as lnm where cws.cdt_id=cd.cdt_id and cd.ch_id=ch.ch_id and ch.well_id=ww.well_id"
		 		+ " and ww.wf_id=wf.wf_id and ww.local_id=lnm.local_id and lnm.wr_id=wr.wr_id and wr.scz_id=scz.scz_id and scz.cl_id=rc.cl_id and "
		 		+ "rc.reg_id=rg.reg_id and du.id=ww.owned_by and cws.data_colln_case='Well Completion Report' and "
		 		+ "ww.db_userid="+fname+" order by ww.well_id desc";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int well_id=rs.getInt(1);
			    String wellField = rs.getString(2);
			    String regionName = rs.getString(3);
			    String ownerGroup = rs.getString(4);
			    String OunerName=rs.getString(5);
			    double waterYield=rs.getDouble(6);
			    double wellDepth=rs.getDouble(7);
			    String constYear=rs.getString(8);
			    String wellIndex = rs.getString(9);
			    String wellCode=rs.getString(10);
			    String wellStatus=rs.getString(11);
			    String dataCollectCase=rs.getString(12);
			    ValuePair ff= new ValuePair(well_id,wellField,regionName,ownerGroup,OunerName,waterYield,wellDepth,constYear,wellIndex,wellCode,wellStatus,
					 dataCollectCase,"");
			   files.add(ff);
			}
		 return files;
	 }
	 //Access GW Data
	 public static List<ValuePair> accessGWDataByWellIndex(int userID,String wellIndex) throws SQLException{
		 List<ValuePair> files=new ArrayList<ValuePair>();
		 String sql="";
		 conn=connection.dbconnection();
		 if(wellIndex.equals("")) {
			 sql ="select ww.well_id,wf.wf_name,rg.reg_name,ww.wellownercatgory,du.orgname,cws.datacondtion,ww.well_depth,"
	 		+ "extract(year from cws.record_date),ww.well_index,ww.wellcode,cws.well_status,cws.data_colln_case from "
	 		+ "gw_db_schema.constwell_status as cws,gw_db_schema.pumping_status as ps,gw_db_schema.con_discharge_test as cd,"
	 		+ "gw_db_schema.well_characteristics as ch,gw_db_schema.water_well as ww,gw_db_schema.well_field as wf,gw_db_schema.db_user as "
	 		+ "du,gw_db_schema.region_city as rg,gw_db_schema.region_class as rc,gw_db_schema.sub_city_zone as scz,gw_db_schema.woreda as wr,"
	 		+ "gw_db_schema.local_name as lnm where cws.cdt_id=cd.cdt_id and ps.ch_id=ch.ch_id and cd.ch_id=ch.ch_id and ch.well_id="
	 		+ "ww.well_id and ww.wf_id=wf.wf_id and ww.local_id=lnm.local_id and lnm.wr_id=wr.wr_id and wr.scz_id=scz.scz_id and scz.cl_id="
	 		+ "rc.cl_id and rc.reg_id=rg.reg_id and du.id=ww.owned_by and cws.data_colln_case='Operational' and ps.data_colln_case="
	 		+ "'Operational' and ww.db_userid="+userID+" order by ww.well_id desc"; 
		 }else {
			 sql ="select ww.well_id,wf.wf_name,rg.reg_name,ww.wellownercatgory,du.orgname,cws.datacondtion,ww.well_depth,"
	 		+ "extract(year from cws.record_date),ww.well_index,ww.wellcode,cws.well_status,cws.data_colln_case from "
	 		+ "gw_db_schema.constwell_status as cws,gw_db_schema.pumping_status as ps,gw_db_schema.con_discharge_test as cd,"
	 		+ "gw_db_schema.well_characteristics as ch,gw_db_schema.water_well as ww,gw_db_schema.well_field as wf,gw_db_schema.db_user as "
	 		+ "du,gw_db_schema.region_city as rg,gw_db_schema.region_class as rc,gw_db_schema.sub_city_zone as scz,gw_db_schema.woreda as wr,"
	 		+ "gw_db_schema.local_name as lnm where cws.cdt_id=cd.cdt_id and ps.ch_id=ch.ch_id and cd.ch_id=ch.ch_id and ch.well_id="
	 		+ "ww.well_id and ww.wf_id=wf.wf_id and ww.local_id=lnm.local_id and lnm.wr_id=wr.wr_id and wr.scz_id=scz.scz_id and scz.cl_id="
	 		+ "rc.cl_id and rc.reg_id=rg.reg_id and du.id=ww.owned_by and cws.data_colln_case='Operational' and ps.data_colln_case="
	 		+ "'Operational' and ww.db_userid="+userID+" and LOWER(ww.well_index) like LOWER('"+wellIndex+"%') order by ww.well_id desc"; 
		 }
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int well_id=rs.getInt(1);
			    String wellField = rs.getString(2);
			    String regionName = rs.getString(3);
			    String ownerGroup = rs.getString(4);
			    String OunerName=rs.getString(5);
			    String datacondition=rs.getString(6);
			    double wellDepth=rs.getDouble(7);
			    String constYear=rs.getString(8);
			    String DBwellIndex = rs.getString(9);
			    String wellCode=rs.getString(10);
			    String wellStatus=rs.getString(11);
			    String dataCollectCase=rs.getString(12);
			    ValuePair ff= new ValuePair(well_id,wellField,regionName,ownerGroup,OunerName,datacondition,wellDepth,constYear,DBwellIndex,wellCode,
			    		wellStatus,dataCollectCase);
			   files.add(ff);
			}
		 return files;
	 }
	 //Access GW Data
	 public static List<ValuePair> access_modify_gwData(String fname) throws SQLException{
		 List<ValuePair> files=new ArrayList<ValuePair>();
		 conn=connection.dbconnection();
		 String sql ="select ww.well_id,wf.wf_name,rg.reg_name,ww.wellownercatgory,du.orgname,cws.datacondtion,ww.well_depth,"
		 		+ "extract(year from cws.record_date),ww.well_index,ww.wellcode,cws.well_status,cws.data_colln_case from "
		 		+ "gw_db_schema.constwell_status as cws,gw_db_schema.pumping_status as ps,gw_db_schema.con_discharge_test as cd,"
		 		+ "gw_db_schema.well_characteristics as ch,gw_db_schema.water_well as ww,gw_db_schema.well_field as wf,gw_db_schema.db_user as "
		 		+ "du,gw_db_schema.region_city as rg,gw_db_schema.region_class as rc,gw_db_schema.sub_city_zone as scz,gw_db_schema.woreda as wr,"
		 		+ "gw_db_schema.local_name as lnm where cws.cdt_id=cd.cdt_id and ps.ch_id=ch.ch_id and cd.ch_id=ch.ch_id and ch.well_id="
		 		+ "ww.well_id and ww.wf_id=wf.wf_id and ww.local_id=lnm.local_id and lnm.wr_id=wr.wr_id and wr.scz_id=scz.scz_id and scz.cl_id="
		 		+ "rc.cl_id and rc.reg_id=rg.reg_id and du.id=ww.owned_by and cws.data_colln_case='Operational' and ps.data_colln_case="
		 		+ "'Operational' and ww.db_userid="+fname+" order by ww.well_id desc";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int well_id=rs.getInt(1);
			    String wellField = rs.getString(2);
			    String regionName = rs.getString(3);
			    String ownerGroup = rs.getString(4);
			    String OunerName=rs.getString(5);
			    String datacondition=rs.getString(6);
			    double wellDepth=rs.getDouble(7);
			    String lastUpdate=rs.getString(8);
			    String wellIndex = rs.getString(9);
			    String wellCode=rs.getString(10);
			    String wellStatus=rs.getString(11);
			    String dataCollectCase=rs.getString(12);
			    ValuePair ff= new ValuePair(well_id,wellField,regionName,ownerGroup,OunerName,datacondition,wellDepth,lastUpdate,wellIndex,wellCode,
			    		wellStatus,dataCollectCase,0);
			   files.add(ff);
			}
		 return files;
	 }
	//Access and group by Project type, then display in the legend
	 public static List<File> access_groupingBy_proType(){
		 List<File>list_region=new ArrayList<>();
		 try {
	conn=connection.dbconnection();
	Statement repo_stm=conn.createStatement();
	String report_query="select wfield,count(count_row) from(select ww.well_index as count_row,wf.wf_name as wfield from "
			+ "gw_db_schema.water_well as ww,gw_db_schema.well_field as wf where wf.wf_id=ww.wf_id group by ww.well_index,wf.wf_name)"
			+ "pr_c group by pr_c.wfield ORDER by count(count_row) desc";
	ResultSet report_rs=repo_stm.executeQuery(report_query);
	while(report_rs.next()) {
		String file_gr=report_rs.getString(1);
		int count_p=report_rs.getInt(2);
		float lat_long=0f;
		File repo_file=new File(file_gr,lat_long,count_p);
		//System.out.println("well field Legend is "+file_gr);
		list_region.add(repo_file);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 return list_region;
	 }
	//Access Specific well to Overlay on the georeferenced map
	 public static List<File> access_specificWellName(String wellName){
		 List<File>list_region=new ArrayList<>();
		 try {
				conn=connection.dbconnection();
				Statement repo_stm=conn.createStatement();		
				String report_query="select ww.well_id,wf.wf_name,gl.longt,gl.lat,gl.longt,gl.lat,ww.well_index,extract(year from ww.cntn_year),"
						+ "ww.well_type from gw_db_schema.water_well as ww,gw_db_schema.geo_location as gl,gw_db_schema.well_field as wf where "
						+ "gl.geoloc_id=ww.geoloc_id and wf.wf_id=ww.wf_id and gl.longt is not null and"
						+ " LOWER(ww.well_index) like LOWER('"+wellName+"%') and gl.longt is not null group by ww.well_id,wf.wf_name,"
								+ "gl.longt,gl.lat,ww.well_index,extract(year from ww.cntn_year),ww.well_type";
				ResultSet report_rs=repo_stm.executeQuery(report_query);
				while(report_rs.next()) {
					int count_file_gr=report_rs.getInt(1);
					String file_gr=report_rs.getString(2);
					double locx=report_rs.getDouble(3);
					double locy=report_rs.getDouble(4);
					double locx2=report_rs.getDouble(5);
					double locy2=report_rs.getDouble(6);
					String Procode=report_rs.getString(7);
					String Pyear=report_rs.getString(8);
					String project_type=report_rs.getString(9);
					File repo_file=new File(count_file_gr, file_gr,locx,locy,locx2,locy2,Procode,Pyear,project_type,"","");
					list_region.add(repo_file);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 return list_region;
	 } 
	//Access Well to Overlay on the georeference map
	 public static List<File> access_All_wellName_byLegend(String Lege_name,String wellFunction){
		 List<File>list_region=new ArrayList<>();
		 try {
				conn=connection.dbconnection();
				Statement repo_stm=conn.createStatement();
				if(wellFunction.equals("undefined")||wellFunction.equals(null)) {
					String report_query="select ww.well_id,wf.wf_name,gl.longt,gl.lat,gl.longt,gl.lat,ww.well_index,extract(year from ww.cntn_year),"
							+ "ww.well_type from gw_db_schema.productive_well_status as pws,gw_db_schema.productive_well as pw,"
							+ "gw_db_schema.constwell_status as cws,gw_db_schema.con_discharge_test as cd,gw_db_schema.well_characteristics as ch,"
							+ "gw_db_schema.water_well as ww,gw_db_schema.geo_location as gl,gw_db_schema.well_field as wf where pws.pro_id="
							+ "pw.pro_id and pw.cws_id=cws.cws_id and cws.cdt_id=cd.cdt_id and cd.ch_id=ch.ch_id and ch.well_id=Ww.well_id and "
							+ "gl.geoloc_id=ww.geoloc_id and wf.wf_id=ww.wf_id and gl.longt is not null and wf.wf_name='"+Lege_name+"' "
							+ "group by ww.well_id,ww.well_index,gl.longt,gl.lat,wf.wf_name,extract(year from ww.cntn_year),ww.well_type";
					ResultSet report_rs=repo_stm.executeQuery(report_query);
					while(report_rs.next()) {
						int count_file_gr=report_rs.getInt(1);
						String file_gr=report_rs.getString(2);
						double locx=report_rs.getDouble(3);
						double locy=report_rs.getDouble(4);
						double locx2=report_rs.getDouble(5);
						double locy2=report_rs.getDouble(6);
						String Procode=report_rs.getString(7);
						String Pyear=report_rs.getString(8);
						String pro_type=report_rs.getString(9);
						File repo_file=new File(count_file_gr, file_gr,locx,locy,locx2,locy2,Procode,Pyear,pro_type);
						//System.out.println("project name to be Overlayed: "+file_gr+ "\t cocation X= "+locx+"\t location Y= "+locy+"\tProject type= "+pro_type);
						list_region.add(repo_file);
					}
				}else {
					String report_query="select ww.well_id,wf.wf_name,gl.longt,gl.lat,gl.longt,gl.lat,ww.well_index,extract(year from ww.cntn_year),"
							+ "ww.well_type from gw_db_schema.productive_well_status as pws,gw_db_schema.productive_well as pw,"
							+ "gw_db_schema.constwell_status as cws,gw_db_schema.con_discharge_test as cd,gw_db_schema.well_characteristics as ch,"
							+ "gw_db_schema.water_well as ww,gw_db_schema.geo_location as gl,gw_db_schema.well_field as wf where pws.pro_id="
							+ "pw.pro_id and pw.cws_id=cws.cws_id and cws.cdt_id=cd.cdt_id and cd.ch_id=ch.ch_id and ch.well_id=Ww.well_id and "
							+ "gl.geoloc_id=ww.geoloc_id and wf.wf_id=ww.wf_id and gl.longt is not null and wf.wf_name='"+Lege_name+"' and "
							+ "((pws.wellfunstatus='"+wellFunction+"' or pws.func_condition='"+wellFunction+"') or "
							+ "pws.wellfunstatus='"+wellFunction+"' or cws.well_status='"+wellFunction+"' or pws.func_condition="
							+ "'"+wellFunction+"')group by ww.well_id,ww.well_index,gl.longt,gl.lat,wf.wf_name,extract(year from ww.cntn_year),"
							+ "ww.well_type";
				ResultSet report_rs=repo_stm.executeQuery(report_query);
				while(report_rs.next()) {
					int count_file_gr=report_rs.getInt(1);
					String file_gr=report_rs.getString(2);
					double locx=report_rs.getDouble(3);
					double locy=report_rs.getDouble(4);
					double locx2=report_rs.getDouble(5);
					double locy2=report_rs.getDouble(6);
					String Procode=report_rs.getString(7);
					String Pyear=report_rs.getString(8);
					String pro_type=report_rs.getString(9);
					File repo_file=new File(count_file_gr, file_gr,locx,locy,locx2,locy2,Procode,Pyear,pro_type);
					//System.out.println("project name to be Overlayed: "+file_gr+ "\t cocation X= "+locx+"\t location Y= "+locy+"\tProject type= "+pro_type);
					list_region.add(repo_file);
				}}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 return list_region;
	 } 
	 
 //Access Well to Overlay on the georeference map
 public static List<File> access_well_name(){
	 List<File>list_region=new ArrayList<>();
	 try {
			conn=connection.dbconnection();
			Statement repo_stm=conn.createStatement();
			String report_query="select ww.well_id,wf.wf_name,gl.longt,gl.lat,gl.longt,gl.lat,ww.well_index,extract(year from ww.cntn_year),"
					+ "ww.well_type from gw_db_schema.water_well as ww,gw_db_schema.well_field as wf,gw_db_schema.geo_location as gl where "
					+ "gl.geoloc_id=ww.geoloc_id and wf.wf_id=ww.wf_id and gl.longt is not null group by ww.well_index,ww.well_id,"
					+ "wf.wf_name,gl.longt,gl.lat,extract(year from ww.cntn_year),ww.well_type";
			ResultSet report_rs=repo_stm.executeQuery(report_query);
			while(report_rs.next()) {
				int count_file_gr=report_rs.getInt(1);
				String file_gr=report_rs.getString(2);
				double locx=report_rs.getDouble(3);
				double locy=report_rs.getDouble(4);
				double locx2=report_rs.getDouble(5);
				double locy2=report_rs.getDouble(6);
				String Procode=report_rs.getString(7);
				String Pyear=report_rs.getString(8);
				String project_type=report_rs.getString(9);
				File repo_file=new File(count_file_gr, file_gr,locx,locy,locx2,locy2,Procode,Pyear,project_type,"");
				//System.out.println("Index Name to be Overlayed: "+file_gr+ "\t cocation X= "+locx+"\t location Y= "+locy);
				list_region.add(repo_file);
			}
			report_rs.close();
			repo_stm.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 return list_region;
 } 
 public static List<File> count_project(String reg_name){
	 List<File>list_region=new ArrayList<>();
	 try {
			conn=connection.dbconnection();
			Statement repo_stm=conn.createStatement();
			String report_query="select subcityZ,count(count_row) from(select ww.well_index as count_row,sz.sub_city_zone_name as subcityZ from "
					+ "gw_db_schema.water_well as ww,gw_db_schema.local_name as lc,gw_db_schema.woreda as wr,gw_db_schema.sub_city_zone as sz "
					+ "where ww.local_id=lc.local_id and lc.wr_id=wr.wr_id and wr.scz_id=sz.scz_id and sz.sub_city_zone_name='"+reg_name+"' "
					+ "group by sz.sub_city_zone_name,ww.well_index) pr_c group by pr_c.subcityZ";
			ResultSet report_rs=repo_stm.executeQuery(report_query);
			while(report_rs.next()) {
				int non_val=0;
				int non_val1=0;
				String file_gr=report_rs.getString(1);
				int count_project=report_rs.getInt(2);
				File repo_file=new File(non_val,file_gr,count_project,non_val1);
				list_region.add(repo_file);
			}
			//report_rs.close();
			//repo_stm.close();
			//conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 return list_region;
 }
 //search by Well Report
 public static List<File> access_ReportByWellId(int pro_ID){
	 List<File>list_region=new ArrayList<>();
	 try {
		 if(pro_ID!=0) {
 conn=connection.dbconnection();
	Statement repo_stm=conn.createStatement();
	String basicChReport_query="select q1.a,q1.b,q1.c,q1.d,q1.e,q1.f,q1.g,q1.h,q1.i,q1.j,q1.k,q1.l,q1.m,q1.o,q1.n,q2.aa,q2.bb,q2.cc,q2.dd,q2.ee,q2.ff,"
	+ "q2.gg,q2.hh,q2.ii,q2.jj,q2.kk,q2.ll,q2.mm,q2.nn,q1.l2 from(select ww.well_index as a,gl.longt as b, gl.lat as c,ww.wellownercatgory as d,"
	+ "extract(year from ww.cntn_year) as e,ww.well_depth as f,cd.yield_lps as g,cd.swl_m as h,cd.dwl_m as i,ch.specific_capacity as j,"
	+ "cws.well_status as k,wf.wf_name as l,scz.sub_city_zone_name as l2,ps.pump_position as m,gl.easting as o,gl.northing as n,ww.well_id as comm "
	+ "from gw_db_schema.well_field as wf,gw_db_schema.sub_city_zone as scz,gw_db_schema.woreda as wr,gw_db_schema.local_name as lnm,"
	+ "gw_db_schema.water_well as ww,gw_db_schema.well_characteristics as ch,gw_db_schema.geo_location as gl,"
	+ "gw_db_schema.pumping_status as ps,gw_db_schema.con_discharge_test as cd,gw_db_schema.constwell_status as cws where "
	+ "scz.scz_id=wr.scz_id and wr.wr_id=lnm.wr_id and lnm.local_id=ww.local_id and "
	+ "gl.geoloc_id=ww.geoloc_id and wf.wf_id=ww.wf_id and ww.well_id=ch.well_id and ch.ch_id=cd.ch_id and ps.ch_id=ch.ch_id and "
	+ "cd.cdt_id=cws.cdt_id and cws.data_colln_case='Well Completion Report' and ps.data_colln_case='Well Completion Report' and "
	+ "cd.data_colln_case='Well Completion Report' and ww.well_id="+pro_ID+") as q1 INNER JOIN"
	+ "(select ps.abstraction_rate as aa,cd.swl_m as bb,cd.dwl_m as cc,pws.wellfunstatus as dd,pws.reason_nonfun as ee,pws.func_condition as "
	+ "ff,pws.reason_inactive as gg,gw.potability_status as hh,gw.non_potable_reason as ii,cws.datacondtion as jj,cws.record_date as kk,"
	+ "prw.pumping_operen_date as ll,ps.pump_position as mm,ps.currenty_designy as nn,ww.well_id as co from gw_db_schema.water_well as ww,"
	+ "gw_db_schema.well_characteristics as ch,gw_db_schema.gw_data as gw,gw_db_schema.pumping_status as ps,gw_db_schema.con_discharge_test as "
	+ "cd,gw_db_schema.constwell_status as cws,gw_db_schema.productive_well as prw,gw_db_schema.productive_well_status as pws where ww.well_id="
	+ "ch.well_id and ww.well_id=gw.well_id and ch.ch_id=cd.ch_id and ps.ch_id=ch.ch_id and gw.gw_id=cws.gw_id and cd.cdt_id=cws.cdt_id and "
	+ "cws.cws_id=prw.cws_id and prw.pro_id=pws.pro_id and cd.data_colln_case='Operational' and cws.data_colln_case='Operational' and "
	+ "ps.data_colln_case='Operational' and cd.datacondtion is not null and gw.potability_status is not null and ww.well_id="+pro_ID+")as "
	+ "q2 ON q1.comm=q2.co limit 1";
ResultSet basicChReport_rs=repo_stm.executeQuery(basicChReport_query);
while(basicChReport_rs.next()) {
	String well_index=basicChReport_rs.getString(1);
	double geoLocX=basicChReport_rs.getDouble(2);
	double geoLocY=basicChReport_rs.getDouble(3);
	String well_Owner=basicChReport_rs.getString(4);
	String Cyear=basicChReport_rs.getString(5);
	double wellDepth=basicChReport_rs.getDouble(6);
	double wellYield=basicChReport_rs.getDouble(7);
	double wellSWL=basicChReport_rs.getDouble(8);
	double wellDWL=basicChReport_rs.getDouble(9);
	double speCapacity=basicChReport_rs.getDouble(10);
	String wellStatus=basicChReport_rs.getString(11);
	String wellFieldOverlay=basicChReport_rs.getString(12);
	double pumpPosition=basicChReport_rs.getDouble(13);
	double easting=basicChReport_rs.getDouble(14);
	double northing=basicChReport_rs.getDouble(15);
	double abstractionRate=basicChReport_rs.getDouble(16);
	double wellSWLOpn=basicChReport_rs.getDouble(17);
	double wellDWLOpn=basicChReport_rs.getDouble(18);
	String currentWS=basicChReport_rs.getString(19);
	if((currentWS.equals("0")) || (currentWS.equals(""))) {
		currentWS="_";
	}
	String reasonNonF=basicChReport_rs.getString(20);
	if((reasonNonF.equals("0")) || (reasonNonF.equals("0.0")) ||(reasonNonF.equals(""))) {
		reasonNonF="_";
	}
	String functionWellCondi=basicChReport_rs.getString(21);
	if((functionWellCondi.equals("0")) || (functionWellCondi.equals(""))) {
		functionWellCondi="_";
	}
	String inActiveRe=basicChReport_rs.getString(22);
	if((inActiveRe.equals("0")) || (inActiveRe.equals(""))) {
		inActiveRe="_";
	}
	String potableS=basicChReport_rs.getString(23);
	if((potableS.equals("0")) || (potableS.equals(""))) {
		potableS="_";
	}
	String nonPotabableR=basicChReport_rs.getString(24);
	if((nonPotabableR.equals("0")) || (nonPotabableR.equals("0.0")) || (nonPotabableR.equals(""))) {
		nonPotabableR="_";
	}
	String dataCond=basicChReport_rs.getString(25);
	if((dataCond.equals("0")) || (dataCond.equals(""))) {
		dataCond="_";
	}
	String recordDate=basicChReport_rs.getString(26);
	if((recordDate.equals("0")) || (recordDate.equals(""))) {
		recordDate="_";
	}
	String operationStartDate=basicChReport_rs.getString(27);
	if((operationStartDate.equals("0")) || (operationStartDate.equals(""))) {
		operationStartDate="_";
	}
	double pumpPositionOpn=basicChReport_rs.getDouble(28);
	String design_current=basicChReport_rs.getString(29);
	if((design_current.equals("0")) || (design_current.equals(""))) {
		design_current="_";
	}
	String subcityZone=basicChReport_rs.getString(30);
	//System.out.println("Well Type= "+well_type);
	File repo_file=new File(well_index,geoLocX,geoLocY,well_Owner,Cyear,wellDepth,wellYield,wellSWL,wellDWL,speCapacity,wellStatus,
			wellFieldOverlay,pumpPosition,abstractionRate,wellSWLOpn,wellDWLOpn,currentWS,reasonNonF,functionWellCondi,inActiveRe,potableS,
			nonPotabableR,dataCond,recordDate,operationStartDate,pumpPositionOpn,design_current,subcityZone);
	list_region.add(repo_file);
}	 
		 }			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 return list_region;
 } 
 //report
 public static List<File> reports_bar(){
	 List<File> filetype=new ArrayList<>();
	 try {
		conn=connection.dbconnection();
		Statement repo_stm=conn.createStatement();
		String report_query="select cws.data_colln_case,count(cws.data_colln_case) from gw_db_schema.constwell_status as cws,"
				+ "gw_db_schema.con_discharge_test as cd,gw_db_schema.well_characteristics as ch,gw_db_schema.gw_data as gw,"
				+ "gw_db_schema.water_well as Ww where cws.cdt_id=cd.cdt_id and cws.gw_id=gw.gw_id and cd.ch_id=ch.ch_id and "
				+ "ch.well_id=Ww.well_id and gw.well_id=Ww.well_id group by cws.data_colln_case";
		ResultSet report_rs=repo_stm.executeQuery(report_query);
		while(report_rs.next()) {
			String file_gr=report_rs.getString(1);
			int count_file_gr=report_rs.getInt(2);
			float size_file=0f;
			File repo_file=new File(file_gr, count_file_gr,size_file);
			//System.out.println("file size: "+size_file);
			filetype.add(repo_file);
		}
		report_rs.close();
		repo_stm.close();
		conn.close();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	 
	 return filetype;
	 }
 //Not Yet Accessible
	 public static List<File> _deleteFile(String wellID,String prepared_by){
		 List<File> files=new ArrayList<File>();
		 //System.out.println("User id is = "+prepared_by);
		 LocalDateTime timedateNow= LocalDateTime.now(ZoneId.systemDefault());
		 int geolocId=0;
		 String well_index="";
		 try {
			conn=connection.dbconnection();
			String deletef="select geoloc_id,well_index from gw_db_schema.water_well where well_id="+wellID+"";
			Statement prep_delete=conn.createStatement();
			ResultSet rssearchGeolocID=prep_delete.executeQuery(deletef);
			while(rssearchGeolocID.next()) {
				geolocId=rssearchGeolocID.getInt(1);
				well_index=rssearchGeolocID.getString(2);
			}
			String deleteWellData="delete from gw_db_schema.geo_location where geoloc_id="+geolocId+"";
		    boolean whathappened= prep_delete.execute(deleteWellData);
		     System.out.println("What Happened is "+whathappened);
			String deletedWellField= "insert into gw_db_schema.database_log(user_id,actiontook,datehapped,datasheetype,well_index)values("+prepared_by+","
					+ "'Well Index Named: "+well_index+" is Deleted',?,'Entire Well Information','"+well_index+"')";
			try (PreparedStatement preparedStatement = conn.prepareStatement(deletedWellField)){
				preparedStatement.setObject(1,timedateNow);
				preparedStatement.executeUpdate();
				preparedStatement.close();
			}
			 prep_delete.close();
		 String sql ="select ww.well_id,wf.wf_name,rg.reg_name,ww.wellownercatgory,du.orgname,cd.yield_lps,ww.well_depth,"
		 		+ "extract(year from ww.cntn_year),ww.well_index,ww.wellcode,cws.well_status,cws.data_colln_case from "
		 		+ "gw_db_schema.constwell_status as cws,gw_db_schema.con_discharge_test as cd,gw_db_schema.well_characteristics as ch,"
		 		+ "gw_db_schema.water_well as ww,gw_db_schema.well_field as wf,gw_db_schema.db_user as du,gw_db_schema.region_city as rg,"
		 		+ "gw_db_schema.region_class as rc,gw_db_schema.sub_city_zone as scz,gw_db_schema.woreda as wr,gw_db_schema.local_name as lnm "
		 		+ "where cws.cdt_id=cd.cdt_id and cd.ch_id=ch.ch_id and ch.well_id=ww.well_id and ww.wf_id=wf.wf_id and ww.local_id=lnm.local_id "
		 		+ "and lnm.wr_id=wr.wr_id and wr.scz_id=scz.scz_id and scz.cl_id=rc.cl_id and rc.reg_id=rg.reg_id and du.id=ww.owned_by and "
		 		+ "cws.data_colln_case='Well Completion Report' and du.usertype='Well Owner' and ww.db_userid="+prepared_by+" "
		 		+ "order by ww.well_id desc";
		 Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int well_id=rs.getInt(1);
			    String wellField = rs.getString(2);
			    String regionName = rs.getString(3);
			    String ownerGroup = rs.getString(4);
			    String OunerName=rs.getString(5);
			    double waterYield=rs.getDouble(6);
			    double wellDepth=rs.getDouble(7);
			    String constYear=rs.getString(8);
			    String wellIndex = rs.getString(9);
			    String wellCode=rs.getString(10);
			    String wellStatus=rs.getString(11);
			    String dataCollectCase=rs.getString(12);
			 File ff= new File(well_id,wellField,regionName,ownerGroup,OunerName,waterYield,wellDepth,constYear,wellIndex,wellCode,wellStatus,
					 dataCollectCase);
			   files.add(ff);
			}
		 return files;

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		 return files;
	 }
	 public static List<File> project_deleteFile(String wellID,String prepared_by){
		 List<File> files=new ArrayList<File>();
		 //System.out.println("User id is = "+prepared_by);
		 int geolocId=0;
		 String well_index="";
		 LocalDateTime timedateNow= LocalDateTime.now(ZoneId.systemDefault());
		 try {
			conn=connection.dbconnection();
			String deletef="select geoloc_id,well_index from gw_db_schema.water_well where well_id="+wellID+"";
			Statement prep_delete=conn.createStatement();
			ResultSet rssearchGeolocID=prep_delete.executeQuery(deletef);
			while(rssearchGeolocID.next()) {
				geolocId=rssearchGeolocID.getInt(1);
				well_index=rssearchGeolocID.getString(2);
			}
			String deleteWellData="delete from gw_db_schema.geo_location where geoloc_id="+geolocId+"";
		    boolean whathappened= prep_delete.execute(deleteWellData);
		     System.out.println("What Happened is "+whathappened);
			String deletedWellField= "insert into gw_db_schema.database_log(user_id,actiontook,datehapped,datasheetype,well_index)values("+prepared_by+","
					+ "'Well Index: "+well_index+" is deleted',?,'Entire well data','"+well_index+"')";
			try (PreparedStatement preparedStatement = conn.prepareStatement(deletedWellField)){
				preparedStatement.setObject(1,timedateNow);
				preparedStatement.executeUpdate();
				preparedStatement.close();
			}
			 prep_delete.close();
					 String sql ="select ww.well_id,wf.wf_name,rg.reg_name,ww.wellownercatgory,du.orgname,cws.datacondtion,ww.well_depth,"
					 		+ "extract(year from cws.record_date),ww.well_index,ww.wellcode,cws.well_status,cws.data_colln_case from "
					 		+ "gw_db_schema.constwell_status as cws,gw_db_schema.pumping_status as ps,gw_db_schema.con_discharge_test as cd,"
					 		+ "gw_db_schema.well_characteristics as ch,gw_db_schema.water_well as ww,gw_db_schema.well_field as wf,"
					 		+ "gw_db_schema.db_user as du,gw_db_schema.region_city as rg,gw_db_schema.region_class as rc,"
					 		+ "gw_db_schema.sub_city_zone as scz,gw_db_schema.woreda as wr,gw_db_schema.local_name as lnm where "
					 		+ "cws.cdt_id=cd.cdt_id and ps.ch_id=ch.ch_id and cd.ch_id=ch.ch_id and ch.well_id=ww.well_id and ww.wf_id=wf.wf_id "
					 		+ "and ww.local_id=lnm.local_id and lnm.wr_id=wr.wr_id and wr.scz_id=scz.scz_id and scz.cl_id=rc.cl_id and "
					 		+ "rc.reg_id=rg.reg_id and du.id=ww.owned_by and cws.data_colln_case='Operational' and ps.data_colln_case="
					 		+ "'Operational' and du.usertype='Well Owner' and ww.db_userid="+prepared_by+" order by ww.well_id desc";
					 Statement stmt = conn.createStatement();
						ResultSet rs = stmt.executeQuery(sql);
						while (rs.next()) {
							int well_id=rs.getInt(1);
						    String wellField = rs.getString(2);
						    String regionName = rs.getString(3);
						    String ownerGroup = rs.getString(4);
						    String OunerName=rs.getString(5);
						    String dataCondition=rs.getString(6);
						    double wellDepth=rs.getDouble(7);
						    String constYear=rs.getString(8);
						    String wellIndex = rs.getString(9);
						    String wellCode=rs.getString(10);
						    String wellStatus=rs.getString(11);
						    String dataCollectCase=rs.getString(12);
						 File ff= new File(well_id,wellField,regionName,ownerGroup,OunerName,dataCondition,wellDepth,constYear,wellIndex,wellCode,
								 wellStatus,dataCollectCase);
						   files.add(ff);
						}
					 return files;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		 return files;
	 }
	 public static List<File> accessHydroChemistryData(int wellID) throws SQLException{
		 List<File> files=new ArrayList<File>();
		 conn=connection.dbconnection();
		 String sql ="select wqd.well_id,wf.wf_name,wqd.well_index,ww.wellownercatgory,wqd.longt,wqd.lat,wqd.ph,wqd.totalh_ca_mg_caco3,wqd.f_,wqd.na_,"
		 		+ "wqd.cl_,wqd.ca_,wqd.fe_,wqd.mn_,wqd.mg_,wqd.temprature,wqd.so4_2_,wqd.no3_,wqd.tot_disolved_solid,wqd.elec_condu,wqd.k_,wqd.hco3_,"
		 		+ "wqd.p_,wqd.water_color,wqd.water_odor,scz.sub_city_zone_name from gw_db_schema.operation_wquality_data as wqd,"
		 		+ "gw_db_schema.water_well as ww,gw_db_schema.well_field as wf,gw_db_schema.sub_city_zone as scz,gw_db_schema.woreda as wr,"
		 		+ "gw_db_schema.local_name as lnm where scz.scz_id=wr.scz_id and wr.wr_id=lnm.wr_id and lnm.local_id=ww.local_id and "
		 		+ "wf.wf_id=ww.wf_id and ww.well_id=wqd.well_id and wqd.well_id="+wellID+"";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int well_id=rs.getInt(1);
			    String wellField = rs.getString(2);
			    String wellIndex = rs.getString(3);
			    String wellOwnerCategory = rs.getString(4);
			    double longtude=rs.getDouble(5);
			    double latitude=rs.getDouble(6);
			    double pH=rs.getDouble(7);
			    double caco3=rs.getDouble(8);
			    double flouride = rs.getDouble(9);
			    double soduim=rs.getDouble(10);
			    double chloride=rs.getDouble(11);
			    double calcium=rs.getDouble(12);
			    double iron=rs.getDouble(13);
			    double manganes=rs.getDouble(14);
			    double magnisium = rs.getDouble(15);
			    double temprature=rs.getDouble(16);
			    double sulfate=rs.getDouble(17);
			    double nitrate=rs.getDouble(18);
			    double TDS=rs.getDouble(19);
			    double electricConductivity=rs.getDouble(20);
			    double potasium = rs.getDouble(21);
			    double bicarbonet=rs.getDouble(22);
			    double posphate=rs.getDouble(23);
			    String waterColor = rs.getString(24);
			    String waterOdor = rs.getString(25);
			    String subcityZone = rs.getString(26);
			 File ff= new File(well_id,wellField,wellIndex,wellOwnerCategory,longtude,latitude,pH,caco3,flouride,soduim,chloride,calcium,iron,
					 manganes,magnisium,temprature,sulfate,nitrate,TDS,electricConductivity,potasium,bicarbonet,posphate,waterColor,waterOdor,
					 subcityZone);
			   files.add(ff);
			}
		 return files;
	 }
	 //Filter Hydro Chemistry By Elelement
	 //TDS
	 public static List<ValuePair>accessBYTDS(double TDS) throws SQLException{
		 double rangeScaller=0;
		 if(TDS<=500){
			 rangeScaller=0.0001;
		 }
		 else if(500< TDS && TDS <=1000){
			 rangeScaller=500.0001;
		 }
		 else if(1000< TDS && TDS <=1000000){
			 rangeScaller=1000.0001;
		 }
			List<ValuePair> TDSLocation=new ArrayList<ValuePair>(); 
			conn=connection.dbconnection();
			Statement statmnt=conn.createStatement();		
			String sql="select wqd.well_id,wqd.lat,wqd.longt,wqd.well_index,wf.wf_name from gw_db_schema.well_field as wf,gw_db_schema.water_well as ww,"
					+ "gw_db_schema.operation_wquality_data as wqd where wf.wf_id=ww.wf_id and wqd.well_id=ww.well_id and "
					+ "("+rangeScaller+" <= wqd.tot_disolved_solid and wqd.tot_disolved_solid <="+TDS+")";
			ResultSet rs=statmnt.executeQuery(sql);
			while(rs.next()){
				 int well_id=rs.getInt(1);
				 double latitude = rs.getDouble(2);
				 double longtude = rs.getDouble(3);
				 String wellIndex = rs.getString(4);
		         String wellField = rs.getString(5);
		         ValuePair ff= new ValuePair(well_id,latitude,longtude, wellIndex,wellField);
		         TDSLocation.add(ff);
			}
			conn.close();
			return TDSLocation;
		 }
	 //EC
	 public static List<ValuePair>accessBYEC(double EC) throws SQLException{
		 //System.out.println("EC value in DAO is "+EC);
		 double rangeScaller=0;
		 if(EC<=1500){
			 rangeScaller=0.0001;
		 }
		 else if(1500< EC && EC <=2500){
			 rangeScaller=1500.0001;
		 }
		 else if(2500< EC && EC <=1000000){
			 rangeScaller=2500.0001;
		 }
			List<ValuePair> TDSLocation=new ArrayList<ValuePair>(); 
			conn=connection.dbconnection();
			Statement statmnt=conn.createStatement();		
			String sql="select wqd.well_id,wqd.lat,wqd.longt,wqd.well_index,wf.wf_name from gw_db_schema.well_field as wf,gw_db_schema.water_well as ww,"
					+ "gw_db_schema.operation_wquality_data as wqd where wf.wf_id=ww.wf_id and wqd.well_id=ww.well_id and "
					+ "("+rangeScaller+" <= wqd.elec_condu and wqd.elec_condu <="+EC+")";
			ResultSet rs=statmnt.executeQuery(sql);
			while(rs.next()){
				 int well_id=rs.getInt(1);
				 double latitude = rs.getDouble(2);
				 double longtude = rs.getDouble(3);
				 String wellIndex = rs.getString(4);
		         String wellField = rs.getString(5);
		         //System.out.println("Well Index By EC "+wellIndex);
		         ValuePair ff= new ValuePair(well_id,latitude,longtude, wellIndex,wellField,"");
		         TDSLocation.add(ff);
			}
			conn.close();
			return TDSLocation;
		 }
	//Flouride
		 public static List<ValuePair>accessBYFlouride(double flouride) throws SQLException{
			 //System.out.println("EC value in DAO is "+EC);
			 double rangeScaller=0;
			 if(flouride<=1.5){
				 rangeScaller=0.000001;
			 }
			 else if(1.5< flouride && flouride <=3){
				 rangeScaller=1.500001;
			 }
			 else if(3< flouride && flouride <=1000000){
				 rangeScaller=3.000001;
			 }
				List<ValuePair> TDSLocation=new ArrayList<ValuePair>(); 
				conn=connection.dbconnection();
				Statement statmnt=conn.createStatement();		
				String sql="select wqd.well_id,wqd.lat,wqd.longt,wqd.well_index,wf.wf_name from gw_db_schema.well_field as wf,gw_db_schema.water_well as ww,"
						+ "gw_db_schema.operation_wquality_data as wqd where wf.wf_id=ww.wf_id and wqd.well_id=ww.well_id and "
						+ "("+rangeScaller+" <= wqd.f_ and wqd.f_ <="+flouride+")";
				ResultSet rs=statmnt.executeQuery(sql);
				while(rs.next()){
					 int well_id=rs.getInt(1);
					 double latitude = rs.getDouble(2);
					 double longtude = rs.getDouble(3);
					 String wellIndex = rs.getString(4);
			         String wellField = rs.getString(5);
			         //System.out.println("Well Index By EC "+wellIndex);
			         ValuePair ff= new ValuePair(well_id,latitude,longtude, wellIndex,wellField,"","");
			         TDSLocation.add(ff);
				}
				conn.close();
				return TDSLocation;
			 }
		//Flouride
		 public static List<ValuePair>accessBYIron(double iron) throws SQLException{
			 //System.out.println("EC value in DAO is "+EC);
			 double rangeScaller=0;
			 if(iron<=0.3){
				 rangeScaller=0.00001;
			 }
			 else if(0.3< iron && iron <=5){
				 rangeScaller=1.50001;
			 }
			 else if(5< iron && iron <=1000000){
				 rangeScaller=3.00001;
			 }
				List<ValuePair> TDSLocation=new ArrayList<ValuePair>(); 
				conn=connection.dbconnection();
				Statement statmnt=conn.createStatement();		
				String sql="select wqd.well_id,wqd.lat,wqd.longt,wqd.well_index,wf.wf_name from gw_db_schema.well_field as wf,gw_db_schema.water_well as ww,"
						+ "gw_db_schema.operation_wquality_data as wqd where wf.wf_id=ww.wf_id and wqd.well_id=ww.well_id and "
						+ "("+rangeScaller+" <= wqd.fe_ and wqd.fe_ <="+iron+")";
				ResultSet rs=statmnt.executeQuery(sql);
				while(rs.next()){
					 int well_id=rs.getInt(1);
					 double latitude = rs.getDouble(2);
					 double longtude = rs.getDouble(3);
					 String wellIndex = rs.getString(4);
			         String wellField = rs.getString(5);
			         //System.out.println("Well Index By EC "+wellIndex);
			         ValuePair ff= new ValuePair(well_id,latitude,longtude, wellIndex,wellField,"","","");
			         TDSLocation.add(ff);
				}
				conn.close();
				return TDSLocation;
			 }
		//Nitrate
		 public static List<ValuePair>accessBYNitrate(double nitrate) throws SQLException{
			 //System.out.println("EC value in DAO is "+EC);
			 double rangeScaller=0;
			 if(nitrate<=45){
				 rangeScaller=0.00001;
			 }
			 else if(45< nitrate && nitrate <=100){
				 rangeScaller=45.00001;
			 }
			 else if(100< nitrate && nitrate <=240){
				 rangeScaller=100.00001;
			 }
				List<ValuePair> TDSLocation=new ArrayList<ValuePair>(); 
				conn=connection.dbconnection();
				Statement statmnt=conn.createStatement();		
				String sql="select wqd.well_id,wqd.lat,wqd.longt,wqd.well_index,wf.wf_name from gw_db_schema.well_field as wf,gw_db_schema.water_well as ww,"
						+ "gw_db_schema.operation_wquality_data as wqd where wf.wf_id=ww.wf_id and wqd.well_id=ww.well_id and "
						+ "("+rangeScaller+" <= wqd.no3_ and wqd.no3_ <="+nitrate+")";
				ResultSet rs=statmnt.executeQuery(sql);
				while(rs.next()){
					 int well_id=rs.getInt(1);
					 double latitude = rs.getDouble(2);
					 double longtude = rs.getDouble(3);
					 String wellIndex = rs.getString(4);
			         String wellField = rs.getString(5);
			         //System.out.println("Well Index By EC "+wellIndex);
			         ValuePair ff= new ValuePair(well_id,latitude,longtude, wellIndex,wellField,"","","","");
			         TDSLocation.add(ff);
				}
				conn.close();
				return TDSLocation;
			 }
		//Temprature
		 public static List<ValuePair>accessBYTemprature(double temprature) throws SQLException{
			 //System.out.println("EC value in DAO is "+EC);
			 double rangeScaller=0;
			 if(temprature<=37){
				 rangeScaller=0.00001;
			 }
			 else if(37< temprature && temprature <=50){
				 rangeScaller=37.00001;
			 }
			 else if(50< temprature && temprature <=90){
				 rangeScaller=50.00001;
			 }
				List<ValuePair> TDSLocation=new ArrayList<ValuePair>(); 
				conn=connection.dbconnection();
				Statement statmnt=conn.createStatement();		
				String sql="select wqd.well_id,wqd.lat,wqd.longt,wqd.well_index,wf.wf_name from gw_db_schema.well_field as wf,gw_db_schema.water_well as ww,"
						+ "gw_db_schema.operation_wquality_data as wqd where wf.wf_id=ww.wf_id and wqd.well_id=ww.well_id and "
						+ "("+rangeScaller+" <= wqd.temprature and wqd.temprature <="+temprature+")";
				ResultSet rs=statmnt.executeQuery(sql);
				while(rs.next()){
					 int well_id=rs.getInt(1);
					 double latitude = rs.getDouble(2);
					 double longtude = rs.getDouble(3);
					 String wellIndex = rs.getString(4);
			         String wellField = rs.getString(5);
			         //System.out.println("Well Index By EC "+wellIndex);
			         ValuePair ff= new ValuePair(well_id,latitude,longtude, wellIndex,wellField,"","","","","");
			         TDSLocation.add(ff);
				}
				conn.close();
				return TDSLocation;
			 }
}