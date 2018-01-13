/**
 * 
 */
package org.radeox.filter.balance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import lombok.extern.slf4j.Slf4j;

/**
 * @author andrew
 */
@Slf4j
public class Balancer
{
	StringBuffer sb;

	TagStack tagStack;

	TagStack rememberedStack;

	Matcher m;

	int head;

	public String filter()
	{
		sb = new StringBuffer();
		tagStack = new TagStack();
		rememberedStack = new TagStack();
		m.reset();
		head = -1;

		while (m.find())
		{
			if (m.group(1) != null)
			{
				tagStack.push(new Tag(m.group(2), m.group(1)));
				// We have an open!!
				if (rememberedStack.size() > 0 && head < m.start())
				{
					/*
					 * We have text between the head and the open tag emit the
					 * remembered tags:
					 */
					emitOpenWithRemembered();
				}
				else
				{
					emitOpen();
				}

			}
			else
			{
				if (rememberedStack.size() > 0 && head < m.start())
				{
					emitCloseWithRemembered();
				}
				else
				{
					emitClose();
				}
			}
			head = m.end();
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private void emitClose()
	{
		doClosing(new StringBuffer());
	}

	private void emitCloseWithRemembered()
	{
		// OPEN the remembered tags
		for (Iterator it = rememberedStack.iterator(); it.hasNext();)
		{
			Tag tag = (Tag) it.next();
			sb.append(tag.open);
		}
		// Create replacement string
		StringBuffer replacementBuffer = new StringBuffer();
		for (Iterator it = rememberedStack.backIterator(); it.hasNext();)
		{
			Tag tag = (Tag) it.next();
			replacementBuffer.append("</").append(tag.name).append(">");
		}

		doClosing(replacementBuffer);
	}

	private void doClosing(StringBuffer replacementBuffer)
	{
		Tag currentTag = new Tag(m.group(4));
		

		// Now check what we're closing...
		if (tagStack.search(currentTag) > -1)
		{
			/*
			 * The tagStack contains this current tag we've got: 1. <A> </A> -->
			 * No work 2. <A> <B> </A> ... --> must remember <B>! 3. <A> <B> <C>
			 * </A> --> must remember <B> and <C>! 4. <A> <B attrs=""> </A> <A>
			 * <B other=""> <C> </B> ... --> remember <C> and keep remembering
			 * <B attrs=""> (Case not dealt by this check: <A> <B> </A> <A> </B>
			 * ... ) Action is: pop everything off the tagStack, and remember
			 * them till we hit the currentTag
			 */

			for (Tag tag = tagStack.pop(); (tagStack.size() > 0) && !currentTag.equals(tag); tag = tagStack.pop())
			{
				if ( tag != null ) {
					replacementBuffer.append("</").append(tag.name).append(">");
					rememberedStack.push(tag);
				} else {
					log.warn("Found Null tag in ballancer ");
				}
			}
			replacementBuffer.append("</").append(currentTag.name).append(">");
		}
		else
		{
			/*
			 * The tag stack doesn't contain the current tag: We are closing a
			 * remembered tag! (well we hope!) 1. <A> <B> </A> <A> </B> ... -->
			 * just forget the B 2. <A> <B> <C> </A> <A> </B> ... --> just
			 * forget the B 3. <A> <B attr=""> </A> <B other=""> <C> </A> <A>
			 * </B> ... --> forget the <B other=""> not the <B>
			 */

			TagStack tempStack = new TagStack();
			for (Tag tag = rememberedStack.pop(); (rememberedStack.size() > 0) && !currentTag.equals(tag); tag = rememberedStack
					.pop())
			{
				if (tag != null)
				{
					tempStack.push(tag);
				}
			}
			for (Tag tag = tempStack.pop(); tag != null; tag = tempStack.pop())
			{
				rememberedStack.push(tag);
			}

		}

		m.appendReplacement(sb, replacementBuffer.toString());
	}

	public void setMatcher(Matcher matcher)
	{
		this.m = matcher;
	}

	private void emitOpenWithRemembered()
	{
		// Append the opens to sb
		for (Iterator it = rememberedStack.iterator(); it.hasNext();)
		{
			Tag tag = (Tag) it.next();
			sb.append(tag.open);
		}
		// Create replacement string
		StringBuffer buffer = new StringBuffer();
		for (Iterator it = rememberedStack.backIterator(); it.hasNext();)
		{
			Tag tag = (Tag) it.next();
			buffer.append("</").append(tag.name).append(">");
		}
		buffer.append(m.group(1).replaceAll("\\\\", "\\\\\\\\").replaceAll("\\$",
				"\\\\\\$"));
		m.appendReplacement(sb, buffer.toString());
	}

	private void emitOpen()
	{
		m.appendReplacement(sb, "$1");
	}

	class Tag
	{
		public Tag(String name, String open)
		{
			this.name = name;
			this.open = open;
		}

		public Tag(String name)
		{
			this.name = name;
			this.open = null;
		}

		public String name;

		public String open;

		public boolean equals(Object o)
		{
			if (o == null) return false;
			if (o instanceof Tag)
			{
				Tag that = (Tag) o;
				if (that.name == null && this.name == null) return true;
				if (this.name == null) return false;
				return (this.name.equals(that.name));
			}
			return false;
		}
	}

	class TagStack
	{
		List internalList;

		int size = 0;

		public TagStack()
		{
			internalList = new ArrayList();
		}

		public Tag push(Tag toPush)
		{
			internalList.add(size++, toPush);
			if (size > 1)
			{
				return (Tag) internalList.get(size - 2);
			}
			else
			{
				return null;
			}
		}

		public Tag peek()
		{
			if (size > 0)
			{
				return (Tag) internalList.get(size - 1);
			}
			else
			{
				return null;
			}
		}

		public Tag pop()
		{
			if (size > 0)
			{
				return (Tag) internalList.get(--size);
			}
			else
			{
				return null;
			}
		}

		public int size()
		{
			return size;
		}

		public boolean empty()
		{
			return (size == 0);
		}

		public int search(Tag o)
		{
			if (o == null)
			{
				return -1;
			}
			for (int i = size; i > 0; i--)
			{
				if (o.equals(internalList.get(i - 1)))
				{
					return (size - i + 1);
				}
			}
			return -1;
		}

		public Tag get(int i)
		{
			if (i < size)
			{
				return (Tag) internalList.get(i);
			}
			else
			{
				throw new ArrayIndexOutOfBoundsException(i);
			}
		}

		public Iterator iterator()
		{
			return new Iterator()
			{

				int ourHead = size;

				public boolean hasNext()
				{
					return ourHead > 0;
				}

				public Object next()
				{
					return internalList.get(--ourHead);
				}

				public void remove()
				{
					throw new UnsupportedOperationException(
							"remove is not implemented for this iterator");
				}

			};
		}

		public Iterator backIterator()
		{
			return new Iterator()
			{
				int ourHead = 0;

				public boolean hasNext()
				{
					return (ourHead < size);
				}

				public Object next()
				{
					return internalList.get(ourHead++);
				}

				public void remove()
				{
					throw new UnsupportedOperationException(
							"remove is not implemented for this iterator");
				}

			};
		}

		public String toString()
		{
			StringBuffer sb = new StringBuffer();
						for (Iterator it = iterator(); it.hasNext();)
			{
				Tag t = (Tag) it.next();
				sb.append("Tag: " + t.open + "\n");
			}
			return sb.toString();
		}
	}

}
