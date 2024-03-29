/**********************************************************************************
 *
 * Copyright (c) ${license.git.copyrightYears} ${holder}
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *             http://opensource.org/licenses/ecl2

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.data.ifc.assessment;

import java.util.Date;


public interface ItemHistoricalIfc extends Comparable {

    Long getId();

    void setId(Long id);

    ItemDataIfc getItem();

    void setItem(ItemDataIfc item);

    String getModifiedBy();

    void setModifiedBy(String modifiedBy);

    Date getModifiedDate();

    void setModifiedDate(Date modifiedDate);

}
