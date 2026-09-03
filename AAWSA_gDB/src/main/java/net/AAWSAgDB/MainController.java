package net.AAWSAgDB.spring;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.gson.Gson;

import net.AAWSAgDB.fileupload.ValuePair;
import net.AAWSAgDB.fileupload.dao.LoginDao;
import net.AAWSAgDB.fileupload.dao.connection;
import net.AAWSAgDB.fileupload.dao.uploadDao;
import net.AAWSAgDB.fileupload.model.CoordinateUpdate;
import net.AAWSAgDB.fileupload.model.File;
@Controller
public class MainController{
	private static final int MAX_FAILED_ATTEMPTS = 3;
	private static final long LOCK_TIME_DURATION = 24 * 60 * 60 * 1000;	// 24 hours
	String ukn;
    String fname;
	String process;
	String center;
	String logedname;
	String password;
    Connection conn;
    
    @RequestMapping(value="/", method = RequestMethod.GET)
	public ModelAndView visitHome() {
		return new ModelAndView("login");
	}
	  //Spring Security see this :
    @RequestMapping(value = "/login", method = {RequestMethod.GET,RequestMethod.POST})
    public ModelAndView login(@RequestParam(value = "error",required = false) String error,
        @RequestParam(value = "logout", required = false) String logout) {
        ModelAndView model = new ModelAndView();
        if (error!= null) {
        	
            model.addObject("error", "Invalid username or password!"); 
            model.setViewName("login");
        }
        else if (logout==null) {
            model.addObject("error", "Invalid username or password!");  
            model.setViewName("login");
        }
        return model;
    }
    @RequestMapping(value = "/welcome", method = {RequestMethod.GET,RequestMethod.POST})
    public ModelAndView welcomePage() {
        ModelAndView modelAndView = new ModelAndView("welcome");
        modelAndView.addObject("message", "Welcome to the secured application!");
        return modelAndView;
    }
    public static String getpassword(String password){
    	String geneatedpassword=null;
    	try{
    	MessageDigest md= MessageDigest.getInstance("SHA-256");	
    	byte [] bytes=md.digest(password.getBytes());
    	StringBuilder sb=new StringBuilder();
    	for(int i=0;i<bytes.length;i++){
    		sb.append(Integer.toString((bytes[i] & 0xff) + 0x00,16).toString());
    	}
    	geneatedpassword=sb.toString();
    	}
    	catch(NoSuchAlgorithmException ae){
    		ae.printStackTrace();
    	}
    	return geneatedpassword;
    }
    /**
     * Encrypts a raw password using BCryptPasswordEncoder.
     *
     * @param rawPassword The plain-text password to encrypt.
     * @return The resulting BCrypt hashed password (approx. 60 chars long).
     */
    public static String encryptPassword(String rawPassword) {
        // Create an instance of the encoder. The default strength (workload) is 10.
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Encode the password
        String hashedPassword = passwordEncoder.encode(rawPassword);
        return hashedPassword;
    }
    
    @RequestMapping(value = "/home", method = {RequestMethod.GET,RequestMethod.POST})
    public ModelAndView adminPage(HttpServletRequest request,HttpServletResponse response,
    		String fname, String username) throws 
    ServletException, IOException, MessagingException, SQLException{
    	ModelAndView model = new ModelAndView();
    	Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
    	if(authentication!=null && authentication.isAuthenticated()) {
    		Object principal=authentication.getPrincipal();
    		if(principal instanceof UserDetails) {
    			username= ((UserDetails) principal).getUsername();
    		}
    		else {
    			username=principal.toString();
    		}
    		conn=connection.dbconnection();
    		String sqlch="select * from gw_db_schema.db_user where username='"+username+"'";
    		Statement stmnt=conn.createStatement();
    		ResultSet rs=stmnt.executeQuery(sqlch);
    		if(rs.next()){
    			fname=rs.getString("fullname");
    		}
    		conn.close();
 		List<File> usertype=new ArrayList<File>();
 		List<File>loginid=new ArrayList<File>();
 		List<File> geo_techfile = new ArrayList<File>();
 		List<File>cnter_name=new ArrayList<File>();
 		List<File> project_Overlay=new ArrayList<File>();
 		List<File>legend=new ArrayList<File>();
 		HttpSession session= request.getSession(false);
			try {
				loginid.addAll(LoginDao.validatecor(fname, username));
				usertype.addAll(LoginDao.usertype());
				cnter_name.addAll(uploadDao.center_name());
				project_Overlay.addAll(LoginDao.access_well_name());
				legend.addAll(LoginDao.access_groupingBy_proType());
			} catch (Throwable ee) {
				ee.printStackTrace();	
			}
			conn=connection.dbconnection();
			String sqlAccess="select us.vern_code,at.authority from gw_db_schema.db_user as us,gw_db_schema.authorities as at where us.id=at.user_id and at.username="
					+ "'"+username+"'";
    		Statement stmntA=conn.createStatement();
    		ResultSet rsA=stmntA.executeQuery(sqlAccess);
    		if(rsA.next()){
    			String pr_code=rsA.getString(1);
    			String centerPr=rsA.getString(2);
    			if(pr_code.equals("110")){//Administrator
    				model.addObject("data1", usertype);
    				if(session!=null){
    					session.setAttribute("U_id1", LoginDao.UserLo);
    					session.setAttribute("pr_typ",centerPr);
    					session.setAttribute("U_auto", fname);
    					}
    				model.setViewName("privilege_confirm");	
    			}
    			else if(pr_code.equals("120")){//Database User
    				if(session!=null){
    					session.setAttribute("U_id1", LoginDao.UserLo);
    					session.setAttribute("U_auto", fname);
    					}
    				    model.addObject("gr_pro", cnter_name);
    				    model.addObject("data", geo_techfile);
    				    model.setViewName("hompage");
    			}
    			else if(pr_code.equals("123")){//Common User
    				if(session!=null){
    			session.setAttribute("U_auto", fname);	
    			session.setAttribute("userid", username);
    			session.setAttribute("U_lname", center);	
    				}
    			model.addObject("data1", usertype);
    			model.addObject("U_auto", fname);
    			model.addObject("georef",project_Overlay);
    			model.addObject("legend",legend);
    			model.setViewName("corporateU");
    			}
    			else if(pr_code.equals("0")){
    				if(session!=null){
    					session.setAttribute("U_id1", LoginDao.UserLo);	
    				}
    				model.setViewName("passwordM");
    				}
    			conn.close();
    		}
    	}
        return model;
    }
	@RequestMapping(value = "/pre_authenticate", method = RequestMethod.POST)
	 public ModelAndView sendMail(HttpServletRequest request,HttpServletResponse response, String[] recipients, 
			 String[] bccRecipients, String subject, String message) 
					 throws ServletException, IOException { 
	ModelAndView retmodel= new ModelAndView();
	List<File> usertype=new ArrayList<File>();
	   usertype.addAll(LoginDao.usertype());
		recipients = request.getParameterValues("U_email");
		System.out.println("get email from user= "+recipients);
		String usertype1=request.getParameter("usertype");
		System.out.println("User Types is= "+usertype1);
		
	        try {  	
			    bccRecipients = new String[]{"dosty2020@outlook.com"};  
			    subject = "Hi this is test Mail"; 
			    int maximum=1000,minimum=1;
			    String plaintext= "c0de"+String.valueOf(minimum + (int)(Math.random() * ((maximum - minimum) + 1)));
			    String messageBody = plaintext;  
			    String host="172.20.4.10";
			final String FROM_ADDRESS="HGSuperAdmin@ecdswc.com.et";
			    final String PASSWORD="Admin@Passc9de";
			    String FROM_NAME="Atnaf Abreham";
		    	Properties props = new Properties();  
	            props.put("mail.smtp.host", host); 
	            props.put("mail.smtp.port","465");
	            props.put("mail.smtp.auth", "true");   
	            props.put("mail.smtp.ssl.trust", host);
	            props.put("mail.smtp.starttls.enable","true" );
	            Session session1 = Session.getInstance(props, new javax.mail.Authenticator(){
	            	protected PasswordAuthentication getPasswordAuthentication() {
	    				return new PasswordAuthentication(
	    					FROM_ADDRESS,PASSWORD);
	    			     }
	            }); 
	            session1.setDebug(true);
	            Message msg = new MimeMessage(session1);  
	            InternetAddress from = new InternetAddress(FROM_ADDRESS, FROM_NAME);  
	            msg.setFrom(from);  
	            InternetAddress[] toAddresses = new InternetAddress[recipients.length];  
	            for (int i = 0; i < recipients.length; i++) {  
	                toAddresses[i] = new InternetAddress(recipients[i]);  
	            }
	            msg.setRecipients(Message.RecipientType.TO, toAddresses); 
	            InternetAddress[] bccAddresses = new InternetAddress[bccRecipients.length];  
	            for (int j = 0; j < bccRecipients.length; j++) {  
	                bccAddresses[j] = new InternetAddress(bccRecipients[j]);  
	            }  
	            msg.setRecipients(Message.RecipientType.BCC, bccAddresses);  
	            msg.setSubject(subject);
	            if(usertype1.equals("22")){
	            msg.setContent(messageBody, "text/plain");  
	            //retmodel.addObject("eluser", "Privileg code is sended Successfully by Company mail for Elite User!!");
	           // retmodel.setViewName("privilege_confirm");
	            
	            }
	            else{
	           msg.setContent("You are succefully Previlaged !!", "text/plain"); 
	           //retmodel.addObject("coruser", "You are succefully Previlaged !!");
	           // retmodel.setViewName("privilege_confirm");
	            }
	        	Transport.send(msg, msg.getAllRecipients());
	        } catch (UnsupportedEncodingException ex) {  
	        	Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex); 
	        	 retmodel.addObject("error","mail.ecdswc.com.et is not functional for now" );
		            retmodel.setViewName("privilege_confirm");
	  
	        } catch (MessagingException ex) {  
	        	Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);  
	        	 retmodel.addObject("error", "mail.ecdswc.com.et is not functional for now");
		           retmodel.setViewName("privilege_confirm");
	        } 
	        return retmodel;
	    }
	//create Elite User or Admin user
	@RequestMapping(value = "/userInformation", method = RequestMethod.POST)
	 public ModelAndView elit_super(HttpServletRequest request,HttpServletResponse response,String[] bccRecipients, String subject, 
			 String message,String[] recipients)throws ServletException, IOException { 
	ModelAndView retmodel= new ModelAndView();
	List<File> usertype=new ArrayList<File>();
	   usertype.addAll(LoginDao.usertype());
	   String fullName = request.getParameter("fullName");
	   recipients = request.getParameterValues("userEmail");
	   String phone = request.getParameter("phone");
	   String userName = request.getParameter("userName");
	   //String passwordSalt=getpassword(request.getParameter("password"));
	   String passW=request.getParameter("password");
	   String passwordsalted=encryptPassword(passW);
	   
	   String userType = request.getParameter("userType");
	   String authorization="";
	   String uesrEmail=recipients[0];
	   int verCode=0;
	   int authorizeUser=0;
		try {
		conn=connection.dbconnection();
		Statement stmnt=conn.createStatement();
		String checkuser="select count(*) from gw_db_schema.db_user where username='"+userName+"'";
		ResultSet rs=stmnt.executeQuery(checkuser);
		rs.next();
		int checkUsername=rs.getInt(1);
		if(checkUsername>0){
			retmodel.addObject("existes1", "The User, "+userName+ " is Already Registered In Repository!!");
			retmodel.addObject("data1", usertype);
			retmodel.setViewName("privilege_confirm");	
			}
			else{
				String queryInsert="insert into gw_db_schema.db_user(fullname,email,phone,username,password,orgname,usertype,vern_code,enabled)"
						+ "values(?,?,?,?,?,'AAWSA',?,?,'true')";
				PreparedStatement prepStatment=conn.prepareStatement(queryInsert);
				prepStatment.setString(1, fullName);
				if(uesrEmail.equals("0") || uesrEmail.equals("non")|| uesrEmail.equals("None")||uesrEmail.equals("none")||uesrEmail.equals("")) {
					prepStatment.setNull(2,java.sql.Types.VARCHAR);
				}else {
					prepStatment.setString(2, uesrEmail);	
				}
				prepStatment.setString(3, phone);
				prepStatment.setString(4, userName);
				prepStatment.setString(5, passwordsalted);
				prepStatment.setString(6, userType);
				prepStatment.setInt(7, verCode);	
				prepStatment.executeUpdate();
				Statement stmntauth=conn.createStatement();
				String checkauthorized="select id from gw_db_schema.db_user where username='"+userName+"'";
				ResultSet rsauthorized=stmntauth.executeQuery(checkauthorized);
				while( rsauthorized.next()) {
					authorizeUser=rsauthorized.getInt(1);
				}
					String queryInsertAuth="insert into gw_db_schema.authorities(user_id,authority,username)values(?,?,?)";
					PreparedStatement prepStatmentAuth=conn.prepareStatement(queryInsertAuth);
					prepStatmentAuth.setInt(1, authorizeUser);
					if(userType.equals("Administrator")) {
						authorization="ADMIN";
						prepStatmentAuth.setString(2, authorization);	
					}else {
						authorization="USER";
						prepStatmentAuth.setString(2, authorization);	
					}
					prepStatmentAuth.setString(3, userName);	
					prepStatmentAuth.executeUpdate();
				
				retmodel.addObject("preved1", "The User, "+userName+" is Created In Your AAWSA gDB Repository!");
				retmodel.addObject("data1", usertype);
				retmodel.setViewName("privilege_confirm");		
			}	
		conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*
	        try {  	
			    bccRecipients = new String[]{"dosty2020@outlook.com"};  
			    subject = "Hi this is test Mail"; 
			    String host="172.20.4.10";
			final String FROM_ADDRESS="HGSuperAdmin@aawsa.com.et";
			    final String PASSWORD="Admin@Passc9de";
			    String FROM_NAME="Atnaf Abreham";
		    	Properties props = new Properties();  
	            props.put("mail.smtp.host", host); 
	            props.put("mail.smtp.port","465");
	            props.put("mail.smtp.auth", "true");   
	            props.put("mail.smtp.ssl.trust", host);
	            props.put("mail.smtp.starttls.enable","true" );
	            Session session1 = Session.getInstance(props, new javax.mail.Authenticator(){
	            	protected PasswordAuthentication getPasswordAuthentication() {
	    				return new PasswordAuthentication(
	    					FROM_ADDRESS,PASSWORD);
	    			     }
	            }); 
	            session1.setDebug(true);
	            Message msg = new MimeMessage(session1);  
	            InternetAddress from = new InternetAddress(FROM_ADDRESS, FROM_NAME);  
	            msg.setFrom(from);  
	            InternetAddress[] toAddresses = new InternetAddress[recipients.length];  
	            for (int i = 0; i < recipients.length; i++) {  
	                toAddresses[i] = new InternetAddress(recipients[i]);  
	            }
	            msg.setRecipients(Message.RecipientType.TO, toAddresses); 
	            InternetAddress[] bccAddresses = new InternetAddress[bccRecipients.length];  
	            for (int j = 0; j < bccRecipients.length; j++) {  
	                bccAddresses[j] = new InternetAddress(bccRecipients[j]);  
	            }  
	            msg.setRecipients(Message.RecipientType.BCC, bccAddresses);  
	            msg.setSubject(subject);
	           msg.setContent("You are succefully Registered !!", "text/plain"); 
	        	Transport.send(msg, msg.getAllRecipients());
	        } catch (UnsupportedEncodingException ex) {  
	        	Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex); 
	        	 retmodel.addObject("error1","mail.aawsa.com.et is not configured for now" );
		            retmodel.setViewName("privilege_confirm");
	  
	        } catch (MessagingException ex) {  
	        	Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);  
	        	 retmodel.addObject("error1", "mail.aawsa.com.et is not configured for now");
		           retmodel.setViewName("privilege_confirm");
	        }*/ 
	        return retmodel;
	    }
	//checking Email and Registration
	//check email if exists
	private static boolean contractructorEmailExists(Connection connection, String orgName) throws Exception {
        String queryemailCkeck = "SELECT COUNT(*) FROM gw_db_schema.db_user WHERE SIMILARITY(orgname,'"+orgName+"')>0.75";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryemailCkeck)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        }
        return false;
    }
