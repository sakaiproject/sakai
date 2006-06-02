<!--
* $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
--%>
-->
<html:form action="startPoolAction.do" method="post">

<h2># of Subpools in
<bean:write name="thisPool" property = "name" />: (
<bean:write name="thisPool" property = "numberOfSubpools" />)
<input type="button" value="Add"  onclick="document.location='<%=request.getContextPath()%>/startCreatePool.do?id=<%=questionpool.getCurrentPool().getId().toString()%>&use=createsub&qpid=<%=questionpool.getCurrentPool().getId().toString()%>'">
</h2>
<div class="h2unit">
<html:submit property="method" >
<bean:message key="button.copy"/>
</html:submit>

<html:submit property="method" >
<bean:message key="button.move"/>
</html:submit>

<html:submit property="method" >
<bean:message key="button.remove"/>
</html:submit>

<html:submit property="method" >
<bean:message key="button.export"/>
</html:submit>

  <table class="tblMain">
    <tr>
      <!-- specifying at least 1 width keeps columns constant
   despite changes in visible content -->
      <th class="altBackground" width="30%"><html:checkbox  onclick="this.value=checkAll(this.form.selectedPools)"  property="allPoolsSelected"/>
        Pool Name</th>
      <th class="altBackground" width="25%">Creator</th>
      <th class="altBackground" width="15%">Last Modified</th>
      <th class="altBackground" width="10%"># of Questions</th>
      <th class="altBackground" width="10%"># of Subpools</th>
      <th class="altBackground" width="10%">&nbsp;</th>
    </tr>
    <logic:iterate name="sortedSubPool" id="pool" type="org.navigoproject.business.entity.questionpool.model.QuestionPool" indexId="ctr">
    <% subpoolTree.setCurrentId(pool.getId()); %>
    <logic:empty name="subpoolTree" property="parent">
    <tr id="<%= subpoolTree.getCurrentObjectHTMLId() %>" >
    </logic:empty>

		<logic:notEmpty name="subpoolTree" property="parent">
    <tr id="<%= subpoolTree.getCurrentObjectHTMLId() %>" >
    </logic:notEmpty>

    <td id="p<%= (ctr.intValue() * 3) + 1 %>">
      <div id="p<%= (ctr.intValue() * 3) + 2 %>"
         class="tier<%= (new Integer(subpoolTree.getCurrentLevel())).intValue()-(parentpools.size()+1) %>" >
        <html:multibox  property="selectedPools"> <bean:write name="pool" property="id"/>
        </html:multibox> <logic:empty name="subpoolTree" property="childList">
        <a id="p<%= (ctr.intValue() * 3) + 3 %>" class="doc">
<!-- need this following line for Mozilla -->
<img border="0" width="17" src="../images/spacer.gif">
</a>
        </logic:empty> <logic:notEmpty name="subpoolTree" property="childList">
        <a id="p<%= (ctr.intValue() * 3) + 3 %>"
             href="#<%=pool.getId()%>" onclick="toggleRows(this)" class="folder">
<!-- need this following line for Mozilla -->
<img border="0" width="17" src="../images/spacer.gif">
</a> </logic:notEmpty>
        <html:link page="/startCreatePool.do?use=edit" paramName="pool" paramProperty="id" paramId="id">
        <bean:write name="pool" property="displayName" /> </html:link> </div></td>
    <logic:iterate name="subpoolTree" property="currentObjectProperties"
       id="props" indexId="propctr">
    <td> <logic:equal name="propctr" value="1"> <bean:write name="props" format="MM/dd/yyyy" />
      </logic:equal> <logic:equal name="propctr" value="2"> <logic:equal name="props" value="0">
      -- </logic:equal> <logic:notEqual name="props" value="0"> <bean:write name="props" />
      </logic:notEqual> </logic:equal> <logic:equal name="propctr" value="3">
      <logic:equal name="props" value="0"> -- </logic:equal> <logic:notEqual name="props" value="0">
      <bean:write name="props" /> </logic:notEqual> </logic:equal> <logic:equal name="propctr" value="0">
      <bean:write name="props" /> </logic:equal> </td>
    </logic:iterate>
    <td> <input type="button" value="Add Subpool"  onclick="document.location='<%=request.getContextPath()%>/startCreatePool.do?id=<%=subpoolTree.getCurrentId().toString()%>&use=createsub&qpid=<%=subpoolTree.getCurrentId().toString()%>'">
    </td>
    </tr>
    </logic:iterate>
  </table>
</div>

</html:form>
