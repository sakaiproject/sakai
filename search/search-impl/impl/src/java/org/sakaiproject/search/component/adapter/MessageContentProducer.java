/*******************************************************************************
 * $Header$
 * **********************************************************************************
 * Copyright (c) 2006 University of Cambridge Licensed under the Educational
 * Community License Version 1.0 (the "License"); By obtaining, using and/or
 * copying this Original Work, you agree that you have read, understand, and
 * will comply with the terms and conditions of the Educational Community
 * License. You may obtain a copy of the License at:
 * http://cvs.sakaiproject.org/licenses/license_1_0.html THE SOFTWARE IS
 * PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
// TODO: Eamble message service
/*
 * package org.sakaiproject.search.api.component.adapter; import java.io.Reader;
 * import java.io.StringReader; import java.util.ArrayList; import
 * java.util.Iterator; import java.util.List; import
 * org.apache.commons.logging.Log; import org.apache.commons.logging.LogFactory;
 * import org.sakaiproject.component.cover.ServerConfigurationService; import
 * org.sakaiproject.entity.api.Entity; import
 * org.sakaiproject.entity.api.EntityProducer; import
 * org.sakaiproject.entity.api.Reference; import
 * org.sakaiproject.entity.api.EntityManager; import
 * org.sakaiproject.event.api.Event; import
 * org.sakaiproject.exception.IdUnusedException; import
 * org.sakaiproject.exception.PermissionException; import
 * org.sakaiproject.search.api.EntityContentProducer; import
 * org.sakaiproject.search.api.SearchIndexBuilder; import
 * org.sakaiproject.search.api.SearchService; import
 * org.sakaiproject.search.api.model.SearchBuilderItem; import
 * sun.net.www.MessageHeader; import
 * com.sun.corba.se.internal.iiop.messages.Message; /** @author ieb / public
 * class MessageContentProducer implements EntityContentProducer { /** debug
 * logger / private static Log dlog =
 * LogFactory.getLog(MessageContentProducer.class); // dependency private String
 * toolName = null; // dependency private List addEvents = null; // dependency
 * private List removeEvents = null; // dependency private MessageService
 * messageService = null; // dependency private SearchService searchService =
 * null; // dependency private SearchIndexBuilder searchIndexBuilder = null;
 * public void init() { if ("true".equals(ServerConfigurationService
 * .getString("wiki.experimental"))) { for (Iterator i = addEvents.iterator();
 * i.hasNext();) { searchService.registerFunction((String) i.next()); } for
 * (Iterator i = removeEvents.iterator(); i.hasNext();) {
 * searchService.registerFunction((String) i.next()); }
 * searchIndexBuilder.registerEntityContentProducer(this); } } /** {@inheritDoc} /
 * public boolean isContentFromReader(Entity cr) { return false; } /**
 * {@inheritDoc} / public Reader getContentReader(Entity cr) { return new
 * StringReader(getContent(cr)); } /** {@inheritDoc} / public String
 * getContent(Entity cr) { dlog.info(" Getting content " + cr); Reference ref =
 * EntityManager.newReference(cr.getReference()); dlog.info(" Got reference " +
 * ref); EntityProducer ep = ref.getEntityProducer(); dlog.info(" Got ep " +
 * ep); if (ep instanceof MessageService) { try { MessageService ms =
 * (MessageService) ep; Message m = ms.getMessage(ref); MessageHeader mh =
 * m.getHeader(); StringBuffer sb = new StringBuffer(); sb.append("Message
 * Headers\n"); sb.append("From ").append(mh.getFrom().getDisplayName())
 * .append("\n"); sb.append("Message Body\n");
 * sb.append(m.getBody()).append("\n"); dlog.info("Index Content is " +
 * sb.toString()); return sb.toString(); } catch (IdUnusedException e) { throw
 * new RuntimeException(" Failed to get message content ", e); } catch
 * (PermissionException e) { throw new RuntimeException(" Failed to get message
 * content ", e); } } throw new RuntimeException(" Not a Message Entity " + cr); }
 * /** @{inheritDoc} / public String getTitle(Entity cr) { Reference ref =
 * EntityManager.newReference(cr.getReference()); EntityProducer ep =
 * ref.getEntityProducer(); if (ep instanceof MessageService) { try {
 * MessageService ms = (MessageService) ep; Message m = ms.getMessage(ref);
 * MessageHeader mh = m.getHeader(); return "Message From " +
 * mh.getFrom().getDisplayName(); } catch (IdUnusedException e) { throw new
 * RuntimeException(" Failed to get message content ", e); } catch
 * (PermissionException e) { throw new RuntimeException(" Failed to get message
 * content ", e); } } throw new RuntimeException(" Not a Message Entity " + cr); }
 * /** @{inheritDoc} / public String getUrl(Entity entity) { return
 * entity.getUrl(); } /** @{inheritDoc} / public boolean matches(Reference ref) {
 * EntityProducer ep = ref.getEntityProducer(); if
 * (ep.getClass().equals(messageService.getClass())) { return true; } return
 * false; } /** @{inheritDoc} / public List getAllContent() { List all = new
 * ArrayList(); List l = messageService.getChannels(); for (Iterator i =
 * l.iterator(); i.hasNext();) { try { MessageChannel c = (MessageChannel)
 * i.next(); List messages = c.getMessages(null, true); // WARNING: I think the
 * implementation caches on thread, if this // is // a builder // thread this
 * may not work for (Iterator mi = messages.iterator(); mi.hasNext();) { Message
 * m = (Message) i.next(); all.add(m.getReference()); } } catch (Exception ex) { } }
 * return all; } /** @{inheritDoc} / public Integer getAction(Event event) {
 * String evt = event.getEvent(); if (evt == null) return
 * SearchBuilderItem.ACTION_UNKNOWN; for (Iterator i = addEvents.iterator();
 * i.hasNext();) { String match = (String) i.next(); if (evt.equals(match)) {
 * dlog.info(" event is add " + evt); return SearchBuilderItem.ACTION_ADD; }
 * else { dlog.info(" no match " + evt + ":" + match); } } for (Iterator i =
 * removeEvents.iterator(); i.hasNext();) { String match = (String) i.next(); if
 * (evt.equals(match)) { dlog.info(" event is delete " + evt); return
 * SearchBuilderItem.ACTION_DELETE; } else { dlog.info(" no match " + evt + ":" +
 * match); } } dlog.info(" event is unknown "); return
 * SearchBuilderItem.ACTION_UNKNOWN; } /** @{inheritDoc} / public boolean
 * matches(Event event) { Reference ref =
 * EntityManager.newReference(event.getResource()); return matches(ref); } /**
 * @{inheritDoc} / public String getTool() { return toolName; } /** @return
 * Returns the addEvents. / public List getAddEvents() { return addEvents; } /**
 * @param addEvents The addEvents to set. / public void setAddEvents(List
 * addEvents) { this.addEvents = addEvents; } /** @return Returns the
 * messageService. / public MessageService getMessageService() { return
 * messageService; } /** @param messageService The messageService to set. /
 * public void setMessageService(MessageService messageService) {
 * this.messageService = messageService; } /** @return Returns the toolName. /
 * public String getToolName() { return toolName; } /** @param toolName The
 * toolName to set. / public void setToolName(String toolName) { this.toolName =
 * toolName; } /** @return Returns the removeEvents. / public List
 * getRemoveEvents() { return removeEvents; } /** @param removeEvents The
 * removeEvents to set. / public void setRemoveEvents(List removeEvents) {
 * this.removeEvents = removeEvents; } /** @return Returns the
 * searchIndexBuilder. / public SearchIndexBuilder getSearchIndexBuilder() {
 * return searchIndexBuilder; } /** @param searchIndexBuilder The
 * searchIndexBuilder to set. / public void
 * setSearchIndexBuilder(SearchIndexBuilder searchIndexBuilder) {
 * this.searchIndexBuilder = searchIndexBuilder; } /** @return Returns the
 * searchService. / public SearchService getSearchService() { return
 * searchService; } /** @param searchService The searchService to set. / public
 * void setSearchService(SearchService searchService) { this.searchService =
 * searchService; } public String getSiteId(Reference ref) { return
 * ref.getContext(); } }
 */
