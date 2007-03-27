package org.sakaiproject.tool.gradebook.test;

import junit.framework.Assert;

import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.service.gradebook.shared.GradebookService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.math.BigDecimal;

public class GradebookManagerOPCTest extends GradebookTestBase {
	private Long assgn1Long;
	private Long assgn3Long;
	private Long cate1Long;
	private Long cate2Long;
	
	protected void onSetUpInTransaction() throws Exception {
		super.onSetUpInTransaction();

		String className = this.getClass().getName();
		gradebookFrameworkService.addGradebook(className, className);
		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
		cate1Long = gradebookManager.createCategory(persistentGradebook.getId(), "cate 1", new Double(0.40), 0);
		cate2Long = gradebookManager.createCategory(persistentGradebook.getId(), "cate 2", new Double(0.60), 0);

		List list = (List) gradebookManager.getCategories(persistentGradebook.getId());

		for(int i=0; i<list.size(); i++)
		{
			Category cat = (Category) list.get(i);
			if(i == 0)
			{
				assgn1Long = gradebookManager.createAssignmentForCategory(persistentGradebook.getId(), cat.getId(), 
						cat.getName() + "_assignment_1", new Double(10.0), new Date(), new Boolean(false), new Boolean(true));
			}
			if(i == 1)
			{
				assgn3Long = gradebookManager.createAssignmentForCategory(persistentGradebook.getId(), cat.getId(), 
						cat.getName() + "_assignment_1", new Double(10.0), new Date(), new Boolean(false), new Boolean(true));
			}
			Long assign2 = gradebookManager.createAssignmentForCategory(persistentGradebook.getId(), cat.getId(), 
					cat.getName() + "_assignment_2", new Double(10.0), new Date(), new Boolean(false), new Boolean(true));
		}
	}

//change them into onSetUpInTransaction
//	public void testCreateGradebook() throws Exception {
//		// Create a gradebook
//		String className = this.getClass().getName();
//		gradebookFrameworkService.addGradebook(className, className);
//		setComplete();
//	}
//
//public void testCreateCategory() throws Exception{
//Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
//
//Long id1 = gradebookManager.createCategory(persistentGradebook.getId(), "cate 1", new Double(0.40), 0);
////test for name conflict      Long id2 = gradebookManager.createCategory(persistentGradebook.getId(), "cate 1", new Double(0), 0);
//Long id2 = gradebookManager.createCategory(persistentGradebook.getId(), "cate 2", new Double(0.60), 0);
//
////save data for testing getCategories. otherwise, it will be gone for next transaction.
//setComplete();
//
////System.out.println("category id1::" + id1.longValue());
////System.out.println("category id2::" + id2.longValue());
//}
//
//public void testCreateAssignmentForCategory() throws Exception{
//Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
//
//List list = (List) gradebookManager.getCategories(persistentGradebook.getId());
//
//for(int i=0; i<list.size(); i++)
//{
//	Category cat = (Category) list.get(i);
//	if(i == 0)
//	{
//		assgn1Long = gradebookManager.createAssignmentForCategory(persistentGradebook.getId(), cat.getId(), 
//				cat.getName() + "_assignment_1", new Double(10.0), new Date(), new Boolean(false), new Boolean(true));
//	}
//	if(i == 1)
//	{
//		assgn3Long = gradebookManager.createAssignmentForCategory(persistentGradebook.getId(), cat.getId(), 
//				cat.getName() + "_assignment_1", new Double(10.0), new Date(), new Boolean(false), new Boolean(true));
//	}
//	Long assign2 = gradebookManager.createAssignmentForCategory(persistentGradebook.getId(), cat.getId(), 
//			cat.getName() + "_assignment_2", new Double(10.0), new Date(), new Boolean(false), new Boolean(true));
//}
//setComplete();
//}
//

	public void testGetGradebook() throws Exception {
		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());

//		System.out.println("grade_type::" + persistentGradebook.getGrade_type());
//		System.out.println("category_type::" + persistentGradebook.getCategory_type());

		Assert.assertTrue(persistentGradebook.getGrade_type() == 1);
		Assert.assertTrue(persistentGradebook.getCategory_type() == 1);
	}

	public void testGetCategories() throws Exception{
		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());

		List list = (List) gradebookManager.getCategories(persistentGradebook.getId());

