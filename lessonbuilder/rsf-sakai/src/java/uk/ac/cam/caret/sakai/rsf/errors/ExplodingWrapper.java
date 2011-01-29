/*
 * Created on 1 Sep 2008
 */
package uk.ac.cam.caret.sakai.rsf.errors;

import uk.org.ponder.arrayutil.ArrayUtil;
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
      if (errorFilter.matchIgnores(message.messagecodes)) continue;
      if (message.exception != null) {
        Throwable nested = message.exception instanceof UniversalRuntimeException? 
            ((UniversalRuntimeException)message.exception).getTargetException() : message.exception;
        if (nested instanceof TargettedMessageException) {
          if (errorFilter.matchIgnores(((TargettedMessageException)nested).getTargettedMessage().messagecodes)) {
            continue;
          }
        }
        throw UniversalRuntimeException.accumulate(message.exception);
      }
    }
    torun.run();
  }

}
