/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/

package example;

import java.io.Serializable;

/**
 * <p>Data for example applications.</p>
 * <p> </p>
 * <p>Copyright: Copyright  Sakai (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley
 * @version $Id$
 */

public class Grade implements Serializable {
  private String name;
  private String score;
  public Grade() {
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getScore() {
    return score;
  }
  public void setScore(String score) {
    this.score = score;
  }

}



