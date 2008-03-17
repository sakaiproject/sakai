<t:div styleClass="additionalInfo" rendered="#{ServiceBean.lastJobRunDateVisible}">
	<t:graphicImage value="/sitestats/images/silk/icons/information.png" style="vertical-align:middle"/>
    <t:outputText value=" #{msgs.last_job_run_date} " style="vertical-align:middle"/>
    <t:outputText value="#{ServiceBean.lastJobRunDate}" style="vertical-align:middle">
    	<f:converter converterId="org.sakaiproject.sitestats.tool.jsf.converter.LOCALIZED_FULLDATE"/>
	</t:outputText>
</t:div>