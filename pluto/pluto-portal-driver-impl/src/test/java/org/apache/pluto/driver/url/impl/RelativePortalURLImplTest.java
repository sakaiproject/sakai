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
package org.apache.pluto.driver.url.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import junit.framework.TestCase;

import org.apache.pluto.driver.url.PortalURLParameter;

public class RelativePortalURLImplTest extends TestCase
{
    private final String context = "/context";
    private final String servlet = "/ServletName";
    private final MockPortalUrlParser urlParser = new MockPortalUrlParser();
    
    private RelativePortalURLImpl underTest = null;
    
    protected void setUp() throws Exception
    {
        super.setUp();
        this.underTest = new RelativePortalURLImpl( context, servlet, urlParser );
    }
    
    public RelativePortalURLImplTest( String name )
    {
        super( name );
    }
    
    public void testRelativePortalURLImplContstructor()
    {
        RelativePortalURLImpl underTest = 
            new RelativePortalURLImpl( "somecontext", "servletname", new MockPortalUrlParser() );
        assertNotNull( underTest );
    }

    public void testSetRenderPath()
    {
        final String path = "foo";
        underTest.setRenderPath( path );
        assertEquals( path, underTest.getRenderPath() );
    }

    public void testAddParameter()
    {
        final PortalURLParameter paramOne = PortalURLParameterFactory.createPortalURLParameter();       
        underTest.addParameter( paramOne );
        
        final Collection params = underTest.getParameters();
        assertNotNull( params );
        assertEquals( 1, params.size() );
        assertTrue( params.contains( paramOne ) );
        
        final PortalURLParameter paramTwo = (PortalURLParameter)params.iterator().next();
        assertEquals( paramOne.getName(), paramTwo.getName() );
        assertEquals( paramOne.getValues()[0], paramTwo.getValues()[0] );
        assertEquals( paramOne.getWindowId(), paramTwo.getWindowId() );
    }
    
    public void testAddParameters()
    {
        final PortalURLParameter paramOne = PortalURLParameterFactory.createPortalURLParameter();
        final PortalURLParameter paramTwo = PortalURLParameterFactory.createPortalURLParameter();
        
        underTest.addParameter( paramOne );
        underTest.addParameter( paramTwo );
        
        final Collection params = underTest.getParameters();
        assertNotNull( params );
        assertEquals( 2, params.size() );
        assertTrue( params.contains( paramOne ) );
        assertTrue( params.contains( paramTwo ) );
        
        int matchCount = 0;
        Iterator itr = params.iterator();
        while ( itr.hasNext() )
        {            
            final PortalURLParameter param = (PortalURLParameter) itr.next();
            final String name = param.getName();
            final String value = param.getValues()[0];
            final String windowId = param.getWindowId();            
            assertNotNull( name );
            assertNotNull( value );            
            assertNotNull( windowId );
            
            PortalURLParameter expected = null;
            if ( windowId.equals( paramOne.getWindowId() ) )
            {
                expected = paramOne;
                matchCount++;
                itr.remove();
            }
            
            if ( windowId.equals( paramTwo.getWindowId() ) )
            {
                expected = paramTwo;
                matchCount++;
                itr.remove();
            }
            
            assertNotNull( expected );                            
            assertEquals( expected.getName(), name );
            assertEquals( expected.getValues()[0], value );            
        }
        
        assertEquals( 2, matchCount );
    }

    public void testGetParameters()
    {
        assertTrue( underTest.getParameters().isEmpty() );
        final PortalURLParameter expected = PortalURLParameterFactory.createPortalURLParameter();
        underTest.addParameter( expected );
        final Collection params = underTest.getParameters();
        assertNotNull( params );
        assertEquals( 1, params.size() );
        final PortalURLParameter param = (PortalURLParameter) params.iterator().next();
        assertEquals( expected.getName(), param.getName() );
        assertEquals( expected.getValues()[0], param.getValues()[0] );
        assertEquals( expected.getWindowId(), param.getWindowId() );
    }

    public void testSetActionWindow()
    {
        final String actionWindow = "actWin";
        underTest.setActionWindow( actionWindow );
        assertEquals( actionWindow, underTest.getActionWindow() );
    }

