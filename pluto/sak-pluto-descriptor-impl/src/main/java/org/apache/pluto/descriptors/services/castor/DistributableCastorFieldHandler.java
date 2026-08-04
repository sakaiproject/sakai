/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.descriptors.services.castor;

import org.apache.pluto.descriptors.servlet.DistributableDD;
import org.exolab.castor.mapping.AbstractFieldHandler;

public class DistributableCastorFieldHandler extends AbstractFieldHandler
{

    public Object getValue(Object arg0) throws IllegalStateException
    {
        // we always return null, because the 
        // distributable element has body.
        return null;
    }

    public Object newInstance(Object arg0) throws IllegalStateException
    {
        // Do nothing.
        return null;
    }

    public Object newInstance(Object arg0, Object[] arg1)
            throws IllegalStateException
    {
        // Do nothing.
        return null;
    }

    public void resetValue(Object arg0) throws IllegalStateException,
            IllegalArgumentException
    {
        // Do nothing.
    }

    public void setValue(Object distributableDD, Object value)
            throws IllegalStateException, IllegalArgumentException
    {
        if (! (distributableDD instanceof DistributableDD) )
        {
            throw new ClassCastException("Error: was expecting " + DistributableDD.class.getName() +
                        " but got a " + distributableDD.getClass().getName() );
        }
        
        if (! (value instanceof Boolean) )
        {
            throw new ClassCastException("Error: was expecting " + Boolean.class.getName() + 
                        " but got a " + value.getClass().getName() );
        }
        
        ((DistributableDD)distributableDD).setDistributable(((Boolean)value).booleanValue());
    }

}
