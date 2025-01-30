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

package org.sakaiproject.tool.assessment.data.dao.assessment;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemHistoricalIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;

@Data
@NoArgsConstructor
public class ItemHistorical implements Serializable, Comparable, ItemHistoricalIfc {

    private static final long serialVersionUID = 1L;

    private Long id;
    private ItemDataIfc item;
    private String modifiedBy;
    private Date modifiedDate;

    public ItemHistorical(ItemDataIfc item, String modifiedBy, Date modifiedDate) {
        this.item = item;
        this.modifiedBy = modifiedBy;
        this.modifiedDate = modifiedDate;
    }

    public int compareTo(Object other) {
        if (other != null && other instanceof ItemHistorical) {
            ItemHistorical itemHistorical = (ItemHistorical) other;
            if (this.modifiedDate != null && itemHistorical.modifiedDate != null) {
                return this.modifiedDate.compareTo(itemHistorical.modifiedDate);
            }
        }
        return 0;
    }

}
