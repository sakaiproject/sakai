/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/Blob.java $
 * $Id: Blob.java 63297 2009-06-04 12:44:44Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import lombok.extern.slf4j.Slf4j;

/**
* A blob is a Binary Large OBject.
* The Blob uses a linked list structure internally so it is
* not susceptible to running out of memory when growing.  This can
* be a problem with very large Vectors -- when they grow, a new, larger
* internal array is created and elements copied over.  Thus, for that
* short amount of time, there are two copies of the array.  That, plus
* the added overhead of storing bytes in object form, make the Blob the
* preferrable method for storing raw data.
* <P>Since it uses ints as indicies, a single Blob
* can hold up to Integer.MAX_VALUE (2,147,483,647) bytes.
* Could be modified to use longs and then could hold up to
* Long.MAX_VALUE (9.2233e18) bytes if your computer had that much storage.  :^)
* <P>This is my 'pet' class.
* @author T. Gee
* @deprecated (KNL-898)
*
*/
@Slf4j
public class Blob implements Cloneable, Serializable {

	/**
   * 
   */
  private static final long serialVersionUID = 3832623997476484914L;

  /**
	* The default internal data storage node size.
	*/
	public static final int NODE_SIZE = 512;

	// THE FOLLOWING THREE FIELDS ARE TRANSIENT BECAUSE WE DO THE
	// SERIALIZATION OURSELVES.  WE DO THIS BECAUSE OF JAVA'S
	// LIMITED STACK SIZE FOR SERIALIZATION.

	// The head of the linked list of data nodes.
	protected transient BlobNode head;

	// The current tail of the linked list of data nodes.
	protected transient BlobNode tail;

	// The current size of the Blob.
	// It is possible to compute this, but it is stored
	// for convience.
	protected transient int size;


	// The actual internal data storage node size.
	protected int nodeSize;

	// Current Node -- set when seek() is called
	protected transient BlobNode curr;

	// For the internal enumeration methods
	protected transient BlobNode enumerationNode = null;
	protected transient int enumerationPos = 0;

	/**
	* An inclusive between function (for chars).
	* @param test The char to test.
	* @param low The lower bound.
	* @param high The upper bound.
	* @return true if test is between low and high (inclusive),
	* false otherwise.
	*
	*/
	protected static final boolean between(char test, char low, char high) {
		return ((test >= low) && (test <= high));
	}

	/**
	* An inclusive between function (for ints).
	* @param test The number to test.
	* @param low The lower bound.
	* @param high The upper bound.
	* @return true if test is between low and high (inclusive),
	* false otherwise.
	*
	*/
	public static final boolean between(int test, int low, int high) {
		return ((test >= low) && (test <= high));
	}

	/**
	* Returns a hex representation of a byte.
	* @param b The byte to convert to hex.
	* @return The 2-digit hex value of the supplied byte.
	*
	*/
	public static final String toHex(byte b) {

		char ret[] = new char[2];

		ret[0] = hexDigit((b >>> 4) & (byte)0x0F);
		ret[1] = hexDigit((b >>> 0) & (byte)0x0F);

		return new String(ret);
	}


	/**
	* Returns the hex representation of a short.
	* @param s The short to convert to hex.
	* @return The 4-digit hex value of the supplied short.
	*
	*/
	public static final String toHex(short s) {

		StringBuilder sb = new StringBuilder(5);

		sb.append(toHex((byte)(s >>> 8)));
		sb.append(' ');
		sb.append(toHex((byte)(s >>> 0)));

		return sb.toString();
	}


	/**
	* Returns the hex representation of an int.
	* @param i The int to convert to hex.
	* @return The 8-digit hex value of the supplied int.
	*
	*/
	public static final String toHex(int i) {

		StringBuilder sb = new StringBuilder(11);

		sb.append(toHex((byte)(i >>> 24)));
		sb.append(' ');
		sb.append(toHex((byte)(i >>> 16)));
		sb.append(' ');
		sb.append(toHex((byte)(i >>> 8)));
		sb.append(' ');
		sb.append(toHex((byte)(i >>> 0)));

		return sb.toString();
	}

	/**
	* Returns the hex representation of an long.
	* @param l The long to convert to hex.
	* @return The 16-digit hex value of the supplied long.
	*
	*/
	public static final String toHex(long l) {

		StringBuilder sb = new StringBuilder(11);

		sb.append(toHex((byte)(l >>> 56)));
		sb.append(' ');
		sb.append(toHex((byte)(l >>> 48)));
		sb.append(' ');
		sb.append(toHex((byte)(l >>> 40)));
		sb.append(' ');
		sb.append(toHex((byte)(l >>> 32)));
		sb.append(' ');
		sb.append(toHex((byte)(l >>> 24)));
		sb.append(' ');
		sb.append(toHex((byte)(l >>> 16)));
		sb.append(' ');
		sb.append(toHex((byte)(l >>> 8)));
		sb.append(' ');
		sb.append(toHex((byte)(l >>> 0)));

		return sb.toString();
	}


	/**
	* Returns the hex representation of the characters of a String.
	* @param s The String to convert to hex.
	* @return A String where each character of the original string
	* is given as a space seperated sequence of hex values.
	*
	*/
	public static final String toHex(String s) {

		StringBuilder sb = new StringBuilder();

		char chars[] = s.toCharArray();
		for (int x = 0 ; x < chars.length ; x++) {
			sb.append(toHex((byte)chars[x]));
			if (x != (chars.length - 1)) {
				sb.append(' ');
			}
		}

		return sb.toString();

	}


	/**
	* Returns the hex digit cooresponding to a number between 0 and 15.
	* @param i The number to get the hex digit for.
	* @return The hex digit cooresponding to that number.
	* @exception java.lang.IllegalArgumentException If supplied digit
	* is not between 0 and 15 inclusive.
	*
	*/
	public static final char hexDigit(int i) {

		switch (i) {
		case 0:
			return '0';
		case 1:
			return '1';
		case 2:
			return '2';
		case 3:
			return '3';
		case 4:
			return '4';
		case 5:
			return '5';
		case 6:
			return '6';
		case 7:
			return '7';
		case 8:
			return '8';
		case 9:
			return '9';
		case 10:
			return 'A';
		case 11:
			return 'B';
		case 12:
			return 'C';
		case 13:
			return 'D';
		case 14:
			return 'E';
		case 15:
			return 'F';
		}

		throw new IllegalArgumentException("Invalid digit:" + i);
	}

