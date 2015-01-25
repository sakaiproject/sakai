package org.sakaiproject.gradebookng.business.helpers;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.gradebookng.business.model.ImportedGrade;
import org.sakaiproject.gradebookng.business.model.ImportedGradeItem;

import java.io.InputStream;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.List;

/**
 * Created by chmaurer on 1/24/15.
 */
public class TestImportGradesHelper {

    @Test
    public void testCsvImport() throws Exception {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("grades_import.csv");
        List<ImportedGrade> importedGrades = ImportGradesHelper.parseCsv(is);
        is.close();

        testImport(importedGrades);
    }

    @Test
    public void testXlsImport() throws Exception {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("grades_import.xls");
        List<ImportedGrade> importedGrades = ImportGradesHelper.parseXls(is);
        is.close();

        testImport(importedGrades);
    }

    private void testImport(List<ImportedGrade> importedGrades) throws Exception {
        Assert.assertNotNull(importedGrades);
        Assert.assertEquals("unexpected list size", 2, importedGrades.size());

        Assert.assertNotNull(importedGrades.get(0).getGradeItemMap());
        Assert.assertEquals(2, importedGrades.get(0).getGradeItemMap().size());

        ImportedGradeItem item11 = importedGrades.get(0).getGradeItemMap().get("a1");
        Assert.assertEquals("comments don't match", "graded", item11.getGradeItemComment());
        Assert.assertEquals("scores don't match", "7", item11.getGradeItemScore());

        ImportedGradeItem item12 = importedGrades.get(0).getGradeItemMap().get("food");
        Assert.assertEquals("comments don't match", "null", item12.getGradeItemComment());
        Assert.assertEquals("scores don't match", "null", item12.getGradeItemScore());

        ImportedGradeItem item21 = importedGrades.get(1).getGradeItemMap().get("a1");
        Assert.assertEquals("comments don't match", "interesting work", item21.getGradeItemComment());
        Assert.assertEquals("scores don't match", "3", item21.getGradeItemScore());

        ImportedGradeItem item22 = importedGrades.get(1).getGradeItemMap().get("food");
        Assert.assertEquals("comments don't match", "I'm hungry", item22.getGradeItemComment());
        Assert.assertEquals("scores don't match", "42", item22.getGradeItemScore());
    }

    @Test
    public void testParseAssignmentHeader() throws Exception {
        String inputString = "The Assignment [10]";

        MessageFormat mf = new MessageFormat(ImportGradesHelper.ASSIGNMENT_HEADER_PATTERN);
        Object[] parsedObject = mf.parse(inputString);

        Assert.assertEquals("Parsed assignment name does not match", "The Assignment", parsedObject[0]);
        Assert.assertEquals("Parsed assignment points do not match", "10", parsedObject[1]);

    }

    @Test
    public void testParseAssignmentCommentHeader() throws Exception {
        String inputString = "*/ The Assignment Comments */";

        MessageFormat mf = new MessageFormat(ImportGradesHelper.ASSIGNMENT_HEADER_COMMENT_PATTERN);
        Object[] parsedObject = mf.parse(inputString);

        Assert.assertEquals("Parsed assignment name does not match", "The Assignment", parsedObject[0]);

    }

    @Test(expected = ParseException.class)
    public void testBadParseAssignmentCommentHeader() throws Exception {
        String inputString = "*/ The Assignment Comments */";

        MessageFormat mf = new MessageFormat(ImportGradesHelper.ASSIGNMENT_HEADER_PATTERN);
        mf.parse(inputString);

    }
}
