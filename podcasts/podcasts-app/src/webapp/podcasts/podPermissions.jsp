<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
    <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.podcasts.bundle.Messages"/>
</jsp:useBean>

<f:view>
    <sakai:view>
        <sakai:view_title value="#{msgs.permissions}" />

        <sakai-permissions group-reference="<h:outputText value="#{podHomeBean.groupReference}" />" disabled-groups=true tool="content" bundle-key="org.sakaiproject.api.podcasts.bundle.Messages" />
    </sakai:view>
</f:view> 