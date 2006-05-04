import java.sql.*;
import java.io.*;
import java.lang.*;
import java.util.*;

/**
 * This class removes any duplicate MC/Survey submitted answer so that only the last one stay in
 * the database. It will also recalculate the final score for an assessment and fix up 
 * data in the gradebook, So that final score in bothe assessment and gradebook is
 * consistent. Note that the item score will not be modified and we can't fix the MCMR question type.
 *
 * @author Daisy Flemming<daisyf@stanford.edu>
 */
public class FixGradingScore {

    public static void main(String args[]){
      if (args[0].equals("fixGradingScore")){
        if (args.length > 1)
          process(args[1], true); // true => persist
        else
          System.out.println("Usage: fixGradingScore <publishedAssessmentId>");
      }
      if (args[0].equals("printFixGradingScore")){
        if (args.length > 1)
          process(args[1], false); // false => just print
        else
          System.out.println("Usage: printFixGradingScore <publishedAssessmentId>");
      }
    }

    public static void process(String pubAssessmentIdString, boolean persist){
        Long pubAssessmentId = new Long(pubAssessmentIdString);
        // 1a. get list of assessmentGradingId who has submitted the assesment
        //  b. get the list of itemGrading taht need to be deleted
        ArrayList assessmentGradingList = getAssessmentGradingList(pubAssessmentId);

        // 2. fix assessmentGrading score
        System.out.println();
        System.out.println("--- fix assessmentGrading score ---");
        updateAllAssessmentGrading(assessmentGradingList, persist);

        // 3. fix GB score
        System.out.println();
        System.out.println("--- fix GB score where assessmentGrading.forGrade=1 ---");
        int scoringType = getScoringType(pubAssessmentId);
        HashMap assessmentGradingMap = getAssessmentGradingMap(pubAssessmentId, scoringType);
        updateGradebookScore(pubAssessmentId, assessmentGradingMap, persist);
    }

    /* return a list of assessmentGradingId for a published assessment */
    public static ArrayList getAssessmentGradingList(Long assessmentId){
 
      ArrayList list = new ArrayList();
      Properties prop = getProperties(); 
      String driver = prop.getProperty("driver");
      String url = prop.getProperty("url");

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      String query = "select a.ASSESSMENTGRADINGID "+
                     " from SAM_ASSESSMENTGRADING_T a "+
                     " where a.PUBLISHEDASSESSMENTID="+assessmentId.toString();
      try{
        // Connect to the database
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url);
        stmt = conn.prepareStatement(query);
        rs = stmt.executeQuery();
        long currentAssessmentGradingId=0;
        long currentPublishedItemId=0;
        while (rs.next()){
          long assesmentGradingId = rs.getLong("ASSESSMENTGRADINGID");
          list.add(new Long(assesmentGradingId));
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
      return list;
    }


    public static void updateAllAssessmentGrading(ArrayList list, boolean persist){
      for (int i=0; i<list.size();i++){
        updateAssessmentGrading((Long)list.get(i), persist);
      }
    }

    public static void updateAssessmentGrading(Long assessmentGradingId, boolean persist){

      Properties prop = getProperties(); 
      String driver = prop.getProperty("driver");
      String url = prop.getProperty("url");
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      float sum = 0;
      float finalScore = 0;
      try{
        // Connect to the database to get all the answer
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url);

        String query="update SAM_ASSESSMENTGRADING_T set "+
                     " TOTALAUTOSCORE=(select SUM(AUTOSCORE) from SAM_ITEMGRADING_T where ASSESSMENTGRADINGID="+
                       assessmentGradingId.toString()+"), "+
                     " FINALSCORE=TOTALOVERRIDESCORE + "+
                     "(select SUM(AUTOSCORE) from SAM_ITEMGRADING_T where ASSESSMENTGRADINGID="+
                       assessmentGradingId.toString()+") "+
                     " where ASSESSMENTGRADINGID="+assessmentGradingId.toString();
        System.out.println(query);
        if (persist){           
          stmt = conn.prepareStatement(query);
          stmt.executeUpdate();
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

    public static void updateGradebookScore(Long assessmentId, 
                                            HashMap map, boolean persist){
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
        Set keys = map.keySet();
        Iterator iter = keys.iterator();
        while (iter.hasNext()){
          String studentId = (String) iter.next();
          Long assessmentGradingId = (Long) map.get(studentId);
          String finalScore ="(select FINALSCORE from SAM_ASSESSMENTGRADING_T where ASSESSMENTGRADINGID="+
                             assessmentGradingId.toString()+") ";
          String title ="(select TITLE from SAM_PUBLISHEDASSESSMENT_T where ID="+assessmentId.toString()+")";
          String gradableObjectId ="(select ID from GB_GRADABLE_OBJECT_T where NAME="+title+")";
          String query = "update GB_GRADE_RECORD_T set "+
                  "POINTS_EARNED="+finalScore+" where "+
                  "STUDENT_ID='"+studentId+"' and "+  
                  "GRADABLE_OBJECT_ID="+gradableObjectId;
          System.out.println(query);
          if (persist){
            stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
	  }
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

    /* 1. get only the assessmentGrading where forgrade=1  and
     * 2. the last or the highest based on publishedAssessment settings
     */
    public static HashMap getAssessmentGradingMap(Long assessmentId, int scoringType){
      HashMap map = new HashMap();
      Properties prop = getProperties(); 
      String driver = prop.getProperty("driver");
      String url = prop.getProperty("url");

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      String query ="select ASSESSMENTGRADINGID, AGENTID from SAM_ASSESSMENTGRADING_T where "+
                    " PUBLISHEDASSESSMENTID="+assessmentId.longValue()+
                    " and FORGRADE=1 order by AGENTID ASC, SUBMITTEDDATE DESC" ;
      if (scoringType == 1){ // highest
        query ="select ASSESSMENTGRADINGID, AGENTID from SAM_ASSESSMENTGRADING_T where "+
               " PUBLISHEDASSESSMENTID="+assessmentId.longValue()+
               " and FORGRADE=1 order by AGENTID ASC, FINALSCORE DESC" ;
      }
      try{
        // Connect to the database
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url);
        stmt = conn.prepareStatement(query);
        rs = stmt.executeQuery();
        while (rs.next())
        {
          long id = rs.getLong("ASSESSMENTGRADINGID");
          String agentId = rs.getString("AGENTID");
          if (map.get(agentId)==null)
            map.put(agentId, new Long(id));
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
      return map;
    }


    public static int getScoringType(Long assessmentId){
      int scoringType = 1; // 1=> highest; 2=> last
      ArrayList list = new ArrayList();
      Properties prop = getProperties(); 
      String driver = prop.getProperty("driver");
      String url = prop.getProperty("url");

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      String query = "select SCORINGTYPE "+
                     " from SAM_PUBLISHEDEVALUATION_T "+
                     " where ASSESSMENTID="+assessmentId.toString();
      try{
        // Connect to the database
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url);
        stmt = conn.prepareStatement(query);
        rs = stmt.executeQuery();
        long currentAssessmentGradingId=0;
        long currentPublishedItemId=0;
        if (rs.next()){
          scoringType = rs.getInt("SCORINGTYPE");
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
      return scoringType;
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
    
