<style type="text/css">
	@import url("sitestats/css/sitestats.css");
</style>

<h:panelGrid styleClass="sectionContainerNav" style="width: 100%;vertical-align:top;" columns="2" columnClasses="left1,right1">			
	<h:panelGrid columns="1" style="vertical-align: top; text-align: left; white-space: nowrap;">
		
		<%/* DATE SELECTORS */%>
		<h:outputText value="#{msgs.select_date}" style="font-weight: bold;"/>
		<h:panelGrid columns="1">
			   <%/* ALL/INTERVAL: DATE SELECTOR */%>
               <t:selectOneRadio id="dateSelector" value="#{bean.selectedDateType}" layout="spread" immediate="true">
                   <f:selectItem itemValue="0" itemLabel="#{bean.dateTypeAllStr}" />
                   <f:selectItem itemValue="1" itemLabel="#{bean.dateFromStr}" />
               </t:selectOneRadio>
               
               <%/* FROM: DATE SELECTOR */%>
               <t:radio for="dateSelector" index="0"/>
               <h:panelGroup>
                   <t:radio for="dateSelector" index="1" /><f:verbatim>&nbsp;</f:verbatim>
                   	<h:selectOneMenu value="#{bean.fromYear}">
    					<f:selectItems value="#{bean.years}"/> 
					</h:selectOneMenu>
					<h:selectOneMenu value="#{bean.fromMonth}">
    					<f:selectItems value="#{bean.months}"/> 
					</h:selectOneMenu>
					<h:selectOneMenu value="#{bean.fromDay}">
    					<f:selectItems value="#{bean.days}"/> 
					</h:selectOneMenu>
					
				<%/* TO: DATE SELECTOR */%>
				<f:verbatim>&nbsp;</f:verbatim><h:outputText value="#{bean.dateToStr}" style="font-weight: bold;"/><f:verbatim>&nbsp;</f:verbatim>
                    <h:selectOneMenu value="#{bean.toYear}">
    					<f:selectItems value="#{bean.years}"/> 
					</h:selectOneMenu>
					<h:selectOneMenu value="#{bean.toMonth}">
    					<f:selectItems value="#{bean.months}"/> 
					</h:selectOneMenu>
					<h:selectOneMenu value="#{bean.toDay}">
    					<f:selectItems value="#{bean.days}"/> 
					</h:selectOneMenu>
               </h:panelGroup>
           </h:panelGrid>
		
		<%/* GROUP SELECTOR */%>
		<t:div style="margin-top: 10px;">
			<h:outputText value="#{msgs.view}" style="font-weight: bold;"/><f:verbatim>&nbsp;</f:verbatim>
			<h:selectOneMenu value="#{bean.selectedGroup}" disabled="#{bean.groupsSize == 1}">
   	            <f:selectItems value="#{bean.groups}"/> 
            </h:selectOneMenu> 	
        </t:div>
	</h:panelGrid>
		
	<%/* SEARCH BUTTON */%>
	<h:commandButton id="searchButton" action="#{bean.processActionSearch}" onkeypress="document.forms[0].submit;" value="#{msgs.search}" style="text-align: right; white-space: nowrap; vertical-align:top;width:80px;text-align:center"/>
</h:panelGrid>
   	 
   	    
<h:panelGrid styleClass="sectionContainerNav" style="width: 100%;" columns="2" columnClasses="sectionLeftNav,sectionRightNav">			
	
	<%/* SEARCH BOX & BUTTONS */%>               
	<t:div style="vertical-align: bottom; text-align: left; white-space: nowrap;">
		<t:div style="margin-left: 3px; margin-top: 10px;">
			<h:outputText value="#{msgs.search_label}" style="font-weight: bold;"/>
			<h:inputText id="inputSearchBox" value="#{bean.searchKeyword}" onclick="this.value=''" valueChangeListener="#{bean.processActionSearchChangeListener}" style="width: 160px;"
					onfocus="if(this.value == '#{msgs.search_int}') this.value = '';"/>				   						
			<h:commandButton id="clearSearchButton" action="#{bean.processActionClearSearch}" onkeypress="document.getElementById('inputSearchBox').value = ''; return false;" value="#{msgs.clear_search}" style="width:80px;text-align:center"/>
		</t:div>
	</t:div>
	
	<%/* PAGER */%>               
	<t:div style="text-align: right; white-space: nowrap; vertical-align:bottom;">
		<sakai:pager
			totalItems="#{bean.totalItems}"
			firstItem="#{bean.firstItem}"
			pageSize="#{bean.pageSize}"
			accesskeys="true" immediate="true"/>
	</t:div>
</h:panelGrid>