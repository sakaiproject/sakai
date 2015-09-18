/**
 * Copyright © 2005, CARET, University of Cambridge
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
/*
 * Created on 1 Sep 2008
 */
package org.sakaiproject.rsf.errors;

import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageException;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.state.support.ErrorStateManager;
import uk.org.ponder.util.RunnableInvoker;
import uk.org.ponder.util.UniversalRuntimeException;

public class ExplodingWrapper implements RunnableInvoker {

	private ErrorStateManager errorStateManager;

	private ErrorFilter errorFilter;

	public void setErrorStateManager(ErrorStateManager errorStateManager) {
		this.errorStateManager = errorStateManager;
	}

	public void setErrorFilter(ErrorFilter errorFilter) {
		this.errorFilter = errorFilter;
	}

	public void invokeRunnable(Runnable torun) {
		TargettedMessageList tml = errorStateManager.getTargettedMessageList();
		for (int i = 0; i < tml.size(); ++i) {
			TargettedMessage message = tml.messageAt(i);
			if (errorFilter.matchIgnores(message.messagecodes))
				continue;
			if (message.exception != null) {
				Throwable nested = message.exception instanceof UniversalRuntimeException
						? ((UniversalRuntimeException) message.exception).getTargetException() : message.exception;
				if (nested instanceof TargettedMessageException) {
					if (errorFilter
							.matchIgnores(((TargettedMessageException) nested).getTargettedMessage().messagecodes)) {
						continue;
					}
				}
				throw UniversalRuntimeException.accumulate(message.exception);
			}
		}
		torun.run();
	}

}
