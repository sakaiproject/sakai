package org.sakaiproject.elfinder.controller.executor;

import java.util.Map;

import cn.bluejoe.elfinder.controller.executor.CommandExecutor;
import cn.bluejoe.elfinder.controller.executor.CommandExecutorFactory;


/**
 * This used to chain 2 sets of executors together. The original elFinder ones and the Sakai ones.
 */
public class SakaiCommandExecutorFactory implements CommandExecutorFactory
{
	String _classNamePattern;
	String _classNamePattern2;

	private Map<String, CommandExecutor> _map;

	@Override
	public CommandExecutor get(String commandName)
	{
		if (_map.containsKey(commandName)) {
            return _map.get(commandName);
        }
        try {
            String className = String.format(_classNamePattern, commandName.substring(0, 1).toUpperCase() + commandName.substring(1));
            return (CommandExecutor)Class.forName(className).newInstance();
        }
        catch (ClassNotFoundException e) {
            try {
                String className2 = String.format(_classNamePattern2, commandName.substring(0, 1).toUpperCase() + commandName.substring(1));
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

	public String getClassNamePattern()
	{
		return _classNamePattern;
	}
	
	public String getClassNamePattern2()
	{
		return _classNamePattern2;
	}

	public Map<String, CommandExecutor> getMap()
	{
		return _map;
	}

	public void setClassNamePattern(String classNamePattern)
	{
		_classNamePattern = classNamePattern;
	}
	
	public void setClassNamePattern2(String classNamePattern2)
	{
		_classNamePattern2 = classNamePattern2;
	}

	public void setMap(Map<String, CommandExecutor> map)
	{
		_map = map;
	}
}