    public void testGetPortletModes()
    {
        final String win = "window";
        final PortletMode mode = PortletMode.VIEW;
        
        underTest.setPortletMode( win, mode );
        final Map modes = underTest.getPortletModes();
        assertNotNull( modes );
        assertEquals( 1, modes.size() );
        assertTrue( modes.containsKey( win ) );
        assertEquals( mode, modes.get( win ) );
    }

    public void testGetPortletMode()
    {
        final String win = "window";
        final PortletMode expected = PortletMode.VIEW;
        
        underTest.setPortletMode( win, expected );
        final PortletMode mode = underTest.getPortletMode( win );
        assertNotNull( mode );
        assertEquals( expected, mode );
    }

    public void testGetWindowStates()
    {
        final String win = "window";
        final WindowState state = WindowState.MAXIMIZED;
        
        underTest.setWindowState( win, state );
        final Map states = underTest.getWindowStates();
        assertNotNull( states );
        assertEquals( 1, states.size() );
        assertTrue( states.containsKey( win ) );
        assertEquals( state, states.get( win ) );            
    }

    public void testGetWindowState()
    {
        final String win = "window";
        final WindowState expected = WindowState.MAXIMIZED;
        
        underTest.setWindowState( win, expected );
        final WindowState state = underTest.getWindowState( win );
        assertNotNull( state );
        assertEquals( expected, state );
    }

    public void testClearParameters()
    {
        final PortalURLParameter param = PortalURLParameterFactory.createPortalURLParameter();        
        assertTrue( underTest.getParameters().isEmpty() );        
        underTest.addParameter( param );
        assertEquals( 1, underTest.getParameters().size() );
        underTest.clearParameters( param.getWindowId() );
        assertTrue( underTest.getParameters().isEmpty() );
    }

    public void testToString()
    {
        assertEquals( "aString", underTest.toString() );
    }

    public void testGetServletPath()
    {
        assertEquals( context + servlet, underTest.getServletPath() );
    }

    public void testClone()
    {
        final PortletMode mode = PortletMode.VIEW;
        final WindowState state = WindowState.MAXIMIZED;
        final PortalURLParameter param = PortalURLParameterFactory.createPortalURLParameter();
        final String renderPath = "foo";
        final String actionWin = param.getWindowId();
       
        // set the state of the portlet url
        underTest.setActionWindow( actionWin );
        underTest.setPortletMode( actionWin, mode );
        underTest.setRenderPath( renderPath );
        underTest.setWindowState( actionWin, state );
        underTest.addParameter( param );
        
        // verify the state
        assertEquals( actionWin, underTest.getActionWindow() );
        assertEquals( param, underTest.getParameters().iterator().next() );
        assertEquals( mode, underTest.getPortletMode( actionWin ) );
        assertEquals( state, underTest.getWindowState( actionWin ) );
        assertEquals( renderPath, underTest.getRenderPath() );   
        assertEquals( context + servlet, underTest.getServletPath() );
        
        // clone
        final RelativePortalURLImpl clone = (RelativePortalURLImpl)underTest.clone();
        assertNotNull( clone );
        
        // verify the state of the cloned object
        assertEquals( actionWin, clone.getActionWindow() );
        assertEquals( param, clone.getParameters().iterator().next() );
        assertEquals( mode, clone.getPortletMode( actionWin ) );
        assertEquals( state, clone.getWindowState( actionWin ) );
        assertEquals( renderPath, clone.getRenderPath() );
        assertEquals( underTest.getServletPath(), clone.getServletPath() );
        assertEquals( underTest.toString(), clone.toString() );
        
        
        // Per Object.clone() none of these conditions are
        // "absolute requirements"
        
        // x.clone() != x
        assertTrue( underTest != clone );
        
        // x.clone().getClass() == x.getClass()
        assertEquals( underTest.getClass(), clone.getClass() );
        
        // x.clone().equals(x) - currently false.         
        assertEquals( false, underTest.equals( clone ) );
    }
    
    private static class PortalURLParameterFactory
    {
        final static String windowId = "windowId";
        final static String paramName = "paramName";
        final static String paramValue = "paramValue";
        
        static int paramCount = 0;
        
        static PortalURLParameter createPortalURLParameter()
        {
            paramCount++;
            
            return new PortalURLParameter( windowId + paramCount, 
                    paramName + paramCount, 
                    paramValue + paramCount );
        }
    }
}
