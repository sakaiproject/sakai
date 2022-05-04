/******************************************************************************
 * Copyright 2022 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.beans;

import org.sakaiproject.authz.api.Member;
import org.springframework.hateoas.Link;

import lombok.Data;
import lombok.NonNull;

@Data
public class MemberRestBean {

    private String id;
    private String eid;
    private String role;
    private String siteId;
    private String displayName;
    private Boolean status;
    private Link link;

    public static MemberRestBean of(@NonNull Member member) {
        MemberRestBean memberBean = new MemberRestBean();
        memberBean.setId(member.getUserId());
        memberBean.setEid(member.getUserEid());
        memberBean.setRole(member.getRole().getId());
        memberBean.setStatus(member.isActive());
        return memberBean;
    }

    public MemberRestBean merge(@NonNull MemberRestBean mergeMemberBean) {
        if(mergeMemberBean.getRole() != null) {
            role = mergeMemberBean.getRole();
        }
        if(mergeMemberBean.getStatus() != null) {
            status = mergeMemberBean.getStatus();
        }
        return this;
    }
}
