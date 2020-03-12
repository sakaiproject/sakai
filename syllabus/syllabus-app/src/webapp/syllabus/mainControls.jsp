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
  <h:outputLink rendered="#{SyllabusTool.addItem}" styleClass="button" value="javascript:showConfirmAddHelper();">
    <span class="fa fa-plus" aria-hidden="true"></span>
    <h:outputText value="#{msgs.addItemTitle}"/>
  </h:outputLink>
  <button class="button" id="expandLink" onclick="return false;">
    <span class="fa fa-expand" aria-hidden="true"></span>
    <h:outputText value="#{msgs.expandAll}"/>
  </button>
  <button class="button" id="collapseLink" onclick="return false;" style="display:none;">
    <span class="fa fa-compress" aria-hidden="true"></span>
    <h:outputText value="#{msgs.collapseAll}"/>
  </button>
  <h:outputLink styleClass="button" id="print" value="javascript:printFriendly('#{SyllabusTool.printFriendlyUrl}');">
    <span class="fa fa-print" aria-hidden="true"></span>
    <h:outputText value="#{msgs.printView}"/>
  </h:outputLink>
</h:panelGroup>
