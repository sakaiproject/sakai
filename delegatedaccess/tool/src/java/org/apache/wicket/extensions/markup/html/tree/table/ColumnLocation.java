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
package org.apache.wicket.extensions.markup.html.tree.table;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.lang.EnumeratedType;

/**
 * This class represents location of a column in tree table.
 * <p>
 * First attribute of location is <b>alignment</b>. Alignment specifies, whether the column is
 * located on the left side of the table, on the right side, or in the middle. Columns in the middle
 * of the table take all space between columns on the left and columns on the right.
 * <p>
 * Next two attributes are <b>size</b> and <b>unit</b>:
 * <ul>
 * <li>For columns aligned to the left and to the right, the <b>size</b> represents the actual width
 * of the column, according to chosen unit. Possible units for left and right aligned columns are
 * <em>PX</em>, <em>EM</em> and <em>PERCENT</em>.</li>
 * <li>For columns in the middle, the only valid unit is <em>PROPORTIONAL</em>. These columns take
 * all available space between columns on the left and columns on the right. How this space is
 * divided between middle columns is determined by the <b>size</b>. In this case the size can be
 * understand as weight. Columns with bigger size take more space than columns with smaller size.
 * For example, if there are three columns and their sizes are 2, 1, 1, the first column takes 50%
 * of the space and the second two columns take 25% each.</li>
 * </ul>
 * 
 * @author Matej Knopp
 */
public class ColumnLocation implements IClusterable
{
	/**
	 * Alignment of the column.
	 */
	public static final class Alignment extends EnumeratedType
	{
		/** Align left. */
		public static final Alignment LEFT = new Alignment("LEFT");

		/** Align middle. */
		public static final Alignment MIDDLE = new Alignment("MIDDLE");

		/** Align right. */
		public static final Alignment RIGHT = new Alignment("RIGHT");

		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param name
		 */
		public Alignment(final String name)
		{
			super(name);
		}
	}

	/**
	 * Units.
	 */
	public static final class Unit extends EnumeratedType
	{

		/** Size of letter M in the current font. */
		public static Unit EM = new Unit("EM");

		/** Percentage. */
		public static Unit PERCENT = new Unit("PERCENT");

		/** Proportional. */
		public static Unit PROPORTIONAL = new Unit("PROPORTIONAL");

		/** Pixels. */
		public static Unit PX = new Unit("PX");

		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param name
		 */
		public Unit(final String name)
		{
			super(name);
		}
	}

	private static final long serialVersionUID = 1L;

	private final Alignment alignment;
	private final int size;
	private final Unit unit;

	/**
	 * Constructs the ColumnLocation object.
	 * 
	 * @param alignment
	 *            The column alignment
	 * @param size
	 *            The column size in expressed in the provided unit
	 * @param unit
	 *            The unit that the size argument is expressed in
	 * @throws IllegalArgumentException
	 *             if the unit does not matche the alignment
	 */
	public ColumnLocation(final Alignment alignment, final int size, final Unit unit)
	{
		this.alignment = alignment;
		this.size = size;
		this.unit = unit;

		if ((alignment == Alignment.MIDDLE) && (unit != Unit.PROPORTIONAL))
		{
			throw new IllegalArgumentException(
				"For alignment MIDDLE the specified unit must be PROPORTIONAL.");
		}
		else if ((alignment != Alignment.MIDDLE) && (unit == Unit.PROPORTIONAL))
		{
			throw new IllegalArgumentException(
				"Unit PROPORTIONAL can be specified only for columns with alignment MIDDLE.");
		}
	}

	/**
	 * Returns the alignment of this column.
	 * 
	 * @return The alignment of this column
	 */
	public Alignment getAlignment()
	{
		return alignment;
	}

	/**
	 * Returns the size of this column.
	 * 
	 * @return The size of this column
	 */
	public int getSize()
	{
		return size;
	}

	/**
	 * Returns the unit of a column.
	 * 
	 * @return The unit of this column
	 */
	public Unit getUnit()
	{
		return unit;
	}
}
