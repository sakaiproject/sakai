<f:view>
<h:form id="overviewForm">

    <sakai:flowState bean="#{editSectionBean}"/>
    
    <h:panelGrid columns="2">
        <h:outputLabel for="title" value="Title"/>
        <h:inputText id="title" value="#{editSectionBean.title}"/>
    </h:panelGrid>

    <h:commandButton action="#{editSectionBean.update}" value="Update"/>
    
    <h:commandButton action="deleteSection" value="Delete">
        <f:param name="sectionUuid" value="#{editSectionBean.sectionUuid}"/>
    </h:commandButton>
    
    <h:commandButton action="overview" value="Cancel"/>
    
</h:form>
</f:view>
