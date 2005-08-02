<f:view>
<h:form id="memberForm">

<h:panelGrid columns="3">
	<h:selectManyListbox id="availableUsers" size="20">
		<f:selectItem itemLabel="Student, Joe" itemValue="user1"/>
		<f:selectItem itemLabel="Student, Sally" itemValue="user2"/>
		<f:selectItem itemLabel="Student, Fred" itemValue="user3"/>		
	</h:selectManyListbox>

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
	
	<h:selectManyListbox id="selectedUsers" size="20">
		<f:selectItem itemLabel="Student, Josh" itemValue="user4"/>
		<f:selectItem itemLabel="Student, Oliver" itemValue="user5"/>
		<f:selectItem itemLabel="Student, Ray" itemValue="user6"/>		
	</h:selectManyListbox>
</h:panelGrid>
		
</h:form>
</f:view>