	/**
	* Returns a string of a specified number of a specified character.
	* @param n The number of characters to create in the return String.
	* @param c The character to create.
	* @return A String of the requested number of the requested character.
	*
	*/
	public static final String strstr(int n, char c) {

		StringBuilder ret = new StringBuilder(n);

		for (int x = 0 ; x < n ; x++) {
			ret.append(c);
		}

		return ret.toString();
	}

	/**
	* Returns a string of a specified number of spaces.
	* @param n The number of spaces to create.
	* @return A String of the requested number of spaces.
	* @see #strstr
	*
	*/
	public static final String spaces(int n) {
		return strstr(n, ' ');
	}

	////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////
	//
	//       CONSTRUCTORS
	//


	/**
	* Creates a new, empty Blob.
	* Uses default internal node size.
	*
	*/
	public Blob() {
		this(NODE_SIZE);
	}

	/**
	* Creates a new, empty Blob and specifies the default internal
	* node size.
	* @param growSize The number of bytes to allocate for a new node
	* in the internal data storage structure.
	*
	*/
	public Blob(int nodeSize) {
		this.nodeSize = nodeSize;

		// Create a new, empty head node
		head = new BlobNode(nodeSize);
		tail = head;

		size = 0;
	}

	/**
	* Creates a new blob that is a copy of an existing blob.
	* @param b The Blob to copy.
	*
	*/
	public Blob(Blob b) {
		this();

		append(b);
	}

	/**
	* Creates a new blob and initialized it with a byte array.
	* @param arr A byte array to copy entirely into the new Blob.
	*
	*/
	public Blob(byte arr[]) {
		this(arr, 0, arr.length);
	}

	/**
	* Creates a new blob and initialized it with a byte array.
	* @param arr A byte array to copy into the new Blob.
	* @param startPos The location to start extracting bytes from.
	* @param len The length of the array to copy in.
	*
	*/
	public Blob(byte arr[], int startPos, int len) {
		this();

		append(arr, startPos, len);
	}



	////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////
	//
	//       EXTERNAL METHODS
	//



	//
	// Append Methods
	//

	/**
	* Appends a byte to the end of the Blob.
	* @param b The byte to add.
	*
	*/
	public synchronized void append(byte b) {

		// Ensure the capacity of the current tail node
		if (tail.freespace() == 0) {
			appendNode(nodeSize);
		}

		// Add the byte to the end node
		tail.data[tail.size] = b;
		tail.size++;

		// incriment our total.
		size++;
	}

	/**
	* Appends one byte (in the form of a char) to the end of the Blob.
	* Strips the high byte off.
	* @param c The character to add.
	*
	*/
	public synchronized void append(char c) {
		append(false, c);
	}

	/**
	* Appends one or two bytes (in the form of a char) to the end
	* of the Blob.
	* Can optionally add the high byte or not.
	* @param addHighByte 'true' to add the character's high byte to the
	* Blob; 'false' to strip it.
	* @param c The character to add.
	*
	*/
	public synchronized void append(boolean addHighByte, char c) {

		if (addHighByte) {
			// Byte 1
			append((byte)(c >> 8));
		}

		// Byte 2
		append((byte)(c >> 0));
	}

	/**
	* Appends two bytes (in the form of a short) to the end of the Blob.
	* @param s The short integer to add.
	*
	*/
	public synchronized void append(short s) {

		// Byte 1
		append((byte)(s >> 8));

		// Byte 2
		append((byte)(s >> 0));
	}

	/**
	* Appends four bytes (in the form of an int) to the end of the Blob.
	* @param i The integer to add.
	*
	*/
	public synchronized void append(int i) {

		// Byte 1
		append((byte)(i >> 24));

		// Byte 2
		append((byte)(i >> 16));

		// Byte 3
		append((byte)(i >> 8));

		// Byte 4
		append((byte)(i >> 0));
	}

	/**
	* Appends eight bytes (in the form of a long) to the end of the Blob.
	* @param l The long integer to add.
	*
	*/
	public synchronized void append(long l) {

		// Byte 1
		append((byte)(l >> 56));

		// Byte 2
		append((byte)(l >> 48));

		// Byte 3
		append((byte)(l >> 40));

		// Byte 4
		append((byte)(l >> 32));

		// Byte 5
		append((byte)(l >> 24));

		// Byte 6
		append((byte)(l >> 16));

		// Byte 7
		append((byte)(l >> 8));

		// Byte 8
		append((byte)(l >> 0));
	}

	/**
	* Appends four bytes (in the form of a float) to the end of the Blob.
	* @param f The float to add.
	*
	*/
	public synchronized void append(float f) {

		// Append the integer created from the bytes of this float
		append(Float.floatToIntBits(f));
	}

	/**
	* Appends eight bytes (in the form of a double) to the end of the Blob.
	* @param d The double to add.
	*
	*/
	public synchronized void append(double d) {

		// Append the long created from the bytes of this double
		append(Double.doubleToLongBits(d));
	}

	/**
	* Appends bytes from a String to the end of the Blob.
	* Strips the high bytes from the characters.
	* @param s The String to add.
	*
	*/
	public synchronized void append(String s) {
		append(false, s);
	}

	/**
	* Appends bytes from a String to the end of the Blob.
	* Can optionally add the high bytes from the characters or not.
	* @param addHighByte 'true' to add the characters' high byte to the
	* Blob; 'false' to strip it.
	* @param s The String to add.
	*
	*/
	public synchronized void append(boolean addHighByte, String s) {

		for (int x = 0 ; x < s.length() ; x++) {
			append(addHighByte, s.charAt(x));
		}
	}

	/**
	* Appends an entire byte array to the end of the Blob.
	* @param arr The array to add bytes from.
	*
	*/
	public synchronized void append(byte arr[]) {
		append(arr, 0, arr.length);
	}

	/**
	* Appends a byte array to the end of the Blob.
	* @param arr The array to add bytes from.
	* @param startPos The position to start the byte extraction
	* from the array at.
	* @param len The number of bytes to read from the array.
	*
	*/
	public synchronized void append(byte arr[], int startPos, int len) {

		// If the current tail node has enuff storage for this
		// new addition, use it, otherwise, append a new node.
		if (tail.freespace() < len) {
			BlobNode oldTail = tail;
			appendNode(Math.max(len, nodeSize));

			// If old tail node was empty, we'll eliminate it
			if (oldTail.size == 0) {
				BlobNode bn = findBefore(oldTail);
				if (bn == null) {
					// oldTail == head
					head = tail;
				} else {
					bn.next = tail;
				}
			} // endif

		} // endif

		System.arraycopy(arr, startPos, tail.data, tail.size, len);
		tail.size += len;

		size += len;

	} // end append()


