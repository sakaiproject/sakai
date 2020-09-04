/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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
package org.sakaiproject.elfinder.controller.executor;

import java.util.Map;

import cn.bluejoe.elfinder.controller.executor.CommandExecutor;
import cn.bluejoe.elfinder.controller.executor.CommandExecutorFactory;
import lombok.Getter;
import lombok.Setter;


/**
 * This used to chain 2 sets of executors together. The original elFinder ones and the Sakai ones.
 */
public class SakaiCommandExecutorFactory implements CommandExecutorFactory
{
	@Getter @Setter private String classNamePattern;
	@Getter @Setter private String classNamePattern2;
	@Getter @Setter private Map<String, CommandExecutor> commands;

	@Override
	public CommandExecutor get(String commandName)
	{
		if (commands.containsKey(commandName)) {
            return commands.get(commandName);
        }
        try {
            String className = String.format(classNamePattern, commandName.substring(0, 1).toUpperCase() + commandName.substring(1));
            return (CommandExecutor)Class.forName(className).newInstance();
        }
        catch (ClassNotFoundException e) {
            try {
                String className2 = String.format(classNamePattern2, commandName.substring(0, 1).toUpperCase() + commandName.substring(1));
                return (CommandExecutor)Class.forName(className2).newInstance();
            }
            catch (Exception ex) {
                return null;
            }
        }
        catch (Exception e2) {
            return null;
        }
	}
}
