/*******************************************************************************
**
** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) Hub grants you
** ("Licensee") a non-exclusive, royalty free, license to use, modify and
** redistribute this software in source and binary code form, provided that
** i) this copyright notice and license appear on all copies of the software;
** and ii) Licensee does not utilize the software in a manner which is
** disparaging to ADL Co-Lab Hub.
**
** This software is provided "AS IS," without a warranty of any kind.  ALL
** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab Hub AND ITS LICENSORS
** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO
** EVENT WILL ADL Co-Lab Hub OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE,
** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE
** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE
** SOFTWARE, EVEN IF ADL Co-Lab Hub HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH
** DAMAGES.
**
*******************************************************************************/
package org.adl.logging;

import java.util.LinkedList;

import org.adl.util.LogMessage;

/**
 * Superclass that holds the majority of the functionality for the
 * LogMessageCollections.
 *   
 * @author ADL Technical Team
 */
public class ADLMessageCollection {
	/**
	 * LinkedList used to hold the messages bound for the Summary Log
	 */
	private LinkedList mMessages;

	/**
	 * Holds the value to indicate whether or not the SummaryLogWriter has been
	 * told to wait()
	 */
	private boolean mLogWriterIsWaiting = false;

	/**
	 * Default Constructor - declares the LinkedList that acts as the collection
	 * object/queue
	 */
	public ADLMessageCollection() {
		mMessages = new LinkedList();
	}

	/**
	 * This method adds a LogMessage object to the end of the LinkedList. If the 
	 * LogWriter Thread was told to wait, notify it that there are
	 * messages queued
	 * 
	 * @param iMessage the message to be added to the collection 
	 */
	public synchronized void addMessage(LogMessage iMessage) {
		mMessages.add(iMessage);

		// If the SummaryLogWriter has been told to wait, let it know that there
		//  are messages waiting to be written to the Summary Log
		if (mLogWriterIsWaiting) {
			mLogWriterIsWaiting = false;
			notify();
		}
	}

	/**
	 * This method removes and returns the first LogMessage object in the
	 * LinkedList. If there are messages waiting to be written to 
	 * the Log. If there are NO Messages in the list waiting to be written tell
	 * the LogWriter Thread to wait
	 * 
	 * @return LogMessage The first message in the queue
	 */
	public synchronized LogMessage getMessage() {
		return (LogMessage) mMessages.removeFirst();
	}

	/**
	 * Returns the size of the collection.
	 * 
	 * @return Size of the collection.
	 */
	public int getSize() {
		return mMessages.size();
	}

	/**
	 * This method returns true if there are messages waiting to be written to 
	 * the Summary Log, and false if there aren't. If there are NO Messages in 
	 * the list waiting to be written tell the SummaryLogWriter Thraed to wait
	 * 
	 * @return boolean this method returns whether or not there are messages
	 *         in the queue
	 */
	public synchronized boolean hasMessages() {
		if (!(mMessages.size() > 0)) {
			// If there aren't any messages queued to be written to the Summary 
			//  Log tell the SummaryLogWriter to wait until notified and set the 
			//  mSummaryLogWriterIsWaiting flag to true
			try {
				mLogWriterIsWaiting = true;
				while (mLogWriterIsWaiting) {
					wait();
				}
			} catch (InterruptedException ie) {
				System.out.println("Exception in " + "SummaryLogMessageCollection.hasMessages():" + ie);
			}
		}// end if

		return mMessages.size() > 0;
	}
}