	/**
	* Appends the bytes from a Blob to the end of the Blob.
	* @param b The Blob to draw bytes from.
	*
	*/
	public synchronized void append(Blob b) {

		BlobNode bn;
		boolean setHead = false;

		// We're going to clone the target Blob's nodes and
		// tack them on the end.

		// if our current node is empty, we'll back up one before appending
		if (tail.size == 0) {
			bn = findBefore(tail);
			if (bn == null) {
				// tail == head
				setHead = true;
			} else {
				tail = bn;
				tail.next = null;
			}
		}

		// Now append all of the target's nodes
		bn = b.head;

		if (setHead) {
			// set the head node
			// (the head node now is empty)
			head = (BlobNode)bn.clone();
			tail = head;
			size = head.size;

			// we'll start with the next node down the line
			// (since we just used the first one)
			bn = bn.next;
		}

		while (bn != null) {
			tail.next = (BlobNode)bn.clone();
			tail = tail.next;
			size += tail.size;

			bn = bn.next;
		}

	} // end append()

	//
	// Data insert Methods
	//

	/**
	* Inserts a byte into the blob at the position pos.
	* Everything else is moved back.
	* @param pos The postition to add the byte at (0 -> beginning).
	* @param b The byte to add.
	* @see Blob#insertBytes
	* @exception java.lang.IndexOutOfBoundsException If pos is
	* outside range of Blob.
	*
	*/
	public synchronized void insertByte(int pos, byte b) {

		byte arr[] = new byte[1];
		arr[0] = b;
		insertBytes(pos, arr, 0, 1);

	} // end insertByte()

	/**
	* Inserts a byte into the blob at the position pos.
	* Everything else is moved back.
	* @param pos The postition to begin adding the bytes at (0 -> beginning).
	* @param arr The array of bytes to add from.
	* @exception java.lang.IndexOutOfBoundsException If pos is
	* outside range of Blob.
	*
	*/
	public synchronized void insertBytes(int pos, byte arr[]) {
		this.insertBytes(pos, arr, 0, arr.length);
	}

	/**
	* Inserts a byte into the blob at the position pos.
	* Everything else is moved back.
	* @param pos The postition to begin adding the bytes at (0 -> beginning).
	* @param arr The array of bytes to add from.
	* @param startPos The position to begin byte copy from.
	* @param len The number of bytes to add from the array.
	* @exception java.lang.IndexOutOfBoundsException If pos is
	* outside range of Blob.
	*
	*/
	public synchronized void insertBytes(int pos, byte arr[],
	                                     int startPos, int len) {

		// are we just appending?
		if (pos == size) {
			append(arr, startPos, len);
			return;
		}

		// Is the position valid?
		if (!between(pos, 0, (size - 1))) {
			throw new IndexOutOfBoundsException();
		} // endif

		// Find the correct node and index into that node
		int currIndex = seek(pos);

		// Do we have enough space in the current node for the
		// new information
		if (curr.freespace() >= len) {
			// Insert the info into the current node

			// Move the old stuff back
			System.arraycopy(curr.data, currIndex,
			                 curr.data, (currIndex + len),
			                 (curr.size - currIndex));

			// Copy in the new stuff
			System.arraycopy(arr, startPos, curr.data, currIndex, len);

			// Set the new size
			this.curr.size += len;

		} else {
			// not enough room in the inn.
			// erect a barn, errr, a new node for it.
			BlobNode newNode = new BlobNode(nodeSize, arr, startPos, len);
			BlobNode before = findBefore(curr);

			// Where do we stick it?
			if (currIndex == 0) {
				// put it before the current node
				if (before == null) {
					// inserting before head
					newNode.next = head;
					head = newNode;
				} else {
					// inserting in middle
					newNode.next = before.next;
					before.next = newNode;
				}
			} else {
				// We'll have to split the current node
				BlobNode a = new BlobNode(Math.max(nodeSize, currIndex));
				BlobNode b = new BlobNode(Math.max(nodeSize,
				                                   (curr.size - currIndex)));

				// Copy the data
				a.size = currIndex;
				System.arraycopy(curr.data, 0, a.data, 0, a.size);
				b.size = curr.size - currIndex;
				System.arraycopy(curr.data, currIndex, b.data, 0, b.size);

				// Set up the links.
				b.next = (before == null) ? head : before.next;
				newNode.next = b;
				a.next = newNode;
				if (before == null) {
					head = a;
				} else {
					before.next = a;
				}
			} // endif

		} // endif

		// adjust size
		size += len;

	} // end insertBytes()


	//
	// Data remove methods
	//

	/**
	* Truncates the Blob to the specified position.
	* The Blob will have the given number of bytes left.
	* @param len The size to truncate the Blob to.
	* @exception java.lang.IndexOutOfBoundsException If len is
	* outside range of Blob.
	*
	*/
	public synchronized void truncate(int len) {

		// Anything to do?
		if (len == size) {
			return;
		}

		// Is the data within bounds?
		if (!between(len, 0, size)) {
			throw new IndexOutOfBoundsException();
		}

		// find the correct node and truncate that node
		// to the required number of bytes to make the
		// whole thing work.
		// We have to do the seek() and truncate() on different
		// lines as seek() sets 'curr'..
		int currIndex = seek(len);
		curr.size = currIndex;

		// Set the new size;
		size = len;

		// set the next pointer of this node to null
		curr.next = null;
		tail = curr;

	} // end truncate()


	/**
	* Removes a byte from the Blob at the requested position.
	* Everything else is moved up.
	* @param pos The position to remove a byte from (0 -> beginning).
	* @see Blob#removeBytes
	* @exception java.lang.IndexOutOfBoundsException If pos is
	* outside range of Blob.
	*
	*/
	public synchronized void removeByte(int pos) {
		removeBytes(pos, 1);
	} // end removeByte()

