<f:view>
<div class="portletBody">
<h:form id="closedForm">

    <sakai:flowState bean="#{optionsBean}"/>


        <h3><h:outputText value="#{msgs.section_close_title}"/></h3>   

        <h4><h:outputText value="#{msgs.section_close_desc}"/></h4>   

        <br>
        <h:outputText value="#{msgs.section_close_opendate}"/>&nbsp;<h:outputText value="#{optionsBean.openDate}"/><br>  

        <br>
        <h:outputText value="#{msgs.section_refresh_info}"/>

</h:form>
</div>
</f:view>