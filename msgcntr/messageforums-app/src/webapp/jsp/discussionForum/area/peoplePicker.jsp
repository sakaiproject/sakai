<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<f:verbatim>
    <div class="sakai-ppkr" id="sakai-ppkr-messages" title="Assign Rank to Individuals">
        <div class="sakai-ppkr-filter">
            <div class="sakai-ppkr-header sakai-ppkr-filter-header">
                <div class="section_header">Filter Site Members...</div>
            </div>
            <div class="sakai-ppkr-filter-fields">
                <label>By Name:</label> <input class="sakai-ppkr-search-field" type="text" value="" id="searchIndividual" /><br /> <label>in
                    Group/Section: </label> <select name="sectionDropdown" class="sectionDropdown">
                    <option value="all-sections" id="group-option-all">All Sections/Groups</option>
                </select>&nbsp; <label>of role: </label> <select name="roleDropdown" class="roleDropdown">
                    <option value="all-roles" id="role-option-all">All Roles</option>
                </select>
            </div>
        </div>
        <div class="sakai-ppkr-source">
            <div class="sakai-ppkr-header sakai-ppkr-collection-header">
                <div class="section_header">Site Members</div>
            </div>
            <div class="sakai-ppkr-source-picker">
                <div class="sakai-ppkr-source-list-header">
                    <div class="sakai-ppkr-num-filtered">
                        showing <span class="sakai-ppkr-source-counter">0</span> of <span class="sakai-ppkr-source-total">0</span> site members
                    </div>
                    <a class="sakai-ppkr-btn-add-all">Add all</a>
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
                <div class="section_header">Individual(s) to be Assigned Rank</div>
            </div>
            <div class="sakai-ppkr-collection-picker">
                <div class="sakai-ppkr-collection-list-header">
                    <a class="sakai-ppkr-btn-remove-all">Remove all</a>
                    <div class="sakai-ppkr-num-filtered">
                        <span class="sakai-ppkr-collection-counter">0</span> recipients selected
                    </div>
                </div>
                <div id="collection-scroller" class="flc-scroller scroller" tabindex="0">
                    <div id="selectedUsers" class="sakai-ppkr-collection-list"></div>
                </div>
            </div>
        </div>
        <div class="sakai-ppkr-submit">
            <input class="sakai-ppkr-btn-cancel" value="Cancel" tabindex="0" type="button" /><input class="sakai-ppkr-btn-save"
                value="Update Individuals" tabindex="0" type="button" />
        </div>

    </div>

    <div id="data" style="display: none">
        <h:outputText escape="false" value="#{ForumTool.totalAssignToListJSON}" />
    </div>
</f:verbatim>
