<f:view>
	<h:form id="loginForm">
		<p>Pick one of the following users and then pick a gradebook:</p>

		<ul>
			<li>
				<h:commandLink action="selectGradebook">
					<h:outputText value="Bizzy Teacher"/>
					<f:param name="userUid" value="authid_teacher"/>
				</h:commandLink>
				(Instructor in 8 gradebooks)
			</li>
			<li>
				<h:commandLink action="selectGradebook">
					<h:outputText value="Abby Lynn Astudent"/>
					<f:param name="userUid" value="stu_0"/>
				</h:commandLink>
				(Student in 7 gradebooks)
			</li>
			<li>
				<h:commandLink action="selectGradebook">
					<h:outputText value="Teaching Student"/>
					<f:param name="userUid" value="authid_teacher_student"/>
				</h:commandLink>
				(Instructor in one gradebook, student in another)
			</li>
		</ul>

		<p>Or pick one of the following users to play in the assignment-loaded gradebook:</p>

		<h:dataTable id="table" value="#{loginAsBean.loginChoices}" var="whoAndWhat">
			<h:column>
				<h:outputLink value="../#{whoAndWhat.entryPage}">
					<h:outputText value="#{whoAndWhat.userUid} - #{whoAndWhat.role}"/>
					<f:param name="userUid" value="#{whoAndWhat.userUid}"/>
					<f:param name="gradebookUid" value="#{whoAndWhat.gradebookUid}"/>
				</h:outputLink>
			</h:column>
		</h:dataTable>

	</h:form>
</f:view>
