package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.shared.TypeService;
import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;

/**
 * @author rshastri
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 *
 * Used to be org.navigoproject.ui.web.asi.author.assessment.AssessmentActionForm.java
 */
public class PublishedAssessmentBeanie
    implements Serializable {
  private static final org.apache.log4j.Logger LOG =
      org.apache.log4j.Logger.getLogger(PublishedAssessmentBean.class);

  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -630950053380808339L;
  private PublishedAssessmentFacade assessment;
  private String assessmentId;
  private String title;
  // ArrayList of SectionContentsBean
  private ArrayList sections = new ArrayList();
  private ArrayList partNumbers = new ArrayList();
  private int questionSize=0;
  private float totalScore=0;
  private String newQuestionTypeId;

  /*
   * Creates a new AssessmentBean object.
   */
  public PublishedAssessmentBeanie() {
  }

  public PublishedAssessmentFacade getAssessment() {
    return assessment;
  }

  public void setAssessment(PublishedAssessmentFacade assessment) {
    try {
      this.assessment = assessment;
      //System.out.println("** beginning of assessment bean "+assessment);
      this.assessmentId = assessment.getAssessmentId().toString();
      this.title = assessment.getTitle();

      // work out the question side & total point
      this.sections = new ArrayList();
      ArrayList sectionArray = assessment.getSectionArraySorted();
      for (int i=0; i<sectionArray.size(); i++){
        SectionDataIfc section = (SectionDataIfc)sectionArray.get(i);
        //System.out.println("** section = "+section);
        SectionContentsBean sectionBean = new SectionContentsBean(section);
        //System.out.println("** sectionContentsBean = "+sectionBean);
        this.sections.add(sectionBean);
      }
      setPartNumbers();
      setQuestionSizeAndTotalScore();
    }
    catch (Exception ex) {
    }
    //System.out.println("** end of setting assessment bean"+assessment);
  }

  // properties from Assessment
  public String getAssessmentId() {
    return this.assessmentId;
  }

  public void setAssessmentId(String assessmentId) {
    this.assessmentId = assessmentId;
  }

  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public ArrayList getSections() {
    return sections;
  }

  public void setSections(ArrayList sections) {
    this.sections = sections;
  }

  public ArrayList getPartNumbers() {
    return partNumbers;
  }

  public void setPartNumbers() {
    this.partNumbers = new ArrayList();
    for (int i=1; i<=this.sections.size(); i++){
      this.partNumbers.add(new SelectItem(i+""));
    }
  }

  public int getQuestionSize() {
    return this.questionSize;
  }

  public void setQuestionSizeAndTotalScore() {
   this.questionSize = 0;
   this.totalScore = 0;
   for(int i=0;i<this.sections.size();i++){
      SectionContentsBean sectionBean = (SectionContentsBean) sections.get(i);
      ArrayList items = sectionBean.getItemContents();
      this.questionSize += items.size();
      for (int j=0; j<items.size();j++){
        ItemContentsBean item = (ItemContentsBean)items.get(j);
        if (item.getItemData().getScore()!=null){
          this.totalScore += item.getItemData().getScore().floatValue();
        }
      }
    }
  }

  public float getTotalScore() {
    return this.totalScore;
  }

  public String getNewQuestionTypeId() {
    return this.newQuestionTypeId;
  }

  public void setNewQuestionTypeId(String newQuestionTypeId) {
    this.newQuestionTypeId = newQuestionTypeId;
  }


  public SelectItem[] getItemTypes(){
    // return list of TypeD
    TypeService service = new TypeService();
    List list = service.getFacadeItemTypes();
    SelectItem[] itemTypes = new SelectItem[list.size()];
    for (int i=0; i<list.size();i++){
      TypeIfc t = (TypeIfc) list.get(i);
      itemTypes[i] = new SelectItem(
          t.getTypeId().toString(), t.getKeyword());
    }
    return itemTypes;
  }

}
