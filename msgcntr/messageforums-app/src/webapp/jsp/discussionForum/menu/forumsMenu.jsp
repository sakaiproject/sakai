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
      <span id="forumsMainMenuLink">
        <h:commandLink value="#{msgs.cdfm_discussion_forums}" action="forumsMain" immediate="true"/>
      </span>
    </li>
    <h:panelGroup rendered="#{ForumTool.newForum}">
      <li role="menuitem">
        <span id="forumsNewMenuLink">
          <h:commandLink value="#{msgs.cdfm_new_forum}" id="newForum" action="#{ForumTool.processActionNewForum}" immediate="true"/>
        </span>
      </li>
    </h:panelGroup>
    <h:panelGroup rendered="#{ForumTool.instructor}">
      <li role="menuitem">
        <span id="forumsOrganizeMenuLink">
          <h:commandLink value="#{msgs.cdfm_organize}" id="organizeForum" action="#{ForumTool.processActionTemplateOrganize}" immediate="true"/>
        </span>
      </li>
    </h:panelGroup>
    <h:panelGroup rendered="#{ForumTool.instructor}">
      <li role="menuitem">
        <span id="forumsTemplateSettingsMenuLink">
          <h:commandLink value="#{msgs.cdfm_template_setting}" id="templateSettings" action="#{ForumTool.processActionTemplateSettings}" immediate="true"/>
        </span>
      </li>
    </h:panelGroup>
    <h:panelGroup rendered="#{ForumTool.instructor}">
      <li role="menuitem">
        <span id="forumsStatisticsMenuLink">
          <h:commandLink value="#{msgs.stat_list}" id="statList" action="#{ForumTool.processActionStatistics}" immediate="true"/>
        </span>
      </li>
    </h:panelGroup>
    <h:panelGroup rendered="#{ForumTool.displayPendingMsgQueue}">
      <li role="menuitem">
        <span id="forumsQueueMenuLink">
          <h:commandLink value="#{msgs.cdfm_msg_pending_queue} #{msgs.cdfm_openb}#{ForumTool.numPendingMessages}#{msgs.cdfm_closeb}" id="pendingQueue" action="#{ForumTool.processPendingMsgQueue}" immediate="true"/>
        </span>
      </li>
    </h:panelGroup>
    <h:panelGroup rendered="#{ForumTool.instructor && ForumTool.ranksEnabled}">
      <li role="menuitem">
        <span id="forumsRanksMenuLink">
            <h:commandLink value="#{msgs.ranks}" id="viewRanks" action="#{ForumTool.processPendingMsgQueue}" immediate="true"/>
        </span>
      </li>
    </h:panelGroup>
    <h:panelGroup>
      <li role="menuitem">
        <span id="forumsWatchMenuLink">
          <h:commandLink value="#{msgs.watch}" id="watch" action="#{ForumTool.processActionWatch}" immediate="true"/>
        </span>
      </li>
    </h:panelGroup>
  </ul>
</h:panelGroup>
