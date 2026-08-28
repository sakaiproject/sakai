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
package org.apache.myfaces.custom.facelets.tag;

import java.io.Serializable;

import jakarta.el.ELException;
import jakarta.el.MethodExpression;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.MethodBinding;
import jakarta.faces.el.MethodNotFoundException;

public final class LegacyMethodBinding extends MethodBinding implements
        Serializable
{

    private static final long serialVersionUID = 1L;

    private final MethodExpression m;

    public LegacyMethodBinding(MethodExpression m)
    {
        this.m = m;
    }

    public Class getType(FacesContext context) throws MethodNotFoundException
    {
        try
        {
            return m.getMethodInfo(context.getELContext()).getReturnType();
        }
        catch (jakarta.el.MethodNotFoundException e)
        {
            throw new MethodNotFoundException(e.getMessage(), e.getCause());
        }
        catch (ELException e)
        {
            throw new EvaluationException(e.getMessage(), e.getCause());
        }
    }

    public Object invoke(FacesContext context, Object[] params)
            throws EvaluationException, MethodNotFoundException
    {
        try
        {
            return m.invoke(context.getELContext(), params);
        }
        catch (jakarta.el.MethodNotFoundException e)
        {
            throw new MethodNotFoundException(e.getMessage(), e.getCause());
        }
        catch (ELException e)
        {
            throw new EvaluationException(e.getMessage(), e.getCause());
        }
    }

    public String getExpressionString()
    {
        return m.getExpressionString();
    }
}
