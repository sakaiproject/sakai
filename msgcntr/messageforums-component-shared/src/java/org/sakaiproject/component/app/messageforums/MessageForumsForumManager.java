/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.messageforums;

import java.util.List;

import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.OpenTopic;

public interface MessageForumsForumManager {

    /**
     * Retrieve the current user's discussion forums
     */
    public List getDiscussionForums();

    /**
     * Retrieve a given discussion forum for the current user
     */
    public DiscussionForum getDiscussionForumById(final String forumId);

    /**
     * Retrieve topics the current user's open forums
     */
    public List getOpenForums();

    /**
     * Create and save an empty discussion forum
     */
    public DiscussionForum createDiscussionForum();

    /**
     * Save a discussion forum
     */
    public void saveDiscussionForum(DiscussionForum forum);

    /**
     * Create and save an empty discussion forum topic
     */
    public DiscussionTopic createDiscussionForumTopic();

    /**
     * Save a discussion forum topic
     */
    public void saveDiscussionForumTopic(DiscussionTopic topic);

    /**
     * Create and save an empty open discussion forum topic
     */
    public OpenTopic createOpenForumTopic();

    /**
     * Save an open forum topic
     */
    public void saveOpenForumTopic(OpenTopic topic);

    /**
     * Delete a discussion forum and all topics/messages
     */
    public void deleteDiscussionForum(DiscussionForum forum);

    /**
     * Delete a discussion forum topic
     */
    public void deleteDiscussionForumTopic(DiscussionTopic topic);

    /**
     * Delete an open forum topic
     */
    public void deleteOpenForumTopic(OpenTopic topic);

    /**
     * Returns a given number of messages if available in the time 
     * provided
     * @param numberMessages the number of messages to retrieve
     * @param numberDaysInPast the number days to look back
     */
    public List getRecentPrivateMessages(int numberMessages, int numberDaysInPast);

    /**
     * Returns a given number of discussion forum messages if available in 
     * the time provided
     * @param numberMessages the number of forum messages to retrieve
     * @param numberDaysInPast the number days to look back
     */
    public List getRecentDiscussionForumMessages(int numberMessages, int numberDaysInPast);

    /**
     * Returns a given number of open forum messages if available in 
     * the time provided
     * @param numberMessages the number of forum messages to retrieve
     * @param numberDaysInPast the number days to look back
     */
    public List getRecentOpenForumMessages(int numberMessages, int numberDaysInPast);

}