<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>
  <sakai:view>
    <h:form>
		<h3><h:outputText value="#{msgs.syn_options}" /></h3>
				
		<p class="instruction"><h:outputText value="#{msgs.syn_directions}"/></p>
				
  	    <h:messages styleClass="alertMessage" id="errorMessages"/> 

 	<%-- (gsilver) 2 issues 
	1.  if there are no sites to populate both selects a message should put in the response to the effect that there are no memberships, hence cannot move things onto tabs group or off it. The table and all its children should then be excluded  from the response.
		2. if a given select is empty (has no option children) the resultant xhtml is invalid - we may need to seed it if this is important. This is fairly standard practice and helps to provide a default width to an empty select item (ie: about 12 dashes)
--%>	

		<table cellspacing="0" cellpadding="5%" class="sidebyside" summary="layout">
    	<tr>
    	  <td>
    	    <b><h:outputText value="#{msgs.syn_site_not_vis}"/></b>
    		  <br />
    		  <h:selectManyListbox value="#{mfSynopticBean.nonNotificationSites}" size="10">
 				<f:selectItems value="#{mfSynopticBean.nonNotificationSitesItems}" />
			  </h:selectManyListbox>
		  </td>

		  <td style="text-align: center;">
			<h:commandButton id="add" value="#{msgs.syn_move_rone}" action="#{mfSynopticBean.processActionAdd}" title="#{msgs.syn_move_inst}" ></h:commandButton>
			<br />
			<h:commandButton id="remove" value="#{msgs.syn_move_lone}" action="#{mfSynopticBean.processActionRemove}" title="#{msgs.syn_remove_inst}" ></h:commandButton>
			<br /><br />
		    <h:commandButton id="addAll" value="#{msgs.syn_move_rall}" action="#{mfSynopticBean.processActionAddAll}" title="#{msgs.syn_move_all_inst}" ></h:commandButton>
		    <br />
		    <h:commandButton id="removeAll" value="#{msgs.syn_move_lall}" action="#{mfSynopticBean.processActionRemoveAll}" title="#{msgs.syn_remove_all_inst}" ></h:commandButton>
		  </td>
	
		  <td>
			<b><h:outputText value="#{msgs.syn_site_vis}"/></b>
    		<br/>
			<h:selectManyListbox value="#{mfSynopticBean.notificationSites}" size="10">
			  <f:selectItems value="#{mfSynopticBean.notificationSitesItems}" />
			</h:selectManyListbox>
		  </td>
    	</tr>
	    </table>
	    
    	<sakai:button_bar>
      		<sakai:button_bar_item action="#{mfSynopticBean.processOptionsChange}" value="#{msgs.syn_change_submit}"
            	accesskey="s" title="Save Synoptic Message Center Options" styleClass="active"/>
      		<sakai:button_bar_item action="#{mfSynopticBean.processOptionsCancel}" value="#{msgs.syn_cancel}" 
            	accesskey="c" title="Cancel Synoptic Message Center Options" />
    	</sakai:button_bar>
    </h:form>
  </sakai:view>
</f:view>