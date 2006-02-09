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
public class FixAnswerScore {
    public static void main(String args[])
    {
      if (args[0].equals("fixAnswerScore")){
        fixImportedScore();
      }
      if (args[0].equals("regrade")){
        if (args.length > 1)
          regrade(args[1], "regrade");
	else
          System.out.println("Usage: regrade <publishedAssessmentId>");
      }
      if (args[0].equals("fixAndRegrade")){
        if (args.length > 1){
          fixImportedScore();
          regrade(args[1], "fixAndRegrade");
	}
	else
          System.out.println("Usage: fixAndRegrade <publishedAssessmentId>");
      }
    }

    public static void regrade(String assessmentIdString, String command){
      try {
        Long assessmentId = new Long(assessmentIdString);
        // 1. fix MC/MCMR/TF/Matching question
        // and answer submitted is saved in sam_itemGrading_t.publishedAnswerId
        regradeMC(assessmentId);
        // 2. fix FIB question
        regradeFIB(assessmentId);

        updateAllAssessmentGrading(assessmentId);
      }
      catch(Exception e){
          System.out.println("Usage: "+command+" <publishedAssessmentId>");
      }
    }


    public static void fixImportedScore(){
        // 1. fix item answer score
        System.out.println("Updating Imported Assessment Answer Score .....");
        HashMap itemScoreHash1 = getAllImportedItemScore();
        updateItemAnswerScore(itemScoreHash1);
        
        // 2. fix published item answer score
        System.out.println("Updating Published Assessment Answer Score .....");
        HashMap itemScoreHash2 = getAllImportedPublishedItemScore();
        updatePublishedItemAnswerScore(itemScoreHash2);
    }

    public static void regradeMC(Long assessmentId){
      HashMap answerScoreHash = new HashMap(); // (answerId, scoreCarried)
      System.out.println("Get all incorrect MC/MCMR/TF/Matching answer Score .....");
      getAllIncorrectAnswer(answerScoreHash, assessmentId);
      System.out.println("Get all correct MC/MCMR/TF/Matching answer Score .....");
      getAllCorrectAnswer(answerScoreHash, assessmentId);
      System.out.println("Updating Item Grading .....");
      updateItemGrading(answerScoreHash);
    }

    public static void regradeFIB(Long assessmentId){
      HashMap answerScoreHash = new HashMap(); // (answerId, scoreCarried)
      System.out.println("Get all correct FIB answer Score .....");
      HashMap correctAnswerHash = getAllCorrectFIBAnswer(answerScoreHash,assessmentId); // (answerId, answerText)
      System.out.println("Updating Item Grading .....");
      updateFIBItemGrading(correctAnswerHash, answerScoreHash, assessmentId);
    }

