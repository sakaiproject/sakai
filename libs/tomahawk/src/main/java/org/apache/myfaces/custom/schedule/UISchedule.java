/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.custom.schedule;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

import jakarta.el.MethodExpression;
import jakarta.el.ValueExpression;
import jakarta.faces.component.ActionSource2;
import jakarta.faces.component.PartialStateHolder;
import jakarta.faces.component.StateHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.MethodBinding;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.PhaseId;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFComponent;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFProperty;
import org.apache.myfaces.component.AttachedDeltaWrapper;
import org.apache.myfaces.component.MethodBindingToMethodExpression;
import org.apache.myfaces.component.MethodExpressionToMethodBinding;
import org.apache.myfaces.custom.schedule.model.ScheduleDay;
import org.apache.myfaces.custom.schedule.model.ScheduleEntry;

/**
 * This class contains all 'interactive' stuff for the Schedule component, meaning
 * actions and actionListeners.
 * 
 * @author Jurgen Lust
 * @version $Revision: 736908 $
 */
@JSFComponent
public class UISchedule extends org.apache.myfaces.custom.schedule.UIScheduleBase implements
        Serializable, ActionSource2
{
    public static final String COMPONENT_TYPE = "org.apache.myfaces.UISchedule";
    
    private class ScheduleActionListener implements ActionListener
    {
        //~ Methods ------------------------------------------------------------

        /**
         * @see jakarta.faces.event.ActionListener#processAction(jakarta.faces.event.ActionEvent)
         */
        public void processAction(ActionEvent event)
                throws AbortProcessingException
        {
            UISchedule schedule = (UISchedule) event.getComponent();
            ScheduleEntry entry = schedule.getSubmittedEntry();
            schedule.getModel().setSelectedEntry(entry);
            schedule.setSubmittedEntry(null);
        }
    }
    
    private static final long serialVersionUID = -8333458172939036755L;
    //private MethodBinding _action;
    private MethodBinding _actionListener;
    private ScheduleActionListener _scheduleListener;
    private ScheduleEntry _submittedEntry;
    //private Date _lastClickedDateAndTime = null;
    



    public UISchedule()
    {
        super();
        _scheduleListener = new ScheduleActionListener();
    }

    public void addActionListener(ActionListener listener)
    {
        addFacesListener(listener);
    }

    
    /**
     * @see jakarta.faces.component.UIComponent#broadcast(jakarta.faces.event.FacesEvent)
     */
    public void broadcast(FacesEvent event) throws AbortProcessingException
    {
        FacesContext context = getFacesContext();
        //invoke the mouselistener first
        if (event instanceof ScheduleMouseEvent)
        {
            ScheduleMouseEvent mouseEvent = (ScheduleMouseEvent) event;
            MethodBinding mouseListener = getMouseListener();

            if (mouseListener != null)
            {
                mouseListener.invoke(context,
                        new Object[] { mouseEvent });
            }
        }
        
        //then invoke private ScheduleActionListener for set
        //the selected entry (if exists), so other
        //listeners can retrieve it from getSelectedEntry.
        if (event.isAppropriateListener(_scheduleListener))
        {
            event.processListener(_scheduleListener);
        }

        //then invoke any other listeners
        super.broadcast(event);

        if (event instanceof ActionEvent)
        {
            //Call registered actionListener if applies
            MethodBinding actionListener = getActionListener();
    
            if (actionListener != null)
            {
                actionListener.invoke(context, new Object[] { event });
            }
            
            //Since UISchedule is an ActionSource component,
            //we should call to the application actionListener
            //when an ActionEvent happens.
            ActionListener defaultActionListener = context.getApplication()
                .getActionListener();
            if (defaultActionListener != null)
            {
                defaultActionListener.processAction((ActionEvent) event);
            }
        }
    }

    /**
     * Find the entry with the given id
     *
     * @param id the id
     *
     * @return the entry
     */
    protected ScheduleEntry findEntry(String id)
    {
        if (id == null)
        {
            return null;
        }

        for (Iterator dayIterator = getModel().iterator(); dayIterator
                .hasNext();)
        {
            ScheduleDay day = (ScheduleDay) dayIterator.next();

            for (Iterator iter = day.iterator(); iter.hasNext();)
            {
                ScheduleEntry entry = (ScheduleEntry) iter.next();

                if (id.equals(entry.getId()))
                {
                    return entry;
                }
            }
        }

        return null;
    }
    
    /**
     * @deprecated Use setActionExpression instead.
     */
    public void setAction(MethodBinding action)
    {
        if(action != null)
        {
            setActionExpression(new MethodBindingToMethodExpression(action));
        } 
        else
        {
            setActionExpression(null);
        }
    }

    /**
     * @deprecated Use getActionExpression() instead.
     */
    public MethodBinding getAction()
    {
        MethodExpression actionExpression = getActionExpression();
        if (actionExpression instanceof MethodBindingToMethodExpression) {
            return ((MethodBindingToMethodExpression)actionExpression).getMethodBinding();
        }
        if(actionExpression != null)
        {
            return new MethodExpressionToMethodBinding(actionExpression);
        }
        return null;
    }
    
    // Property: actionExpression
    private MethodExpression _actionExpression;

    /**
     * Gets Specifies the action to take when this command is invoked.
     *         If the value is an expression, it is expected to be a method
     *         binding EL expression that identifies an action method. An action method
     *         accepts no parameters and has a String return value, called the action
     *         outcome, that identifies the next view displayed. The phase that this
     *         event is fired in can be controlled via the immediate attribute.
     * 
     *         If the value is a string literal, it is treated as a navigation outcome
     *         for the current view.  This is functionally equivalent to a reference to
     *         an action method that returns the string literal.
     *
     * @return  the new actionExpression value
     */
    @JSFProperty(
        returnSignature="java.lang.String",
       jspName="action")
    public MethodExpression getActionExpression()
    {
      if (_actionExpression != null)
      {
        return _actionExpression;
      }
      ValueExpression expression = getValueExpression("actionExpression");
      if (expression != null)
      {
        return (MethodExpression)expression.getValue(getFacesContext().getELContext());
      }
      return null;
    }

    /**
     * Sets Specifies the action to take when this command is invoked.
     *         If the value is an expression, it is expected to be a method
     *         binding EL expression that identifies an action method. An action method
     *         accepts no parameters and has a String return value, called the action
     *         outcome, that identifies the next view displayed. The phase that this
     *         event is fired in can be controlled via the immediate attribute.
     * 
     *         If the value is a string literal, it is treated as a navigation outcome
     *         for the current view.  This is functionally equivalent to a reference to
     *         an action method that returns the string literal.
     * 
     * @param actionExpression  the new actionExpression value
     */
    public void setActionExpression(MethodExpression actionExpression)
    {
      this._actionExpression = actionExpression;
      if (initialStateMarked())
      {
          getStateHelper().put(PropertyKeys.actionExpressionSet,Boolean.TRUE);
      }
    }
    
    private boolean _isSetActionExpression()
    {
        Boolean value = (Boolean) getStateHelper().get(PropertyKeys.actionExpressionSet);
        return value == null ? false : value;
    }
    
    // Property: mouseListenerExpression
    private MethodExpression _mouseListenerExpression;

    /**
     *   
     * @return
     */
    @JSFProperty(
        returnSignature="void",
        methodSignature="org.apache.myfaces.custom.schedule.ScheduleMouseEvent",
        stateHolder=true,
        jspName="mouseListener")
    public MethodExpression getMouseListenerExpression()
    {
      if (_mouseListenerExpression != null)
      {
        return _mouseListenerExpression;
      }
      ValueExpression expression = getValueExpression("mouseListenerExpression");
      if (expression != null)
      {
        return (MethodExpression)expression.getValue(getFacesContext().getELContext());
      }
      return null;
    }

    public void setMouseListenerExpression(MethodExpression mouseListenerExpression)
    {
      this._mouseListenerExpression = mouseListenerExpression;
      if (initialStateMarked())
      {
          getStateHelper().put(PropertyKeys.mouseListenerExpressionSet,Boolean.TRUE);
      }
    }    
    
    /*
    public MethodBinding getAction()
    {
        return _action;
    }*/

    /**
     * 
     */
    @JSFProperty(
        returnSignature="void",
        methodSignature="jakarta.faces.event.ActionEvent")
    public MethodBinding getActionListener()
    {
        return _actionListener;
    }

    public ActionListener[] getActionListeners()
    {
        return (ActionListener[]) getFacesListeners(ActionListener.class);
    }

    /**
     * The last date and time of day that was clicked. This is set when
     * submitOnClick is true, and the schedule is clicked by the user.
     * 
     * @return the last clicked date and time
     */
    @JSFProperty(tagExcluded = true)        
    public Date getLastClickedDateAndTime()
    {
        return (Date) getStateHelper().get(PropertyKeys.lastClickedDateAndTime);
    }
    
    /**
     * @deprecated Use setMouseListenerExpression instead.
     */
    public void setMouseListener(MethodBinding mouseListener)
    {
        if(mouseListener != null)
        {
            setMouseListenerExpression(new MethodBindingToMethodExpression(mouseListener));
        } 
        else
        {
            setMouseListenerExpression(null);
        }
    }

    /**
     *   
     * @return the method binding to the mouse listener method
     */
    public MethodBinding getMouseListener()
    {
        MethodExpression mouseListenerExpression = getMouseListenerExpression();
        if (mouseListenerExpression instanceof MethodBindingToMethodExpression) {
            return ((MethodBindingToMethodExpression)mouseListenerExpression).getMethodBinding();
        }
        if(mouseListenerExpression != null)
        {
            return new MethodExpressionToMethodBinding(mouseListenerExpression);
        }
        return null;
    }

    /**
     * @return the submittedEntry
     */
    public ScheduleEntry getSubmittedEntry()
    {
        return _submittedEntry;
    }

    /**
     * @see jakarta.faces.component.UIComponent#queueEvent(jakarta.faces.event.FacesEvent)
     */
    public void queueEvent(FacesEvent event)
    {
        if (event instanceof ActionEvent || event instanceof ScheduleMouseEvent)
        {
            if (isImmediate())
            {
                event.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
            }
            else
            {
                event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            }
        }

        super.queueEvent(event);
    }

    public void removeActionListener(ActionListener listener)
    {
        removeFacesListener(listener);
    }

    /**
     * This method is invoked at the beginning of the restore view phase,
     * resetting all mouse event variables that were left from the previous
     * request
     */
    protected void resetMouseEvents()
    {
        getStateHelper().put(PropertyKeys.lastClickedDateAndTime, null);
    }

    /**
     * @see org.apache.myfaces.custom.schedule.UIScheduleBase#restoreState(jakarta.faces.context.FacesContext, java.lang.Object)
     */
    public void restoreState(FacesContext facesContext, Object state)
    {
        if (state == null)
        {
            return;
        }
        
        Object[] values = (Object[])state;
        super.restoreState(facesContext,values[0]);
        if (values[1] instanceof AttachedDeltaWrapper)
        {
            //Delta
            ((StateHolder)_actionListener).restoreState(facesContext, ((AttachedDeltaWrapper) values[1]).getWrappedStateObject());
        }
        else
        {
            //Full
            _actionListener = (jakarta.faces.el.MethodBinding) restoreAttachedState(facesContext,values[1]);
        }         
        if (values[2] instanceof AttachedDeltaWrapper)
        {
            //Delta
            ((StateHolder)_actionExpression).restoreState(facesContext, ((AttachedDeltaWrapper) values[2]).getWrappedStateObject());
        }
        else
        {
            //Full
            _actionExpression = (jakarta.el.MethodExpression) restoreAttachedState(facesContext,values[2]);
        }         
        if (values[3] instanceof AttachedDeltaWrapper)
        {
            //Delta
            ((StateHolder)_mouseListenerExpression).restoreState(facesContext, ((AttachedDeltaWrapper) values[3]).getWrappedStateObject());
        }
        else
        {
            //Full
            _mouseListenerExpression = (jakarta.el.MethodExpression) restoreAttachedState(facesContext,values[3]);
        }         
    }
    
    /**
     * @see org.apache.myfaces.custom.schedule.UIScheduleBase#saveState(jakarta.faces.context.FacesContext)
     */
    public Object saveState(FacesContext facesContext)
    {
        if (initialStateMarked())
        {
            boolean nullDelta = true;
            Object parentSaved = super.saveState(facesContext);
            Object actionListenerSaved = null;
            if (!_isSetActionListener() &&
                _actionListener != null && _actionListener instanceof PartialStateHolder)
            {
                //Delta
                StateHolder holder = (StateHolder) _actionListener;
                if (!holder.isTransient())
                {
                    Object attachedState = holder.saveState(facesContext);
                    if (attachedState != null)
                    {
                        nullDelta = false;
                    }
                    actionListenerSaved = new AttachedDeltaWrapper(_actionListener.getClass(),
                        attachedState);
                }
            }
            else  if (_isSetActionListener() || _actionListener != null)
            {
                //Full
                actionListenerSaved = saveAttachedState(facesContext,_actionListener);
                nullDelta = false;
            }        
            Object actionExpressionSaved = null;
            if (!_isSetActionExpression() &&
                _actionExpression != null && _actionExpression instanceof PartialStateHolder)
            {
                //Delta
                StateHolder holder = (StateHolder) _actionExpression;
                if (!holder.isTransient())
                {
                    Object attachedState = holder.saveState(facesContext);
                    if (attachedState != null)
                    {
                        nullDelta = false;
                    }
                    actionExpressionSaved = new AttachedDeltaWrapper(_actionExpression.getClass(),
                        attachedState);
                }
            }
            else  if (_isSetActionExpression() || _actionExpression != null)
            {
                //Full
                actionExpressionSaved = saveAttachedState(facesContext,_actionExpression);
                nullDelta = false;
            }        
            Object mouseListenerExpressionSaved = null;
            if (!_isSetActionListener() &&
                _mouseListenerExpression != null && _mouseListenerExpression instanceof PartialStateHolder)
            {
                //Delta
                StateHolder holder = (StateHolder) _mouseListenerExpression;
                if (!holder.isTransient())
                {
                    Object attachedState = holder.saveState(facesContext);
                    if (attachedState != null)
                    {
                        nullDelta = false;
                    }
                    mouseListenerExpressionSaved = new AttachedDeltaWrapper(_mouseListenerExpression.getClass(),
                        attachedState);
                }
            }
            else  if (_isSetActionListener() || _mouseListenerExpression != null)
            {
                //Full
                mouseListenerExpressionSaved = saveAttachedState(facesContext,_mouseListenerExpression);
                nullDelta = false;
            }        
            if (parentSaved == null && nullDelta)
            {
                //No values
                return null;
            }
            
            Object[] values = new Object[4];
            values[0] = parentSaved;
            values[1] = actionListenerSaved;
            values[2] = actionExpressionSaved;
            values[3] = mouseListenerExpressionSaved;
            return values;
        }
        else
        {
            Object[] values = new Object[4];
            values[0] = super.saveState(facesContext);
            values[1] = saveAttachedState(facesContext, _actionListener);
            values[2] = saveAttachedState(facesContext, _actionExpression);
            values[3] = saveAttachedState(facesContext, _mouseListenerExpression);
            return values;
        }
    }

    

    public void setActionListener(MethodBinding actionListener)
    {
        this._actionListener = actionListener;
        if (initialStateMarked())
        {
            getStateHelper().put(PropertyKeys.actionListenerSet,Boolean.TRUE);
        }
    }

    private boolean _isSetActionListener()
    {
        Boolean value = (Boolean) getStateHelper().get(PropertyKeys.actionListenerSet);
        return value == null ? false : value;
    }
    
    /**
     * The last date and time of day that was clicked. This is set when
     * submitOnClick is true, and the schedule is clicked by the user.
     * 
     * @return the last clicked date and time
     */
    protected void setLastClickedDateAndTime(Date lastClickedDateAndTime)
    {
        getStateHelper().put(PropertyKeys.lastClickedDateAndTime, lastClickedDateAndTime);
    }


    /**
     * @param submittedEntry the submittedEntry to set
     */
    protected void setSubmittedEntry(ScheduleEntry submittedEntry)
    {
        this._submittedEntry = submittedEntry;
    }
    
    protected enum PropertyKeys
    {
        lastClickedDateAndTime
        , actionExpressionSet
        , actionListenerSet
        , mouseListenerExpressionSet
    }
}
