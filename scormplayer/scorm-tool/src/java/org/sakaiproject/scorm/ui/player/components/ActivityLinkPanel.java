/**
 * Copyright (c) 2007-2019 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui.player.components;

import java.net.URLDecoder;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.adl.sequencer.SeqActivity;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.ThrottlingSettings;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.time.Duration;

import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.ResourceNavigator;
import org.sakaiproject.scorm.ui.UISynchronizerPanel;
import org.sakaiproject.scorm.ui.player.behaviors.ActivityAjaxEventBehavior;

/**
 *
 * @author bjones86
 */
@Slf4j
public class ActivityLinkPanel extends Panel
{
    @SpringBean
    LearningManagementSystem lms;

    @SpringBean( name = "org.sakaiproject.scorm.service.api.ScormSequencingService" )
    ScormSequencingService sequencingService;

    protected UISynchronizerPanel synchronizer;

    @SpringBean( name = "org.sakaiproject.scorm.service.api.ScormResourceService" )
    ScormResourceService resourceService;

    public ActivityLinkPanel( String id, IModel<SeqActivityNode> imodel, SessionBean sessionBean, UISynchronizerPanel synchronizer, ActivityTreePanel treePanel )
    {
        super( id );
        this.synchronizer = synchronizer;

        ActivityAjaxLink link = new ActivityAjaxLink( "link" )
        {
            @Override
            public void onClick( AjaxRequestTarget target )
            {
                SeqActivity seqActivity = imodel.getObject().getSeqActivity();
                log.debug( "ID: {}  State ID: {}", seqActivity.getID(), seqActivity.getStateID() );

                sequencingService.navigateToActivity( seqActivity.getID(), sessionBean, new LocalResourceNavigator(), target );

                if( synchronizer != null )
                {
                    synchronizer.synchronizeState( sessionBean, target );
                }

                treePanel.updateTree( sessionBean.getActivityId(), target );
            }
        };

        Optional<String> selectedID = treePanel.getSelectedID();
        if( selectedID.isPresent() )
        {
            String selectedNodeID = selectedID.get();
            if( selectedNodeID.equals( imodel.getObject().getSeqActivity().getID()))
            {
                link.add( AttributeModifier.append( "class", "selected" ) );
            }
        }
        add( link );

        SeqActivity seqActivity = imodel.getObject().getSeqActivity();
        String text = "";
        try
        {
            text = (null == seqActivity) ? "" : URLDecoder.decode( seqActivity.getTitle(), "UTF-8" );
        }
        catch( Exception e )
        {
            log.error( "Caught exception ", e );
        }

        add( link.setBody( Model.of( text ) ) );
    }

    public class LocalResourceNavigator extends ResourceNavigator
    {
        private static final long serialVersionUID = 1L;

        @Override
        protected ScormResourceService resourceService()
        {
            return ActivityLinkPanel.this.resourceService;
        }

        @Override
        public Component getFrameComponent()
        {
            if( synchronizer != null )
            {
                return synchronizer.getContentPanel();
            }

            return null;
        }

        @Override
        public boolean useLocationRedirect()
        {
            return false;
        }
    }

    public abstract class ActivityAjaxLink extends AjaxLink
    {
        private static final String ARIA_TREEITEM_ROLE = "wairole:treeitem";

        /**
         * Construct.
         *
         * @param id
         */
        public ActivityAjaxLink( final String id )
        {
            this( id, null );
        }

        /**
         * Construct.
         *
         * @param id
         * @param model
         */
        public ActivityAjaxLink( final String id, final IModel model )
        {
            super( id, model );

            add( new ActivityAjaxEventBehavior( "onclick", lms.canUseRelativeUrls() )
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onEvent( AjaxRequestTarget target )
                {
                    onClick( target );
                }

                @Override
                protected void onComponentTag( ComponentTag tag )
                {
                    // add the onclick handler only if link is enabled
                    if( isEnabledInHierarchy() )
                    {
                        super.onComponentTag( tag );
                    }
                }

                @Override
                protected void updateAjaxAttributes( AjaxRequestAttributes attributes )
                {
                    super.updateAjaxAttributes( attributes );
                    attributes.setThrottlingSettings( new ThrottlingSettings( id, Duration.milliseconds( 50 ), true ) );
                }
            } );

            add( new AttributeModifier( "role", new Model( ARIA_TREEITEM_ROLE ) ) );
        }
    }
}