	/**
	* Removes a number of bytes from the Blob at the requested position.
	* Everything else is moved up.
	* @param pos The position to remove the bytes from (0 -> beginning).
	* @param len The number of bytes to remove.
	* @exception java.lang.IndexOutOfBoundsException If pos and len are
	* outside range of Blob.
	*
	*/
	public synchronized void removeBytes(int pos, int len) {

		// Is the data within bounds?
		if (!(between(pos, 0, (size - 1)) &&
            between((pos + len), 0, size))) {

			throw new IndexOutOfBoundsException();
		}

		BlobNode startNode, endNode;
		int startPos, endPos;

		// Get the starting and ending locations
		startPos = seek(pos);
		startNode = curr;

		endPos = seek(pos + len);
		endNode = curr;

		if (startNode == endNode) {
			// Just removing stuff from one node

			// move the stuff up and adjust length
			System.arraycopy(curr.data, endPos,
			                 curr.data, startPos, (curr.size - endPos));
			curr.size -= len;

		} else {
			// Removing stuff across several nodes
			// Create a new node to hold the info
			BlobNode newNode =
			     new BlobNode(Math.max(nodeSize,
			                           (startPos + (endNode.size - endPos))));

			// Move stuff into this new node
			System.arraycopy(startNode.data, 0,
			                 newNode.data, 0, startPos);

			System.arraycopy(endNode.data, endPos,
			                 newNode.data,  startPos, (endNode.size - endPos));

			newNode.size = (startPos + 1) + (endNode.size - endPos);

			// update the pointers
			newNode.next = endNode.next;

			// Find the node PREVIOUS to the startNode
			BlobNode before = findBefore(startNode);
			if (before == null) {
				head = newNode;
			} else {
				before.next = newNode;
			}
		} // endif

		// Set the new size
		size -= len;

	} // end removeBytes()


	//
	// Data Retrieval methods
	//

	/**
	* Gets the bytes of the blob as a byte array.
	* @return An array of the bytes that compose the Blob.
	*
	*/
	public byte[] getBytes() {
		 return getBytes(0, size);
	}

	/**
	* Returns a subset of the bytes of the blob as a byte array.
	* @param start start index to begin draw (included in get).
	* @param len number of bytes to extract.
	* @return An array of the bytes that compose the Blob.
	* @exception java.lang.IndexOutOfBoundsException If start and len are
	* outside range of Blob.
	*
	*/
	public synchronized byte[] getBytes(int start, int len) {

		// Special case
		if ((start == 0) && (len == 0)) {
			return new byte[0];
		}

		// Is the data within bounds?
		if (!(between(start, 0, (size - 1)) &&
            between((start + len), 0, size))) {
			throw new IndexOutOfBoundsException();
		}

		byte ret[] = new byte[len];
		int bytesCopied = 0;
		int copyFromThis = 0;

		int currIndex = seek(start);
		while (bytesCopied < len) {
			copyFromThis = Math.min((curr.size - currIndex),
			                        (len - bytesCopied));

			System.arraycopy(curr.data, currIndex,
			                 ret, bytesCopied, copyFromThis);
			bytesCopied += copyFromThis;

			curr = curr.next;
			currIndex = 0;
		} // endwhile

		return ret;

	} // end getBytes()


	/**
	* Returns the byte at a specific location.
	* @param pos The position to from which to return the byte
	* (0 -> beginning).
	* @return The byte at requested position.
	* @exception java.lang.IndexOutOfBoundsException If pos is
	* outside range of Blob.
	*
	*/
	public synchronized byte byteAt(int pos) {

		// Is the data within bounds?
		if (!between(pos, 0, (size - 1))) {
			throw new IndexOutOfBoundsException();
		}

		// Find the correct node and index into that node
		int currIndex = seek(pos);

		// Get the byte
		return curr.data[currIndex];
	}

	/**
	* Returns a character reconstructed from one byte at a specific location.
	* Sets the high byte in the character to 0x0.
	* @param pos The position to get the byte to reconstuct the character
	* (0 -> beginning).
	* @return The reconstructed character.
	* @exception java.lang.IndexOutOfBoundsException If the requested
	* char is outside the Blob.
	*
	*/
	public synchronized char charAt(int pos) {
		return charAt(false, pos);
	} // end charAt()

	/**
	* Returns a character reconstructed from one or two bytes at a
	* specific location.
	* @param useHighByte 'true' to use two bytes (one for the high byte)
	* to reconstruct a character; 'false' to use one byte and set the
	* high byte to 0x0.
	* @param pos The position to get the byte(s) to reconstuct the character
	* (0 -> beginning).
	* @return The reconstructed character.
	* @exception java.lang.IndexOutOfBoundsException If the requested
	* bytes are outside the Blob.
	*
	*/
	public synchronized char charAt(boolean useHighByte, int pos) {

		// Is the data within bounds?
		if (!between(pos, 0, (size - (useHighByte ? 2 : 1)))) {
			throw new IndexOutOfBoundsException();
		}

		// Return character
		char ret;

		if (useHighByte) {
			// get a short and cast it as a char.
			ret = (char)shortAt(pos);

		} else {
			// get a byte and cast it as a char.
			ret = (char)byteAt(pos);

		} // endif

		return ret;
	} // end charAt()

	/**
	* Returns a short int reconstructed from two bytes at a specific
	* location.
	* @param pos The position to get the bytes to reconstuct the short
	* (0 -> beginning).
	* @return The reconstructed short.
	* @exception java.lang.IndexOutOfBoundsException If the requested
	* bytes are outside the Blob.
	*
	*/
	public synchronized short shortAt(int pos) {

		// Is the data within bounds?
		if (!between(pos, 0, (size - 2))) {
			throw new IndexOutOfBoundsException();
		}

		// Return value
		short ret = 0x0, temp;

		// Find proper location
		beginEnumeration(pos);

		for (int x = 0 ; x < 2 ; x++) {

			// get next byte
			// We shift the input byte up all the way to the high
			// byte and then back down to the req'd position because
			// java won't let us cast an a byte as a larger integer
			// type without propigating the sign bit thruout the
			// higher bytes.  This ain't what we need so we jump
			// thru some hoops to avoid it.
			temp = (short)((nextByte() << (1 * 8)) >>> (1 * 8));
			ret |= (temp << (8 * (1 - x)));

		} // endfor

		return ret;
	} // end shortAt()

