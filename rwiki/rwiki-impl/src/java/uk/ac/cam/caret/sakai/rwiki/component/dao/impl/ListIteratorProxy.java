/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
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
package uk.ac.cam.caret.sakai.rwiki.component.dao.impl;

import java.util.ListIterator;

import uk.ac.cam.caret.sakai.rwiki.service.api.dao.ObjectProxy;
/**
 * Provides a proxy implementation of a ListIterator, proxying objects with
 * a ObjectProxy
 * @author ieb
 *
 */
//FIXME: Component

public class ListIteratorProxy implements ListIterator
{
	private ListIterator li;
	private ObjectProxy lop;
	public ListIteratorProxy(ListIterator li, ObjectProxy lop) {
		this.li = li;
		this.lop = lop;
	}
	
	public boolean hasNext() {
		return li.hasNext();
	}
	public Object next() {
		return lop.proxyObject(li.next());
	}
	
	public boolean hasPrevious() {
		return li.hasPrevious();
	}
	
	public Object previous() {
		return lop.proxyObject(previous());
	}
	
	public int nextIndex() {
		return li.nextIndex();
	}
	
	public int previousIndex() {
		return li.previousIndex();
	}
	
	public void remove() {
		li.remove();
	}
	
	public void set(Object arg0) {
		li.set(lop.proxyObject(arg0));
	}
	
	public void add(Object arg0) {
		li.add(lop.proxyObject(arg0));
	}
	
}
