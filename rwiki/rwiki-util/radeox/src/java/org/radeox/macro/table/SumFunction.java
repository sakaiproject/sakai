/*
 * This file is part of "SnipSnap Radeox Rendering Engine".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://radeox.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --LICENSE NOTICE--
 */

package org.radeox.macro.table;

import lombok.extern.slf4j.Slf4j;

/**
 * A function that summerizes table cells
 * 
 * @author stephan
 * @version $Id$
 */
@Slf4j
public class SumFunction implements Function
{
	public String getName()
	{
		return "SUM";
	}

	public void execute(Table table, int posx, int posy, int startX,
			int startY, int endX, int endY)
	{
		float sum = 0;
		boolean floating = false;
		for (int x = startX; x <= endX; x++)
		{
			for (int y = startY; y <= endY; y++)
			{
				// Logger.debug("x="+x+" y="+y+" >"+getXY(x,y));
				try
				{
					sum += Integer.parseInt((String) table.getXY(x, y));
				}
				catch (Exception e)
				{
					try
					{
						sum += Float.parseFloat((String) table.getXY(x, y));
						floating = true;
					}
					catch (NumberFormatException e1)
					{
						log.debug("SumFunction: unable to parse "
								+ table.getXY(x, y));
					}
				}
			}
		}
		// Logger.debug("Sum="+sum);
		if (floating)
		{
			table.setXY(posx, posy, "" + sum);
		}
		else
		{
			table.setXY(posx, posy, "" + (int) sum);
		}
	}

}