    public static void updateItemGrading(HashMap answerScoreHash){

      Properties prop = getProperties(); 
      String driver = prop.getProperty("driver");
      String url = prop.getProperty("url");
      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      String query="update SAM_ITEMGRADING_T set AUTOSCORE=? where PUBLISHEDANSWERID=?";

      try{
        // Connect to the database to get all the answer
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url);

        Set keys = answerScoreHash.keySet();
        Iterator iter = keys.iterator();
        
        while (iter.hasNext()){
          Long answerId = (Long) iter.next();
          Float score = (Float) answerScoreHash.get(answerId);
          System.out.println("answerId="+answerId.longValue()+", score="+score.floatValue());
          stmt = conn.prepareStatement(query);
          stmt.setFloat(1, score.floatValue());
          stmt.setLong(2, answerId.longValue());
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

    public static void updateFIBItemGrading(HashMap correctAnswerHash, HashMap answerScoreHash, Long assessmentId){

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
        String query="select i.PUBLISHEDANSWERID, i.ANSWERTEXT, i.ITEMGRADINGID from SAM_ITEMGRADING_T i, SAM_PUBLISHEDASSESSMENT_t p, SAM_PUBLISHEDSECTION_t s, SAM_PUBLISHEDITEM_T t where p.ID=s.ASSESSMENTID and s.SECTIONID=t.SECTIONID and t.ITEMID=i.PUBLISHEDITEMID and t.TYPEID=8 and p.ID="+assessmentId.longValue();
        stmt = conn.prepareStatement(query);
        rs = stmt.executeQuery();
        while (rs.next())
        {
          float autoScore =0;
          if (rs.getObject("PUBLISHEDANSWERID") !=null){
            long correctAnswerId = rs.getLong("PUBLISHEDANSWERID");
            String answerSubmitted = rs.getString("ANSWERTEXT");
            String correctAnswer = (String)correctAnswerHash.get(new Long(correctAnswerId));
           
            autoScore = 0;
            if (answerSubmitted!=null && correctAnswer!=null &&(answerSubmitted.trim()).equals(correctAnswer.trim())){
              if (answerScoreHash.get(new Long(correctAnswerId))!=null)
                autoScore = ((Float)answerScoreHash.get(new Long(correctAnswerId))).floatValue();
	    }

            // update ItemGrading accordingly
            query="update SAM_ITEMGRADING_T set AUTOSCORE=? where ITEMGRADINGID="+rs.getLong("ITEMGRADINGID");
            stmt = conn.prepareStatement(query);
            stmt.setFloat(1, autoScore);
            stmt.executeUpdate();
            if (stmt != null){ stmt.close(); };
            System.out.println("correctAnswerId="+correctAnswerId+", autoScore="+autoScore);
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

    public static List getAssessmentGradingList(Long assessmentId){
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
        stmt = conn.prepareStatement
	           ("select ASSESSMENTGRADINGID from SAM_ASSESSMENTGRADING_T where PUBLISHEDASSESSMENTID="+assessmentId.longValue());
        rs = stmt.executeQuery();
        while (rs.next())
        {
          long id = rs.getLong("ASSESSMENTGRADINGID");
          list.add(new Long(id));
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

    public static void updateAllAssessmentGrading(Long assessmentId){
      List list = getAssessmentGradingList(assessmentId);
      for (int i=0; i<list.size();i++){
        updateAssessmentGrading((Long)list.get(i));
      }
    }

    public static void updateAssessmentGrading(Long assessmentGradingId){

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

        String query="select SUM(AUTOSCORE) from SAM_ITEMGRADING_T where ASSESSMENTGRADINGID=?";
        stmt = conn.prepareStatement(query);
        stmt.setLong(1, assessmentGradingId.longValue());
        rs = stmt.executeQuery();
        if (rs.next())
        {
          sum = rs.getFloat(1);
	}

        query="select TOTALOVERRIDESCORE from SAM_ASSESSMENTGRADING_T where ASSESSMENTGRADINGID=?";
        stmt = conn.prepareStatement(query);
        stmt.setLong(1, assessmentGradingId.longValue());
        rs = stmt.executeQuery();
        if (rs.next())
        {
          finalScore = sum + rs.getFloat(1);
          System.out.println("sum="+sum+ ", finalScore="+finalScore +", gradingId="+assessmentGradingId.longValue());
	}

        query="update SAM_ASSESSMENTGRADING_T set TOTALAUTOSCORE=?, FINALSCORE=? where ASSESSMENTGRADINGID=?";
        stmt = conn.prepareStatement(query);
        stmt.setFloat(1, sum);
        stmt.setFloat(2, finalScore);
        stmt.setLong(3, assessmentGradingId.longValue());
        stmt.executeUpdate();
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

    public static void getAllIncorrectAnswer(HashMap h, Long assessmentId){
      Properties prop = getProperties(); 

      String driver = prop.getProperty("driver");
      String url = prop.getProperty("url");

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      String query = "select ans.* from SAM_PUBLISHEDASSESSMENT_T a, SAM_PUBLISHEDSECTION_T s, SAM_PUBLISHEDITEM_T t, SAM_PUBLISHEDANSWER_T ans where a.ID=s.ASSESSMENTID and s.SECTIONID=t.SECTIONID and t.ITEMID=ans.ITEMID and ans.ISCORRECT='0' and (t.TYPEID=1 or t.TYPEID=2 or t.TYPEID=4 or t.TYPEID=9) and a.ID="+assessmentId.longValue() ;
      try{
        // Connect to the database
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url);
        stmt = conn.prepareStatement(query);
        rs = stmt.executeQuery();
        while (rs.next())
        {
          long answerId = rs.getLong("ANSWERID");
          System.out.println(answerId +":"+ 0);
          h.put(new Long(answerId), new Float(0));
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

    public static void getAllCorrectAnswer(HashMap h, Long assessmentId){

      HashMap scoreHash = getAverageScoreHash(assessmentId);
      Properties prop = getProperties(); 
      String driver = prop.getProperty("driver");
      String url = prop.getProperty("url");

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      String query = "select ans.* from SAM_PUBLISHEDASSESSMENT_T a, SAM_PUBLISHEDSECTION_T s, SAM_PUBLISHEDITEM_T t, SAM_PUBLISHEDANSWER_T ans where a.ID=s.ASSESSMENTID and s.SECTIONID=t.SECTIONID and t.ITEMID=ans.ITEMID and ans.ISCORRECT='1' and a.ID="+assessmentId.longValue() + " and (t.TYPEID=1 or t.TYPEID=2 or t.TYPEID=4 or t.TYPEID=9) order by ans.ITEMID";
      try{
        // Connect to the database
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url);
        stmt = conn.prepareStatement(query);
        rs = stmt.executeQuery();
        int correctCount = 0;
        long currentItemId = 0; 
        while (rs.next())
        {
          long itemId = rs.getLong("ITEMID");
          long answerId = rs.getLong("ANSWERID");
          h.put(new Long(answerId), (Float)scoreHash.get(new Long(itemId)));
          System.out.println(answerId +":"+ scoreHash.get(new Long(itemId)));
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

    public static HashMap getAllCorrectFIBAnswer(HashMap answerScoreHash, Long assessmentId){

      HashMap scoreHash = getAverageScoreHash(assessmentId);
      HashMap h = new HashMap();
      Properties prop = getProperties(); 
      String driver = prop.getProperty("driver");
      String url = prop.getProperty("url");

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      String query = "select ans.* from SAM_PUBLISHEDASSESSMENT_T a, SAM_PUBLISHEDSECTION_T s, SAM_PUBLISHEDITEM_T t, SAM_PUBLISHEDANSWER_T ans where a.ID=s.ASSESSMENTID and s.SECTIONID=t.SECTIONID and t.ITEMID=ans.ITEMID and ans.ISCORRECT='1' and a.ID="+assessmentId.longValue() + " and t.TYPEID=8 order by ans.ITEMID";
      try{
        // Connect to the database
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url);
        stmt = conn.prepareStatement(query);
        rs = stmt.executeQuery();
        int correctCount = 0;
        long currentItemId = 0; 
        while (rs.next())
        {
          long itemId = rs.getLong("ITEMID");
          long answerId = rs.getLong("ANSWERID");
          h.put(new Long(answerId), rs.getString("TEXT")); //(answerId, answerText)
          answerScoreHash.put(new Long(answerId), (Float)scoreHash.get(new Long(itemId)));
          System.out.println(answerId +":"+ scoreHash.get(new Long(itemId)));
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

    public static HashMap getAverageScoreHash(Long assessmentId){

      HashMap h = new HashMap();
      Properties prop = getProperties(); 
      String driver = prop.getProperty("driver");
      String url = prop.getProperty("url");

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet rs = null;
      String query = "select ans.* from SAM_PUBLISHEDASSESSMENT_T a, SAM_PUBLISHEDSECTION_T s, SAM_PUBLISHEDITEM_T t, SAM_PUBLISHEDANSWER_T ans where a.ID=s.ASSESSMENTID and s.SECTIONID=t.SECTIONID and ans.ITEMID=t.ITEMID and ans.ISCORRECT='1' and a.ID="+assessmentId.longValue() + " order by ans.ITEMID";
      try{
        // Connect to the database
        Class.forName(driver).newInstance();
        conn = DriverManager.getConnection(url);
        stmt = conn.prepareStatement(query);
        rs = stmt.executeQuery();
        int correctCount = 1;
        long currentItemId = 0; 
        long itemId = 0;
        float currentScore = 0;
        float averageScore = 0;
        while (rs.next())
        {
          itemId = rs.getLong("ITEMID");
          if (itemId == currentItemId)
            correctCount++;
          else {
            if (currentItemId != 0){
              averageScore = currentScore/correctCount;
              h.put(new Long(currentItemId), new Float(averageScore));
	    }
            correctCount = 1;
            currentItemId = itemId;
            currentScore = rs.getFloat("SCORE");
	  }
	}
        averageScore = currentScore/correctCount;
        h.put(new Long(currentItemId), new Float(averageScore));   
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


    public static HashMap getAllImportedItemScore(){
      String query = "select t.ITEMID, t.SCORE from SAM_ASSESSMENTBASE_T a, SAM_SECTION_T s, SAM_ITEM_T t where a.COMMENTS like '%Imported assessment%' and a.ISTEMPLATE=0 and a.ID=s.ASSESSMENTID and s.SECTIONID=t.SECTIONID";
      return getAllImportedAssessmentItemScore(query);
    }

    public static HashMap getAllImportedPublishedItemScore(){
      String query = "select t.ITEMID, t.SCORE from SAM_PUBLISHEDASSESSMENT_T a, SAM_PUBLISHEDSECTION_T s, SAM_PUBLISHEDITEM_T t where a.COMMENTS like '%Imported assessment%' and a.ID=s.ASSESSMENTID and s.SECTIONID=t.SECTIONID";
      return getAllImportedAssessmentItemScore(query);
    }

    public static HashMap getPublishedItemScore(long assessmentId){
      String query = "select t.ITEMID, t.SCORE from SAM_PUBLISHEDASSESSMENT_T a, SAM_PUBLISHEDSECTION_T s, SAM_PUBLISHEDITEM_T t where a.ID=s.ASSESSMENTID and s.SECTIONID=t.SECTIONID and a.ID="+assessmentId;
      return getAllImportedAssessmentItemScore(query);
    }

    public static HashMap getAllImportedAssessmentItemScore(String query){
      HashMap h = new HashMap();
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
        stmt = conn.prepareStatement(query);
        rs = stmt.executeQuery();
        while (rs.next())
        {
          long id = rs.getLong("ITEMID");
          float score = rs.getFloat("SCORE");
          System.out.println("itemId="+id+":"+score);
          h.put(new Long(id), new Float(score));
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

    public static void updateItemAnswerScore(HashMap itemScoreHash){
      String query="update SAM_ANSWER_T set SCORE=? where ITEMID=?";
      updateAnswerScore(itemScoreHash, query);
    }

    public static void updatePublishedItemAnswerScore(HashMap itemScoreHash){
      String query="update SAM_PUBLISHEDANSWER_T set SCORE=? where ITEMID=?";
      updateAnswerScore(itemScoreHash, query);
    }

    public static void updateAnswerScore(HashMap itemScoreHash, String query){

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

        Set keys = itemScoreHash.keySet();
        Iterator iter = keys.iterator();
        
        while (iter.hasNext()){
          Long itemId = (Long) iter.next();
          Float score = (Float) itemScoreHash.get(itemId);
          if (score!=null){
            stmt = conn.prepareStatement(query);
            stmt.setFloat(1, score.floatValue());
            stmt.setLong(2, itemId.longValue());
            stmt.executeUpdate();
            if (stmt != null){ stmt.close(); };
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

    /** return list of AssessmentId (Long) */
    public static List getAllImportedAssessment(){
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
        stmt = conn.prepareStatement
	           ("select * from SAM_ASSESSMENTBASE_T where COMMENTS like '%Imported assessment%' and ISTEMPLATE=0");
        rs = stmt.executeQuery();
        while (rs.next())
        {
          long id = rs.getLong("ID");
          String comments = rs.getString("COMMENTS");
          System.out.println("assessmentId="+id+":"+comments);
          list.add(new Long(id));
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


    /** return list of PublishedAssessmentId (Long) */
    public static List getAllImportedPublishedAssessment(){
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
        stmt = conn.prepareStatement
	           ("select * from SAM_PUBLISHEDASSESSMENT_T where COMMENTS like '%Imported assessment%'");
        rs = stmt.executeQuery();
        while (rs.next())
        {
          long id = rs.getLong("ID");
          System.out.println("publishedAssessmentId="+id);
          list.add(new Long(id));
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
    
