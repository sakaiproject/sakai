/**********************************************************************************
* $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/tool/src/java/org/sakaiproject/tool/assessment/integration/context/IntegrationContextFactory.java $
* $Id: IntegrationContextFactory.java 2503 2005-10-11 18:59:41Z esmiley@stanford.edu $
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.tool.assessment.api.spring;

import org.sakaiproject.tool.assessment.api.SamigoApiFactory;
import org.sakaiproject.tool.assessment.shared.api.assessment.AssessmentServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.ItemServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.PublishedAssessmentServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.assessment.SectionServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.common.MediaServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.common.TypeServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.grading.GradebookServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.grading.GradingServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.qti.QTIServiceAPI;
import org.sakaiproject.tool.assessment.shared.api.questionpool.QuestionPoolServiceAPI;

/**
 *
 * <p> </p>
 * <p>Description: Concrete class for SamigoApiFactory</p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p>Implements setters for Spring injection.</p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */
public class SamigoApi extends SamigoApiFactory
{
  private AssessmentServiceAPI assessmentServiceAPI;
  private GradebookServiceAPI gradebookServiceAPI;
  private GradingServiceAPI gradingServiceAPI;
  private ItemServiceAPI itemServiceAPI;
  private MediaServiceAPI mediaServiceAPI;
  private PublishedAssessmentServiceAPI publishedAssessmentServiceAPI;
  private QTIServiceAPI qtiServiceAPI;
  private QuestionPoolServiceAPI questionPoolServiceAPI;
  private SectionServiceAPI sectionServiceAPI;
  private TypeServiceAPI typeServiceAPI;

  public void setAssessmentServiceAPI(AssessmentServiceAPI assessmentServiceAPI)
  {
    this.assessmentServiceAPI = assessmentServiceAPI;
  }
  public void setGradebookServiceAPI(GradebookServiceAPI gradebookServiceAPI)
  {
    this.gradebookServiceAPI = gradebookServiceAPI;
  }
  public void setGradingServiceAPI(GradingServiceAPI gradingServiceAPI)
  {
    this.gradingServiceAPI = gradingServiceAPI;
  }
  public void setItemServiceAPI(ItemServiceAPI itemServiceAPI)
  {
    this.itemServiceAPI = itemServiceAPI;
  }
  public void setMediaServiceAPI(MediaServiceAPI mediaServiceAPI)
  {
    this.mediaServiceAPI = mediaServiceAPI;
  }
  public void setPublishedAssessmentServiceAPI(PublishedAssessmentServiceAPI publishedAssessmentServiceAPI)
  {
    this.publishedAssessmentServiceAPI = publishedAssessmentServiceAPI;
  }
  public void setQtiServiceAPI(QTIServiceAPI qtiServiceAPI)
  {
    this.qtiServiceAPI = qtiServiceAPI;
  }
  public void setQuestionPoolServiceAPI(QuestionPoolServiceAPI questionPoolServiceAPI)
  {
    this.questionPoolServiceAPI = questionPoolServiceAPI;
  }
  public void setSectionServiceAPI(SectionServiceAPI sectionServiceAPI)
  {
    this.sectionServiceAPI = sectionServiceAPI;
  }
  public void setTypeServiceAPI(TypeServiceAPI typeServiceAPI)
  {
    this.typeServiceAPI = typeServiceAPI;
  }
  public AssessmentServiceAPI getAssessmentServiceAPI()
  {
    return assessmentServiceAPI;
  }
  public GradebookServiceAPI getGradebookServiceAPI()
  {
    return gradebookServiceAPI;
  }
  public GradingServiceAPI getGradingServiceAPI()
  {
    return gradingServiceAPI;
  }
  public MediaServiceAPI getMediaServiceAPI()
  {
    return mediaServiceAPI;
  }
  public PublishedAssessmentServiceAPI getPublishedAssessmentServiceAPI()
  {
    return publishedAssessmentServiceAPI;
  }
  public QTIServiceAPI getQtiServiceAPI()
  {
    return qtiServiceAPI;
  }
  public QuestionPoolServiceAPI getQuestionPoolServiceAPI()
  {
    return questionPoolServiceAPI;
  }
  public SectionServiceAPI getSectionServiceAPI()
  {
    return sectionServiceAPI;
  }
  public TypeServiceAPI getTypeServiceAPI()
  {
    return typeServiceAPI;
  }

  public ItemServiceAPI getItemServiceAPI()
  {
    return itemServiceAPI;
  }
}