import java.sql.*;
import java.io.*;
import java.lang.*;
import java.util.*;

/**
 * This class fix set the score value of all the answer the same as the 
 * totalscore of the item.
 *
 * @author Daisy Flemming<daisyf@stanford.edu>
 */
public class FixRealmPermission {
    private static String[] access_functionName = {"assessment.takeAssessment", "assessment.submitAssessmentForGrade"};
    private static String[] maintain_functionName = {"assessment.createAssessment", 
                                              "assessment.editAssessment.any", "assessment.editAssessment.own",
                                              "assessment.deleteAssessment.any", "assessment.deleteAssessment.own",
                                              "assessment.publishAssessment.any", "assessment.publishAssessment.own",
                                              "assessment.gradeAssessment.any", "assessment.gradeAssessment.own",
                                              "assessment.questionpool.create",
                                              "assessment.questionpool.edit.own", "assessment.questionpool.delete.own",
                                              "assessment.questionpool.copy.own",
                                              "assessment.template.create",
                                              "assessment.template.edit.own", "assessment.template.delete.own"};

    public static void main(String args[])
    {
      if (args[0].equals("getRealm")){
        getRealmIdList();
      }
      if (args[0].equals("fixRealmPermission")){
        List realmIdList = getRealmIdList();
        fixRealmPermission(realmIdList,"access", access_functionName);
        fixRealmPermission(realmIdList,"maintain", maintain_functionName);
      }
      if (args[0].equals("getInsertStatement")){
        List realmIdList = getRealmIdList();
        addRealmPermission(realmIdList,"access", access_functionName);
        addRealmPermission(realmIdList,"maintain", maintain_functionName);
      }
    }

    public static ArrayList addRealmPermission(List l, String roleName, String[] functionNameArray){
      ArrayList queryList = new ArrayList();
      for (int i=0; i<functionNameArray.length; i++){
        System.out.println();
        String functionName = functionNameArray[i];
        HashMap h = getRealmHash(roleName, functionName);
        for (int j =0; j<l.size(); j++){
          String realmId = (String) l.get(j);
          if (h.get(realmId)==null){
            // insert
            String query = "INSERT INTO SAKAI_REALM_RL_FN VALUES"+
                           "((select REALM_KEY from SAKAI_REALM where REALM_ID = '"+realmId+
                           "'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '"+roleName+
                           "'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION "+
                           "where FUNCTION_NAME = '"+functionName+"'))"; 
            System.out.println(query+";");
            queryList.add(query); 
	  }
	}
      }
      return queryList;
    }

    public static void fixRealmPermission(List realmIdList, String roleName, String[] functionNameArray){

      Properties prop = getProperties(); 
      String driver = prop.getProperty("driver");
      String url = prop.getProperty("url");
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      ArrayList queryList = addRealmPermission(realmIdList, roleName, functionNameArray);

      try{
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url);
        for (int i=0; i<queryList.size(); i++){
          String query = (String)queryList.get(i);
          stmt = conn.prepareStatement(query);
          stmt.executeUpdate();
          if (stmt != null){ stmt.close(); };
	}
      } 
      catch (Exception e) {
          e.printStackTrace();
      }
      finally{
        try {
	  if (rs !=null){rs.close();}
	  if (stmt !=null){stmt.close();}
	  if (conn !=null){conn.close();}
        } catch (Exception e1){
          e1.printStackTrace();
	}
      }
    }

    public static List getRealmIdList(){
      System.out.println("List of course/project REALM_ID in site:");
      ArrayList list = new ArrayList();
      Properties prop = getProperties(); 
      String driver = prop.getProperty("driver");
      String url = prop.getProperty("url");

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      try{
        // Connect to the database
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url);
        String query = "select REALM_ID from SAKAI_REALM where REALM_ID like '/site/%' " +
                       " and REALM_ID != '/site/mercury' " +
                       " and REALM_ID not like '/site/!%' " +
                       " and REALM_ID not like '/site/~%' ";
        stmt = conn.prepareStatement(query);
        rs = stmt.executeQuery();
        while (rs.next())
        {
          String realm_id = rs.getString("REALM_ID");
          System.out.println(realm_id);
          list.add(realm_id);
	}
      } 
      catch (Exception e) {
          e.printStackTrace();
      }
      finally{
        try {
	  if (rs !=null){rs.close();}
	  if (stmt !=null){stmt.close();}
	  if (conn !=null){conn.close();}
        } catch (Exception e1){
          e1.printStackTrace();
	}
      }
      System.out.println();
      return list;
    }

    //returns HashMap(realmId, count), represent count per realmId that has the given roleKey & functionName
    public static HashMap getRealmHash(String roleName, String functionName){
      HashMap h = new HashMap();
      ArrayList list = new ArrayList();
      Properties prop = getProperties(); 
      String driver = prop.getProperty("driver");
      String url = prop.getProperty("url");

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      try{
        // Connect to the database
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url);
        String query = "select rm.REALM_ID from SAKAI_REALM rm, SAKAI_REALM_ROLE rr, "+
                       " SAKAI_REALM_FUNCTION rf, SAKAI_REALM_RL_FN r where "+
                       " rm.REALM_KEY = r.REALM_KEY " +
                       " and rr.ROLE_KEY = r.ROLE_KEY and rr.ROLE_NAME=?" +
                       " and rf.FUNCTION_KEY = r.FUNCTION_KEY and rf.FUNCTION_NAME=?";
        stmt = conn.prepareStatement(query);
        stmt.setString(1, roleName);
        stmt.setString(2, functionName);
        rs = stmt.executeQuery();
        while (rs.next())
        {
          String realm_id = rs.getString("REALM_ID");
          h.put(realm_id, "1"); // if there is a record, that means the function is available to the realm
	}
      } 
      catch (Exception e) {
          e.printStackTrace();
      }
      finally{
        try {
	  if (rs !=null){rs.close();}
	  if (stmt !=null){stmt.close();}
	  if (conn !=null){conn.close();}
        } catch (Exception e1){
          e1.printStackTrace();
	}
      }
      return h;
    }

    public static Properties getProperties(){
      Connection conn = null;
      Properties prop = new Properties();
      try {
        FileInputStream in = new FileInputStream("database.properties");
        prop.load(in);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      return prop;
    }

}
