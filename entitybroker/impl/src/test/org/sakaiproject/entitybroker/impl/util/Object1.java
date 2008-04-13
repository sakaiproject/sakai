/*
 *  Copyright 2007 CodersLog.com 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *   
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *   
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */

package org.sakaiproject.entitybroker.impl.util;

public class Object1 {
	private Integer i;
	private Long l;
	private String s;

	public Integer getI() {
		return i;
	}
	public void setI(Integer i) {
		this.i = i;
	}
	public Long getL() {
		return l;
	}
	public void setL(Long l) {
		this.l = l;
	}
	public String getS() {
		return s;
	}
	public void setS(String s) {
		this.s = s;
	}
	public Object1() {
	}
	public Object1(Integer i, Long l, String s) {
		this.i = i;
		this.l = l;
		this.s = s;
	}
}
