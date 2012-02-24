package edu.indiana.lib.osid.base.repository.http;

/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
/**
 * @author Massachusetts Institute of Techbology, Sakai Software Development Team
 * @version
 */
public class Properties extends edu.indiana.lib.osid.base.repository.Properties
{
		private static org.apache.commons.logging.Log	_log = edu.indiana.lib.twinpeaks.util.LogUtils.getLog(Properties.class);
		
    public org.osid.shared.ObjectIterator getKeys()
    throws org.osid.shared.SharedException
    {
        return new ObjectIterator(new java.util.Vector());
    }

    public java.io.Serializable getProperty(java.io.Serializable key)
    throws org.osid.shared.SharedException
    {
        throw new org.osid.shared.SharedException(org.osid.shared.SharedException.UNKNOWN_KEY);
    }

    public org.osid.shared.Type getType()
    throws org.osid.shared.SharedException
    {
        return new Type("edu.mit","properties","general");
    }
}