	/**
	* Returns an int reconstructed from four bytes at a specific
	* location.
	* @param pos The position to get the bytes to reconstuct the int
	* (0 -> beginning).
	* @return The reconstructed int.
	* @exception java.lang.IndexOutOfBoundsException If the requested
	* bytes are outside the Blob.
	*
	*/
	public synchronized int intAt(int pos) {

		// Is the data within bounds?
		if (!between(pos, 0, (size - 4))) {
			throw new IndexOutOfBoundsException();
		}

		// Return value
		int ret = 0x0, temp;

		// Find proper location
		beginEnumeration(pos);

		for (int x = 0 ; x < 4 ; x++) {

			// get next byte
			// We shift the input byte up all the way to the high
			// byte and then back down to the req'd position because
			// java won't let us cast an a byte as a larger integer
			// type without propigating the sign bit thruout the
			// higher bytes.  This ain't what we need so we jump
			// thru some hoops to avoid it.
			temp = ((int)nextByte() << (3 * 8)) >>> (3 * 8);
			ret |= (temp << (8 * (3 - x)));

		} // endfor

		return ret;
	} // end intAt()


	/**
	* Returns a long int reconstructed from eight bytes at a specific
	* location.
	* @param pos The position to get the bytes to reconstuct the long
	* (0 -> beginning).
	* @return The reconstructed long.
	* @exception java.lang.IndexOutOfBoundsException If the requested
	* bytes are outside the Blob.
	*
	*/
	public synchronized long longAt(int pos) {

		// Is the data within bounds?
		if (!between(pos, 0, (size - 8))) {
			throw new IndexOutOfBoundsException();
		}

		// Return value
		long ret = 0x0, temp;

		// Find proper location
		beginEnumeration(pos);

		for (int x = 0 ; x < 8 ; x++) {

			// get next byte
			// We shift the input byte up all the way to the high
			// byte and then back down to the req'd position because
			// java won't let us cast an a byte as a larger integer
			// type without propigating the sign bit thruout the
			// higher bytes.  This ain't what we need so we jump
			// thru some hoops to avoid it.
			temp = ((long)nextByte() << (7 * 8)) >>> (7 * 8);
			ret |= (temp << (8 * (7 - x)));

		} // endfor

		return ret;
	} // end longAt()


	/**
	* Returns a floating point number reconstructed from four bytes
	* at a specific
	* location.
	* @param pos The position to get the bytes to reconstuct the float
	* (0 -> beginning).
	* @return The reconstructed float.
	* @exception java.lang.IndexOutOfBoundsException If the requested
	* bytes are outside the Blob.
	*
	*/
	protected synchronized float floatAt(int pos) {

		return Float.intBitsToFloat(intAt(pos));

	} // end floatAt()

	/**
	* Returns a double precision floating point number reconstructed
	* from eight bytes at a specific
	* @param pos The position to get the bytes to reconstuct the double
	* (0 -> beginning).
	* @return The reconstructed double.
	* @exception java.lang.IndexOutOfBoundsException If the requested
	* bytes are outside the Blob.
	*
	*/
	public synchronized double doubleAt(int pos) {

		return Double.longBitsToDouble(longAt(pos));

	} // end floatAt()


	/**
	* Returns a String reconstructed from the bytes of the Blob.
	* Sets the high byte in each character to 0x0.
	* @return The reconstructed String.
	*
	*/
	public synchronized String getString() {

		return getString(false, 0, size);
	} // end getString()

	/**
	* Returns a String reconstructed from bytes at a specific location.
	* Sets the high byte in each character to 0x0.
	* @param pos The position to start retrieving the bytes to reconstuct
	* the String (0 -> beginning).
	* @param len The length of the to-be-returned String.
	* @return The reconstructed String.
	* @exception java.lang.IndexOutOfBoundsException If the requested
	* bytes are outside the Blob.
	*
	*/
	public synchronized String getString(int pos, int len) {
		return getString(false, pos, len);
	} // end getString()

	/**
	* Returns a String reconstructed from bytes at a specific location.
	* @param useHighByte 'true' to use two bytes (one for the high byte)
	* to reconstruct each character; 'false' to use one byte and set the
	* high byte to 0x0.
	* @param pos The position to start retrieving the bytes to reconstuct
	* the String (0 -> beginning).
	* @param len The length of the to-be-returned String.
	* @return The reconstructed String.
	* @exception java.lang.IndexOutOfBoundsException If the requested
	* bytes are outside the Blob.
	*
	*/
	public synchronized String getString(boolean useHighByte,
	                                     int pos, int len) {

		// Special case
		if ((pos == 0) && (len == 0)) {
			return "";
		}

		// Is the data within bounds?
		if (!(between(pos, 0, (size - 1)) &&
		      between((pos + (len * (useHighByte ? 2 : 1))),
				               0, size))) {
			throw new IndexOutOfBoundsException();
		}

		// Return String
		StringBuilder ret = new StringBuilder(len);

		// hold character
		char c;

		// Find proper location
		beginEnumeration(pos);

		for (int x = 0 ; x < len ; x++) {

			if (useHighByte) {
				// Use both bytes

				// high byte
				c = (char)(nextByte() << 8);

				// low byte
				c |= nextByte();

			} else {
				// just get one (low) byte
				c = (char)nextByte();

			} // endif

			// Add the new character
			ret.append(c);

		} // endfor

		return ret.toString();
	} // end getString()


	/**
	* Clones this Blob.
	* The resulting Blob will have the same data as the first.
	* (altho possibly not the same internal configuration).
	*
	*/
	public synchronized Object clone() {
		// We can just call our getBlob method to do this.
		return getBlob(0, size);
	}

	/**
	* Returns a new Blob drawn from bytes at a specific location.
	* @param pos The position to start retrieving the bytes to build
	* the new Blob (0 -> beginning).
	* @param len The number of bytes to put into the new Blob.
	* @return The newly constructed Blob.
	* @exception java.lang.IndexOutOfBoundsException If the requested
	* bytes are outside the Blob.
	*
	*/
	public synchronized Blob getBlob(int pos, int len) {
		// cop out and use our other methods.  Speed be damned -- this
		// isn't that important anyway.
		// We *should* just go thru and clone the appropiate
		// BlobNodes and just modifiy the ones on the end....
		return new Blob(getBytes(pos, len));

	} // end getBlob()

	//
	// Misc Methods
	//

