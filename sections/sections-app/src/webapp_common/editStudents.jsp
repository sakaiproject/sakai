<f:view>
<h:form id="memberForm">

    <sakai:flowState bean="#{editStudentsBean}"/>

    <h:panelGrid columns="3">
    
        <h:panelGroup>
            <h:selectOneMenu value="#{editStudentsBean.availableSectionUuid}" valueChangeListener="#{editStudentsBean.processChangeSection}" onchange="this.form.submit()">
                <f:selectItems value="#{editStudentsBean.availableSectionItems}"/>
            </h:selectOneMenu>
            
            <f:verbatim>
                <br/>
            </f:verbatim>
            
        	<h:selectManyListbox id="availableUsers" size="20" style="width:200px;">
        		<f:selectItems value="#{editStudentsBean.availableUsers}"/>
        	</h:selectManyListbox>
        </h:panelGroup>
    
    	<h:panelGroup>
    		<f:verbatim>
    			<p>
    				<input type="button" onclick="addAll();" value="&gt;&gt;&gt;" />
    			</p>
    			<p>
    				<input type="button" onclick="addUser();" value="&gt;"/>
    			</p>
    			<p>
    				<input type="button" onclick="removeUser();" value="&lt;"/>
    			</p>
    			<p>
    				<input type="button" onclick="removeAll();" value="&lt;&lt;&lt;"/>
    			</p>
    		</f:verbatim>
    	</h:panelGroup>
    	
        <h:panelGroup>
            <h:outputText value="#{editStudentsBean.sectionTitle}"/>
        
            <f:verbatim>
                <br/>
            </f:verbatim>
        
        	<h:selectManyListbox id="selectedUsers" size="20" style="width:200px;">
                <f:selectItems value="#{editStudentsBean.selectedUsers}"/>
        	</h:selectManyListbox>
        </h:panelGroup>

    </h:panelGrid>
    
    <h:commandButton
        action="#{editStudentsBean.update}"
        onclick="highlightUsers()"
        value="#{msgs.edit_student_update}"/>


</h:form>
</f:view>
