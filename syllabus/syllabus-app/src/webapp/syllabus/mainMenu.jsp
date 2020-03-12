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
  <ul class="navIntraTool actionToolbar">
    <li>
      <span id="syllabusMenuMainLink">
        <h:commandLink action="#{SyllabusTool.processStudentView}" immediate="true">
          <h:outputText value="#{msgs.main}" />
        </h:commandLink>
      </li>
    <h:panelGroup rendered="#{SyllabusTool.bulkAddItem}">
      <li>
        <span id="syllabusMenuBulkAddItemLink">
          <h:commandLink action="#{SyllabusTool.processListNewBulkMain}" immediate="true">
            <h:outputText value="#{msgs.bar_new_bulk}"/>
          </h:commandLink>
        </span>
      </li>
    </h:panelGroup>
    <h:panelGroup rendered="#{SyllabusTool.bulkEdit}">
      <li>
        <span id="syllabusMenuBulkEditLink">
          <h:commandLink action="#{SyllabusTool.processCreateAndEdit}" immediate="true">
            <h:outputText value="#{msgs.bar_create_edit}"/>
          </h:commandLink>
        </span>
      </li>
    </h:panelGroup>
    <h:panelGroup rendered="#{SyllabusTool.redirect}">
      <li>
        <span id="syllabusMenuRedirectLink">
          <h:commandLink action="#{SyllabusTool.processRedirect}" immediate="true">
            <h:outputText value="#{msgs.bar_redirect}"/>
          </h:commandLink>
        </span>
      </li>
    </h:panelGroup>
  </ul>
</h:panelGroup>