	/**
	* Returns an enumeration of the bytes in this Blob.
	* The objects returned from the calls to nextElement are
	* of type Byte.
	* @param pos The location from which to begin enumerating bytes.
	* @exception java.lang.IndexOutOfBoundsException If pos is
	* outside range of Blob.
	*
	*/
	@SuppressWarnings("rawtypes")
    public synchronized Enumeration enumerateBytes(int pos) {

		// Is the data within bounds?
		if (!between(pos, 0, (size - 1))) {
			throw new IndexOutOfBoundsException();
		}

		final int cep = seek(pos);
		final BlobNode cen = curr;

		return new Enumeration() {

			int currEnumerationPos = cep;
			BlobNode currEnumerationNode = cen;

			public synchronized boolean hasMoreElements() {
				return (currEnumerationNode != null);
			}

			public synchronized Object nextElement() {
				if (currEnumerationNode == null) {
					throw new NoSuchElementException("Past end of current Enumeration");
				}

				byte ret = currEnumerationNode.data[currEnumerationPos++];

				// check to see if we're at the end of the current node
				if (currEnumerationPos == currEnumerationNode.size) {
					// At end, go to next node
					currEnumerationNode = currEnumerationNode.next;
					currEnumerationPos = 0;
				}

				// All done
				return Byte.valueOf(ret);
			} // end nextElement()

		}; // end Enumeration
	} // end enumerateBytes

	/**
	* Sets a byte at a particular position.
	* @param pos The position to set the byte at.
	* @param b The value to set that postion to.
	* @exception java.lang.IndexOutOfBoundsException If pos is
	* outside range of Blob.
	*
	*/
	public synchronized void setByteAt(int pos, byte b) {
		// Is the data within bounds?
		if (!between(pos, 0, (size - 1))) {
			throw new IndexOutOfBoundsException();
		}

		// Find the correct node and index into that node
		int currIndex = seek(pos);

		// Set the byte
		curr.data[currIndex] = b;
	}


	/**
	* Gets the number of bytes in the Blob.
	* @return The number of bytes in the Blob.
	*
	*/
	public synchronized int length() {
		return size;
	}

	/**
	* Returns true if the blob is equal to the given object.
	* True only if supplied object is a Blob and the two contain
	* exactly the same data.
	*
	*/
	public synchronized boolean equals(Object o) {
		if ((o == null) || !(o instanceof Blob)) {
			return false;
		}

		Blob b = (Blob)o;

		// Are they the same length?
		if (size != b.size) {
			return false;
		}

		// Compare the data
		beginEnumeration(0);
		b.beginEnumeration(0);
		while (hasMoreBytes()) {
			if (nextByte() != b.nextByte()) {
				return false;
			}
		}

		// If we got here, they must be equal
		return true;

	} // end equals()

	/**
	 * Objects that are equal must have the same hashcode
	 */
	public synchronized int hashCode() {
		return toString().hashCode();
	}
	
	/**
	* Searches for a byte and returns an index to the first one found.
	* @param b The byte to search for.
	* @return The index of the first match or -1 if not found.
	*
	*/
	public synchronized int indexOf(byte b) {
		return indexOf(b, 0);
	}

	/**
	* Searches for a byte and returns an index to the first one found.
	* Search starts at given index and includes that index.
	* @param b The byte to search for.
	* @param pos The position to begin searching at (0 -> beginning).
	* @return The index of the first match or -1 if not found.
	* @exception java.lang.IndexOutOfBoundsException If pos is
	* outside range of Blob.
	*
	*/
	public synchronized int indexOf(byte b, int pos) {

		// Is the data within bounds?
		if (!between(pos, 0, (size - 1))) {
			throw new IndexOutOfBoundsException();
		}

		beginEnumeration(pos);
		int currentPos = pos;

		while (hasMoreBytes()) {

			if (nextByte() == b) {
					return currentPos;
			}

			currentPos++;
		} // endwhile

		return -1;
	}

	//
	// IO Methods
	//

	/**
	* Reads bytes from an InputStream into the Blob.
	* @param len The number of bytes to attempt to read.
	* @param in The InputStream to read from.
	* @return The number of bytes read and appended to the blob
	* or -1 if the end of the stream was reached.
	* @exception java.io.IOException If there is a problem reading.
	*
	*/
	public synchronized int read(int len, InputStream in)
	                              throws IOException {

		byte b[] = new byte[len];
		int bytesRead;
		try {
			bytesRead = in.read(b);
		} catch (EOFException e) {
			// We'll just return a -1 just as if
			// we had gotten a -1 in response from the read
			return -1;

		} catch (IOException e) {
			// Throw this one to the caller
			throw (IOException)e.fillInStackTrace();
		}
		append(b, 0, bytesRead);
		return bytesRead;

	} // end read();

	/**
	* Reads all bytes from an InputStream into the Blob.
	* This will read an InputStream byte-by-byte until EOF and
	* append the data to the end of the Blob.
	* @param in The InputStream to read from.
	* @return The number of bytes read and appended to the blob
	* or -1 if the end of the stream was reached immediately.
	* @exception java.io.IOException If there is a problem reading.
	*
	*/
	public synchronized int read(InputStream in) throws IOException {

		byte b[] = new byte[nodeSize];
		int bytesRead, totalBytesRead = -1;
		while (true) {
			try {
				bytesRead = in.read(b);
			} catch (EOFException e) {
				// We'll just break out of the loop just as if
				// we had gotten a -1 in response from the read
				break;

			} catch (IOException e) {
				// Throw this one to the caller
				throw (IOException)e.fillInStackTrace();
			}

			// if we're at EOF, get out of the loop.
			if (bytesRead == -1) {
				break;
			}

			// We have totalBytesRead set to -1 initially
			// so that we'll return -1 if the stream is at EOF
			// right off the bat.
			// Now, we have to set it to 0 so that our byte sum
			// will be correct.
			if (totalBytesRead == -1) {
				totalBytesRead = 0;
			}

			append(b, 0, bytesRead);
			totalBytesRead += bytesRead;
		}
		return totalBytesRead;

	} // end read();


