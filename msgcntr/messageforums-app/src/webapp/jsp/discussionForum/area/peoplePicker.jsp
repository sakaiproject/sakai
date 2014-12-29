
<f:verbatim>
    <h:outputText escape="false" value='<div class="sakai-ppkr" id="sakai-ppkr-messages" title="#{msgs.rank_picker_assign_individuals}">'/>
        <div class="sakai-ppkr-filter">
            <div class="sakai-ppkr-header sakai-ppkr-filter-header">
                <div class="section_header"><h:outputText value="#{msgs.rank_assign_filter_site_members}"/></div>
            </div>
            <div class="sakai-ppkr-filter-fields">
                <label><h:outputText value="#{msgs.rank_assign_by_name}"/></label> <input class="sakai-ppkr-search-field" type="text" value="" id="searchIndividual" /><br /> 
                <label><h:outputText value="#{msgs.rank_assign_in_group}"/></label> <select name="sectionDropdown" class="sectionDropdown">
                    <option value="all-sections" id="group-option-all"><h:outputText value="#{msgs.rank_assign_all_groups}"/></option>
                </select>&nbsp; 
                <label><h:outputText value="#{msgs.rank_assign_of_role}"/></label> <select name="roleDropdown" class="roleDropdown">
                    <option value="all-roles" id="role-option-all"><h:outputText value="#{msgs.rank_assign_all_roles}"/></option>
                </select>
            </div>
        </div>
        <div class="sakai-ppkr-source">
            <div class="sakai-ppkr-header sakai-ppkr-collection-header">
                <div class="section_header"><h:outputText value="#{msgs.rank_assign_title_site_members}"/></div>
            </div>
            <div class="sakai-ppkr-source-picker">
                <div class="sakai-ppkr-source-list-header">
                    <div class="sakai-ppkr-num-filtered">
                        <h:outputText value="#{msgs.rank_assign_showing}"/>
                        <span class="sakai-ppkr-source-counter">0</span>
                        <h:outputText value="#{msgs.rank_assign_of}"/>
                        <span class="sakai-ppkr-source-total">0</span>
                        <h:outputText value="#{msgs.rank_assign_site_members}"/>
                    </div>
                    <a class="sakai-ppkr-btn-add-all"><h:outputText value="#{msgs.rank_assign_add_all}"/></a>
                </div>
                <div id="source-scroller" class="flc-scroller scroller" tabindex="0">
                    <div class="sakai-ppkr-source-scroller-inner">
                        <!-- Individual List -->
                        <div class="sakai-ppkr-source-list">
                            <img src="images/checkbox-off.gif"
                                alt="This is here to force the loading of the image in the case were we restore the HTML from a buffer. If you see this, something has gone wrong." />
                            <img src="images/checkbox-on.gif"
                                alt="This is here to force the loading of the image in the case were we restore the HTML from a buffer. If you see this, something has gone wrong." />
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="sakai-ppkr-collection">
            <div class="sakai-ppkr-header sakai-ppkr-collection-header">
                <div class="section_header"><h:outputText value="#{msgs.rank_assign_individuals_to_assign}"/></div>
            </div>
            <div class="sakai-ppkr-collection-picker">
                <div class="sakai-ppkr-collection-list-header">
                    <a class="sakai-ppkr-btn-remove-all"><h:outputText value="#{msgs.rank_assign_remove_all}"/></a>
                    <div class="sakai-ppkr-num-filtered">
                        <span class="sakai-ppkr-collection-counter">0</span> <h:outputText value="#{msgs.rank_assign_recipients_selected}"/>
                    </div>
                </div>
                <div id="collection-scroller" class="flc-scroller scroller" tabindex="0">
                    <div id="selectedUsers" class="sakai-ppkr-collection-list"></div>
                </div>
            </div>
        </div>
        <div class="sakai-ppkr-submit">
            <h:commandButton type="button" value="#{msgs.rank_assign_button_cancel}" styleClass="sakai-ppkr-btn-cancel"/>
            <h:commandButton type="button" value="#{msgs.rank_assign_button_update}" styleClass="sakai-ppkr-btn-save"/>
        </div>

    </div>

    <div id="data" style="display: none">
        <h:outputText escape="false" value="#{ForumTool.totalAssignToListJSON}" />
    </div>
</f:verbatim>
