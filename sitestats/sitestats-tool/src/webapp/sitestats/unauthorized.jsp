
<%/* #####  TAGLIBS, BUNDLE, Etc  ##### */%>
<%@include file="inc/common.jsp"%>

<f:view>
<sakai:view_container title="#{msgs.title}">
  <h2><h:outputText value="#{msgs.unauthorized}"/></h2>
	<h:outputLink value="/portal">
		<h:outputText value="#{msgs.return_to_portal}"/>
	</h:outputLink>
</sakai:view_container>
</f:view>