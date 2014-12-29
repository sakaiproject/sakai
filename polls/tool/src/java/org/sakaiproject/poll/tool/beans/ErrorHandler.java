/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.poll.tool.beans;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.state.support.ErrorStateManager;
import uk.org.ponder.util.RunnableInvoker;
import uk.org.ponder.util.UniversalRuntimeException;


/**
 * Allows all errors to pass through to the outside portal
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ErrorHandler implements RunnableInvoker {

   private ErrorStateManager errorStateManager;
   public void setErrorStateManager(ErrorStateManager errorStateManager) {
      this.errorStateManager = errorStateManager;
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.util.RunnableInvoker#invokeRunnable(java.lang.Runnable)
    */
   public void invokeRunnable(Runnable torun) {
      TargettedMessageList tml = errorStateManager.getTargettedMessageList();
      for (int i = 0; i < tml.size(); ++ i) {
         TargettedMessage message = tml.messageAt(i);
         if (message.exception != null) { 
            throw UniversalRuntimeException.accumulate(message.exception);
         }
      }
      torun.run();
   }

}
