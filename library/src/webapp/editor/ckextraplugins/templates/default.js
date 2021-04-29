/*
Copyright (c) 2003-2011, CKSource - Frederico Knabben. All rights reserved.
For licensing, see LICENSE.html or http://ckeditor.com/license
*/
CKEDITOR.addTemplates('customtemplates', {imagesPath: CKEDITOR.getUrl(CKEDITOR.basePath.substr(0, CKEDITOR.basePath.indexOf("ckeditor/"))+"../editor/ckextraplugins/" + 'templates/images/'), templates: [
        {
            title:'<span class="fa fa-id-card template-icon" style="color:var(--sakai-color-blue--lighter-2);"></span>Instructor Insight Panel',
            description:'Panel box with photo where you can share a personal message.',
            html:`
                <style type="text/css">
                    *.panel-insight-speaker { 
                        display: block; 
                        max-width: 130.0px; 
                        max-height: 130.0px; 
                        width: auto; 
                        height: auto; 
                        margin-right: 10.0px; 
                        float: left;
                    } 
                    *.panel-insight-title { 
                        margin: 0 0 0 0; 
                    } 
                </style> 
                <div class="panel">
                    <div class="panel-heading sakai-colorize--sakai-color-blue--lighter-4">
                        <h3 class="panel-insight-title">Professor Last Name&#39;s Insight</h3>
                        <p>Subtitle</p>
                    </div>
                    <div class="panel-body sakai-colorize--sakai-color-gray--lighter-7">
                        <img alt="Instructors Photo" class="panel-insight-speaker" src="/library/image/genericProf.png" />
                        <span class="fa fa-quote-left"></span> Replace this text, the title, the subtitle, and the picture to create a personal message you would like to communicate to your students. It should be no more than a short paragraph or two. An image size of <b>130px x 130px</b> would fit best with this template.<span class="fa fa-quote-right"></span>
                        <span></span>
                    </div>
                    <div class="panel-body sakai-colorize--sakai-color-gray--lighter-7"><em>Here&#39;s an extra bit of instruction you can optionally add.</em></div>
                </div>
                `
        },
        {
            title:'<span class="fa fa-comment template-icon" style="color:var(--sakai-color-green--lighter-2);"></span>Instructor Insight Conversation',
            description:'Text message styled box with photo on the left.',
            html:`
                <style type="text/css">
                    *.insight-section ul li {
                        list-style: none;
                        margin-top: 10.0px;
                    }
                    *.insight-section ul {
                        padding: 0.0px;
                    }
                    *.left-insight img,
                    *.right-insight img {
                        width: 70.0px;
                        height: 70.0px;
                        float: left;
                        margin: 0.0px 5.0px;
                        border-radius: 50.0%;
                    }
                    *.right-insight img {
                        float: right;
                    }
                    *.left-insight,
                    *.right-insight {
                        overflow: hidden;
                    }
                    *.left-insight p,
                    *.right-insight p {
                        padding: 10.0px;
                        border-radius: 5.0px;
                        float: left;
                        width: 60.0%;
                        margin-bottom: 20.0px;
                        margin-left: 0.0px;
                    }
                    *.right-insight p {
                        float: right;
                        margin-right: 2.0px;
                    } 
                </style>
                <div class="insight-section">
                    <ul>
                        <li>
                            <div class="left-insight">
                                <img alt="Instructor speaking" src="/library/image/genericProf.png" />
                                <p class="sakai-colorize--sakai-color-green--lighter-4">Replace this text and photo to create a casual personal message you would like to communicate to your students. It should be no more than a short paragraph or two. An image size of <b>70px x 70px</b> would fit best with this template.</p>
                            </div>
                        </li>
                    </ul>
                </div>
                `
        },
        {
            title:'<span class="fa fa-comments template-icon" style="color:var(--sakai-color-purple--lighter-2);"></span>Instructor Insight Reply',
            description:'Text message styled box with photo on the right.',
            html:`
                <style type="text/css">
                *.insight-section ul li {
                    list-style: none;
                    margin-top: 10.0px;
                }
                *.insight-section ul {
                    padding: 0.0px;
                }
                *.left-insight img,
                *.right-insight img {
                    width: 70.0px;
                    height: 70.0px;
                    float: left;
                    margin: 0.0px 5.0px;
                    border-radius: 50.0%;
                }
                *.right-insight img,
                *.right-insight span {
                    float: right;
                }
                *.left-insight,
                *.right-insight {
                    overflow: hidden;
                }
                *.left-insight p,
                *.right-insight p {
                    padding: 10.0px;
                    border-radius: 5.0px;
                    float: left;
                    width: 60.0%;
                    margin-bottom: 20.0px;
                    margin-left: 0.0px;
                }
                *.right-insight p {
                    float: right;
                    margin-right: 2.0px;
                }
                </style>
                <div class="insight-section">
                    <ul>
                        <li>
                            <div class="right-insight">
                                <img alt="Student" src="/library/image/genericProf.png" />
                                <p class="sakai-colorize--sakai-color-purple--lighter-4">Replace this text and maybe the photo to create the effect of another side of a conversation or of a student responding to an instructor insight. It should be no more than a short paragraph or two. <b>An image size of 70px x 70px</b> would fit best with this template.</p>
                            </div>
                        </li>
                    </ul>
                </div>
                `
        },
        {
            title:'<span class="fa fa-lightbulb-o template-icon" style="color:var(--sakai-color-gold--lighter-2);"></span>Key Idea',
            description:'Yellow box with lightbulb and key idea text.',
            html:`
                <div class="alert sakai-colorize--sakai-color-gold--lighter-4">
                    <div style="float:left"><span class="fa fa-3x fa-lightbulb-o" style="margin-top:-10px;"></span></div>
                    <div style="margin-left:35.0px"><strong>Enter a statement that highlights a key idea.</strong></div>
                </div>
                `
        },
        {
            title:'<span class="fa fa-exclamation-triangle template-icon" style="color:var(--sakai-color-red--lighter-2);"></span>Warning',
            description:'Red panel box containing a warning message.',
            html:`
                <div class="panel">
                    <div class="panel-heading sakai-colorize--sakai-color-red--lighter-4">
                        <h3 class="callout-title"><span class="fa fa-exclamation-triangle"></span> Warning!</h3>
                    </div>
                    <div class="panel-body sakai-colorize--sakai-color-gray--lighter-7">Use this to give your students a very noticeable warning. Don&#39;t overuse these, or your students will stop noticing them. </div>
                </div>
                `
        },
        {
            title:'<span class="fa fa-star template-icon" style="color:var(--sakai-color-gold--lighter-2);"></span>Alert with Star',
            description:'Yellow box with a star and text',
            html:`
                <div class="alert sakai-colorize--sakai-color-gold--lighter-4">
                    <div style="float:left"><span class="fa fa-star" style="font-size:26.0px"></span></div>
                    <div style="margin-left:35.0px"><strong>Replace this with appropriate text to alert your students about something important.</strong></div>
                </div>
                `
        },
        {
            title:'<span class="fa fa-lightbulb-o template-icon" style="color:var(--sakai-text-color)"></span>Learning Outcomes',
            description:'Organized list of learning outcomes with icon',
            html:`
                <h2><span class="fa fa-fw fa-lightbulb-o" style="color:var(--sakai-text-color); font-size:25.0px"></span>&nbsp; Learning Outcomes</h2>
                <p style="margin-left:40.0px">After completing this module, students will be able to:</p>
                <ol>
                    <li style="margin-left: 40.0px;">Construct learning outcomes using Bloom&#39;s Taxonomy.</li>
                    <li style="margin-left: 40.0px;">Explain why each learning outcome must be measurable.</li>
                    <li style="margin-left: 40.0px;">Revise this list as the course content changes.</li>
                    <li style="margin-left: 40.0px;">State the contact information for E-Learning to get help with learning outcomes.</li>
                </ol>
                `
        },
        {
            title:'<span class="fa fa-file-text template-icon" style="color:var(--sakai-text-color);"></span>Assignment Task',
            description:'Instructions for an assignment',
            html:`
                <h3><span class="fa fa-file-text"></span>&nbsp;Assignment Title</h3>
                <p> <strong>Due:</strong> Thursday, July 5<br />
                    <strong>Estimated Time:</strong> 2.5 hours<br />
                    <strong>Value:</strong> 100 points
                </p>
                <h4>Instructions</h4>
                <p>Replace this with appropriate assignment instructions. Detail all expectations including content and format. Your goal should be to write instructions so clearly that students do not need to ask you questions. The assignment title may not be necessary if you are providing the title in a Section in the Lessons tool.&nbsp;Also&nbsp;in the Lessons tool, you can add a link to submit to an assignment immediately below this element.</p>
                `
        },
        {
            title:'<span class="fa fa-comments template-icon" style="color:var(--sakai-text-color);"></span>Discussion Forum Task',
            description:'Instructions for a discussion forum task',
            html:`
                <h3><span class="fa fa-comments"></span>&nbsp;Discussion Forum</h3>
                <p> 
                    <strong>Initial Post Due:</strong> Thursday, July 5<br />
                    <strong>Response Post Due:</strong> Sunday, July 8<br />
                    <strong>Estimated Time:</strong> 45 minutes<br />
                    <strong>Value:</strong> 20 points
                </p>
                <h4>Step 1</h4>
                <p>Post a response to the following prompt. Your contribution should be complete while staying under 100 words.</p>
                <p style="margin-left:40.0px"><strong>Prompt:</strong>&nbsp;All of the &quot;#1 Dad&quot; mugs in the world suddenly change to show the actual ranking of Dads. Project what effect this would have on society.</p>
                <h4>Step 2</h4>
                <p>Respond to two classmates\' posts by politely pointing out how their projections may be off-base.&nbsp;</p>
                <p>Replace all this text with the appropriate text for your discussion forum exercise. The Lessons tool allows you to link to a forum topic directly below this text.</p>
                `
        },
        {
            title:'<span class="fa fa-book template-icon" style="color:var(--sakai-text-color);"></span>Reading Assignment',
            description:'Instructions for a reading assignment',
            html:`
                <h3><span class="fa fa-book"></span>&nbsp;Reading Assignment</h3>
                <p>
                    <strong>Due:</strong> Thursday, July 5<br />
                    <strong>Estimated Time:</strong> 30 minutes
                </p>
                <h4>Instructions</h4>
                <p>Read chapters 1 and 2 from <em>Opening Up Education</em> by Iiyoshi and Kumar. As you read, pay close attention to how traditional models of education have become less appropriate, and consider what this means for the future.</p>
                `
        },
        {
            title:'<span class="fa fa-play-circle template-icon" style="color:var(--sakai-text-color);"></span>Video Assignment',
            description:'Instructions for a video assignment',
            html:`
                <h3><span class="fa fa-play-circle"></span>&nbsp;Video Assignment</h3>
                <p>
                    <strong>Due:</strong> Thursday, July 5<br />
                    <strong>Estimated Time:</strong> 30 minutes
                </p>
                <h4>Instructions</h4>
                <p>Watch the following video. The speaker presents a compelling case for why wind turbines should be abandoned as a primary municipal power source. You will be expected to reference this video in the upcoming assignments.</p>
                <p style="margin-left:40.0px"><em>Embed your video here using the video links in the editor tool bar.</em></p>
                `
        }
]});
