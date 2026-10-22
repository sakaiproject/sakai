/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.tool.dataproviders;

import org.apache.wicket.injection.Injector;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.attendance.logic.AttendanceLogic;
import org.sakaiproject.attendance.logic.SakaiProxy;

import java.util.Iterator;
import java.util.List;

/**
 * A BaseProvider to consolidate common features
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
public abstract class BaseProvider<T> implements IDataProvider<T> {
    private static final long serialVersionUID = 1L;

    protected List<T> list;

    @SpringBean(name="org.sakaiproject.attendance.logic.AttendanceLogic")
    protected AttendanceLogic attendanceLogic;

    @SpringBean(name="org.sakaiproject.attendance.logic.SakaiProxy")
    protected SakaiProxy sakaiProxy;

    public BaseProvider() {
        Injector.get().inject(this);
    }

    abstract List<T> getData();

    @Override
    public long size(){
        return getData().size();
    }

    @Override
    public Iterator<T> iterator(long first, long count){
        int f = (int) first; //not ideal but ok for demo
        int c = (int) count; //not ideal but ok for demo
        return getData().subList(f, f + c).iterator();
    }

    @Override
    public void detach(){
        list = null;
    }
}
