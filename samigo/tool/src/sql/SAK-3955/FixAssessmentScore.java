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
public class FixAssessmentScore {

    public static void main(String args[]){
      if (args[0].equals("fixAssessmentScore")){
        if (args.length > 1)
          process(args[1], true); // true => persist
        else
          System.out.println("Usage: fixAssessmentScore <publishedAssessmentId>");
      }
      if (args[0].equals("printFixAssessmentScore")){
        if (args.length > 1)
          process(args[1], false); // false => just print
        else
          System.out.println("Usage: printFixAssessmentScore <publishedAssessmentId>");
      }
    }

    public static void process(String pubAssessmentIdString, boolean persist){
        Long pubAssessmentId = new Long(pubAssessmentIdString);
        // 1a. get list of assessmentGradingId who has submitted the assesment
        //  b. get the list of itemGrading taht need to be deleted
        ArrayList assessmentGradingList = new ArrayList();
        ArrayList itemGradingList = new ArrayList();
        getItemGradingList(pubAssessmentId, assessmentGradingList, itemGradingList);

        // 2. fix extra itemGrading
        System.out.println();
        System.out.println("--- fix extra itemGradings ---");
        fixItemGradings(itemGradingList, persist);

        // 3. fix assessmentGrading score
        System.out.println();
        System.out.println("--- fix assessmentGrading score ---");
        updateAllAssessmentGrading(assessmentGradingList, persist);

        // 4. fix GB score
        System.out.println();
        System.out.println("--- fix GB score where assessmentGrading.forGrade=1 ---");
        HashMap assessmentGradingMap = getAssessmentGradingMap(pubAssessmentId);
        updateGradebookScore(pubAssessmentId, assessmentGradingMap, persist);
    }

    /* return a list of assessmentGradingId for a published assessment */
    public static void getItemGradingList(Long assessmentId, ArrayList assessmentGradingList, 
                                     ArrayList itemGradingList){
      Properties prop = getProperties(); 
      String driver = prop.getProperty("driver");
      String url = prop.getProperty("url");

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      String query = "select a.ASSESSMENTGRADINGID, i.PUBLISHEDITEMID, i.ITEMGRADINGID "+
                     " from SAM_ASSESSMENTGRADING_T a, SAM_ITEMGRADING_T i, SAM_PUBLISHEDITEM_T item "+ 
                     " where a.PUBLISHEDASSESSMENTID="+assessmentId.toString() +
                     " and i.ASSESSMENTGRADINGID=a.ASSESSMENTGRADINGID "+
                     " and i.PUBLISHEDITEMID = item.ITEMID "+
                     " and (item.TYPEID=1 or item.TYPEID=3) "+
                     " order by a.ASSESSMENTGRADINGID ASC, i.PUBLISHEDITEMID ASC, i.ITEMGRADINGID DESC";         
      try{
        // Connect to the database
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url);
        stmt = conn.prepareStatement(query);
        rs = stmt.executeQuery();
        long currentAssessmentGradingId=0;
        long currentPublishedItemId=0;
        while (rs.next()){
	  //1. build a list of assessmentGradingId that we need to update
          long assesmentGradingId = rs.getLong("ASSESSMENTGRADINGID");
          if (currentAssessmentGradingId != assesmentGradingId){
	    currentAssessmentGradingId = assesmentGradingId;
            assessmentGradingList.add(new Long(currentAssessmentGradingId));
            currentPublishedItemId = 0;
	  }
          //2. build a list of itemGrading that we need to fix
          long publishedItemId = rs.getLong("PUBLISHEDITEMID");
          if (currentPublishedItemId != publishedItemId){
            currentPublishedItemId = publishedItemId;
	  }
          else{ // keep the latest itemGrading and reset the rest of them
            itemGradingList.add(new Long(rs.getLong("ITEMGRADINGID")));
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


    public static void fixItemGradings(ArrayList list, boolean persist){

      Properties prop = getProperties(); 
      String driver = prop.getProperty("driver");
      String url = prop.getProperty("url");
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;

      try{
        // Connect to the database to get all the answer
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url);

        for (int i=0; i<list.size(); i++){
          Long itemGradingId= (Long) list.get(i);
          String query="update SAM_ITEMGRADING_T set "+
                       " PUBLISHEDANSWERID = NULL, "+
                       " ANSWERTEXT = NULL, "+
                       " AUTOSCORE = 0 "+
                       " where ITEMGRADINGID="+itemGradingId.toString();
          System.out.println(query);
          if (persist){           
            stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
	  }
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

    public static void updateGradebookScore(Long assessmentId, HashMap map, boolean persist){
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
          Long assessmentGradingId =(Long)iter.next();
          String studentId = (String) map.get(assessmentGradingId);
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

    /* get only the assessmentGrading where forgrade=1 */
    public static HashMap getAssessmentGradingMap(Long assessmentId){
      HashMap map = new HashMap();
      Properties prop = getProperties(); 
      String driver = prop.getProperty("driver");
      String url = prop.getProperty("url");

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      String query ="select ASSESSMENTGRADINGID, AGENTID from SAM_ASSESSMENTGRADING_T where "+
                    " PUBLISHEDASSESSMENTID="+assessmentId.longValue()+
                    " and FORGRADE=1" ;
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
          map.put(new Long(id), agentId);
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
    
