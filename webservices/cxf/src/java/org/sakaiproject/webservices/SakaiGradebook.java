package org.sakaiproject.webservices;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.Session;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by: SCRIBA
 * Date: 8/19/15
 * Time: 10:21 AM
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public class SakaiGradebook extends AbstractWebService {
    private static final Log LOG = LogFactory.getLog(SakaiGradebook.class);

    /**
     * @param scaleUuid - unique id for this gradescale
     * @param scaleName - display name for this gradescale
     * @param grades    - array of strings representing the grade
     * @param percents  - array of string representing the percent value associated with the grade {100, 90, 80, 70}.  The grade and value arrays must be the same size.
     * @return string "success" if successful, otherwise error msg
     */
    @WebMethod
    @Path("/createOrUpdateGradeScale")
    @Produces("text/plain")
    @GET
    public String createOrUpdateGradeScale(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "scaleUuid", partName = "scaleUuid") @QueryParam("scaleUuid") String scaleUuid,
            @WebParam(name = "scaleName", partName = "scaleName") @QueryParam("scaleName") String scaleName,
            @WebParam(name = "grades", partName = "grades") @QueryParam("grades") String[] grades,
            @WebParam(name = "percents", partName = "percents") @QueryParam("percents") String[] percents) {
        Session session = establishSession(sessionid);

        try {
            boolean scaleExists = gradeScaleExists(scaleUuid);

            if (percents[0].endsWith("}") && percents[0].startsWith("{") && grades[0].endsWith("}") && grades[0].startsWith("{")) {
                grades = grades[0].substring(1, grades[0].length() - 1).split(",");
                percents = percents[0].substring(1, percents[0].length() - 1).split(",");
            }
            if (!scaleExists) {
                createGradeScale(scaleUuid, scaleName);
            }

            int gradeScaleId = getGradeScaleId(scaleUuid);

            if (gradeScaleId == -1) {
                throw new Exception("gradescale id is not an int, please check your database");
            }

            if (scaleExists) {
                updateGradeScale(scaleUuid, scaleName);
                deleteGradeScaleGrades(gradeScaleId);
                deleteGradeScalePercents(gradeScaleId);
                deletePercentMappings(gradeScaleId);
            }

            for (int i = 0; i < grades.length; i++) {
                addGradeScalePercent(gradeScaleId, grades[i], Float.valueOf(percents[i]));
                addGradeScaleGrade(gradeScaleId, grades[i], i);
            }


            updateGradeMaps(gradeScaleId);
            addMappingsForGradebooks(gradeScaleId);
        } catch (Exception e) {
            LOG.error("error attempting to manage a gradescale:" + e.getMessage(), e);
            return e.getMessage();
        }
        return "success";
    }


    protected void addMappingsForGradebooks(int gradeScaleId) throws Exception {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            String query = "INSERT \n" +
                    "INTO \n" +
                    "    GB_GRADE_TO_PERCENT_MAPPING_T \n" +
                    "SELECT DISTINCT \n" +
                    "    GB_GRADE_MAP_T.id, \n" +
                    "    GB_GRADING_SCALE_PERCENTS_T.percent, \n" +
                    "    GB_GRADING_SCALE_PERCENTS_T.letter_grade \n" +
                    "FROM \n" +
                    "    GB_GRADE_MAP_T, \n" +
                    "    GB_GRADING_SCALE_T, \n" +
                    "    GB_GRADING_SCALE_PERCENTS_T \n" +
                    "WHERE \n" +
                    "    GB_GRADING_SCALE_T.id                           = gb_grade_map_t.gb_grading_scale_t \n" +
                    "    AND GB_GRADING_SCALE_PERCENTS_T.grading_scale_id= GB_GRADING_SCALE_T.ID"+
                    "    AND GB_GRADING_SCALE_T.id = ?";

            connection = sqlService.borrowConnection();

            ps = connection.prepareStatement(query);
            ps.setInt(1, gradeScaleId);
            ps.execute();
            connection.commit();

        } catch (Exception e) {
            throw e;
        } finally {

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }

    }

    protected void updateGradeMaps(int gradeScaleId) throws Exception {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            String query = "INSERT INTO GB_GRADE_MAP_T ( OBJECT_TYPE_ID, VERSION, GRADEBOOK_ID, GB_GRADING_SCALE_T ) \n" +
                    "SELECT DISTINCT \n" +
                    "    0,0, \n" +
                    "    GB_GRADEBOOK_T.ID, \n" +
                    "    GB_GRADING_SCALE_T.ID \n" +
                    "FROM \n" +
                    "    GB_GRADEBOOK_T, \n" +
                    "    GB_GRADING_SCALE_T \n" +
                    "WHERE \n" +
                    "    (\n" +
                    "        GB_GRADEBOOK_T.ID, \n" +
                    "        GB_GRADING_SCALE_T.ID\n" +
                    "    ) \n" +
                    "    NOT IN \n" +
                    "    (\n" +
                    "    SELECT DISTINCT \n" +
                    "        GRADEBOOK_ID, \n" +
                    "        GB_GRADING_SCALE_T \n" +
                    "    FROM \n" +
                    "        GB_GRADE_MAP_T \n" +
                    "    WHERE \n" +
                    "        GB_GRADING_SCALE_T= ?\n" +
                    "    ) \n" +
                    "    AND GB_GRADING_SCALE_T.ID = ?";

            connection = sqlService.borrowConnection();

            ps = connection.prepareStatement(query);
            ps.setInt(1, gradeScaleId);
            ps.setInt(2, gradeScaleId);

            ps.execute();
            connection.commit();

        } catch (Exception e) {
            throw e;
        } finally {

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }

    }

    protected void deletePercentMappings(int gradeScaleId) throws Exception {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            String query = "DELETE FROM GB_GRADE_TO_PERCENT_MAPPING_T WHERE GRADE_MAP_ID IN (SELECT ID FROM GB_GRADE_MAP_T WHERE GB_GRADING_SCALE_T =?) ";
            connection = sqlService.borrowConnection();

            ps = connection.prepareStatement(query);
            ps.setInt(1, gradeScaleId);
            ps.execute();
            connection.commit();

        } catch (Exception e) {
            throw e;
        } finally {

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }

    }

    protected void updateGradeScale(String scaleUuid, String scaleName) throws Exception {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            String query = "UPDATE GB_GRADING_SCALE_T SET NAME = ? where SCALE_UID = ?";
            connection = sqlService.borrowConnection();

            ps = connection.prepareStatement(query);
            ps.setString(2, scaleUuid);
            ps.setString(1, scaleName);
            ps.execute();
            connection.commit();

        } catch (Exception e) {
            throw e;
        } finally {

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }

    }

    protected void deleteGradeScaleGrades(int gradeScaleId) throws Exception {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            String query = "DELETE FROM GB_GRADING_SCALE_GRADES_T WHERE GRADING_SCALE_ID=?";
            connection = sqlService.borrowConnection();

            ps = connection.prepareStatement(query);
            ps.setInt(1, gradeScaleId);

            ps.execute();
            connection.commit();

        } catch (Exception e) {
            throw e;
        } finally {

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }


    }


    protected void deleteGradeScalePercents(int gradeScaleId) throws Exception {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            String query = "DELETE FROM GB_GRADING_SCALE_PERCENTS_T WHERE GRADING_SCALE_ID=?";
            connection = sqlService.borrowConnection();

            ps = connection.prepareStatement(query);
            ps.setInt(1, gradeScaleId);

            ps.execute();
            connection.commit();

        } catch (Exception e) {
            throw e;
        } finally {

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }


    }


    protected void addGradeScaleGrade(int gradeScaleId, String grade, int index) throws Exception {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            String query = "INSERT INTO GB_GRADING_SCALE_GRADES_T (GRADING_SCALE_ID,LETTER_GRADE,GRADE_IDX) VALUES (?,?,?)";
            connection = sqlService.borrowConnection();

            ps = connection.prepareStatement(query);
            ps.setInt(1, gradeScaleId);
            ps.setString(2, grade);
            ps.setInt(3, index);

            ps.execute();
            connection.commit();

        } catch (Exception e) {
            throw e;
        } finally {

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }


    }


    protected void addGradeScalePercent(int gradeScaleId, String grade, float percent) throws Exception {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            String query = "INSERT INTO GB_GRADING_SCALE_PERCENTS_T (GRADING_SCALE_ID,PERCENT,LETTER_GRADE) VALUES (?,?,?)";
            connection = sqlService.borrowConnection();

            ps = connection.prepareStatement(query);
            ps.setInt(1, gradeScaleId);
            ps.setFloat(2, percent);
            ps.setString(3, grade);

            ps.execute();
            connection.commit();

        } catch (Exception e) {
            throw e;
        } finally {

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
    }

    protected void createGradeScale(String scaleUuid, String scaleName) throws Exception {
        Connection connection = null;
        PreparedStatement ps = null;

        try {
            String query = "INSERT INTO GB_GRADING_SCALE_T (OBJECT_TYPE_ID,VERSION,SCALE_UID,NAME,UNAVAILABLE) VALUES (0,0,?,?,false)";
            connection = sqlService.borrowConnection();

            ps = connection.prepareStatement(query);
            ps.setString(1, scaleUuid);
            ps.setString(2, scaleName);
            ps.execute();

            connection.commit();


        } catch (Exception e) {
            throw e;
        } finally {

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }

    }

    protected int getGradeScaleId(String scaleUuid) throws Exception {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String query = "select id from gb_grading_scale_t where scale_uid = ?";
            connection = sqlService.borrowConnection();

            ps = connection.prepareStatement(query);
            ps.setString(1, scaleUuid);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
        return -1;
    }

    protected boolean gradeScaleExists(String scaleUuid) throws Exception {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String query = "select * from gb_grading_scale_t where scale_uid = ?";
            connection = sqlService.borrowConnection();

            ps = connection.prepareStatement(query);
            ps.setString(1, scaleUuid);
            rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }

            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
        return false;
    }
}
