<f:view>
<div class="portletBody">
<h:form id="addSectionsForm">

	<sakai:flowState bean="#{addSectionsBean}"/>

	<t:aliasBean alias="#{viewName}" value="addSections">
		<%@ include file="/inc/navMenu.jspf"%>
	</t:aliasBean>

	<h3><h:outputText value="#{msgs.add_sections}"/></h3>

	<t:div styleClass="instructions">
		<h:outputText value="#{msgs.add_section_instructions}"/>
	</t:div>

	<%@ include file="/inc/globalMessages.jspf"%>
	
	<h:outputText value="#{msgs.add}"/>
	<h:selectOneMenu
		id="numToAdd"
		immediate="true"
		value="#{addSectionsBean.numToAdd}"
		valueChangeListener="#{addSectionsBean.processChangeNumSections}"
		onchange="this.form.submit()">
		<f:selectItems value="#{addSectionsBean.numSectionsSelectItems}"/>
	</h:selectOneMenu>
	<h:outputText value="#{msgs.add_section_sections_of}"/>
	<h:selectOneMenu
		id="category"
		immediate="true"
		value="#{addSectionsBean.category}"
		valueChangeListener="#{addSectionsBean.processChangeSectionsCategory}"
		onchange="this.form.submit()">
		<f:selectItem itemLabel="#{msgs.add_sections_select_one}" itemValue=""/>
		<f:selectItems value="#{addSectionsBean.categoryItems}"/>
	</h:selectOneMenu>
	<h:outputText value="#{msgs.add_section_category}"/>

	<t:aliasBean alias="#{bean}" value="#{addSectionsBean}">
		<%@ include file="/inc/sectionEditor.jspf"%>
	</t:aliasBean>

	<t:div styleClass="act">
		<h:commandButton
			action="#{addSectionsBean.addSections}"
			disabled="#{empty addSectionsBean.category}"
			value="#{msgs.add_sections}"
			styleClass="active"
			onclick="reEnableLimits();" />
		
		<h:commandButton action="overview" immediate="true" value="#{msgs.cancel}"/>
	</t:div>
</h:form>
</div>
</f:view>
