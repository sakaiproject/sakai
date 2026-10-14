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
package org.apache.myfaces.component;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;
import jakarta.el.MethodNotFoundException;
import jakarta.el.PropertyNotFoundException;
import jakarta.faces.component.StateHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.MethodBinding;

import org.apache.myfaces.shared_tomahawk.util.ClassUtils;

/**
 * Converts a MethodBinding to a MethodExpression
 * 
 * TODO: find a way to share the implementation of class with impl.
 * 
 * This class could be moved or changed in the future
 * 
 * @since 1.1.7
 * @author Stan Silvert
 */
@SuppressWarnings("deprecation")
public class MethodBindingToMethodExpression extends MethodExpression implements StateHolder
{
    private static final Class[] EXPECTED_TYPES = new Class[] { MethodBinding.class, StateHolder.class };

    private MethodBinding methodBinding;

    private boolean _transientFlag;

    private transient MethodInfo methodInfo;

    /**
     * No-arg constructor used during restoreState
     */
    public MethodBindingToMethodExpression()
    {
    }

    /** Creates a new instance of MethodBindingToMethodExpression */
    public MethodBindingToMethodExpression(MethodBinding methodBinding)
    {
        checkNullArgument(methodBinding, "methodBinding");
        this.methodBinding = methodBinding;
    }

    /**
     * Return the wrapped MethodBinding.
     */
    public MethodBinding getMethodBinding()
    {
        return methodBinding;
    }
    
    void setMethodBinding(MethodBinding methodBinding)
    {
        this.methodBinding = methodBinding;
    }

    /**
     * Note: MethodInfo.getParamTypes() may incorrectly return an empty class array if invoke() has not been called.
     * 
     * @throws IllegalStateException
     *             if expected params types have not been determined.
     */
    public MethodInfo getMethodInfo(ELContext context) throws PropertyNotFoundException, MethodNotFoundException,
            ELException
    {
        checkNullArgument(context, "elcontext");
        checkNullState(methodBinding, "methodBinding");

        if (methodInfo == null)
        {
            final FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
            if (facesContext != null)
            {
                methodInfo = invoke(new Invoker<MethodInfo>()
                {
                    public MethodInfo invoke()
                    {
                        return new MethodInfo(null, methodBinding.getType(facesContext), null);
                    }
                });
            }
        }
        return methodInfo;
    }

    public Object invoke(ELContext context, final Object[] params) throws PropertyNotFoundException,
            MethodNotFoundException, ELException
    {
        checkNullArgument(context, "elcontext");
        checkNullState(methodBinding, "methodBinding");
        final FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        if (facesContext != null)
        {
            return invoke(new Invoker<Object>()
            {
                public Object invoke()
                {
                    return methodBinding.invoke(facesContext, params);
                }
            });
        }
        return null;
    }

    public boolean isLiteralText()
    {
        if (methodBinding == null)
            throw new IllegalStateException("methodBinding is null");
        String expr = methodBinding.getExpressionString();
        return !(expr.startsWith("#{") && expr.endsWith("}"));
    }

    public String getExpressionString()
    {
        return methodBinding.getExpressionString();
    }

    public Object saveState(FacesContext context)
    {
        if (!isTransient())
        {
            if (methodBinding instanceof StateHolder)
            {
                Object[] state = new Object[2];
                state[0] = methodBinding.getClass().getName();
                state[1] = ((StateHolder) methodBinding).saveState(context);
                return state;
            }
            else
            {
                return methodBinding;
            }
        }
        return null;
    }

    public void restoreState(FacesContext context, Object state)
    {
        if (state instanceof MethodBinding)
        {
            methodBinding = (MethodBinding) state;
            methodInfo = null;
        }
        else if (state != null)
        {
            Object[] values = (Object[]) state;
            methodBinding = (MethodBinding) ClassUtils.newInstance(values[0].toString(), EXPECTED_TYPES);
            ((StateHolder) methodBinding).restoreState(context, values[1]);
            methodInfo = null;
        }
    }

    public void setTransient(boolean transientFlag)
    {
        _transientFlag = transientFlag;
    }

    public boolean isTransient()
    {
        return _transientFlag;
    }

    @Override
    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((methodBinding == null) ? 0 : methodBinding.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MethodBindingToMethodExpression other = (MethodBindingToMethodExpression) obj;
        if (methodBinding == null)
        {
            if (other.methodBinding != null)
                return false;
        }
        else if (!methodBinding.equals(other.methodBinding))
            return false;
        return true;
    }

    private void checkNullState(Object notNullInstance, String instanceName)
    {
        if (notNullInstance == null)
            throw new IllegalStateException(instanceName + " is null");
    }

    private void checkNullArgument(Object notNullInstance, String instanceName)
    {
        if (notNullInstance == null)
            throw new IllegalArgumentException(instanceName + " is null");
    }

    private <T> T invoke(Invoker<T> invoker)
    {
        try
        {
            return invoker.invoke();
        }
        catch (jakarta.faces.el.MethodNotFoundException e)
        {
            throw new MethodNotFoundException(e.getMessage(), e);
        }
        catch (EvaluationException e)
        {
            throw new ELException(e.getMessage(), e);
        }
    }

    private interface Invoker<T>
    {
        T invoke();
    }
}
