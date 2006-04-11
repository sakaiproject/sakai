<%-- $Id: index.jsp,v 1.17 2005/05/12 14:29:52 janderse.umich.edu Exp $ --%>
<f:view>
<sakai:view title="Tag Usage Demos">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>

<h2>Tag Usage demonstration.</h2>
<h:panelGrid columns="3" border="2">
<h:commandLink action="alphaIndex"><h:outputText value="*alphaIndex" /></h:commandLink>
<h:commandLink action="anchorReference"><h:outputText value="*anchorReference" /></h:commandLink>
<%-- <h:commandLink action="applet"><h:outputText value="applet" /></h:commandLink> --%>
<h:commandLink action="buttonBar"><h:outputText value="buttonBar" /></h:commandLink>
<h:commandLink action="buttonBarItem"><h:outputText value="buttonBarItem" /></h:commandLink>
<h:commandLink action="dataLine"><h:outputText value="*dataLine" /></h:commandLink>
<h:commandLink action="debug"><h:outputText value="*debug" /></h:commandLink>
<h:commandLink action="docProperties"><h:outputText value="docProperties" /></h:commandLink>
<h:commandLink action="docSection"><h:outputText value="docSection" /></h:commandLink>
<h:commandLink action="docSectionTitle"><h:outputText value="docSectionTitle" /></h:commandLink>
<%-- <h:commandLink action="dynaTable"><h:outputText value="dynaTable" /></h:commandLink>--%>
<h:commandLink action="flatList"><h:outputText value="flatList" /></h:commandLink>
<h:commandLink action="hideDivision"><h:outputText value="*hideDivision" /></h:commandLink>
<h:commandLink action="inputColor"><h:outputText value="*inputColor" /></h:commandLink>
<h:commandLink action="inputDate"><h:outputText value="*inputDate" /></h:commandLink>
<h:commandLink action="inputFileUpload"><h:outputText value="inputFileUpload" /></h:commandLink>
<h:commandLink action="inputRichText"><h:outputText value="*inputRichText" /></h:commandLink>
<h:commandLink action="messageInstruction"><h:outputText value="messageInstruction" /></h:commandLink>
<%-- <h:commandLink action="dynaTable"><h:outputText value="multiColumn" /></h:commandLink>--%>
<h:commandLink action="outputDate"><h:outputText value="*outputDate" /></h:commandLink>
<h:commandLink action="pager"><h:outputText value="*pager" /></h:commandLink>
<h:commandLink action="panelEdit"><h:outputText value="panelEdit" /></h:commandLink>
<h:commandLink action="panelTitle"><h:outputText value="panelTitle" /></h:commandLink>
<h:commandLink action="popup"><h:outputText value="popup" /></h:commandLink>
<h:commandLink action="progressBar"><h:outputText value="*progressBar" /></h:commandLink>
<h:commandLink action="script"><h:outputText value="*script" /></h:commandLink>
<%-- <h:commandLink action="selectCommand"><h:outputText value="selectCommand" /></h:commandLink>--%>
<h:commandLink action="stylesheet"><h:outputText value="*stylesheet" /></h:commandLink>
<h:commandLink action="timerBar"><h:outputText value="*timerBar" /></h:commandLink>
<h:commandLink action="titleBar"><h:outputText value="titleBar" /></h:commandLink>
<h:commandLink action="toolBar"><h:outputText value="toolBar" /></h:commandLink>
<h:commandLink action="toolBarItem"><h:outputText value="toolBarItem" /></h:commandLink>
<h:commandLink action="toolBarMessage"><h:outputText value="toolBarMessage" /></h:commandLink>
<h:commandLink action="toolBarSpacer"><h:outputText value="toolBarSpacer" /></h:commandLink>
<h:commandLink action="view"><h:outputText value="view" /></h:commandLink>
<h:commandLink action="viewContent"><h:outputText value="viewContent" /></h:commandLink>
<h:commandLink action="viewTitle"><h:outputText value="*viewTitle" /></h:commandLink>
</h:panelGrid>
<f:verbatim><br /></f:verbatim>
<h:outputText value="*If this tag is broken, its an immediate and definite bug." />

</sakai:view>
</f:view>
