<f:view>
<div class="portletBody">
<h:form id="addSectionsForm">

	<sakai:flowState bean="#{addSectionsBean}"/>

	<x:aliasBean alias="#{viewName}" value="addSections">
		<%@include file="/inc/navMenu.jspf"%>
	</x:aliasBean>

	<h3><h:outputText value="#{msgs.nav_add_sections}"/></h3>

	<x:div styleClass="instructions">
		<h:outputText value="#{msgs.add_section_instructions}"/>
	</x:div>

	<%@include file="/inc/globalMessages.jspf"%>
	
	<h:outputText value="#{msgs.add_section_add}"/>
	<h:selectOneMenu
		id="numToAdd"
		immediate="true"
		value="#{addSectionsBean.numToAdd}"
		valueChangeListener="#{addSectionsBean.processChangeSections}"
		onchange="this.form.submit()">
		<f:selectItem itemValue="1"/>
		<f:selectItem itemValue="2"/>
		<f:selectItem itemValue="3"/>
		<f:selectItem itemValue="4"/>
		<f:selectItem itemValue="5"/>
		<f:selectItem itemValue="6"/>
		<f:selectItem itemValue="7"/>
		<f:selectItem itemValue="8"/>
		<f:selectItem itemValue="9"/>
		<f:selectItem itemValue="10"/>
	</h:selectOneMenu>
	<h:outputText value="#{msgs.add_section_sections_of}"/>
	<h:selectOneMenu
		id="category"
		immediate="true"
		value="#{addSectionsBean.category}"
		valueChangeListener="#{addSectionsBean.processChangeSections}"
		onchange="this.form.submit()">
		<f:selectItem itemLabel="#{msgs.add_sections_select_one}" itemValue=""/>
		<f:selectItems value="#{addSectionsBean.categoryItems}"/>
	</h:selectOneMenu>
	<h:outputText value="#{msgs.add_section_category}"/>

	<x:aliasBean alias="#{bean}" value="#{addSectionsBean}">
		<%@include file="/inc/sectionEditor.jspf"%>
	</x:aliasBean>

	<x:div styleClass="act">
		<h:commandButton
			action="#{addSectionsBean.addSections}"
			disabled="#{empty addSectionsBean.category}"
			value="#{msgs.add_sections_add}"
			styleClass="active" />
		
		<h:commandButton action="overview" immediate="true" value="#{msgs.add_sections_cancel}"/>
	</x:div>
</h:form>
</div>
</f:view>
