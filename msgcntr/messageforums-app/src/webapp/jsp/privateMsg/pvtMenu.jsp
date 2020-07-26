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
<h:panelGroup rendered="#{PrivateMessagesTool.messages}">
  <ul class="navIntraTool actionToolbar" role="menu">
    <li role="menuitem">
      <span id="messagesMainMenuLink">
        <h:commandLink value="#{msgs.cdfm_message_pvtarea}" action="pvtMsgHpView" immediate="true"/>
      </span>
    </li>
    <h:panelGroup rendered="#{! PrivateMessagesTool.dispError}">
      <li role="menuitem">
        <span id="messagesComposeMenuLink">
          <h:commandLink value="#{msgs.pvt_compose}" id="composeMessage" action="#{PrivateMessagesTool.processPvtMsgCompose}" immediate="true"/>
        </span>
      </li>
    </h:panelGroup>
    <h:panelGroup rendered="#{! PrivateMessagesTool.dispError}">
      <li role="menuitem">
        <span id="messagesNewFolderMenuLink">
          <h:commandLink value="#{msgs.pvt_newfolder}" id="newFolder" action="#{PrivateMessagesTool.processPvtMsgFolderSettingAdd}" immediate="true"/>
        </span>
      </li>
    </h:panelGroup>
    <h:panelGroup rendered="#{PrivateMessagesTool.showSettingsLink}">
      <li role="menuitem">
        <span id="messagesSettingsMenuLink">
          <h:commandLink value="#{msgs.pvt_settings}" id="settings" action="#{PrivateMessagesTool.processPvtMsgSettings}" immediate="true"/>
        </span>
      </li>
    </h:panelGroup>
    <h:panelGroup rendered="#{PrivateMessagesTool.instructor}">
      <li role="menuitem">
        <span id="messagesPermissionsMenuLink">
          <h:commandLink value="#{msgs.pvt_permissions}" id="permissions" action="#{PrivateMessagesTool.processActionPermissions}" immediate="true"/>
        </span>
      </li>
    </h:panelGroup>
  </ul>
</h:panelGroup>