// Contractor Registration 
    private static void addContractorProfile(Connection connection,String OrgName,String email,String phoneN,String userType,String wellCode)throws Exception {
        String query = "INSERT INTO gw_db_schema.db_user(orgname,email,phone,usertype,wellindex)VALUES (?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, OrgName);
            if(email.equals("0") || email.equals("non")|| email.equals("None")||email.equals("none")||email.equals("")|| email.equals("NA")) {
            	preparedStatement.setNull(2,java.sql.Types.VARCHAR);
			}else {
				preparedStatement.setString(2, email);	
			}
            preparedStatement.setString(3, phoneN);
            preparedStatement.setString(4, userType);
            preparedStatement.setString(5, wellCode);
            preparedStatement.executeUpdate();
            System.out.println("User registered: " + email);
        }
    }
  //check Consultant email if exists
  	private static boolean consultantEmailExists(Connection connection, String orgName) throws Exception {
          String queryemailCkeck = "SELECT COUNT(*) FROM gw_db_schema.db_user WHERE SIMILARITY(orgname,'"+orgName+"')>0.75";
          try (PreparedStatement preparedStatement = connection.prepareStatement(queryemailCkeck)) {
              ResultSet resultSet = preparedStatement.executeQuery();
              if (resultSet.next()) {
                  return resultSet.getInt(1) > 0;
              }
          }
          return false;
      }
  //Registration of Consultant
    private static void addConsultantProfile(Connection connection,String OrgName,String email,String phoneN,String userType,String wellCode)throws Exception {
        String query = "INSERT INTO gw_db_schema.db_user(orgname,email,phone,usertype,wellindex)VALUES (?,?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, OrgName);
            if(email.equals("0") || email.equals("non")|| email.equals("None")||email.equals("none")||email.equals("")|| email.equals("NA")) {
            	preparedStatement.setNull(2,java.sql.Types.VARCHAR);
			}else {
				preparedStatement.setString(2, email);	
			}
            preparedStatement.setString(3, phoneN);
            preparedStatement.setString(4, userType);
            preparedStatement.setString(5, wellCode);
            preparedStatement.executeUpdate();
            System.out.println("User registered: " + email);
        }
    }
    //check lat long inDB
    private static void checkLatLongInDB(Connection conn ,List<CoordinateUpdate>updates){
    	try {
			conn.setAutoCommit(false);
         String updateSql = "UPDATE gw_db_schema.geo_location SET easting = ?,northing=? WHERE lat = ? AND longt = ?";
         try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
             for (CoordinateUpdate item : updates) {
                 updatePstmt.setDouble(1, item.getUpdateEastingValue());
                 updatePstmt.setDouble(2, item.getUpdateNorthingValue());
                 updatePstmt.setDouble(3, item.getLatitude());
                 updatePstmt.setDouble(4, item.getLongitude());
                 updatePstmt.addBatch();
             }
             // Execute the batch update
             int[] updateCounts = updatePstmt.executeBatch();
             
             // You can check updateCounts to see which rows were updated.
             // Rows that were not updated (count 0) might need an INSERT operation if that's desired (an UPSERT logic).

             // Commit the transaction
             conn.commit();
    	 } catch (SQLException e) {
             // Rollback on error
    		 e.printStackTrace();
    		 //conn.rollback();
             
         }
    	} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
  //calculate and compare if sheet name are equivalent
    public static double calculateSimilarity(String s1, String s2) {
        // Normalize the strings (optional: convert to lowercase, remove extra spaces, etc.)
        s1 = s1.toLowerCase().trim();
        s2 = s2.toLowerCase().trim();

        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 1.0; // Both empty, so 100% similar
        }

        // Calculate the Levenshtein distance
        LevenshteinDistance levenshtein = new LevenshteinDistance();
        int distance = levenshtein.apply(s1, s2);

        // Calculate similarity percentage: (Max Length - Distance) / Max Length
        double similarity = (double) (maxLength - distance) / maxLength;
        return similarity;
    }
	// file upload
	    @ResponseBody
	  	@RequestMapping(value = "/Upload", method = RequestMethod.POST)
	  	public ModelAndView uploadfile(MultipartHttpServletRequest request, HttpServletResponse response,
	  			RedirectAttributes redirectAttributes)
	  			throws Throwable {
	    	ModelAndView modelview = new ModelAndView();
	  		List<File> files = new ArrayList<File>();
	    	//northHemespher=true;
	    	String wellIndex= request.getParameter("wellIndex");
	  		if(wellIndex!=null) {
	  			int db_userIDI=0;
		    	if(request.getParameter("db_userId")=="") {
		    		db_userIDI=0;
		    	}else {
		    		db_userIDI=Integer.parseInt(request.getParameter("db_userId"));
		    		System.out.println("Who is the User"+ db_userIDI);
		    	}
		    	 int gwDataId;
		    	 if(request.getParameter("gwDataname")==null) {
		    		 gwDataId=0;
		    	 }
		    	 else {
		    		 gwDataId=Integer.parseInt(request.getParameter("gwDataname"));
		    	 }
			  		String wellCode;
			  		if(request.getParameter("wellCode")==null) {
			  			wellCode="Well code value is Null";
			  		}
			  		else {
			  			wellCode=request.getParameter("wellCode");
			  		}
		  		 int waterWellLocation;
		  		 if(request.getParameter("waterWellLocation")==null){
		  			waterWellLocation=0;
		  		 }
		  		else{
		  			waterWellLocation=Integer.parseInt(request.getParameter("waterWellLocation")); 
			  		 }
		  		 
		  		 int regionCityID;
		  		 if(request.getParameter("regionCityID")==null) {
		  			regionCityID=0; 
		  		 }
		  		 else {
		  			regionCityID=Integer.parseInt(request.getParameter("regionCityID"));
		  			 }
		  		 
		  		 int subcityzoneId;
		  		 if(request.getParameter("subcityzoneId")==null){
		  			subcityzoneId=0; 
		  		 }
		  		 else {
		  			subcityzoneId=Integer.parseInt(request.getParameter("subcityzoneId"));	 
		  		 }
		  		//woreda and local name information
		  		String woredaName;
		  		if(request.getParameter("woredaName")==null) {
		  			woredaName="";	
		  		}else {
		  			woredaName=request.getParameter("woredaName");	
		  		}
		  		String localName;
		  		if(request.getParameter("localName")==null) {
		  			localName="";	
		  		}else {
		  			localName=request.getParameter("localName");	
		  		}
		  		int wellFieldId;
		  		if(request.getParameter("wellField")==null) {
		  			wellFieldId=0;
		  		}
		  		else {
		  			wellFieldId=Integer.parseInt(request.getParameter("wellField"));
		  		}
		  		String geoLocationId[]; double laty = 0.0,lonx = 0.0,elvation = 0.0;
		  		if(request.getParameterValues("geoLocationId")==null){
		  			geoLocationId= new String[]{"0"};
		  			laty=0.0;
		  			lonx=0.0;
		  			elvation=0.0;
		  		}else{
		  			geoLocationId=request.getParameterValues("geoLocationId")[0].split(",");
		  				laty=Double.parseDouble(geoLocationId[0]);
			  			lonx=Double.parseDouble(geoLocationId[1]);	
		  			elvation=Double.parseDouble(geoLocationId[2]);
		  		}
		  		String wellOwnerCata;
		  		if(request.getParameter("owendByName")==null) {
		  			wellOwnerCata="";
		  		}
		  		else {
		  			wellOwnerCata=request.getParameter("owendByName");	
		  		}
		  		String ownerName[];
		  		String Ownemail=null,Ownphone=null,ownOrgName=null;
		  		if(request.getParameterValues("owendBy")==null){
		  			ownerName=new String[]{"Value for Owner name is Null"};
		  		}
		  		else{ownerName=request.getParameterValues("owendBy")[0].split(",");
		  		ownOrgName=ownerName[0];
		  		Ownemail=ownerName[1];
		  		Ownphone=ownerName[2];
		  		}
		  		 String constraction_date;
		  		 if(request.getParameter("constDate")==null){
		  			constraction_date="1970-01-01"; 
			  		 } 
		  		 else{
		  			constraction_date = request.getParameter("constDate");
			  		 }
		  		java.util.Date dateraw= new SimpleDateFormat("yyyy-MM-dd").parse(constraction_date);
		  		java.sql.Date wellConstractionDate= new java.sql.Date(dateraw.getTime());
		  		String wellType;
		  		if(request.getParameter("wellType")==null) {
		  			wellType="Value of well type is Null";
		  		}
		  		else {
		  			wellType=request.getParameter("wellType");
		  		}
		  	//Drilling Permit
		  		String drillingPermit;
		  		if(request.getParameter("drillingPermit")==null) {
		  			drillingPermit="";
		  		}
		  		else {
		  			drillingPermit=request.getParameter("drillingPermit");
		  		}
		  		double wellDepth;
		  		if(request.getParameter("wellDethName")==null) {
		  			wellDepth=0.0;
		  		}else {
		  			wellDepth=Double.parseDouble(request.getParameter("wellDethName"));	
		  		}
		  		double SpecificCapacity;
		  		if(request.getParameter("waterLevelName")==null) {
		  			SpecificCapacity=0.0;
		  		}
		  		else {
		  			SpecificCapacity=Double.parseDouble(request.getParameter("waterLevelName"));
		  		}
		  	//will Expanded.....
		  		double largeCasingID,telescopCasingID;
		  		if(request.getParameter("largeCasingName")==null) {
		  			largeCasingID=0.0;
		  		}
		  		else {
		  			largeCasingID=Double.parseDouble(request.getParameter("largeCasingName"));
		  		}
		  		if(request.getParameter("telescopedCasingName")==null) {
		  			telescopCasingID=0.0;
		  		}else {
		  			telescopCasingID=Double.parseDouble(request.getParameter("telescopedCasingName"));
		  		}
		  		
		  	//Casing Material
		  		String casingMaterName;
		  		if(request.getParameter("casingMaterName")==null) {
		  			casingMaterName="";
		  		}
		  		else {
		  			casingMaterName=request.getParameter("casingMaterName");
		  		}
		  		String mainAqiefer;
		  		if(request.getParameter("mainAqiefer")==null) {
		  			mainAqiefer="Value of main acquifer is Null";
		  		}
		  		else {
		  			mainAqiefer=request.getParameter("mainAqiefer");
		  		}
		  		String wellStatus;
		  		if(request.getParameter("wellStatus")==null) {
		  			wellStatus="";
		  		}
		  		else {
		  			wellStatus=request.getParameter("wellStatus");
		  		}
		  		double yieldLs=0,SWL=0,DWL=0;
		  		
		  		if(request.getParameter("yieldLs").isEmpty()) {
		  			yieldLs= 0.0;
		  		}
		  		else {
		  			yieldLs=Double.parseDouble(request.getParameter("yieldLs"));
		  			/*
		  			pumpHandPName11=request.getParameterValues("pumpHeadName")[0].split(",");
		  			double[] doubleArray = new double[pumpHandPName11.length];
		  			for (int i = 0; i < pumpHandPName11.length; i++){
		  			    doubleArray[i] = Double.parseDouble(pumpHandPName11[i]);
		  			}
		  			int halfArraylength=doubleArray.length/2;
		  			pumpHeadName=new double[halfArraylength];
		  			pumpDischargeR=new double[halfArraylength];
		  			
		  			System.arraycopy(doubleArray, 0, pumpHeadName, 0, halfArraylength);
		  	        System.arraycopy(doubleArray, halfArraylength, pumpDischargeR, 0, halfArraylength);
				*/
				}
		  		if(request.getParameter("SWLName").isEmpty()) {
		  			SWL= 0.0;
		  		}
		  		else {
		  			SWL=Double.parseDouble(request.getParameter("SWLName"));
		  		}
		  		if(request.getParameter("DWLName").isEmpty()) {
		  			DWL= 0.0;
		  		}
		  		else {
		  			DWL=Double.parseDouble(request.getParameter("DWLName"));
		  		}
		  		
		  		//Abandoned or Productive Well data parameters from user
		  		InputStream fileStream=null;
				String filetype = "";
				long fileSize = 0;
				String fileName = "";
				String wellCellIndex = null,wellCellContCN = null,wellCellContemail = null,wellCellContPhone = null,wellCellConsultCN = null,wellCellConsulemail = null,
				wellCell_consultPhone = null,locationType=null, wellIndexfromDB=null;
				double excelLong=0,excelLat=0,excelEasting=0,excelNorthing=0,longt=0,lat=0,northingDB=0,eastingDB = 0;
				int geolocationIDDB=0, wellCell_idfromDB=0;
				LocalDateTime recordDate;
				List<MultipartFile> productiveData = request.getFiles("productiveWelldata"); 
				if(productiveData.isEmpty()|| productiveData==null) {
					productiveData=new ArrayList<>();
				}
				List<MultipartFile> abandata = request.getFiles("abandata");
				if(abandata.isEmpty()||abandata==null) {
					abandata=new ArrayList<>();
				}
				List<MultipartFile>mergaeTwofiles = new ArrayList<>();
				if(productiveData.size()>0) {
					mergaeTwofiles.addAll(productiveData);	
				}
				else if (abandata.size()>0) {
					mergaeTwofiles.addAll(abandata);
				}
				List<MultipartFile> productivewerrRepo = request.getFiles("reportProWell");
				if(productivewerrRepo.isEmpty()|| productivewerrRepo==null) {
					productivewerrRepo=new ArrayList<>();
				}
				List<MultipartFile> abanWellReport = request.getFiles("abanWellreport");
				if(abanWellReport.isEmpty()||abanWellReport==null) {
					abanWellReport=new ArrayList<>();
				}
				List<MultipartFile>mergaeTwoReportfiles = new ArrayList<>();
				if(productivewerrRepo.size()>0) {
					mergaeTwoReportfiles.addAll(productivewerrRepo);	
				}
				else if (abanWellReport.size()>0) {
					mergaeTwoReportfiles.addAll(abanWellReport);
				}
				
				//handle check box from user form
				List<MultipartFile> pumpingData=null;
				String reportDocCheck[];
				if(request.getParameterValues("reportCheck")==null) {
					reportDocCheck=new String[] {"CHeck List Undefined"};
				}else {
					reportDocCheck=request.getParameterValues("reportCheck");
				}
				List<String>checkReportList= Arrays.asList(reportDocCheck);
				String gwExcel_checkwellReport[];
				if(request.getParameterValues("checkwellReport")==null){
					gwExcel_checkwellReport= new String[]{"Undefined"};
				}else{
					gwExcel_checkwellReport=request.getParameterValues("checkwellReport");
				}
				List<String>checkList= Arrays.asList(gwExcel_checkwellReport);
				
				String gwExcel_checkwellPumping[];
				List<String> checkListforPump=null;
		  		String proWellPowerId = ""; 
		  		double genePowerMeasureId = 0.00,pumpCapName=0.00,pumpPositionName=0.00,PumpHead=0,dischargeRate = 0;
		  		String wellAbanReason = "",availableGId = "",generatorStatusId = "",SCADAId = "",SCADAStatusId = "",
		  				enameCompany="",enameEmail="",enamePhone="",productiveWellCond="",functioningwellCon="",pumpStatusName="";
		  		String reportedCompany="",reportedEmail="",reportedPhone="",enameName="";
		  		String sealedYN="";
		  		java.sql.Date abandonSealedDate = null,pumpInstalledDate = null,pumpReplacedDate = null;
		  		if(wellStatus.equals("Abandoned")) {
		  			if(request.getParameter("aReason")=="") {
			  			wellAbanReason="";
			  		}
			  		else {
			  			wellAbanReason=request.getParameter("aReason");
					}
		  			String reportedBy[];
			  		if(request.getParameterValues("reported_by")== null) {
			  			reportedBy = new String[]{"Value Reported By is Null"};
			  		}
			  		else {
			  			reportedBy=request.getParameterValues("reported_by")[0].split(",");
			  			reportedCompany=reportedBy[0];
			  			reportedEmail=reportedBy[1];
			  			reportedPhone=reportedBy[2];
					}
			  		if(request.getParameter("sealed_by")=="") {
			  			sealedYN="";
			  		}
			  		else {
			  			sealedYN=request.getParameter("sealed_by");
					}
			  		String enumName[];
			  		if(request.getParameterValues("enumNameAban")==null) {
			  			enumName=new String[]{"Enumerator Value is Null"};
			  		}
			  		else {
			  			enumName=request.getParameterValues("enumNameAban")[0].split(",");
			  			enameCompany=enumName[0];
			  			enameName=enumName[1];
			  			enameEmail=enumName[2];
			  			enamePhone=enumName[3];
					}
			  		
			  		
			  		String sealedDate;
			  		 if(request.getParameter("sealedDate")==""){
			  			sealedDate="1970-01-01"; 
				  		 } 
			  		 else{
			  			sealedDate = request.getParameter("sealedDate");
				  		 }
			  		java.util.Date convertRDate= new SimpleDateFormat("yyyy-MM-dd").parse(sealedDate);
			  		abandonSealedDate= new java.sql.Date(convertRDate.getTime());
		  		}
		  		//productive well parameter
		  		else {
		  			if(request.getParameter("pumpStatusName")=="") {
			  			pumpStatusName="";
			  		}
			  		else {
			  			pumpStatusName=request.getParameter("pumpStatusName");
					}
			  		if(request.getParameter("pumpCapacityName").isEmpty()||request.getParameter("pumpCapacityName")==null) {
			  			pumpCapName=0.0;
			  		}
			  		else {
			  			pumpCapName=Double.parseDouble(request.getParameter("pumpCapacityName"));
					}
			  		java.util.Date convertInDate;
			  		String installedDateStr=request.getParameter("installedDate");
			  		 if(installedDateStr!=null && !installedDateStr.isEmpty()){
			  			convertInDate= new SimpleDateFormat("yyyy-MM-dd").parse(installedDateStr);
			  			pumpInstalledDate= new java.sql.Date(convertInDate.getTime());
				  		 } 
			  		 else{
			  			convertInDate= new SimpleDateFormat("yyyy-MM-dd").parse("1970-01-01");
			  			pumpInstalledDate= new java.sql.Date(convertInDate.getTime());
				  		 }
			  		java.util.Date convertRepDate;
			  		String pumpRepdate =request.getParameter("pumpReplacedDate");
			  		 if(pumpRepdate!=null && !pumpRepdate.isEmpty()){
			  			convertRepDate= new SimpleDateFormat("yyyy-MM-dd").parse(pumpRepdate);
				  		 } else {
				  			convertRepDate= new SimpleDateFormat("yyyy-MM-dd").parse("1970-01-01"); 
				  		 }
			  		     pumpReplacedDate= new java.sql.Date(convertRepDate.getTime());
			  
			  		 if(request.getParameter("pumpHead").isEmpty()|| request.getParameter("pumpHead")==null){
			  			PumpHead=0.0; 
				  		 } 
			  		 else{
			  			PumpHead = Double.parseDouble(request.getParameter("pumpHead"));
				  		 }
			  		 
			  		if(request.getParameter("dischargeRate").isEmpty()) {
			  			dischargeRate=0.0;
			  		}
			  		else {
			  			dischargeRate=Double.parseDouble(request.getParameter("dischargeRate"));
			  		}
			  		
			  		
			  		if(request.getParameter("pumPosition").isEmpty()) {
			  			pumpPositionName=0.0;
			  		}else {
			  			pumpPositionName= Double.parseDouble(request.getParameter("pumPosition"));
			  		}
			  		if(request.getParameter("powerName")=="") {
			  			proWellPowerId="";
			  		}
			  		else {
			  			proWellPowerId=request.getParameter("powerName");
					}
			  		if(request.getParameter("genAvaiName")=="") {
			  			availableGId="";
			  		}
			  		else {
			  			availableGId=request.getParameter("genAvaiName");
					}
			  		if(request.getParameter("genStatName")=="") {
			  			generatorStatusId="";
			  		}
			  		else {
			  			generatorStatusId=request.getParameter("genStatName");
					}
			  		if(request.getParameter("genPowerName").isEmpty()){
			  			genePowerMeasureId=0.0;
			  		}
			  		else {
			  			genePowerMeasureId=Double.parseDouble(request.getParameter("genPowerName"));
					}
			  		if(request.getParameter("scadaName")=="") {
			  			SCADAId="";
			  		}
			  		else {
			  			SCADAId=request.getParameter("scadaName");
					}
			  		if(request.getParameter("scadaSName")=="") {
			  			SCADAStatusId="";
			  		}
			  		else {
			  			SCADAStatusId=request.getParameter("scadaSName");
					}
			  		String enumName[];
			  		if(request.getParameterValues("enumName")==null) {
			  			enumName=new String[]{"Enumerator Value is Null"};
			  		}
			  		else {
			  			enumName=request.getParameterValues("enumName")[0].split(",");
			  			enameCompany=enumName[0];
			  			enameName=enumName[1];
			  			enameEmail=enumName[2];
			  			enamePhone=enumName[3];
					}
			  		
			  		if(request.getParameter("prodwellStatus")=="") {
			  			productiveWellCond="";
			  		}
			  		else {
			  			productiveWellCond=request.getParameter("prodwellStatus");
					}
			 
			  		if(request.getParameter("wellCondtionName")=="") {
			  			functioningwellCon="";
			  		}
			  		else {
			  			functioningwellCon=request.getParameter("wellCondtionName");
					}
			  	//pumping system
					if(request.getParameterValues("pumpingCheck")==null){
						gwExcel_checkwellPumping= new String[]{"Undefined"};
					}else{
						gwExcel_checkwellPumping=request.getParameterValues("pumpingCheck");
					}
					checkListforPump= Arrays.asList(gwExcel_checkwellPumping);
			  		pumpingData=request.getFiles("pumpingSdata");
			  		if(pumpingData.isEmpty()||pumpingData==null) {
			  			pumpingData=new ArrayList<>();
			  		}
		  		}
		   		//Inserting Parameters to database manually
		  		int localNameId=0,storedWoredaID;
		  		if (subcityzoneId!= 0) {
		  			try {
	  				conn = connection.dbconnection();
	  				//check if worada is already present in the database
	  				String queryCheckWoreda = "select wo.wr_id from gw_db_schema.woreda as wo,gw_db_schema.sub_city_zone as scz,"
					+ "gw_db_schema.region_class as rcC,gw_db_schema.region_city as rc where scz.scz_id=wo.scz_id and scz.cl_id=rcC.cl_id and "
					+ "rcC.reg_id=rc.reg_id and SIMILARITY(wo.wor_name, '"+woredaName+"')>0.75  and scz.scz_id="+subcityzoneId+" and "
					+ "rcC.cl_id="+regionCityID+" and rc.reg_id="+waterWellLocation+"";
	  				Statement stmtcheckWoreda = conn.createStatement();
	  				ResultSet rsCheckWoreda = stmtcheckWoreda.executeQuery(queryCheckWoreda);
	  		    if (rsCheckWoreda.next()){
	  				storedWoredaID=rsCheckWoreda.getInt(1);
	  				//System.out.println("Woreda ID Is "+storedWoredaID);
		        String querycheckLocal="select lN.local_id from gw_db_schema.local_name as lN,gw_db_schema.woreda as wo,gw_db_schema.sub_city_zone as scz,"
				+ "gw_db_schema.region_class as rcC,gw_db_schema.region_city as rc where scz.scz_id=wo.scz_id and scz.cl_id=rcC.cl_id and "
				+ "rcC.reg_id=rc.reg_id and lN.wr_id=wo.wr_id and SIMILARITY(lN.local_name,'"+localName+"')>0.75 and lN.wr_id="+storedWoredaID+""
				+ " and scz.scz_id="+subcityzoneId+" and rcC.cl_id="+regionCityID+" and rc.reg_id="+waterWellLocation+"";
				Statement stmtcheckLocal = conn.createStatement();
				ResultSet rscheckLocal = stmtcheckLocal.executeQuery(querycheckLocal);
		        if(rscheckLocal.next()){
		        	localNameId=rscheckLocal.getInt(1);
		        	System.out.println("Local Id "+localNameId+ " is avialable in the databes with "+localName+" local name");
		        }
		        else {
			PreparedStatement prestatmentLocalName = conn.prepareStatement("insert into gw_db_schema.local_name(wr_id,local_name)values("+storedWoredaID+","
			+ "'"+localName+"')");
			prestatmentLocalName.executeUpdate();
	        String queryextractLocal = "select lN.local_id from gw_db_schema.local_name as lN,gw_db_schema.woreda as wo,gw_db_schema.sub_city_zone as scz,"
			+ "gw_db_schema.region_class as rcC,gw_db_schema.region_city as rc where scz.scz_id=wo.scz_id and scz.cl_id=rcC.cl_id and "
			+ "rcC.reg_id=rc.reg_id and lN.wr_id=wo.wr_id and lN.local_name='"+localName+"' and wo.wr_id=" +storedWoredaID +" and "
			+ "scz.scz_id="+subcityzoneId+" and rcC.cl_id="+regionCityID+" and rc.reg_id="+waterWellLocation+"";
			Statement stmtextractlocalId = conn.createStatement();
			ResultSet rsextractLocalId = stmtextractlocalId.executeQuery(queryextractLocal);
	        while(rsextractLocalId.next()){
	        	localNameId=rsextractLocalId.getInt(1);
	        }}
	  		}else {
			PreparedStatement prestatmentWoreda = conn.prepareStatement("insert into gw_db_schema.woreda(scz_id,wor_name)values("+subcityzoneId+",'" +woredaName + "')");
	        prestatmentWoreda.executeUpdate();
		PreparedStatement prestatmentLocalName = conn.prepareStatement("insert into gw_db_schema.local_name(wr_id,local_name)select wr_id,'"+localName+"' from "
		+ "gw_db_schema.woreda as wo,gw_db_schema.sub_city_zone as scz,gw_db_schema.region_class as rcC,gw_db_schema.region_city as rc where "
		+ "scz.scz_id=wo.scz_id and scz.cl_id=rcC.cl_id and rcC.reg_id=rc.reg_id and wo.wor_name='" + woredaName + "' and scz.scz_id="+subcityzoneId+" "
		+ "and rcC.cl_id="+regionCityID+" and rc.reg_id="+waterWellLocation+"");
		prestatmentLocalName.executeUpdate();	
			String query = "select local_id from gw_db_schema.local_name as lN,gw_db_schema.woreda as wo,gw_db_schema.sub_city_zone as scz,"
					+ "gw_db_schema.region_class as rcC,gw_db_schema.region_city as rc where scz.scz_id=wo.scz_id and scz.cl_id=rcC.cl_id and "
					+ "rcC.reg_id=rc.reg_id and local_name='"+localName+"' and wo.wor_name='" +woredaName +"' and scz.scz_id="+subcityzoneId+" and "
							+ "rcC.cl_id="+regionCityID+" and rc.reg_id="+waterWellLocation+"";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				localNameId = rs.getInt(1);
			}
		} 
	  		  conn.close();
		  			} catch (Exception e) {
	  				System.out.println("Error...." + e);
	  				e.printStackTrace();}
	  		}
		  		int ownerId = 0, repoId=0,enumId=0;
		  		String dbUserEmail="";
		  		 String pumpFilename=null,pumpFileType=null, wellreportName=null,wellReportType=null;
		  		  int pumpfileSize=0,wellRepoSize=0;
		  		  InputStream pumpdataStream=null,wellRepoInputStream=null;
		  		if (ownerName != null){
		  			//Insert in to DB from user form
		  			int geolocId=0;
			  		if (geoLocationId!=null ) {
			  			try {
					conn = connection.dbconnection();
					String checkGeolocation = "select geoloc_id from gw_db_schema.geo_location where lat="+laty+" and longt="+lonx+"";
					Statement stmtcheckGeolocation = conn.createStatement();
					ResultSet rscheckGeolocation = stmtcheckGeolocation.executeQuery(checkGeolocation);
					if(rscheckGeolocation.next()){
						geolocId = rscheckGeolocation.getInt(1);
					}
					else{
				PreparedStatement prestatment =conn.prepareStatement("insert into gw_db_schema.geo_location(lat,longt,l_elevation,"
						+ "geom_latlong)values("+laty+","+lonx+","+elvation+","
						+ "ST_SetSRID(ST_MakePoint("+lonx+"::double precision, "+laty+"::double precision),4326))");
				prestatment.executeUpdate();
				String query1 = "select geoloc_id from gw_db_schema.geo_location where lat="+laty+" and longt="+lonx+" and l_elevation="+elvation+"";
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(query1);
				while (rs.next()) {
					geolocId = rs.getInt(1);
					}
				}
					conn.close();
				} catch (Exception e) {
					System.out.println("Error...." + e);
					e.printStackTrace();
					}
			  		}
		 			try {
		 				//Well User Profile
				conn = connection.dbconnection();
				String query_checkownerEmail="select id from gw_db_schema.db_user where SIMILARITY(orgname,'"+ownOrgName+"')>0.80 and usertype='Well Owner'";
				Statement stmntcheckownerEmail=conn.createStatement();
				ResultSet rstcheckownerEmail=stmntcheckownerEmail.executeQuery(query_checkownerEmail);
				if(rstcheckownerEmail.next()){
				ownerId=rstcheckownerEmail.getInt(1);
				}
				else{
				PreparedStatement prepStatementOwner=conn.prepareStatement("insert into gw_db_schema.db_user(orgname,email,phone,usertype,wellindex)"
				+ "values('"+ownOrgName+"',?,'"+Ownphone+"','Well Owner','"+wellIndex+"')");
				if(Ownemail.equals("0") || Ownemail.equals("non") || Ownemail.equals("None")||Ownemail.equals("none")|| Ownemail.equals("NA")|| Ownemail.equals("")) {
					prepStatementOwner.setNull(1,java.sql.Types.VARCHAR);
				}else {
					prepStatementOwner.setString(1, Ownemail);	
				}
				prepStatementOwner.executeUpdate();
				String query_Ownerdetail="select id from gw_db_schema.db_user where SIMILARITY(orgname,'"+ownOrgName+"')>0.80 and wellindex='"+wellIndex+"'";
				Statement stmntOwner=conn.createStatement();
				ResultSet rstOwner=stmntOwner.executeQuery(query_Ownerdetail);
				while(rstOwner.next()){
					ownerId=rstOwner.getInt(1);
				}}
				String query_checkDbuserEmail="select email from gw_db_schema.db_user where (email='"+enameEmail+"' or email='"+Ownemail+"' or "
						+ "email='"+reportedEmail+"') and (usertype='Database User' or usertype='Common User' or usertype='Administrator')";
				Statement stmntcheckDbuserEmail=conn.createStatement();
				ResultSet rstcheckDbuserEmail=stmntcheckDbuserEmail.executeQuery(query_checkDbuserEmail);
				if(rstcheckDbuserEmail.next()){
				dbUserEmail=rstcheckDbuserEmail.getString(1);
				System.out.println("User Email <"+dbUserEmail+"> is Exists in Database. Please, Change the Email");
				}
				if(wellStatus.equals("Abandoned")){
					String query_checReporterkEmail="select id from gw_db_schema.db_user where email='"+reportedEmail+"' and usertype='Reporter'";
					Statement stmntcheckRepoEmail=conn.createStatement();
					ResultSet rstcheckRepoEmail=stmntcheckRepoEmail.executeQuery(query_checReporterkEmail);
					if(rstcheckRepoEmail.next()){
					repoId=rstcheckRepoEmail.getInt(1);
					}else {
					PreparedStatement prepstateAbandonedRepo=conn.prepareStatement("insert into gw_db_schema.db_user(orgname,email,phone,usertype,"
							+ "wellindex)values('"+reportedCompany+"',?,'"+reportedPhone+"','Reporter','"+wellIndex+"')");
					if(reportedEmail.equals("0") || reportedEmail.equals("non") || reportedEmail.equals("None")||reportedEmail.equals("none")||reportedEmail.equals("NA")|| reportedEmail.equals("")) {
						prepstateAbandonedRepo.setNull(1,java.sql.Types.VARCHAR);
					}else {
						prepstateAbandonedRepo.setString(1, reportedEmail);	
					}
					prepstateAbandonedRepo.executeUpdate();
					String query_abandonedRepo="select id from gw_db_schema.db_user where email='"+reportedEmail+"' and wellindex='"+wellIndex+"'";
					Statement stmntabandonedRepo=conn.createStatement();
					ResultSet rsabandonedRepo=stmntabandonedRepo.executeQuery(query_abandonedRepo);
					while(rsabandonedRepo.next()){
						repoId=rsabandonedRepo.getInt(1);
					}}
					String query_checkEnumeratorEmail="select id from gw_db_schema.db_user where email='"+enameEmail+"' and usertype='Enumerator'";
					Statement stmntcheckEnumEmail=conn.createStatement();
					ResultSet rstcheckEnumEmail=stmntcheckEnumEmail.executeQuery(query_checkEnumeratorEmail);
					if(rstcheckEnumEmail.next()){
					enumId=rstcheckEnumEmail.getInt(1);
					}
					else {
					PreparedStatement prepstateDataEnum=conn.prepareStatement("insert into gw_db_schema.db_user(orgname,fullname,email,phone,"
							+ "usertype,wellindex)values('"+enameCompany+"','"+enameName+"',?,'"+enamePhone+"','Enumerator',"
							+ "'"+wellIndex+"')");
					if(enameEmail.equals("0") || enameEmail.equals("non") || enameEmail.equals("None")||enameEmail.equals("none")||enameEmail.equals("NA")|| enameEmail.equals("")) {
						prepstateDataEnum.setNull(1,java.sql.Types.VARCHAR);
					}else {
						prepstateDataEnum.setString(1, enameEmail);	
					}
					prepstateDataEnum.executeUpdate();
					String query_dataEnum="select id from gw_db_schema.db_user where email='"+enameEmail+"' and wellindex='"+wellIndex+"'";
					Statement stmntDataEnum=conn.createStatement();
					ResultSet rsDataEnum=stmntDataEnum.executeQuery(query_dataEnum);
					while(rsDataEnum.next()){
						enumId=rsDataEnum.getInt(1);
					}}
				}else if(wellStatus.equals("Productive")){
					String query_checkEnumeratorEmail="select id from gw_db_schema.db_user where email='"+enameEmail+"' and usertype='Enumerator'";
					Statement stmntcheckEnumEmail=conn.createStatement();
					ResultSet rstcheckEnumEmail=stmntcheckEnumEmail.executeQuery(query_checkEnumeratorEmail);
					if(rstcheckEnumEmail.next()){
					enumId=rstcheckEnumEmail.getInt(1);
					}
					else {
					PreparedStatement prepstateDataEnum=conn.prepareStatement("insert into gw_db_schema.db_user(orgname,fullname,email,phone,"
							+ "usertype,wellindex)values('"+enameCompany+"','"+enameName+"',?,'"+enamePhone+"','Enumerator',"
							+ "'"+wellIndex+"')");
					if(enameEmail.equals("0") || enameEmail.equals("non")|| enameEmail.equals("None")||enameEmail.equals("none")||enameEmail.equals("NA")|| enameEmail.equals("")) {
						prepstateDataEnum.setNull(1,java.sql.Types.VARCHAR);
					}else {
						prepstateDataEnum.setString(1, enameEmail);	
					}
					prepstateDataEnum.executeUpdate();
					String query_dataEnum="select id from gw_db_schema.db_user where email='"+enameEmail+"' and wellindex='"+wellIndex+"'";
					Statement stmntDataEnum=conn.createStatement();
					ResultSet rsDataEnum=stmntDataEnum.executeQuery(query_dataEnum);
					while(rsDataEnum.next()){
						enumId=rsDataEnum.getInt(1);
					}	
					}
				}
				conn.close();
			}catch (Exception e) {
				e.printStackTrace();
			}
		 			//Insert into DB from Excel sheet 
		  		  if(checkList!=null) {
	  		  	for(String checkName:checkList){
		if(checkName.equals("Productive Well")||checkName.equals("Abandoned Well")){
			List<CoordinateUpdate> update=new ArrayList<CoordinateUpdate>();
	  		if((null!=mergaeTwofiles && mergaeTwofiles.size()>0)) {
	  			try {
	  				conn = connection.dbconnection();
				for (MultipartFile abanfile:mergaeTwofiles){	
					filetype= abanfile.getContentType();
					fileSize= (long) abanfile.getSize();
					fileName=abanfile.getOriginalFilename();
					//System.out.println("Files accessed from User Interface "+fileName);
					fileStream= abanfile.getInputStream();
					Workbook workbook=new XSSFWorkbook(fileStream);
					double highestSimilarity=0.0;
					Sheet matchingSheet=null;
					List<String[]> excelCellData = new ArrayList<>();
					for(int i=0;i<workbook.getNumberOfSheets();i++) {
						String currentSheet = workbook.getSheetName(i);
						String target="2.DrillingPumpTestHistoryData";
						double similarity= calculateSimilarity(target,currentSheet);	
						if(similarity > highestSimilarity){
							highestSimilarity=similarity;
							matchingSheet=workbook.getSheetAt(i);
						}
					}
					if(matchingSheet!=null && highestSimilarity>0.80) {
						for(int rowIndex=5;rowIndex<matchingSheet.getLastRowNum();rowIndex++) {
							Row rowOpdata=matchingSheet.getRow(rowIndex);
							if(rowOpdata==null) {
								continue;}
							 String[] excelRowData = new String[32];
							 int dataColumnIndex = 0; // Index for the rowData array
							 boolean hasNonNullCell = false;
							for (int i=0;i<rowOpdata.getLastCellNum();i++) {
								Cell cell=rowOpdata.getCell(i);
								String value="";
            if (cell!= null && cell.getCellType()!= CellType.BLANK ){
            	hasNonNullCell = true;
            	switch (cell.getCellType()) {
                case STRING:
                	value=cell.getStringCellValue().trim();
                    System.out.println("Cell Value of String from Main Controller is: "+value);
                    break;
                case NUMERIC:
                	if(DateUtil.isCellDateFormatted(cell)) {
                		value=String.valueOf(cell.getDateCellValue()).trim();
                	}else {
                		 value=String.valueOf(cell.getNumericCellValue()).trim();
                	}
                	break;
                case BOOLEAN:
                	value=String.valueOf(cell.getBooleanCellValue()).trim();
                	break;
                case FORMULA:
                	 // Evaluate formula to get the result
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(cell);
                    switch (cellValue.getCellType()) {
                        case STRING:
                        	value= String.valueOf(cellValue.getStringValue()).trim();
                            break;
                        case NUMERIC:
                        	value= String.valueOf(cellValue.getNumberValue()).trim();
                            break;
                        case BOOLEAN:
                        	value= String.valueOf(cellValue.getBooleanValue()).trim();
                            break;
                        default:
                        	break;
                    }
                    break;
                default:
                	break;
		            }
            	}else {
            		value="0";
            		}
            
		            if(dataColumnIndex<32){
		            	excelRowData[dataColumnIndex++]=value;   
		            	}
		            }	//WOrking With Further well Information     
							if(hasNonNullCell) {
			    				excelCellData.add(excelRowData);
			    				//System.out.println("Well Index extracted from excel sheet with contructor or consultant info "+excelRowData[1]);	
			    				}	
		            }	
						}		
					//String commaSpardata=rowData.stream().collect(Collectors.joining(",")
					for (String []rowData: excelCellData) {
			            for (int i=0;i<rowData.length;i++){
			            		wellCellIndex=rowData[1];
			            		excelLong=Double.parseDouble(rowData[16]);
			            		excelLat=Double.parseDouble(rowData[17]);
			            		excelEasting=Double.parseDouble(rowData[18]);
			            		excelNorthing=Double.parseDouble(rowData[19]);
			            		wellCellContCN=rowData[26];
			            		wellCellContemail=rowData[27];
			            		wellCellContPhone=rowData[28];
			            		wellCellConsultCN=rowData[29];
								wellCellConsulemail=rowData[30];
								wellCell_consultPhone=rowData[31];
								CoordinateUpdate coord=new CoordinateUpdate(excelLat,excelLong,excelEasting,excelNorthing);
								update.add(coord);
			            		}
			            checkLatLongInDB(conn,update);
			         String queryselectwellIDforreport="select ww.well_id,ww.well_index from gw_db_schema.water_well as ww where "
								+ "SIMILARITY(ww.well_index,'"+wellCellIndex+"')>0.80";
						Statement stmntwellIdforReport=conn.createStatement();
						ResultSet rswellIDforRepo = stmntwellIdforReport.executeQuery(queryselectwellIDforreport);
						while(rswellIDforRepo.next()){
							wellCell_idfromDB=rswellIDforRepo.getInt(1);
							wellIndexfromDB=rswellIDforRepo.getString(2);
						}
		                // Check if email exists
		            if(wellCellIndex.equals(wellIndex) || wellCellIndex.equals(wellIndexfromDB)){
		            	if (!consultantEmailExists(conn, wellCellConsultCN)) {
	                		// Register new user
		                    addConsultantProfile(conn, wellCellConsultCN, wellCellConsulemail, wellCell_consultPhone,"Consultant", wellCellIndex);
	                    
	                } else {
	                    System.out.println("Name already exists, checked to register consultant info: " + wellCellConsulemail);
	                }	
	                // Check if email exists
	                if (!contractructorEmailExists(conn, wellCellContCN)) {
	                		 // Register new user
		                	addContractorProfile(conn, wellCellContCN, wellCellContemail, wellCellContPhone,"Contractor", wellCellIndex);
	                   
	                } else {
	                    System.out.println("Name already exists, checked to register contractor info: " + wellCellContemail);
	                }
		            }
		            }
				}
				conn.close();
				}catch (Exception error) {
						error.printStackTrace();}	
				}
	  		}
		}}
			  		
		  		  if(checkReportList!=null) {
		  			  for(String checkedName:checkReportList) {
		  				  if(checkedName.equals("wellRepoDoc")|| checkedName.equals("abanwellRepoDoc")){
		  					  System.out.println("Well Report Check List Name "+checkedName);
		  					if(null!= mergaeTwoReportfiles && mergaeTwoReportfiles.size()>0){
		  			  			for(MultipartFile pumpfile:mergaeTwoReportfiles){
		  			  				wellreportName=pumpfile.getOriginalFilename();
		  			  				wellReportType=pumpfile.getContentType();
		  			  				wellRepoSize=(int) pumpfile.getSize();
		  			  				wellRepoInputStream=pumpfile.getInputStream(); 
		  	 					 }
		  				 	}
		  				  }
		  			  }
		  		  }
		  		  //pumping system
		  		  if(checkListforPump!=null) {
		  			  for(String checklistname:checkListforPump){
		  				  if(checklistname.equals("pumping_doc")){
		  					if(null!=pumpingData && pumpingData.size()>0){
			  					 for(MultipartFile pumpfile:pumpingData){
			  						 pumpFilename=pumpfile.getOriginalFilename();
			  						 pumpFileType=pumpfile.getContentType();
			  						 pumpfileSize=(int) pumpfile.getSize();
			  						 pumpdataStream=pumpfile.getInputStream(); 
			  					 }
			  				  }  
		  				  }
		  			  }
		  		  } 	
		  		//Insert to Water well table
		  		java.sql.Date recorDate=null;
		  		recordDate=LocalDateTime.now();
		  		DateTimeFormatter timeformat;
		  		timeformat= DateTimeFormatter.ofPattern("yyyy-MM-dd");
		  		java.util.Date convertRDate= new SimpleDateFormat("yyyy-MM-dd").parse(timeformat.format(recordDate));
		  		recorDate= new java.sql.Date(convertRDate.getTime());
	        files.addAll(uploadDao.savespatialfile(gwDataId,db_userIDI,localNameId,wellFieldId,geolocId,wellCode,wellIndex,wellDepth,
	        		mainAqiefer,wellType,wellConstractionDate,ownerId,wellOwnerCata,wellStatus,drillingPermit,
	        		casingMaterName,largeCasingID,telescopCasingID,recorDate,mergaeTwofiles,wellAbanReason,repoId,sealedYN,abandonSealedDate,
	        		productiveWellCond,functioningwellCon,proWellPowerId,availableGId,
	        		generatorStatusId,genePowerMeasureId,SCADAId,SCADAStatusId,enumId,pumpCapName,
	        		PumpHead,yieldLs,SWL,DWL,pumpInstalledDate,pumpPositionName,dischargeRate,pumpReplacedDate,pumpStatusName,pumpFilename,pumpFileType,
	        		pumpfileSize,pumpdataStream,SpecificCapacity,wellreportName,wellReportType,wellRepoSize,wellRepoInputStream));
		//modelview.addObject("msg1",files);
		redirectAttributes.addFlashAttribute("msg1", files);
		modelview.setViewName("redirect:/successPage");
		  		}
	  		}else {
	  		//Access Operational (Real time data) from Excel sheet and Send to the UploadDao page
		  		int db_userIDII=0;
		    	if(request.getParameter("db_userIdII")=="") {
		    		db_userIDII=0;
		    	}else {
		    		db_userIDII=Integer.parseInt(request.getParameter("db_userIdII"));
		    		System.out.println("Who is the User"+ db_userIDII);
		    	}
		  		List<MultipartFile> operationalData = request.getFiles("operationalFile"); 
				if(operationalData.isEmpty()|| operationalData==null) {
					operationalData=new ArrayList<>();
				}
				String gwExcel_checkoperationFile[];
				if(request.getParameterValues("aditionalGWlog")==null){
					gwExcel_checkoperationFile= new String[]{"Undefined"};
				}else{
					gwExcel_checkoperationFile=request.getParameterValues("aditionalGWlog");
				}
				List<String>operatioFilecheckList= Arrays.asList(gwExcel_checkoperationFile);
				 //Operational documents
		  		  if(operatioFilecheckList!=null) {
		  			  for(String checklistname:operatioFilecheckList){
		  				  if(checklistname.equals("Operation_data")) {
		  				files.addAll(uploadDao.saveotherofficefile(operationalData,db_userIDII));
		  				redirectAttributes.addFlashAttribute("msg1", files);
		  				modelview.setViewName("redirect:/successPage");
		  				  }
		  			  }
		  		  } 	
	  		}
		//return to the user home page
	return modelview;
 		}
    @RequestMapping(value = "/updateWCR", method = RequestMethod.POST)
    public ModelAndView updateWCRData(HttpServletRequest request,RedirectAttributes redirectAttributes) throws Exception {
    	ModelAndView modelview = new ModelAndView();
  		List<File> files = new ArrayList<File>();
  		int dbUserId=Integer.parseInt(request.getParameter("db_userUpdateWCR"));
    	int userID=Integer.parseInt(request.getParameter("CROwnerIdUName"));
    	int wellID=Integer.parseInt(request.getParameter("CRwellIdUName"));
    	int geolocID=Integer.parseInt(request.getParameter("CRGeolocUName"));
    	String wellCode=request.getParameter("CRwellCodeUName");
    	double easting=Double.parseDouble(request.getParameter("CRweEastingUName"));
    	double northing=Double.parseDouble(request.getParameter("CRNorthingUName"));
    	double longT=Double.parseDouble(request.getParameter("CRLongTUName"));
    	double lat=Double.parseDouble(request.getParameter("CRLatUName"));
    	double elivation=Double.parseDouble(request.getParameter("CRElevationUName"));
    	String constructionYear;
 		 if(request.getParameter("CRConstYUName")==null){
 			constructionYear="1970-01-01"; 
	  		 } 
 		 else{
 			constructionYear =request.getParameter("CRConstYUName");
	  		 }
 		java.util.Date dateraw= new SimpleDateFormat("yyyy-MM-dd").parse(constructionYear);
 		java.sql.Date wellConstractionDate= new java.sql.Date(dateraw.getTime());
    	String drillingLicense=request.getParameter("CRDrillingLUName");
    	double wellDepth=Double.parseDouble(request.getParameter("CRwellDepthUName"));
    	double wellYield=Double.parseDouble(request.getParameter("CRwellYieldUName"));
    	double SWL=Double.parseDouble(request.getParameter("CRSWLUName"));
    	double DWL=Double.parseDouble(request.getParameter("CRDWLUName"));
    	double specificCapacity=Double.parseDouble(request.getParameter("CRSpecificCUName"));
    	String wellOwnerName=request.getParameter("CROwnerCUName");
    	String wellOwnerEmail=request.getParameter("CREmailUName");
    	String wellOwnerHpone=request.getParameter("CRPhoneUName");
    	String mainAquifer=request.getParameter("CRmainaquiferName");
    	String CRwellTypeName=request.getParameter("CRwellTypeName");
    	String CRabanRWellName=request.getParameter("CRabanRWellName");
    	String CRsealedYNName=request.getParameter("CRsealedYNName");
    	String CRSealedDateName;
		 if(request.getParameter("CRSealedDateName")==null){
			 CRSealedDateName="1970-01-01"; 
	  		 } 
		 else{
			 CRSealedDateName =request.getParameter("CRSealedDateName");
	  		 }
		 java.util.Date sealedDate= new SimpleDateFormat("yyyy-MM-dd").parse(CRSealedDateName);
	 	 java.sql.Date wellConSealedDate= new java.sql.Date(sealedDate.getTime());
    	String CRfunWellConName=request.getParameter("CRfunWellConName");
    	String CRpumpstatusName=request.getParameter("CRpumpstatusName");
    	String CRpumpInstalledDateName;
		 if(request.getParameter("CRpumpInstalledDateName")==null){
			 CRpumpInstalledDateName="1970-01-01"; 
	  		 } 
		 else{
			 CRpumpInstalledDateName =request.getParameter("CRpumpInstalledDateName");
	  		 }
		 java.util.Date installedDate= new SimpleDateFormat("yyyy-MM-dd").parse(CRpumpInstalledDateName);
	 	 java.sql.Date wellpumpInstallDate= new java.sql.Date(installedDate.getTime());
		double CRpumpCapacityName=Double.parseDouble(request.getParameter("CRpumpCapacityName"));
		String CRpumpHeadName=request.getParameter("CRpumpHeadName");
	    double CRpumpPosiName=Double.parseDouble(request.getParameter("CRpumpPosiName"));
	    double CRdischargeRateName=Double.parseDouble(request.getParameter("CRdischargeRateName"));
	    String CRgeneratorStaName=request.getParameter("CRgeneratorStaName");
	    double CRcapacityCapaName=Double.parseDouble(request.getParameter("CRcapacityCapaName"));
	    String CRSOwnerCatName=request.getParameter("CRSOwnerCatName");
	    double CRlargeCasingName=Double.parseDouble(request.getParameter("CRlargeCasingName"));
	    double CRtelCasingName=Double.parseDouble(request.getParameter("CRtelCasingName"));
    	String CRCasingMaterName=request.getParameter("CRCasingMaterName");
    	String SCADAName=request.getParameter("SCADAName");
    	String SCADACStatusName=request.getParameter("SCADACStatusName");
    	//System.out.println("User Id for Update WCR is "+userID+" and Well Id Is "+wellID+" and Well Cod is "+wellCode);
    	files.addAll(uploadDao.updateWCRData(userID,wellID,geolocID,wellCode,easting,northing,longT,lat,elivation,wellConstractionDate,
    			drillingLicense,wellDepth, wellYield, SWL, DWL, specificCapacity, wellOwnerName, wellOwnerEmail,wellOwnerHpone,dbUserId,
    			CRSOwnerCatName,CRlargeCasingName,CRtelCasingName,CRCasingMaterName,mainAquifer,CRwellTypeName,CRabanRWellName,CRsealedYNName,
    			wellConSealedDate,CRfunWellConName,CRpumpstatusName,wellpumpInstallDate,CRpumpCapacityName,CRpumpHeadName,CRpumpPosiName,
    			CRdischargeRateName,CRgeneratorStaName,CRcapacityCapaName,SCADAName,SCADACStatusName));
    	redirectAttributes.addFlashAttribute("updateMsg", files);
		modelview.setViewName("redirect:/successPage");
        return modelview;
    }
    @RequestMapping(value = "/updateGWOD", method = RequestMethod.POST)
    public ModelAndView updateGWOData(HttpServletRequest request,RedirectAttributes redirectAttributes) throws Exception {
    	ModelAndView modelview = new ModelAndView();
  		List<File> files = new ArrayList<File>();
    	int userID=Integer.parseInt(request.getParameter("enumIdName"));
    	int wellID=Integer.parseInt(request.getParameter("wellIdName"));
    	String wellIndex=request.getParameter("wellIndexName");
    	String waterUserLName=request.getParameter("waterUserLName");
    	String operationStartDate;
 		 if(request.getParameter("opStartDName").equals("_")|| request.getParameter("opStartDName").equals("NR")){
 			operationStartDate="1970-01-01"; 
	  		 } 
 		 else{
 			operationStartDate =request.getParameter("opStartDName");
	  		 }
 		java.util.Date dateraw= new SimpleDateFormat("yyyy-MM-dd").parse(operationStartDate);
 		java.sql.Date wellOpernStartDate= new java.sql.Date(dateraw.getTime());
 		String enumCompanyName=request.getParameter("enumCompanyName");
    	String enumEmailName=request.getParameter("enumEmailName");
    	String enumEmailPhoneName=request.getParameter("enumEmailPhoneName");
    	//System.out.println("User Id for Update WCR is "+userID+" and Well Id Is "+wellID+" and Well Cod is "+wellCode);
    	files.addAll(uploadDao.updateGWOData(userID,wellID,wellIndex,waterUserLName,wellOpernStartDate,enumCompanyName,enumEmailName,
    			enumEmailPhoneName));
    	redirectAttributes.addFlashAttribute("updateMsg", files);
		modelview.setViewName("redirect:/successPage");
        return modelview;
    } 
    @RequestMapping(value = "/successPage", method = RequestMethod.GET)
    public ModelAndView showSuccessPage() {
    	 ModelAndView modelAndView = new ModelAndView("hompage"); // "successPage" is the logical view name
        return modelAndView;
    } 
    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public ModelAndView changePassword(HttpServletRequest request) {
    	conn=connection.dbconnection();
    	ModelAndView modelAndView = new ModelAndView();
    	String userType="";
    	//String currentPW=request.getParameter("");
    	String newPasspord=request.getParameter("newPassword");
    	int vCode=0;
    	int userID=Integer.parseInt(request.getParameter("userID"));
    	//System.out.println("User Id for confirmation is "+userID);
    	try {String userQuery="select usertype from gw_db_schema.db_user where id="+userID+"";
			Statement statmentConfirm=conn.createStatement();
			ResultSet rsConfirm=statmentConfirm.executeQuery(userQuery);
			if(rsConfirm.next()) {
				userType=rsConfirm.getString(1);
				String InsertQuery="update gw_db_schema.db_user set vern_code=?, password=? where id="+userID+"";
				PreparedStatement preStatement=conn.prepareStatement(InsertQuery);
				if(userType.equals("Administrator")) {
					vCode=110;
				 preStatement.setInt(1, vCode);	
				}else if(userType.equals("Database User")){
					vCode=120;
					preStatement.setInt(1, vCode);	
				}else if(userType.equals("Common User")){
					vCode=123;
					preStatement.setInt(1, vCode);	
				}
				preStatement.setString(2, encryptPassword(newPasspord));
				preStatement.executeUpdate();
				modelAndView.addObject("changeSuccess", "Password changed. Login Please.");
			}else {
				modelAndView.addObject("changeFailed", "Information Mismatch, Password Not changed!");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  modelAndView.setViewName("login"); // "successPage" is the logical view name
        return modelAndView;
    }
    @RequestMapping(value = "/changeForgatenPassword", method = RequestMethod.POST)
    public ModelAndView changeForgatenPassword(HttpServletRequest request) {
    	ModelAndView modelAndView = new ModelAndView();;
    	conn=connection.dbconnection();
    	int userID=0;
    	String InsertQuery="";
    	String changerFullName=request.getParameter("changerFullName");
    	String changerUserName=request.getParameter("changerUserName");
    	String changerEmail=request.getParameter("changerEmail");
    	String changerPhoneNumber=request.getParameter("changerPhoneNumber");
    	String changernewPassword=request.getParameter("changernewPassword");
    	//System.out.println("User Id for confirmation is "+userID);
    	try {String userQuery="select id from gw_db_schema.db_user where SIMILARITY(fullname,'"+changerFullName+"')>0.90 and "
    			+ "SIMILARITY(username,'"+changerUserName+"')>0.90 and SIMILARITY(email,'"+changerEmail+"')>0.90 and phone='"+changerPhoneNumber+"'";
			Statement statmentConfirm=conn.createStatement();
			ResultSet rsConfirm=statmentConfirm.executeQuery(userQuery);
			if(rsConfirm.next()){
				userID=rsConfirm.getInt(1);
					InsertQuery="update gw_db_schema.db_user set password=? where id="+userID+"";
					PreparedStatement preStatement=conn.prepareStatement(InsertQuery);
					preStatement.setString(1, encryptPassword(changernewPassword));
					preStatement.executeUpdate();
					modelAndView.addObject("success", "Password changed. Login Please.");
			}else {
					//System.out.println("Access Denied");
				modelAndView.addObject("denay", "Information Mismatch, Password Not changed!");
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  modelAndView.setViewName("login"); // "successPage" is the logical view name
        return modelAndView;
    }
 	// download non spatial files
 		@RequestMapping(value = "/downloadfile", method = RequestMethod.GET)
 		public void nonspatialfile(HttpServletRequest request, HttpServletResponse response) throws Throwable {
 	 		int fileId = Integer.parseInt(request.getParameter("file_id"));
 	 		String dataType=request.getParameter("DPTH_ID");
 	 		//System.out.println("Data Type For Download is "+dataType);
 	 		ValuePair file=LoginDao.downloadDPTHData(fileId,dataType);
 				//response.setContentType(file.contentType);
 				response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.name+ "\"");
 				//response.setContentLength(file.size1);
 				response.getOutputStream().write(file.ddat);		
 			
 			response.flushBuffer();
 		}
 		//input response
 		@ResponseBody
		@RequestMapping(value="/Upload_", method = RequestMethod.POST)
		public String respondto_interface(HttpServletRequest request) throws Throwable {
 			System.out.println("Id of Pushed button= ");	
			Gson gson = new Gson();
			return gson.toJson(null);
		}
 		
 		//get selected GHM by project name
 		@ResponseBody
		@RequestMapping(value="/GHM_selected", method = RequestMethod.POST)
		public String Select_GHM(HttpServletRequest request) throws Throwable {
 			int cat_name=Integer.parseInt(request.getParameter("cat"));
 			//System.out.println("Id of Project__= "+cat_name);
			Gson gson = new Gson();
			return gson.toJson(LoginDao.data_investigated(cat_name));
		}
 		//get Sample category
 		@ResponseBody
		@RequestMapping(value="/accountManagement", method = RequestMethod.POST)
		public String sample_cata(HttpServletRequest request) throws Throwable {
 			int adminID=Integer.parseInt(request.getParameter("usreID"));
 			//System.out.println("Id of Type__= "+type_Id);
			Gson gson = new Gson();
			return gson.toJson(LoginDao.userAccountManagement(adminID));
		}
		//get category of GHM
 		@ResponseBody
		@RequestMapping(value="/updateUserAccount", method = RequestMethod.POST)
		public String updateUserAccount(HttpServletRequest request) throws Throwable {
 			int userID=Integer.parseInt(request.getParameter("userID"));
 			String accountStatus=request.getParameter("accountStatus");
 			int privilege=Integer.parseInt(request.getParameter("privilege"));
 			int adminID=Integer.parseInt(request.getParameter("1404Id"));
 			System.out.println("Account status= "+accountStatus+" and privilage = "+privilege);
			Gson gson = new Gson();
			return gson.toJson(LoginDao.updateUserAccount(userID,privilege,accountStatus,adminID));
		}
        //Ground water Data mapping
 		@ResponseBody
		@RequestMapping(value = "/gWDBParam", method = RequestMethod.POST)
		public String gwDatabaseParameter(HttpServletRequest request) throws Throwable {
				String national_cor=request.getParameter("Na");
                //System.out.println("GW data types "+national_cor);
			Gson gson = new Gson();
			return gson.toJson(LoginDao.gwDBParam(national_cor));
		}
 		//Region City Mapping
 		@ResponseBody
		@RequestMapping(value="/regionCity", method = RequestMethod.POST)
		public String proj_center(HttpServletRequest request) throws Throwable {
				int  national_cor=Integer.parseInt(request.getParameter("regionC"));
			Gson gson = new Gson();
			return gson.toJson(LoginDao.region_class(national_cor));
		}
 		
 		//Sub city Zone Mapping
 		@ResponseBody
			@RequestMapping(value = "/subcityZone", method = RequestMethod.POST)
			public String prcomponent(HttpServletRequest request) throws Throwable {
 			    int searchkey=Integer.parseInt(request.getParameter("cityZone_id"));
				Gson gson = new Gson();
				return gson.toJson(LoginDao.subCityZone(searchkey));
			}
 			
 			//project Detail for request 
 			@ResponseBody
 			@RequestMapping(value = "/detailsPro", method = RequestMethod.POST)
 			public String accessspro_details(HttpServletRequest request) throws SQLException {
 				int center_id = Integer.parseInt(request.getParameter("cen_id"));
 				int process = Integer.parseInt(request.getParameter("process_id"));
 				int pro_id = Integer.parseInt(request.getParameter("proj_id"));
 				String desisup_id = request.getParameter("de_su_key");
 				Gson gson = new Gson();
 				return gson.toJson(null);
 			}
 			//for request send
 			//project Detail for request 
 			@ResponseBody
 			@RequestMapping(value = "/Sendreqeust", method = RequestMethod.POST)
 			public String sendrequest(HttpServletRequest request) throws SQLException {
 				int center_id = Integer.parseInt(request.getParameter("cen_id"));
 				int process = Integer.parseInt(request.getParameter("pro_id"));
 				int pro_id = Integer.parseInt(request.getParameter("proj_id"));
 				String username= request.getParameter("userId");
 				String fname= request.getParameter("fname");
 				String lname= request.getParameter("lname");
 				String desisup_id= request.getParameter("de_su_key");
 				List<String> listitem=new ArrayList<String>();
 				listitem.add(desisup_id);
 				Gson gson = new Gson();
 				for(String cat_id:listitem){
 	      return gson.toJson(uploadDao.ask_request(center_id, process, pro_id,cat_id,username,fname,lname));	
 				}
				return gson.toJson("");
 			}
 		
 		//Search Engine 
 		@ResponseBody
			@RequestMapping(value = "/DPTHDataURL", method = RequestMethod.POST)
			public String searchdetail(HttpServletRequest request) throws Throwable {
				Gson gson = new Gson();
				return gson.toJson(LoginDao.Listdetails());
			}
 		@ResponseBody
 		@RequestMapping(value = "/CRUDOperationLog", method = RequestMethod.POST)
 		public String CRUDOperationLogFunction(HttpServletRequest request) throws Throwable{
 			Gson gson = new Gson();
 			return gson.toJson(LoginDao.accessCRUDOperationLog());
 		}
 		@ResponseBody
 		@RequestMapping(value = "/CRUDOperationLogByDate", method = RequestMethod.POST)
 		public String CRUDOperationLogByDate(HttpServletRequest request) throws Throwable{
 			String Bydate =  request.getParameter("CRUDdate");
 			//System.out.println("Date is "+Bydate);
 			Gson gson = new Gson();
 			return gson.toJson(LoginDao.accessCRUDOperationLogByDate(Bydate));
 		}
 		
 		//grant project
 		@ResponseBody
 		@RequestMapping(value = "/grant_pro", method = RequestMethod.POST)
 		public String grant_project(HttpServletRequest request) throws Throwable{
 			String id = request.getParameter("cent_id");
 			Gson gson = new Gson();
 			return gson.toJson(uploadDao.process_name(id));
 		}
 		//user details
 		@ResponseBody
 		@RequestMapping(value = "/userholder", method = RequestMethod.POST)
 		public String user_details(HttpServletRequest request) throws Throwable{
 			String id = request.getParameter("email");
 			System.out.println("Email Address= "+id);
 			Gson gson = new Gson();
 			return gson.toJson(LoginDao.Userdetails(id));
 		}
 		@ResponseBody
 		@RequestMapping(value = "/process_granted", method = RequestMethod.POST)
 		public String granted_process(HttpServletRequest request) throws Throwable{
 			String id = request.getParameter("process_id");
 			int user_id = Integer.parseInt(request.getParameter("user_id"));
 			Gson gson = new Gson();
 			return gson.toJson(uploadDao.username_project(id,user_id));
 		}
 		//grant project
 		@ResponseBody
 		@RequestMapping(value = "/grantdessup_pro", method = RequestMethod.POST)
 		public String grantDesSup_project(HttpServletRequest request){
 			String []recipients = request.getParameterValues("userN");
 			String userId[]=recipients[0].split("[@ ']");
 			String username=userId[0];
 			String Design = null;
 			String supvision = null;
 			if(request.getParameter("dse")!=null || request.getParameter("sup")!=null){
 			 Design=	request.getParameter("dse");
 			 supvision=request.getParameter("sup");
 			}
 			String []sup_desi_id={Design,supvision};
 			Gson gson = new Gson();
 			System.out.println("Requested Center = "+Arrays.toString(sup_desi_id));
 			return gson.toJson(uploadDao.grant_project(username,sup_desi_id));
 		}
 		@ResponseBody
 		@RequestMapping(value = "/addParameter", method = RequestMethod.POST)
 		public String addParameter(HttpServletRequest request) throws Throwable{
 			String addtionalParam = request.getParameter("data_id");
 			int user_id = Integer.parseInt(request.getParameter("user_id"));
 			String content = request.getParameter("contentID");
 			int cityZId=Integer.parseInt(request.getParameter("cityZID"));
 			//System.out.println("Parameter is "+addtionalParam+", user ID is "+user_id+", Contents is "+content+"City Zone Id is "+cityZId);
 			Gson gson = new Gson();
 			return gson.toJson(uploadDao.addParameter(addtionalParam,user_id,content,cityZId));
 		}
 		@ResponseBody
 		@RequestMapping(value = "/accessByWellIndex", method = RequestMethod.POST)
 		public String granted_project(HttpServletRequest request) throws Throwable{
 			int user_id = Integer.parseInt(request.getParameter("userId"));
 			String wellIndex = request.getParameter("wellIndex");
 			Gson gson = new Gson();
 			return gson.toJson(LoginDao.accesswellIndex4DPTHData(user_id,wellIndex));
 		}
 		@ResponseBody
 		@RequestMapping(value = "/access_mod_del", method = RequestMethod.POST)
 		public String access_mode_delete(HttpServletRequest request) throws Throwable{
 			String id = request.getParameter("access_id");
 			//System.out.println("User Id from Elite User is "+id);
 			Gson gson = new Gson();
 			return gson.toJson(LoginDao.access_modify_delete(id));
 		}
 		
 		@ResponseBody
 		@RequestMapping(value = "/access_mod_gwData", method = RequestMethod.POST)
 		public String access_mode_GWData(HttpServletRequest request) throws Throwable{
 			String id = request.getParameter("access_id");
 			//System.out.println("User Id from GW User is "+id);
 			Gson gson = new Gson();
 			return gson.toJson(LoginDao.access_modify_gwData(id));
 		}
 		@ResponseBody
 		@RequestMapping(value = "/accessGWByWellIndex", method = RequestMethod.POST)
 		public String accessGWDataByWellIndex(HttpServletRequest request) throws Throwable{
 			int userId = Integer.parseInt(request.getParameter("userId"));
 			String wellIndex = request.getParameter("wellIndex");
 			//System.out.println("User Id from GW User is "+id);
 			Gson gson = new Gson();
 			return gson.toJson(LoginDao.accessGWDataByWellIndex(userId,wellIndex));
 		}
 		
 		@ResponseBody
 		@RequestMapping(value = "/accessDatatoUpdate", method = RequestMethod.POST)
 		public String acccessWCRforUpdate(HttpServletRequest request) throws Throwable {
 			int id = Integer.parseInt(request.getParameter("well_id"));
 			//System.out.println("Well Id Is "+id);
 			Gson gson = new Gson();
 			return gson.toJson(LoginDao.selectbyWellId(id));
 		}
 		@ResponseBody
 		@RequestMapping(value = "/accessGWODforUpdate", method = RequestMethod.POST)
 		public String acccessGWOforUpdate(HttpServletRequest request) throws Throwable {
 			int id = Integer.parseInt(request.getParameter("well_id"));
 			//System.out.println("Well Id to Access GWO data is "+id);
 			Gson gson = new Gson();
 			return gson.toJson(LoginDao.accessGWODbyWellId(id));
 		}
 		@ResponseBody
 		@RequestMapping(value = "/delete_WOfile", method = RequestMethod.POST)
 		public String _deleteWOFile(HttpServletRequest request) throws Throwable{
 	 		String wellID = request.getParameter("well_id");
 	 		String user_id=request.getParameter("user_id");
 			System.out.println("Well to be deleted is = "+wellID +" and User id is = "+user_id);
 			Gson gson = new Gson();
 			return gson.toJson(LoginDao._deleteFile(wellID, user_id));
 		}
 		@ResponseBody
 		@RequestMapping(value = "/delete_ODfile", method = RequestMethod.POST)
 		public String proj_deleteFile(HttpServletRequest request) throws Throwable{

 	 		String filname = request.getParameter("well_id");
 	 		String user_id=request.getParameter("user_id");
 			System.out.println("Well to be deleted using Operational Information is = "+filname);
 			Gson gson = new Gson();
 			return gson.toJson( LoginDao.project_deleteFile(filname, user_id));
 		}
 		@ResponseBody
 		@RequestMapping(value = "/acc_region", method = RequestMethod.POST)
 		public String access_yearII(HttpServletRequest request) throws Throwable{
 			String reg_name = request.getParameter("reg_name");
 			//System.out.println("Name of Sub city: "+reg_name);
 			Gson gson = new Gson();
 			return gson.toJson( LoginDao.count_project(reg_name));
 		}
 		@ResponseBody
 		@RequestMapping(value = "/acc_Legend", method = RequestMethod.POST)
 		public String access_by_gened(HttpServletRequest request) throws Throwable{
 			String reg_name = request.getParameter("leg_name");
 			String wellFunction=request.getParameter("checkList");
 			//System.out.println("Name of Function Type is : "+wellFunction);
 			Gson gson = new Gson();
 			return gson.toJson( LoginDao.access_All_wellName_byLegend(reg_name,wellFunction));
 		}
 		@ResponseBody
 		@RequestMapping(value = "/acc_All_pro_data", method = RequestMethod.POST)
 		public String access_project(HttpServletRequest request) throws Throwable{
 			int Pro_def_id = Integer.parseInt(request.getParameter("pro_id"));
 			//System.out.println("Name of region: "+reg_name);
 			Gson gson = new Gson();
 			return gson.toJson( LoginDao.access_ReportByWellId(Pro_def_id));
 		}
 		@ResponseBody
 		@RequestMapping(value = "/acc_proNameId", method = RequestMethod.POST)
 		public String access_toOverlayProName(HttpServletRequest request) throws Throwable{
 			//System.out.println("Name of region: "+reg_name);
 			Gson gson = new Gson();
 			return gson.toJson( LoginDao.access_well_name());
 		}
 		@ResponseBody
 		@RequestMapping(value = "/acc_proName", method = RequestMethod.POST)
 		public String access_ProName(HttpServletRequest request) throws Throwable{
 			String Pro_name = request.getParameter("ProName");
 			//System.out.println("Project Name....: "+Pro_name);
 			Gson gson = new Gson();
 			return gson.toJson( LoginDao.access_specificWellName(Pro_name));
 		}
 		@ResponseBody
 		@RequestMapping(value = "/accessWellBy_parameters", method = RequestMethod.POST)
 		public String accessWellByWellStatus(HttpServletRequest request) throws Throwable {
 			String queryName = request.getParameter("param_name");
 			//System.out.println("Query Parameter = "+queryName);
 			Gson gson = new Gson();
 			return gson.toJson(LoginDao.accessByWellStatus(queryName));
 		}
 		@ResponseBody
 		@RequestMapping(value = "/accessWellBy_funcActive", method = RequestMethod.POST)
 		public String accessWellByWellFunctionStatus(HttpServletRequest request) throws Throwable {
 			Gson gson = new Gson();
 			String wellStatus = request.getParameter("param_name");
 			String functionStatus=request.getParameter("activeStatus");
 				return gson.toJson(LoginDao.accessByWellFunctionStatus(wellStatus,functionStatus));		
 		}
 		//Access Well Name Category
 		@ResponseBody
 		@RequestMapping(value = "/accessByOwnerCatname", method = RequestMethod.POST)
 		public String accessByOwnerCategpryname(HttpServletRequest request) throws Throwable {
 			Gson gson = new Gson();
 			String ownerCategoryName = request.getParameter("param_name");
 				return gson.toJson(LoginDao.accessByOwnerCategpryname(ownerCategoryName));		
 		}
 		//Access Well By Well Depth Range
 		@ResponseBody
 		@RequestMapping(value = "/accessByWellDepthByRange", method = RequestMethod.POST)
 		public String accessByWellDepthByRange(HttpServletRequest request) throws Throwable {
 			Gson gson = new Gson();
 			int depthRange = Integer.parseInt(request.getParameter("param_name"));
 				return gson.toJson(LoginDao.accessByWellDepthByRange(depthRange));		
 		}
 		@ResponseBody
 		@RequestMapping(value = "/accessByDischargeRange", method = RequestMethod.POST)
 		public String accessByCurrentDischargeRange(HttpServletRequest request) throws Throwable {
 			Gson gson = new Gson();
 			double dischargeRange = Double.parseDouble(request.getParameter("param_name"));
 				return gson.toJson(LoginDao.accessByCurrentDischargeRange(dischargeRange));		
 		}
 		@ResponseBody
 		@RequestMapping(value = "/accessBySWLRange", method = RequestMethod.POST)
 		public String accessByCurrentSWLRange(HttpServletRequest request) throws Throwable {
 			Gson gson = new Gson();
 			double dischargeRange = Double.parseDouble(request.getParameter("param_name"));
 				return gson.toJson(LoginDao.accessByCurrentSWLRange(dischargeRange));		
 		}
 		@ResponseBody
 		@RequestMapping(value = "/accessByDWLRange", method = RequestMethod.POST)
 		public String accessByCurrentDWLRange(HttpServletRequest request) throws Throwable {
 			Gson gson = new Gson();
 			double dischargeRange = Double.parseDouble(request.getParameter("param_name"));
 				return gson.toJson(LoginDao.accessByCurrentDWLRange(dischargeRange));		
 		}
 		@ResponseBody
 		@RequestMapping(value = "/accessByTRMTRange", method = RequestMethod.POST)
 		public String accessByCurrentTransimissivityRange(HttpServletRequest request) throws Throwable {
 			Gson gson = new Gson();
 			double dischargeRange = Double.parseDouble(request.getParameter("param_name"));
 			//System.out.println("Searched Key in Controllor is "+dischargeRange);
 				return gson.toJson(LoginDao.accessByCurrentTransimissivityRange(dischargeRange));		
 		}
 		
 		@ResponseBody
 		@RequestMapping(value = "/waterChemistryUrl", method = RequestMethod.POST)
 		public String waterChemistryUrl(HttpServletRequest request) throws Throwable{
 			int id = Integer.parseInt(request.getParameter("well_id"));
 			Gson gson = new Gson();
 			return gson.toJson(LoginDao.accessHydroChemistryData(id));
 		}
 		@ResponseBody
 		@RequestMapping(value = "/accessBYTDS", method = RequestMethod.POST)
 		public String accessBYTDS(HttpServletRequest request) throws Throwable {
 			Gson gson = new Gson();
 			double TDSRange = Double.parseDouble(request.getParameter("param_name"));
 			//System.out.println("TDS value is "+TDSRange);
 				return gson.toJson(LoginDao.accessBYTDS(TDSRange));		
 		}
 		@ResponseBody
 		@RequestMapping(value = "/accessBYEC", method = RequestMethod.POST)
 		public String accessBYEC(HttpServletRequest request) throws Throwable {
 			Gson gson = new Gson();
 			double ECRange = Double.parseDouble(request.getParameter("param_name"));
 			//System.out.println("EC value is "+ECRange);
 				return gson.toJson(LoginDao.accessBYEC(ECRange));		
 		}
 		@ResponseBody
 		@RequestMapping(value = "/accessBYFlouride", method = RequestMethod.POST)
 		public String accessBYFlouride(HttpServletRequest request) throws Throwable {
 			Gson gson = new Gson();
 			double ECRange = Double.parseDouble(request.getParameter("param_name"));
 			//System.out.println("EC value is "+ECRange);
 				return gson.toJson(LoginDao.accessBYFlouride(ECRange));		
 		}
 		@ResponseBody
 		@RequestMapping(value = "/accessBYIron", method = RequestMethod.POST)
 		public String accessBYIron(HttpServletRequest request) throws Throwable {
 			Gson gson = new Gson();
 			double ECRange = Double.parseDouble(request.getParameter("param_name"));
 			//System.out.println("EC value is "+ECRange);
 				return gson.toJson(LoginDao.accessBYIron(ECRange));		
 		}
 		@ResponseBody
 		@RequestMapping(value = "/accessBYNitrate", method = RequestMethod.POST)
 		public String accessBYNitrate(HttpServletRequest request) throws Throwable {
 			Gson gson = new Gson();
 			double No3Range = Double.parseDouble(request.getParameter("param_name"));
 			//System.out.println("EC value is "+ECRange);
 				return gson.toJson(LoginDao.accessBYNitrate(No3Range));		
 		}
 		@ResponseBody
 		@RequestMapping(value = "/accessBYTemprature", method = RequestMethod.POST)
 		public String accessBYTemprature(HttpServletRequest request) throws Throwable {
 			Gson gson = new Gson();
 			double No3Range = Double.parseDouble(request.getParameter("param_name"));
 			//System.out.println("EC value is "+ECRange);
 				return gson.toJson(LoginDao.accessBYTemprature(No3Range));		
 		}
 		
 		@ResponseBody
 		@RequestMapping(value = "/R_rport", method = RequestMethod.POST)
 		public String report_bar(HttpServletRequest request) throws Throwable{
 			Gson gson = new Gson();
 			return gson.toJson( LoginDao.reports_bar());
 		}
}