	/**
	* Reads most bytes from an InputStream into the Blob -
	* not to exceed <max> bytes by much (it may read up to nodeSize more).
	* This will read an InputStream byte-by-byte until EOF and
	* append the data to the end of the Blob.
	* @param in The InputStream to read from.
	* @param max the max (sort of) bytes to read
	* @return The number of bytes read and appended to the blob
	* or -1 if the end of the stream was reached immediately.
	* or -2 if we cut off due to max before end
	* @exception java.io.IOException If there is a problem reading.
	*
	*/
	public synchronized int readLimiting(InputStream in, long max)
		throws IOException
	{
		byte b[] = new byte[nodeSize];
		int bytesRead, totalBytesRead = -1;
		while (true) {
			try {
				bytesRead = in.read(b);
			} catch (EOFException e) {
				// We'll just break out of the loop just as if
				// we had gotten a -1 in response from the read
				break;

			} catch (IOException e) {
				// Throw this one to the caller
				throw (IOException)e.fillInStackTrace();
			}

			// if we're at EOF, get out of the loop.
			if (bytesRead == -1) {
				break;
			}

			// We have totalBytesRead set to -1 initially
			// so that we'll return -1 if the stream is at EOF
			// right off the bat.
			// Now, we have to set it to 0 so that our byte sum
			// will be correct.
			if (totalBytesRead == -1) {
				totalBytesRead = 0;
			}
			
			// if we're already over the limit, get out!
			if ((totalBytesRead > max) && (bytesRead > 0))
			{
				totalBytesRead = -2;
				break;
			}

			append(b, 0, bytesRead);
			totalBytesRead += bytesRead;
		}
		return totalBytesRead;

	}	// readLimiting


	/**
	* provide an output stream that writes to the END of the blob (appends)
	*/
	public OutputStream outputStream()
	{
		return new OutputStream ()
		{
			public void write(int b)
			{
				append((byte)b);
			}
			
			public void write(byte[] b)
			{
				append(b);
			}
			
			public void write(byte[] b, int off, int len)
			{
				append(b, off, len);
			}
		};

	}	// outputStream

	/**
	* provide an input stream that reads the blob contents
	*/
	public InputStream inputStream()
	{
		return new InputStream ()
		{
			/** next byte to return */
			private int m_pos = 0;
			
			/*public int read(byte b[], int off, int len)
				throws IOException
			{
				return super.read(b, off, len);
			}*/

			public int read()
				throws IOException
			{
				int rv = -1;
				try
				{
					rv = byteAt(m_pos);
					
					// input streams must return values 0..255, but our bytes are signed
					if (rv < 0) rv = 256 + rv;

					m_pos++;
				}
				catch (IndexOutOfBoundsException e) {}
				
				return rv;
			}

			public int available()
			{
				return size-m_pos;
			}
		};

	}	// inputStream


	/**
	* Writes the entire contents of the Blob to an OutputStream.
	* @param out The OutputStream to write to.
	* @exception java.io.IOException If there is a problem writing.
	*
	*/
	public synchronized void write(OutputStream out) throws IOException {
		write(0, size, out);
	}

	/**
	* Writes the contents of a part of the Blob to an OutputStream.
	* @param pos The position to begin writing from.
	* @param len The number of bytes to write.
	* @param out The OutputStream to write to.
	* @exception java.lang.IndexOutOfBoundsException If pos and len are
	* outside range of Blob.
	* @exception java.io.IOException If there is a problem writing.
	*
	*/
	public synchronized void write(int pos, int len,
	                               OutputStream out) throws IOException {

		// Is the data within bounds?
		if (!(between(pos, 0, (size - 1)) &&
		      between((pos + len), 0, size))) {
			throw new IndexOutOfBoundsException();
		}

		// Go to the beginning of the data
		int bytesWritten = 0;
		int writeFromThis = 0;

		int currIndex = seek(pos);
		while (bytesWritten < len) {
			writeFromThis = Math.min((curr.size - currIndex),
			                         (len - bytesWritten));

			// Write out the stuff,
			out.write(curr.data, currIndex, writeFromThis);
			bytesWritten += writeFromThis;

			curr = curr.next;
			currIndex = 0;
		} // endwhile

	} // end write();


	/**
	* Generates and returns an 8-byte checksum.
	*
	*/
	public synchronized long checksum() {

		long ret = 0, temp = 0, hold = 0;
		int index = 0;

		if (size == 0) {
			return ret;
		}

		beginEnumeration(0);
		while (hasMoreBytes()) {

			// put the byte into the low end byte of a long
			temp = ((long)nextByte() << (7 * 8)) >>> (7 * 8);

			// put the byte into a holder long
			hold |= (temp << (8 * (7 - index)));

			if (++index == 8) {
				// XOR the last eight bytes with the checksum value
				ret ^= hold;

				// reset the holder long
				hold = 0;
				index = 0;
			}
		} // endwhile

		// Get any remaining bytes in hold
		if (index != 0) {
			ret ^=hold;
		}

		return ret;
	}


	/**
	* Returns a string representation of the Blob.
	* Includes length and checksum.
	* @see #printContents
	*
	*/
	public synchronized String toString() {
		return "Blob[length=" + size +
		                  ";checksum=" + toHex(checksum()) + "]";
	}


	/**
	* Prints the contents of this Blob.
	* Formats the data neatly and debug it.
	* @see #toString
	*
	*/
	public synchronized void printContents() {

		int x, bytesPast;
		String holdStr;
		char rep[] = new char[16];
		byte b;
		long cksum = checksum();

		// current output width
		int currWidth = 86;

		holdStr = ("Blob: length = " + size +
		           " -- Checksum = " + toHex(cksum) + " ");
		log.debug("{}{}", holdStr, strstr((currWidth - holdStr.length()), '-'));

		// If no data, just print the last line and get outta' here
		if (size == 0) {
			log.debug(strstr(currWidth, '-'));
			return;
		}

		log.debug("     0 | ");

		bytesPast = 0;
		beginEnumeration(0);
		while (hasMoreBytes()) {

				// print next byte
				b = nextByte();
				log.debug(toHex(b));

				// get the representation
				if (between(b, 32, 126)) {
					rep[bytesPast % 16] = (char)b;
				} else {
					rep[bytesPast % 16] = '.';
				}

				// incriment pointer
				bytesPast++;

				if ((bytesPast % 16) == 0) {
					// print out representation
					log.debug("   <");
					for (x = 0 ; x < 16 ; x++) {
						log.debug("{}", rep[x]);
					}
					log.debug(">");

					// Start of new line
					if (hasMoreBytes()) {
						holdStr = Integer.toString(bytesPast);
						log.debug(spaces(6 - holdStr.length()));
						log.debug(holdStr);
						log.debug(" | ");
					}

				} else if ((bytesPast % 4) == 0) {
					// end of 4-byte chunk
					log.debug("   ");

				} else {
					log.debug(" ");
				} // endif

		} // endwhile

		if ((bytesPast % 16) != 0) {
			// write out some filler spaces
			for (x = (bytesPast % 16) ; x < 16 ; x++) {
				log.debug("  ");
				rep[x] = ' ';

				if ((x % 4) == 0) {
					// end of (what would have been a) 4-byte chunk
					log.debug("   ");
				} else {
					log.debug(" ");
				} // endif
			}

			// print out representation
			log.debug("   <");
			for (x = 0 ; x < 16 ; x++) {
				log.debug("{}", rep[x]);
			}
			log.debug(">");
		}

		// Write last dashed line
		holdStr = ("---------------" +
		           strstr(String.valueOf(size).length(), '-') +
		           "--- Checksum = " + toHex(cksum) + " ");
		log.debug(holdStr);
		log.debug(strstr((currWidth - holdStr.length()), '-'));
	}

