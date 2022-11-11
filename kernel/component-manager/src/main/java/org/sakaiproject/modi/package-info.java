/**
 * MOdern Dependency Injection for Sakai components.
 * <p>
 * This package completes the transition to Spring managing the dependency injection for Sakai. The SpringCompMgr was
 * most of the way there, but left some confusing lifecycle pieces and a strange relationship with the
 * ApplicationContext. With modi, there is a top-level context available globally and web applications can use it as a
 * parent by declaring the context-class.
 */
package org.sakaiproject.modi;
