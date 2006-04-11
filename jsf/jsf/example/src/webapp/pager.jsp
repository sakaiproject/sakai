<%-- $Id: pager.jsp,v 1.4 2005/05/04 21:20:40 janderse.umich.edu Exp $ --%>
<f:view>
<sakai:view title="pager tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>


<hr />
<h2>pager tag examples</h2>
<hr />

<h:form id="theForm">
<pre>
    &lt;sakai:pager totalItems="92" pageSize="20" textItem="students" renderPageSize="false" /&gt;
</pre>
    <sakai:pager totalItems="92" pageSize="20" textItem="students" renderPageSize="false" />

 <br />

<pre>

    &lt;sakai:pager 
        totalItems="#{pagerBean.totalItems}" 
        firstItem="#{pagerBean.firstItem}" 
        pageSize="#{pagerBean.pageSize}"
        valueChangeListener="#{pagerBean.handleValueChange}"
        accesskeys="true"
        immediate="true" /&gt;
        
	&lt;h:dataTable first="#{pagerBean.firstItem}" rows="#{pagerBean.pageSize}" value="#{pagerBean.data}" var="item"&gt;
		&lt;h:column&gt;
			&lt;h:outputText value="#{item}" /&gt;
		&lt;/h:column&gt;
	&lt;/h:dataTable&gt;
</pre>

    <sakai:pager 
        totalItems="#{pagerBean.totalItems}" 
        firstItem="#{pagerBean.firstItem}" 
        pageSize="#{pagerBean.pageSize}"
        valueChangeListener="#{pagerBean.handleValueChange}"
        accesskeys="true"
        immediate="true" />
        
	<h:dataTable first="#{pagerBean.firstItem}" rows="#{pagerBean.pageSize}" value="#{pagerBean.data}" var="item">
		<h:column>
			<h:outputText value="#{item}" />
		</h:column>
	</h:dataTable>

 <br />
	
<pre>
    &lt;sakai:pager totalItems="101" firstItem="49" pageSize="3" pageSizes="3,5,7,11" 
    renderFirst="false" renderLast="false" 
    textPrev="Previous page" textNext="Next page" immediate="true" 
    textStatus="There are {2} things this pager is managing.  You're looking at {0} to {1} right now." /&gt;
</pre>
    <sakai:pager totalItems="101" firstItem="49" pageSize="3" pageSizes="3,5,7,11" 
    renderFirst="false" renderLast="false" 
    textPrev="Previous page" textNext="Next page" immediate="true" 
    textStatus="There are {2} things this pager is managing.  You are looking at {0} to {1} right now."
    />

<pre>
    &lt;sakai:pager totalItems="0" /&gt;
</pre>
    <sakai:pager totalItems="0" />

 <br />

</h:form>
</sakai:view>
</f:view>