	////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////
	//
	//       INTERNAL METHODS
	//


	/**
	* Appends a new node to the end of the internal list.
	* @param newNodeSize Size of the new node to create.
	*
	*/
	protected void appendNode(int newNodeSize) {

		// Create a new node -- put it on the end of the list
		tail.next = new BlobNode(newNodeSize);

		// Set the new tail
		tail = tail.next;
	}

	/**
	* Seeks a position within the internal list.
	* Takes an offset from the beginning and sets the 'curr' pointer
	* to the reqested node and returns the offset within that node
	* to the correct position.
	* @param pos The position within the Blob to seek.
	* @return The offest into the current node where the requested
	* byte can be found.
	* @exception java.lang.IndexOutOfBoundsException If pos is
	* outside range of Blob.
	*
	*/
	protected int seek(int pos) {
		if (!between(pos, 0, (size - 1))) {
			throw new IndexOutOfBoundsException("Seek past end:" +
			                                    "pos=" + pos + "   " +
			                                    "size=" + size);

		}

		int bytesPast = 0;
		curr = this.head;

		while ((curr.size + bytesPast) <= pos) {

			// Go to next node...
			bytesPast += curr.size;
			curr = curr.next;
		}

		// Return the offset into the current node.
		return (pos - bytesPast);
	}

	/**
	* Finds the node before the given node.
	* This is the one whose next pointer is the given node.
	* @return the node before the supplied node or
	* null if the given node is the head node.
	* @exception java.util.NoSuchElementException if the node is
	* not found in the list.
	*
	*/
	protected BlobNode findBefore(BlobNode target) {

		BlobNode bn;

		if (target == head) {
			return null;
		}

		bn = head;
		while (bn.next != target) {
			if (bn.next == null) {
				throw new NoSuchElementException("Couldn't find BlobNode");
			}
			bn = bn.next;
		}
		return bn;

	} // end findBefore()



	//
	// Internal Enumeration Methods
	//


	/**
	* Sets up an enumeration of the bytes of the Blob
	* starting from a particular point.
	* @param pos The location from which to begin enumerating bytes.
	* @exception java.lang.IndexOutOfBoundsException If pos and len are
	* outside range of Blob.
	*
	*/
	protected synchronized void beginEnumeration(int pos) {

		// Set the current position null
		enumerationNode = null;

		// Is the data within bounds?
		if (!between(pos, 0, (size - 1))) {
			throw new IndexOutOfBoundsException();
		}

		// Find the position
		enumerationPos = seek(pos);
		enumerationNode = curr;

		// We're ready to begin enumerating the bytes
	} // end beginInternalEnumeration()

	/**
	* Returns 'true' if the Blob has more bytes, 'false' if empty.
	*
	*/
	protected synchronized boolean hasMoreBytes() {
		return (enumerationNode != null);
	}

	/**
	* Returns the next byte in the Blob.
	* @exception java.util.NoSuchElementException If there are no more bytes
	* in the current enumeration
	*
	*/
	protected synchronized byte nextByte() {

		if (enumerationNode == null) {
			throw new NoSuchElementException("Past end of current Enumeration");
		}

		byte ret = enumerationNode.data[enumerationPos++];

		// check to see if we're at the end of the current node
		if (enumerationPos == enumerationNode.size) {

			// At end, go to next node
			enumerationNode = enumerationNode.next;
			enumerationPos = 0;
		}

		// All done
		return ret;
	} // end nextByte()


	//
	// Serialization Methods
	//


	/**
	* A specialized object write routine.
	* This is because java gets a stack overflow error when
	* trying to write the linked list.  Damn!
	*
	*/
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();

		byte a[] = getBytes();
		out.writeObject(a);
	}

	/**
	* A specialized object read routine.
	*
	*/
	private void readObject(ObjectInputStream in) throws IOException,
	                                                     ClassNotFoundException {
		in.defaultReadObject();

		// Create a new node to contain the one massive chunk of data.
		byte a[] = (byte [])in.readObject();
		head = new BlobNode(nodeSize, a, 0, a.length);

		tail = head;
		size = a.length;
	}
} // end Blob


/**
* A node in the underlying data-storage structure of the Blob class.
* @see Blob
* @author T. Gee
*
*/
class BlobNode implements Cloneable, Serializable {

	/**
   * 
   */
  private static final long serialVersionUID = 3833749897282336560L;

  /**
	* The next BlobNode in the list, null if last node.
	*/
	BlobNode next = null;

	/**
	* The number of bytes currently in this node
	*/
	int size;

	/**
	* The actual data held by this node
	*/
	byte data[];


	/**
	* Constructs a new, empty node with the requested capacity.
	* @param capacity The size of this node's internal storage.
	*
	*/
	public BlobNode(int capacity) {
		data = new byte[capacity];
		size = 0;
	}

	/**
	* Constructs a new node initialized with the supplied data.
	* @param capacity The size of the new node -- if smaller than
	* 'data', will be increased to accomidate all of data.
	* @param data The data to use to initialize this BlobNode.
	*
	*/
	public BlobNode(int capacity, byte arr[], int startPos, int len) {
		data = new byte[Math.max(capacity, len)];
		System.arraycopy(arr, startPos, data, 0, len);
		size = len;
	}

	/**
	* Gets the number of bytes of free storage within this node.
	* @return the number of free bytes.
	*
	*/
	public int freespace() {
		return data.length - size;
	}

	/**
	* Clones the node.
	*
	*/
	public Object clone() {
		BlobNode b = new BlobNode(data.length);

		System.arraycopy(this.data, 0, b.data, 0, size);
		b.size = this.size;

		return b;
	}

} // end BlobNode
