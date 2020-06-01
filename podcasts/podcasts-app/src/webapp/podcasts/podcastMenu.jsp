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
<h:panelGroup><f:verbatim>
  <ul class="navIntraTool actionToolbar" role="menu">
    <li role="menuitem">
      <span id="podcastMainMenuLink"></f:verbatim>
        <h:commandLink value="#{msgs.podcast_home_title}" action="cancel"/><f:verbatim>
      </span>
    </li></f:verbatim>
    <h:panelGroup rendered="#{podHomeBean.hasNewPerm || podHomeBean.canUpdateSite}"><f:verbatim>
      <li role="menuitem">
        <span id="podcastAddMenuLink"></f:verbatim>
          <h:commandLink value="#{msgs.add}" action="podcastAdd"/><f:verbatim>
        </span>
      </li></f:verbatim>
    </h:panelGroup>
    <h:panelGroup rendered="#{podHomeBean.canUpdateSite}"><f:verbatim>
      <li role="menuitem">
        <span id="podcastOptionsMenuLink"></f:verbatim>
          <h:commandLink value="#{msgs.options}" action="podcastOptions"/><f:verbatim>
        </span>
      </li></f:verbatim>
    </h:panelGroup>
    <h:panelGroup rendered="#{podHomeBean.canUpdateSite}"><f:verbatim>
      <li role="menuitem">
        <span id="podcastPermissionsMenuLink"></f:verbatim>
          <h:commandLink value="#{msgs.permissions}" action="#{podHomeBean.processPermissions}" /><f:verbatim>
        </span>
      </li></f:verbatim>
    </h:panelGroup><f:verbatim>
  </ul></f:verbatim>
</h:panelGroup>

