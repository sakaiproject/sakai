<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:view>

<p>
    Does the course object for this site exist (from component manager)? <h:outputText value="#{testServiceBean.courseExists}"/>
</p>

<h:dataTable value="#{testServiceBean.sections}" var="section" title="Sections (from component manager)">
    <h:column>
        <h:outputText value="#{section.title}"/>
    </h:column>
</h:dataTable>


<hr/>

<p>
    Does the course object for this site exist (from static cover)? <h:outputText value="#{testServiceBean.courseExistsFromCover}"/>
</p>

<h:dataTable value="#{testServiceBean.sectionsFromCover}" var="section" title="Sections (from static cover)">
    <h:column>
        <h:outputText value="#{section.title}"/>
    </h:column>
</h:dataTable>

</f:view>
