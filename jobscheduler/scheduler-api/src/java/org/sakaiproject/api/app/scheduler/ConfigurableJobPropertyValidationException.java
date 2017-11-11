/**
 * Copyright (c) 2003-2010 The Apereo Foundation
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
package org.sakaiproject.api.app.scheduler;

/**
 * Created by IntelliJ IDEA.
 * User: duffy
 * Date: Aug 3, 2010
 * Time: 10:01:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurableJobPropertyValidationException extends Exception
{
    public ConfigurableJobPropertyValidationException()
    {
        super();
    }

    public ConfigurableJobPropertyValidationException(Throwable t)
    {
        super(t);
    }

    public ConfigurableJobPropertyValidationException(String message)
    {
        super(message);
    }

    public ConfigurableJobPropertyValidationException(String message, Throwable t)
    {
        super(message, t);
    }
}
