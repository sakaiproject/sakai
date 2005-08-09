<f:view>
	<h:form id="loginForm">
		<p>
			<h:outputLabel for="userName" value="Username:"/>
			<h:inputText id="userName" value="#{loginBean.userName}"/>
		</p>

		<p>
			<h:outputLabel for="context" value="Context:"/>
			<h:selectOneMenu id="context" value="#{loginBean.context}">
                <f:selectItem itemValue="site1" itemLabel="Site 1"/>
                <f:selectItem itemValue="site2" itemLabel="Site 2"/>
                <f:selectItem itemValue="site3" itemLabel="Site 3"/>
            </h:selectOneMenu>            
		</p>
		
		<p>
			<h:commandButton value="Login with this username and context" action="#{loginBean.processSetUserNameAndContext}"/>
		</p>
	</h:form>
</f:view>
