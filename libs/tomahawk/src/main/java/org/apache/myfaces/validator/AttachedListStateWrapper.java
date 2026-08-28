/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.validator;

import java.io.Serializable;
import java.util.List;

/**
 * @author Manfred Geiler (latest modification by $Author: bdudney $)
 * @version $Revision: 225333 $ $Date: 2005-07-26 17:49:19 +0200 (Di, 26 Jul 2005) $
 */
class AttachedListStateWrapper
        implements Serializable
{
    private static final long serialVersionUID = -3958718149793179776L;
    private List _wrappedStateList;

    public AttachedListStateWrapper(List wrappedStateList)
    {
        _wrappedStateList = wrappedStateList;
    }

    public List getWrappedStateList()
    {
        return _wrappedStateList;
    }
}
