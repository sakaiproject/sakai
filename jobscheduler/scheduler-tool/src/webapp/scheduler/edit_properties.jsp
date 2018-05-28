<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ include file="security_static_include.jsp"%>


<f:view>
    <sakai:view_container title="#{msgs.title_job}">
        <sakai:view_content>
            <h:form>
                <h:graphicImage value="/images/quartz.jpg"/>
                <sakai:group_box title="#{msgs.edit_properties}">
                    <h:outputText value="#{msgs.properties_instructions}"/>
                    <h:dataTable value="#{schedulerTool.configurableJobErrorMessages}"
                                 var="error">
                        <h:column>
                            <h:outputText value="#{error}" style="color:red"/>
                        </h:column>
                    </h:dataTable>
                    <sakai:panel_edit>
                        <h:dataTable value="#{schedulerTool.configurableProperties}"
                                     var="prop"
                                     styleClass="table table-hover table-striped table-bordered">
                            <h:column>
                                <f:facet name="header">
                                    <h:outputText value="#{msgs.properties_name_header}"/>
                                </f:facet>
                                <h:outputText value="#{schedulerTool.configurableJobResources[prop.jobProperty.labelResourceKey]}" rendered="#{schedulerTool.configurableJobResources[prop.jobProperty.labelResourceKey] != null}"/>
                                <h:outputText value="#{prop.jobProperty.labelResourceKey}" rendered="#{schedulerTool.configurableJobResources[prop.jobProperty.labelResourceKey] == null}"/>
                            </h:column>
                            <h:column>
                                <f:facet name="header">
                                    <h:outputText value="#{msgs.properties_value_header}"/>
                                </f:facet>
                                <h:inputText value="#{prop.value}">
                                </h:inputText>
                                <h:outputText rendered="#{prop.jobProperty.required}" value="*" style="color:red"/>
                            </h:column>
                            <h:column>
                                <f:facet name="header">
                                    <h:outputText value="#{msgs.properties_desc_header}"/>
                                </f:facet>
                                <h:outputText value="#{schedulerTool.configurableJobResources[prop.jobProperty.descriptionResourceKey]}" rendered="#{schedulerTool.configurableJobResources[prop.jobProperty.descriptionResourceKey] != null}"/>
                                <h:outputText value="#{prop.jobProperty.descriptionResourceKey}" rendered="#{schedulerTool.configurableJobResources[prop.jobProperty.descriptionResourceKey] == null}"/>
                            </h:column>
                        </h:dataTable>
                    </sakai:panel_edit>
                </sakai:group_box>
                <sakai:button_bar>
                    <sakai:button_bar_item
                        action="#{schedulerTool.processSetProperties}"
                        value="#{msgs.bar_post}" />
                    <sakai:button_bar_item immediate="true"
                        action="jobs"
                        value="#{msgs.cancel}" />
                </sakai:button_bar>
                
            </h:form>
        </sakai:view_content>
    </sakai:view_container>
</f:view>