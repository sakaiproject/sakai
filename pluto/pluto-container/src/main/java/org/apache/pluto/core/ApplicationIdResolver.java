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
package org.apache.pluto.core;

import javax.servlet.ServletContext;

public interface ApplicationIdResolver {

    public static final int DECISIVE = 1;

    public static final int MANUAL = 2;

    public static final int DEFAULT = 3;

    /**
     * Resolve the applicationId for the given
     * context.
     *
     * @param context
     * @return
     */
    String resolveApplicationId(ServletContext context);

    /**
     * Retrive the degree of authority with which
     * the resolver speaks.
     * 
     * @return
     */
    int getAuthority();

}
