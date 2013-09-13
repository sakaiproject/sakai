<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
    
<f:verbatim>
    <div class="topic-picker" id="topic-picker" title="Move Thread(s)">
        <div class="selected-threads-to-move">
            <div style="padding-left: .3em; margin-top: .5em">
                <h:outputText value="#{msgs.move_thread_info1}"  />
                <h:outputText styleClass="sourcetitle" escape="true" value="#{ForumTool.selectedTopic.topic.title}"  /> 
                 <h:outputText value="#{msgs.move_thread_info2}"  />
            </div>
            <div class="threads-to-move" escape="true"></div>
            <input class="checkbox-reminder" id="checkbox-reminder" type="checkbox" name="checkbox-reminder"  /><h:outputText value="#{msgs.leave_reminder}"  />
        </div>
                       
        <div class="topic-filter">
            <div class="topic-filter-header">
                <div class="topic-header-h3"><h:outputText value="#{msgs.filter_topics}"  /></div>
            </div>
            <div class="topic-filter-fields">
                <label><h:outputText value="#{msgs.by_name}"  /></label>
                <input class="topic-search-field" type="text" value="" id="searchTopic" /><br />
                <label><h:outputText value="#{msgs.in_forum}"  /></label>
                <select style="margin-left: 0.5em;" name="forumDropdown" class="forumDropdown">
                    <option value="select-forum" id="select-forum">- Select Forum -</option>
                </select>
            </div>
            <div style="padding-left: .3em; margin-top: 1em">
                <img src="../../images/exclamation.gif" alt="warning"/>
                <h:outputText value="#{msgs.locked_topics_hidden}"  />
            </div>
        </div>
          
        <div class="topic-source">
            <div class="topic-filter-header">
                <span class="topic-header-h3"><h:outputText value="#{msgs.topics}"  /></span>
                (showing <span class="topic-source-counter">0</span> of <span class="topic-source-total">0</span> topics)
            </div>
            <div class="topic-source-picker">
                <div id="source-scroller" class="flc-scroller scroller"  style="max-height: 270px;" tabindex="0">
                    <div class="topic-source-scroller-inner">
                        <!-- Individual List -->
                        <div class="topic-source-list"> </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="topic-submit">
            <input class="topic-btn-cancel" value="Cancel" tabindex="0" type="button" />
            <input class="topic-btn-save" disabled value="Move Thread(s) to Selected Topic" tabindex="0" type="button" />
        </div>
    </div>
    <div id="data" style="display:none">
        <h:outputText escape="true" value="#{ForumTool.moveThreadJSON}" />
    </div>
</f:verbatim>
