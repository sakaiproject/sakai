<f:view>
	<h:form id="loginForm">
    
        <h:messages globalOnly="true"/>
        
		<p>
			<h:outputLabel for="userName" value="Username:"/>
            <h:selectOneMenu id="userName" value="#{loginBean.userName}">
                <f:selectItem itemValue="instructor1" itemLabel="Instructor 1 (Site 1)"/>
                <f:selectItem itemValue="instructor2" itemLabel="Instructor 2 (Sites 2, and 3)"/>
                <f:selectItem itemValue="ta1" itemLabel="TA 1"/>
                <f:selectItem itemValue="ta2" itemLabel="TA 2"/>
                <f:selectItem itemValue="studenta" itemLabel="Joe Student"/>
                <f:selectItem itemValue="studentb" itemLabel="Jane Undergrad"/>
                <f:selectItem itemValue="studentc" itemLabel="Max Guest"/>
                <f:selectItem itemValue="student1" itemLabel="Student 1"/>
                <f:selectItem itemValue="student2" itemLabel="Student 2"/>
                <f:selectItem itemValue="student3" itemLabel="Student 3"/>
                <f:selectItem itemValue="student4" itemLabel="Student 4"/>
                <f:selectItem itemValue="student5" itemLabel="Student 5"/>
                <f:selectItem itemValue="student6" itemLabel="Student 6"/>
                <f:selectItem itemValue="student7" itemLabel="Student 7"/>
                <f:selectItem itemValue="student8" itemLabel="Student 8"/>
                <f:selectItem itemValue="student9" itemLabel="Student 9"/>
                <f:selectItem itemValue="student10" itemLabel="Student 10"/>
            </h:selectOneMenu>
		</p>

		<p>
			<h:outputLabel for="context" value="Context:"/>
			<h:selectOneMenu id="context" value="#{loginBean.context}">
                <f:selectItem itemValue="site1" itemLabel="Site 1 (Enterprise Managed)"/>
                <f:selectItem itemValue="site2" itemLabel="Site 2"/>
                <f:selectItem itemValue="site3" itemLabel="Site 3"/>
            </h:selectOneMenu>            
		</p>
		
		<p>
			<h:commandButton value="Login with this username and context" action="#{loginBean.processSetUserNameAndContext}"/>
		</p>
	</h:form>
</f:view>