//		for(int i=0; i<list.size(); i++)
//		{
//		Category cat = (Category) list.get(i);
//		System.out.println("category::" + cat.getName());
//		System.out.println("category::" + cat.getId());
//		System.out.println("category::" + cat.getGradebook().getId().longValue());
//		System.out.println("category::" + cat.getWeight().longValue());
//		System.out.println("category::" + cat.getDrop_lowest());
//		}
		Assert.assertTrue(list.size() == 2);
		Assert.assertTrue(((Category)list.get(0)).getName().equals("cate 1"));
		Assert.assertTrue(((Category)list.get(1)).getName().equals("cate 2"));
	}

	public void testGetAssignmentsForCategory() throws Exception{
		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());

		List list = (List) gradebookManager.getCategories(persistentGradebook.getId());

		for(int i=0; i<list.size(); i++)
		{
			Category cat = (Category) list.get(i);
			List asslist = (List) gradebookManager.getAssignmentsForCategory(cat.getId());
			for(int j=0; j<asslist.size(); j++)
			{
				Assignment as = (Assignment) asslist.get(j);
//				System.out.println("category::" + cat.getName() + "--assignment::" + as.getName());
				Assert.assertTrue(as.getName().equals(cat.getName() + "_assignment_" + new Integer(j+1).intValue()));
			}
		} 
	}

	public void testGetCategory() throws Exception{
		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
		Category cat1 = gradebookManager.getCategory(cate1Long);
		Category cat2 = gradebookManager.getCategory(cate2Long);
//		System.out.println(cat1 + "---" + cat1.getName());
//		System.out.println(cat2 + "---" + cat2.getName());
	}

	public void testUpdateCategory() throws Exception{
		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
		Category cat1 = gradebookManager.getCategory(cate1Long);
//		test for name conflicts with removed category    	Category cat2 = gradebookManager.getCategory(new Long(2));
//		gradebookManager.removeCategory(cat2.getId());
//		cat1.setName("cate 2");
		cat1.setName("cate-rename");
		gradebookManager.updateCategory(cat1);
		Category cat_after = gradebookManager.getCategory(cate1Long);
		Assert.assertTrue(cat_after.getName().equals("cate-rename"));
	}

	public void testRemoveCategory() throws Exception{
		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
		Category cat1 = gradebookManager.getCategory(cate1Long);
		gradebookManager.removeCategory(cate1Long);
		List list = (List) gradebookManager.getCategories(persistentGradebook.getId());
		Assert.assertTrue(list.size() == 1);
//		for(int i=0; i<list.size(); i++)
//		{
//		Category cat = (Category) list.get(i);
//		System.out.println("category::" + cat.getName());
//		System.out.println("category::" + cat.getId());
//		System.out.println("category::" + cat.getGradebook().getId().longValue());
//		System.out.println("category::" + cat.getWeight().longValue());
//		System.out.println("category::" + cat.getDrop_lowest());
//		}
	}

	public void testValidateCategoryWeighting() throws Exception{
		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
		Assert.assertTrue(gradebookManager.validateCategoryWeighting(persistentGradebook.getId()));

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		gradebookManager.createCategory(persistentGradebook.getId(), "cate 3", new Double(0), 0);
		List list = (List) gradebookManager.getCategories(persistentGradebook.getId());
		for(int i=0; i<list.size(); i++)
		{
			Category cat = (Category) list.get(i);
			cat.setWeight(1.0/list.size());
			gradebookManager.updateCategory(cat);
//			System.out.println(cat.getWeight().doubleValue());
		}
		Assert.assertTrue(gradebookManager.validateCategoryWeighting(persistentGradebook.getId()));
	}

	public void testUpdateAssignmentGradeRecords() throws Exception{
		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
		Assignment assign = gradebookManager.getAssignment(assgn1Long);

		//for percentage type
		persistentGradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
		assign.setPointsPossible(new Double(5));
		gradebookManager.updateAssignment(assign);

		List gradeRecords = generateGradeRecords(assign, 5);
		Collection studentUids = new ArrayList();
		studentUids.add("studentId1");
		studentUids.add("studentId2");
		studentUids.add("studentId3");
		studentUids.add("studentId4");
		studentUids.add("studentId5");
		List returnGradeRecords = gradebookManager.getAssignmentGradeRecords(assign, studentUids);
		List convertGradeRecords = new ArrayList();
		for(int i=0; i<returnGradeRecords.size(); i++)
		{
			AssignmentGradeRecord agr = (AssignmentGradeRecord)returnGradeRecords.get(i);
//			System.out.println("student::" + agr.getStudentId() + "--assign::" + agr.getAssignment() + "--grade::" + agr.getPointsEarned());
			agr.setPointsEarned(new Double((agr.getPointsEarned().doubleValue() * 0.9) / assign.getPointsPossible()));
			convertGradeRecords.add(agr);
		}
		gradebookManager.updateAssignmentGradeRecords(assign, convertGradeRecords, GradebookService.GRADE_TYPE_PERCENTAGE);
//		System.out.println("after convert===============");
		for(int i=0; i<returnGradeRecords.size(); i++)
		{
			AssignmentGradeRecord agr = (AssignmentGradeRecord)returnGradeRecords.get(i);
			Assert.assertTrue((new BigDecimal(agr.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP)).doubleValue() == (0.9 * (i+1)));
//			System.out.println("student::" + agr.getStudentId() + "--assign::" + agr.getAssignment() + "--grade::" + agr.getPointsEarned());
//			System.out.println(new BigDecimal(agr.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP));			
		}		
	}

	private List generateGradeRecords(Assignment go, int gradeRecordsToGenerate) {
		List records = new ArrayList();
		AssignmentGradeRecord record;
		for(int i = 1; i <= gradeRecordsToGenerate; i++) {
			record = new AssignmentGradeRecord();

			record.setPointsEarned(new Double(i));
			record.setGradableObject(go);
			record.setStudentId("studentId" + i);
			records.add(record);
		}
		gradebookManager.updateAssignmentGradeRecords(go, records, GradebookService.GRADE_TYPE_POINTS);

		return records;
	}

	public void testGetAssignmentGradeRecordsConverted() throws Exception{
		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
		Assignment assign = gradebookManager.getAssignment(assgn1Long);

		//for percentage type
		persistentGradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
		gradebookManager.updateGradebook(persistentGradebook);
		assign.setPointsPossible(new Double(5));
		gradebookManager.updateAssignment(assign);

		List gradeRecords = generateGradeRecords(assign, 5);
		Collection studentUids = new ArrayList();
		studentUids.add("studentId1");
		studentUids.add("studentId2");
		studentUids.add("studentId3");
		studentUids.add("studentId4");
		studentUids.add("studentId5");

		List returnGradeRecords = gradebookManager.getAssignmentGradeRecordsConverted(assign, studentUids);
		for(int i=0; i<returnGradeRecords.size(); i++)
		{
			AssignmentGradeRecord agr = (AssignmentGradeRecord)returnGradeRecords.get(i);
			Assert.assertTrue((new BigDecimal(agr.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP)).doubleValue() == new BigDecimal(((double)(i+1))/assign.getPointsPossible().doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//			System.out.println(new BigDecimal(((double)(i+1))/assign.getPointsPossible().doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//			System.out.println("student::" + agr.getStudentId() + "--assign::" + agr.getAssignment() + "--point possible::" + agr.getAssignment().getPointsPossible() + "--grade::" + agr.getPointsEarned());
		}
	}

//for testing internal calculation
//need add  public double getTotalPointsEarnedInternal(final Long gradebookId, final String studentId, Gradebook gradebook, List categories);
//and public double getTotalPointsInternal(final Long gradebookId, final Gradebook gradebook, final List categories);
//into GradebookManager API.
//	public void testGetTotalPointsEarnedInternal() throws Exception{
//		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
//		Assignment assign = gradebookManager.getAssignment(assgn1Long);
//		Assignment assign2 = gradebookManager.getAssignment(assgn3Long);
//
//		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
//		gradebookManager.updateGradebook(persistentGradebook);
//		assign.setPointsPossible(new Double(5));
//		gradebookManager.updateAssignment(assign);
//		List categories = gradebookManager.getCategories(persistentGradebook.getId());
//		Category cate = gradebookManager.getCategory(assign.getCategory().getId());
//
//		List gradeRecords = generateGradeRecords(assign, 5);
//		List graderRecords2 = generateGradeRecords(assign2, 5);
//
////		Double piontsEearned = gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId1", persistentGradebook, categories);
////		System.out.println(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId1", persistentGradebook, categories));
////		System.out.println(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId2", persistentGradebook, categories));
////		System.out.println(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId3", persistentGradebook, categories));
////		System.out.println(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId4", persistentGradebook, categories));
////		System.out.println(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId5", persistentGradebook, categories));
//
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId1", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal( 1.0 / 15 * cate.getWeight().doubleValue() + 1.0 / 20 * 0.6)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId2", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(2.0 / 15 * cate.getWeight().doubleValue() + 2.0 / 20 * 0.6)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId3", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(3.0 / 15 * cate.getWeight().doubleValue() + 3.0 / 20 * 0.6)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId4", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal( 4.0 / 15 * cate.getWeight().doubleValue() + 4.0 / 20 * 0.6)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId5", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal( 5.0 / 15 * cate.getWeight().doubleValue() + 5.0 / 20 * 0.6)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//
//		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_NO_CATEGORY);
//		gradebookManager.updateGradebook(persistentGradebook);
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId1", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(1.0+1.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId2", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(2.0+2.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId3", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(3.0+3.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId4", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(4.0+4.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId5", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(5.0+5.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//
//		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_ONLY_CATEGORY);
//		gradebookManager.updateGradebook(persistentGradebook);
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId1", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(1.0+1.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId2", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(2.0+2.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId3", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(3.0+3.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId4", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(4.0+4.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId5", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(5.0+5.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//
//		gradebookManager.removeCategory(cate.getId());
//		categories = gradebookManager.getCategories(persistentGradebook.getId());
//		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
//		gradebookManager.updateGradebook(persistentGradebook);
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId1", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(1.0 / 20 * 0.6)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId2", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(2.0 / 20 *0.6)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//
//		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_ONLY_CATEGORY);
//		gradebookManager.updateGradebook(persistentGradebook);
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId1", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(1.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId2", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(2.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//
//		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_NO_CATEGORY);
//		gradebookManager.updateGradebook(persistentGradebook);
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId1", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(1.0+1.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsEarnedInternal(persistentGradebook.getId(), "studentId2", persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(2.0+2.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//	}
//
//	public void testGetTotalPointsInternal() throws Exception{
//		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
//		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
//		gradebookManager.updateGradebook(persistentGradebook);
//		List categories = gradebookManager.getCategories(persistentGradebook.getId());
//		Category cate = gradebookManager.getCategory(cate1Long);
//
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsInternal(persistentGradebook.getId(), persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(1.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//
//		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_NO_CATEGORY);
//		gradebookManager.updateGradebook(persistentGradebook);
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsInternal(persistentGradebook.getId(), persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(10.0 + 10.0 + 10.0 + 10.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//
//		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_ONLY_CATEGORY);
//		gradebookManager.updateGradebook(persistentGradebook);
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsInternal(persistentGradebook.getId(), persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(10.0 + 10.0 + 10.0 + 10.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//
//		gradebookManager.removeCategory(cate.getId());
//		categories = gradebookManager.getCategories(persistentGradebook.getId());
//		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
//		gradebookManager.updateGradebook(persistentGradebook);
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsInternal(persistentGradebook.getId(), persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(1.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//
//		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_ONLY_CATEGORY);
//		gradebookManager.updateGradebook(persistentGradebook);
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsInternal(persistentGradebook.getId(), persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(10.0 + 10.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//
//		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_NO_CATEGORY);
//		gradebookManager.updateGradebook(persistentGradebook);
//		Assert.assertTrue((new BigDecimal(gradebookManager.getTotalPointsInternal(persistentGradebook.getId(), persistentGradebook, categories))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
//			(new BigDecimal(10.0 + 10.0 + 10.0 + 10.0)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());		
//	}

	public void testGetPointsEarnedCourseGradeRecords() throws Exception{
		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
		Assignment assign = gradebookManager.getAssignment(assgn1Long);
		Assignment assign2 = gradebookManager.getAssignment(assgn3Long);

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		assign.setPointsPossible(new Double(5));
		gradebookManager.updateAssignment(assign);
		List categories = gradebookManager.getCategories(persistentGradebook.getId());
		Category cate = gradebookManager.getCategory(assign.getCategory().getId());
		List assignments = gradebookManager.getAssignments(persistentGradebook.getId());

		List gradeRecords = generateGradeRecords(assign, 5);
		List gradeRecords2 = generateGradeRecords(assign2, 5);
		for(int i=0; i<gradeRecords2.size(); i++)
		{
			gradeRecords.add(gradeRecords.get(i));
		}

		List uid = new ArrayList();
		uid.add("studentId1");
		uid.add("studentId2");
		uid.add("studentId3");
		uid.add("studentId4");
		uid.add("studentId5");
		Map studentIdMap = new HashMap();
		studentIdMap.put("studentId1", new Double(1.0));
		studentIdMap.put("studentId2", new Double(2.0));
		studentIdMap.put("studentId3", new Double(3.0));
		studentIdMap.put("studentId4", new Double(4.0));
		studentIdMap.put("studentId5", new Double(5.0));

		Map filteredGradesMap = new HashMap();
		gradeRecords = gradebookManager.getAllAssignmentGradeRecords(persistentGradebook.getId(), uid);
		gradebookManager.addToGradeRecordMap(filteredGradesMap, gradeRecords);

		CourseGrade courseGrade = gradebookManager.getCourseGrade(persistentGradebook.getId());
		List courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, uid, assignments, filteredGradesMap);

		for(int i=0; i<courseGradeRecords.size(); i++)
		{
			CourseGradeRecord cgr = (CourseGradeRecord) courseGradeRecords.get(i);
			Assert.assertTrue(new BigDecimal(cgr.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId()))) * 0.4 / 15.0 + ((Double)studentIdMap.get(cgr.getStudentId())) * 0.6 / 20.0 ).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			Assert.assertTrue(new BigDecimal(cgr.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId()))) * 0.4 * 100 / 15.0 + ((Double)studentIdMap.get(cgr.getStudentId())) * 0.6 * 100 / 20.0 ).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		}

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_NO_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, uid, assignments, filteredGradesMap);
		for(int i=0; i<courseGradeRecords.size(); i++)
		{
			CourseGradeRecord cgr = (CourseGradeRecord) courseGradeRecords.get(i);
			Assert.assertTrue(new BigDecimal(cgr.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId())).doubleValue() * 2)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			Assert.assertTrue(new BigDecimal(cgr.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId())).doubleValue() * 2) * 100 / 35.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		}

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_ONLY_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, uid, assignments, filteredGradesMap);
		for(int i=0; i<courseGradeRecords.size(); i++)
		{
			CourseGradeRecord cgr = (CourseGradeRecord) courseGradeRecords.get(i);
			Assert.assertTrue(new BigDecimal(cgr.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId())).doubleValue() * 2)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			Assert.assertTrue(new BigDecimal(cgr.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId()))) * 100 * 2 / 35.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		}

		gradebookManager.removeCategory(cate.getId());
		categories = gradebookManager.getCategories(persistentGradebook.getId());

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, uid, assignments, filteredGradesMap);
		for(int i=0; i<courseGradeRecords.size(); i++)
		{
			CourseGradeRecord cgr = (CourseGradeRecord) courseGradeRecords.get(i);
			Assert.assertTrue(new BigDecimal(cgr.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal(((Double)studentIdMap.get(cgr.getStudentId())) * 0.6 / 20.0 ).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			Assert.assertTrue(new BigDecimal(cgr.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal(((Double)studentIdMap.get(cgr.getStudentId())) * 0.6 * 100 / 20.0 ).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		}

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_ONLY_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, uid, assignments, filteredGradesMap);
		for(int i=0; i<courseGradeRecords.size(); i++)
		{
			CourseGradeRecord cgr = (CourseGradeRecord) courseGradeRecords.get(i);
			Assert.assertTrue(new BigDecimal(cgr.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal(((Double)studentIdMap.get(cgr.getStudentId())) ).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			Assert.assertTrue(new BigDecimal(cgr.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal(((Double)studentIdMap.get(cgr.getStudentId())) * 100 / 20.0 ).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		}

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_NO_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, uid, assignments, filteredGradesMap);
		for(int i=0; i<courseGradeRecords.size(); i++)
		{
			CourseGradeRecord cgr = (CourseGradeRecord) courseGradeRecords.get(i);
			Assert.assertTrue(new BigDecimal(cgr.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal(((Double)studentIdMap.get(cgr.getStudentId())).doubleValue() * 2 ).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			Assert.assertTrue(new BigDecimal(cgr.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal(((Double)studentIdMap.get(cgr.getStudentId())).doubleValue() * 2 * 100 / 35.0 ).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		}
	}

	public void testGetStudentCourseGradeRecord() throws Exception {
		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
		Assignment assign = gradebookManager.getAssignment(assgn1Long);
		Assignment assign2 = gradebookManager.getAssignment(assgn3Long);

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		assign.setPointsPossible(new Double(5));
		gradebookManager.updateAssignment(assign);
		List categories = gradebookManager.getCategories(persistentGradebook.getId());
		Category cate = gradebookManager.getCategory(assign.getCategory().getId());
		List assignments = gradebookManager.getAssignments(persistentGradebook.getId());

		List gradeRecords = generateGradeRecords(assign, 5);
		List gradeRecords2 = generateGradeRecords(assign2, 5);
		for(int i=0; i<gradeRecords2.size(); i++)
		{
			gradeRecords.add(gradeRecords2.get(i));
		}

		List uid = new ArrayList();
		uid.add("studentId1");
		uid.add("studentId2");
		uid.add("studentId3");
		uid.add("studentId4");
		uid.add("studentId5");
		Map studentIdMap = new HashMap();
		studentIdMap.put("studentId1", new Double(1.0));
		studentIdMap.put("studentId2", new Double(2.0));
		studentIdMap.put("studentId3", new Double(3.0));
		studentIdMap.put("studentId4", new Double(4.0));
		studentIdMap.put("studentId5", new Double(5.0));

		CourseGrade courseGrade = gradebookManager.getCourseGrade(persistentGradebook.getId());
		CourseGradeRecord cgr1 = (CourseGradeRecord) gradebookManager.getStudentCourseGradeRecord(persistentGradebook, "studentId1");
		CourseGradeRecord cgr2 = (CourseGradeRecord) gradebookManager.getStudentCourseGradeRecord(persistentGradebook, "studentId1");
		Assert.assertTrue(new BigDecimal(cgr1.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr1.getStudentId()))) * 0.4 / 15 + (((Double)studentIdMap.get(cgr1.getStudentId()))) * 0.6 / 20).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr1.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr1.getStudentId()))) * 0.4 * 100 / 15 + (((Double)studentIdMap.get(cgr1.getStudentId()))) * 0.6 * 100 / 20).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr2.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr2.getStudentId()))) * 0.4 / 15 + (((Double)studentIdMap.get(cgr2.getStudentId()))) * 0.6 / 20).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr2.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr2.getStudentId()))) * 0.4 * 100 / 15 + (((Double)studentIdMap.get(cgr2.getStudentId()))) * 0.6 * 100 / 20).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_NO_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		cgr1 = (CourseGradeRecord) gradebookManager.getStudentCourseGradeRecord(persistentGradebook, "studentId1");
		cgr2 = (CourseGradeRecord) gradebookManager.getStudentCourseGradeRecord(persistentGradebook, "studentId1");
		Assert.assertTrue(new BigDecimal(cgr1.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr1.getStudentId()))) * 2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr1.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr1.getStudentId()))) * 100 * 2 / 35.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr2.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr2.getStudentId()))) * 2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr2.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr2.getStudentId()))) * 100 * 2 / 35.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_ONLY_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		cgr1 = (CourseGradeRecord) gradebookManager.getStudentCourseGradeRecord(persistentGradebook, "studentId1");
		cgr2 = (CourseGradeRecord) gradebookManager.getStudentCourseGradeRecord(persistentGradebook, "studentId1");
		Assert.assertTrue(new BigDecimal(cgr1.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr1.getStudentId()))) * 2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr1.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr1.getStudentId()))) * 100 * 2 / 35.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr2.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr2.getStudentId()))) * 2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr2.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr2.getStudentId()))) * 100 * 2 / 35.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

		gradebookManager.removeCategory(cate.getId());
		categories = gradebookManager.getCategories(persistentGradebook.getId());

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		cgr1 = (CourseGradeRecord) gradebookManager.getStudentCourseGradeRecord(persistentGradebook, "studentId1");
		cgr2 = (CourseGradeRecord) gradebookManager.getStudentCourseGradeRecord(persistentGradebook, "studentId1");
		Assert.assertTrue(new BigDecimal(cgr1.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr1.getStudentId()))) * 0.6 / 20).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr1.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr1.getStudentId()))) * 0.6 * 100 / 20).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr2.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr2.getStudentId()))) * 0.6 / 20).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr2.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr2.getStudentId()))) * 0.6 * 100 / 20).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_ONLY_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		cgr1 = (CourseGradeRecord) gradebookManager.getStudentCourseGradeRecord(persistentGradebook, "studentId1");
		cgr2 = (CourseGradeRecord) gradebookManager.getStudentCourseGradeRecord(persistentGradebook, "studentId1");
		Assert.assertTrue(new BigDecimal(cgr1.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr1.getStudentId())))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr1.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr1.getStudentId()))) * 100 / 20).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr2.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr2.getStudentId())))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr2.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr2.getStudentId()))) * 100 / 20).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_NO_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		cgr1 = (CourseGradeRecord) gradebookManager.getStudentCourseGradeRecord(persistentGradebook, "studentId1");
		cgr2 = (CourseGradeRecord) gradebookManager.getStudentCourseGradeRecord(persistentGradebook, "studentId1");
		Assert.assertTrue(new BigDecimal(cgr1.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr1.getStudentId()))) * 2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr1.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr1.getStudentId()))) * 100.0 * 2 / 35.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr2.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr2.getStudentId()))) * 2 ).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		Assert.assertTrue(new BigDecimal(cgr2.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal((((Double)studentIdMap.get(cgr1.getStudentId()))) * 100.0 * 2/ 35.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

	}

	public void testGetPointsEarnedCourseGradeRecords2Params() throws Exception {
		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
		Assignment assign = gradebookManager.getAssignment(assgn1Long);
		Assignment assign2 = gradebookManager.getAssignment(assgn3Long);

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		assign.setPointsPossible(new Double(5));
		gradebookManager.updateAssignment(assign);
		List categories = gradebookManager.getCategories(persistentGradebook.getId());
		Category cate = gradebookManager.getCategory(assign.getCategory().getId());
		List assignments = gradebookManager.getAssignments(persistentGradebook.getId());

		List gradeRecords = generateGradeRecords(assign, 5);
		List gradeRecords2 = generateGradeRecords(assign2, 5);
		for(int i=0; i<gradeRecords2.size(); i++)
		{
			gradeRecords.add(gradeRecords2.get(i));
		}

		List uid = new ArrayList();
		uid.add("studentId1");
		uid.add("studentId2");
		uid.add("studentId3");
		uid.add("studentId4");
		uid.add("studentId5");
		Map studentIdMap = new HashMap();
		studentIdMap.put("studentId1", new Double(1.0));
		studentIdMap.put("studentId2", new Double(2.0));
		studentIdMap.put("studentId3", new Double(3.0));
		studentIdMap.put("studentId4", new Double(4.0));
		studentIdMap.put("studentId5", new Double(5.0));

		gradeRecords = gradebookManager.getAllAssignmentGradeRecords(persistentGradebook.getId(), uid);

		CourseGrade courseGrade = gradebookManager.getCourseGrade(persistentGradebook.getId());
		List courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, uid);

		for(int i=0; i<courseGradeRecords.size(); i++)
		{
			CourseGradeRecord cgr = (CourseGradeRecord) courseGradeRecords.get(i);
			Assert.assertTrue(new BigDecimal(cgr.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId()))) * 0.4 / 15 + (((Double)studentIdMap.get(cgr.getStudentId()))) * 0.6 / 20).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			Assert.assertTrue(new BigDecimal(cgr.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId()))) * 0.4 * 100 / 15 + (((Double)studentIdMap.get(cgr.getStudentId()))) * 0.6 * 100 / 20).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		}

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_NO_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, uid);
		for(int i=0; i<courseGradeRecords.size(); i++)
		{
			CourseGradeRecord cgr = (CourseGradeRecord) courseGradeRecords.get(i);
			Assert.assertTrue(new BigDecimal(cgr.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId()))) * 2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			Assert.assertTrue(new BigDecimal(cgr.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId()))) * 2 * 100 / 35.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		}

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_ONLY_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, uid);
		for(int i=0; i<courseGradeRecords.size(); i++)
		{
			CourseGradeRecord cgr = (CourseGradeRecord) courseGradeRecords.get(i);
			Assert.assertTrue(new BigDecimal(cgr.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId()))) * 2 ).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			Assert.assertTrue(new BigDecimal(cgr.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId()))) * 100 * 2 / 35.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		}

		gradebookManager.removeCategory(cate.getId());
		categories = gradebookManager.getCategories(persistentGradebook.getId());

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, uid);
		for(int i=0; i<courseGradeRecords.size(); i++)
		{
			CourseGradeRecord cgr = (CourseGradeRecord) courseGradeRecords.get(i);
			Assert.assertTrue(new BigDecimal(cgr.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId()))) * 0.6 / 20).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			Assert.assertTrue(new BigDecimal(cgr.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId()))) * 0.6  * 100 / 20).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		}

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_ONLY_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, uid);
		for(int i=0; i<courseGradeRecords.size(); i++)
		{
			CourseGradeRecord cgr = (CourseGradeRecord) courseGradeRecords.get(i);
			Assert.assertTrue(new BigDecimal(cgr.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId())))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			Assert.assertTrue(new BigDecimal(cgr.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((((Double)studentIdMap.get(cgr.getStudentId()))) * 100 / 20).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		}

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_NO_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		courseGradeRecords = gradebookManager.getPointsEarnedCourseGradeRecords(courseGrade, uid);
		for(int i=0; i<courseGradeRecords.size(); i++)
		{
			CourseGradeRecord cgr = (CourseGradeRecord) courseGradeRecords.get(i);
			Assert.assertTrue(new BigDecimal(cgr.getPointsEarned()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((Double)studentIdMap.get(cgr.getStudentId()) * 2).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
			Assert.assertTrue(new BigDecimal(cgr.getGradeAsPercentage()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
				new BigDecimal((Double)studentIdMap.get(cgr.getStudentId()) * 100 * 2 / 35.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
		}
	}

	public void testGetTotalPoints() throws Exception {
		Gradebook persistentGradebook = gradebookManager.getGradebook(this.getClass().getName());
		Assignment assign = gradebookManager.getAssignment(assgn1Long);

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		assign.setPointsPossible(new Double(5));
		gradebookManager.updateAssignment(assign);

		Double total = gradebookManager.getTotalPoints(persistentGradebook.getId());
		Assert.assertTrue(new BigDecimal(total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal(1.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_ONLY_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		total = gradebookManager.getTotalPoints(persistentGradebook.getId());
		Assert.assertTrue(new BigDecimal(total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal(35.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_NO_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		total = gradebookManager.getTotalPoints(persistentGradebook.getId());
		Assert.assertTrue(new BigDecimal(total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal(35.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

		gradebookManager.removeCategory(cate1Long);

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		total = gradebookManager.getTotalPoints(persistentGradebook.getId());
		Assert.assertTrue(new BigDecimal(total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal(1.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_ONLY_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		total = gradebookManager.getTotalPoints(persistentGradebook.getId());
		Assert.assertTrue(new BigDecimal(total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal(10 + 10).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());

		persistentGradebook.setCategory_type(GradebookService.CATEGORY_TYPE_NO_CATEGORY);
		gradebookManager.updateGradebook(persistentGradebook);
		total = gradebookManager.getTotalPoints(persistentGradebook.getId());
		Assert.assertTrue(new BigDecimal(total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() == 
			new BigDecimal(5 + 10 + 10 + 10).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
	}

	//test updateAssignmentGradesAndComments
}