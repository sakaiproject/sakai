/**
 * Copyright (c) 2007-2009 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * MyEntity.java - created by aaronz on Jul 25, 2007
 */

package org.sakaiproject.entitybroker.mocks.data;


/**
 * This is a sample entity object for testing, it is a bean with no default values and comparison
 * overrides
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class MyEntity {

    private String id;
    private String stuff;
    private int number;
    public String extra;

    /**
     * Basic empty constructor
     */
    public MyEntity() {
    }

    public MyEntity(String id, String stuff) {
        this.id = id;
        this.stuff = stuff;
    }

    public MyEntity(String stuff, int number) {
        this.stuff = stuff;
        this.number = number;
    }

    public MyEntity(String id, String stuff, int number) {
        this.id = id;
        this.stuff = stuff;
        this.number = number;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (!(obj instanceof MyEntity)) {
            return false;
        } else {
            MyEntity castObj = (MyEntity) obj;
            if (null == this.id || null == castObj.id) {
                return false;
            } else {
                return (this.id.equals(castObj.id));
            }
        }
    }

    @Override
    public int hashCode() {
        if (null == this.id) {
            return super.hashCode();
        }
        String hashStr = this.getClass().getName() + ":" + this.id.hashCode();
        return hashStr.hashCode();
    }

    @Override
    public String toString() {
        return "id:" + this.id + ", stuff:" + this.stuff + ", number:" + number;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStuff() {
        return stuff;
    }

    public void setStuff(String stuff) {
        this.stuff = stuff;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * @return a copy of this object
     */
    public MyEntity copy() {
        return copy(this);
    }

    /**
     * @return a copy of the supplied object
     */
    public static MyEntity copy(MyEntity me) {
        if (me == null) {
            throw new IllegalArgumentException("entity to copy must not be null");
        }
        MyEntity togo = new MyEntity(me.id, me.stuff, me.number);
        togo.extra = me.extra;
        return togo;
    }

}
