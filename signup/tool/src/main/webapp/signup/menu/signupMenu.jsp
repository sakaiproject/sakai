<%--
***********************************************************************************
*
* Copyright (c) 2020 Apereo Foundation
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
--%>
<h:panelGroup>
  <ul class="navIntraTool actionToolbar" role="menu">
    <li role="menuitem">
      <span id="signupMainMenuLink">
        <h:commandLink value="#{msgs.main_meetings}" action="listMeetings"/>
      </span>
    </li>
    <h:panelGroup rendered="#{SignupMeetingsBean.allowedToCreate}">
      <li role="menuitem">
        <span id="signupAddMeetingMenuLink">
          <h:commandLink value="#{msgs.add_new_event}" action="#{SignupMeetingsBean.addMeeting}"/>
        </span>
      </li>
    </h:panelGroup>
    <h:panelGroup rendered="#{SignupPermissionsUpdateBean.showPermissionLink}">   
      <li role="menuitem">
        <span id="signupPermissionMenuLink">
          <h:commandLink value="#{msgs.permission_feature_link}" action="#{SignupPermissionsUpdateBean.processPermission}"/>
        </span>
      </li>
    </h:panelGroup>
    <li role="menuitem">
      <span id="signupExportMenuLink">
        <h:commandLink value="#{msgs.event_pageTop_link_for_download}" action="#{DownloadEventBean.downloadSelections}" />
      </span>
    </li>
  </ul>
</h:panelGroup>
