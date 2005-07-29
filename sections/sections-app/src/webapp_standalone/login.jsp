<f:view>
	<h:form id="loginForm">
		<p>
			<h:outputLabel for="userName" value="Username:"/>
			<h:inputText id="userName" value="#{loginBean.userName}"/>
		</p>

		<p>
			<h:outputLabel for="context" value="Context:"/>
			<h:inputText id="context" value="#{loginBean.context}"/>
		</p>
		
		<p>
			<h:commandButton value="Login with this username and context" action="#{loginBean.processSetUserNameAndContext}"/>
		</p>
	</h:form>
</f:view>
