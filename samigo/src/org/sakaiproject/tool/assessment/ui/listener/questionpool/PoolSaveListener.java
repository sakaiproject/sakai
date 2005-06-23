/**********************************************************************************
* $HeadURL$
* $Id$
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

package org.sakaiproject.tool.assessment.ui.listener.questionpool;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AgentFacade;
import org.sakaiproject.tool.assessment.facade.QuestionPoolFacade;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolDataBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id$
 */

public class PoolSaveListener implements ActionListener
{

  private static Log log = LogFactory.getLog(PoolSaveListener.class);
  private static ContextUtil cu;


  /**
   * Standard process action method.
   * @param ae ActionEvent
   * @throws AbortProcessingException
   */
  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    log.info("PoolSaveListener :");
    QuestionPoolBean  qpoolbean= (QuestionPoolBean) cu.lookupBean("questionpool");
    if (!savePool(qpoolbean))
    {
      throw new RuntimeException("failed to populateItemBean.");
    }

  }

  public boolean savePool(QuestionPoolBean qpbean) {
      //System.out.println("lydiatest PoolSaveListener savePool");

    try
    {

      QuestionPoolDataBean bean = qpbean.getCurrentPool();
      Long beanid = new Long ("0");
      if(bean.getId() != null)
      {
        beanid = bean.getId();
      }

      Long parentid = new Long("0");
      if(bean.getParentPoolId() != null)
      {
        parentid = bean.getParentPoolId();
      }

      QuestionPoolFacade questionpool =
        new QuestionPoolFacade (beanid, parentid);
      questionpool.updateDisplayName(bean.getDisplayName());
      questionpool.updateDescription(bean.getDescription());
//System.out.println("lydiatest orgname " + bean.getOrganizationName() );
      questionpool.setOrganizationName(bean.getOrganizationName());
//System.out.println("lydiatest orgname " + bean.getObjectives() );
//System.out.println("lydiatest orgname " + bean.getKeywords() );
      questionpool.setObjectives(bean.getObjectives());
      questionpool.setKeywords(bean.getKeywords());
// need to set owner and accesstype
//owner is hardcoded for now
      questionpool.setOwnerId(AgentFacade.getAgentString());
      questionpool.setAccessTypeId(QuestionPoolFacade.ACCESS_DENIED); // set as default

      QuestionPoolService delegate = new QuestionPoolService();
      //System.out.println("Saving pool");
      delegate.savePool(questionpool);

      // Rebuild the tree with the new pool
      qpbean.buildTree();
//	qpbean.setCurrentPool(null);




      if ("editpool".equals(qpbean.getAddPoolSource())) {
    // so reset subpools tree
//    QuestionPoolFacade thepool= delegate.getPool(parentid, AgentFacade.getAgentString());
//    qpbean.getCurrentPool().setNumberOfSubpools(thepool.getSubPoolSize().toString());
      qpbean.startEditPoolAgain(parentid.toString());  // return to edit poolwith the current pool set to the parentpool
	qpbean.setOutcome("editPool");
	qpbean.setAddPoolSource("");
      }
      else {
	qpbean.setOutcome("poolList");
      }
   }
    catch(Exception e)
    {
      e.printStackTrace();
      return false;
    }


	// set outcome for action
	return true;
  }




}
