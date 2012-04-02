/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.services.qti;

/**
 * <p>Isolates exceptions in the QTI service layer.</p>
 * <p> </p>
 * <p>Copyright: Copyright (c) 2005 Sakai</p>
 * <p> </p>
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */

public class QTIServiceException extends RuntimeException
{

  public QTIServiceException()
  {
    super();
  }

  public QTIServiceException(String message)
  {
    super(message);
  }

  public QTIServiceException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public QTIServiceException(Throwable cause)
  {
    super(cause);
  }
}
