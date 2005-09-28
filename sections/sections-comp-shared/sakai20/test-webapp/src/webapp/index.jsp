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


<hr/>

<p>
    All unassigned students
</p>

<h:dataTable value="#{testServiceBean.unassignedStudents}" var="enr" title="Unsectioned Students">
    <h:column>
        <h:outputText value="#{enr.user.userUid}"/>
    </h:column>
</h:dataTable>

<hr/>

<p>
    All unassigned TAs
</p>

<h:dataTable value="#{testServiceBean.unassignedTas}" var="ta" title="Unsectioned TAs">
    <h:column>
        <h:outputText value="#{ta.user.userUid}"/>
    </h:column>
</h:dataTable>

</f:view>
