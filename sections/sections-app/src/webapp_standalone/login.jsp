<f:view>
	<h:form id="loginForm">
		<h:inputText id="userName" value="#{loginBean.userName}"/>
		<h:commandButton value="Login with this username" action="#{loginBean.processSetUserName}"/>
	</h:form>
</f:view>