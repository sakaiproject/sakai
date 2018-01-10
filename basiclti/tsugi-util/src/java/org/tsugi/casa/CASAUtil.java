/*
 * $URL$
 * $Id$
 *
 * Copyright (c) 2016- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.tsugi.casa;

public class CASAUtil {

	/*
	    $entry = new stdClass();
	    $entry->identity = new stdClass();
	    $entry->identity->product_instance_guid = $CFG->product_instance_guid;
	    $entry->identity->originator_id = $CFG->casa_originator_id;
	    // id an string unique to the app among all apps published by the publisher.
	    $entry->identity->id = $id;
	    $orig = new stdClass();
	    $orig->timestamp = "2015-01-02T22:17:00.371Z";
	    $orig->uri = $CFG->wwwroot;
	    $orig->share = true;
	    $orig->propagate = true;
	    $use = new stdClass();
	    $use->{"1f2625c2-615f-11e3-bf13-d231feb1dc81"} = $title;
	    $use->{"b7856963-4078-4698-8e95-8feceafe78da"} = $text;
	    // $use->{"d59e3a1f-c034-4309-a282-60228089194e"} = [{"name":"Paul Gray","email":"pfbgray@gmail.com"}],

	    if ( $icon !== false ) $use->{"d25b3012-1832-4843-9ecf-3002d3434155"} = $icon;
	    $launch = new stdClass();
	    $script = isset($REGISTER_LTI2['script']) ? $REGISTER_LTI2['script'] : "index.php";
	    $script = $CFG->wwwroot . '/' . str_replace("register.php", $script, $path);
	    $launch->launch_url = $script;
	    $launch->registration_url = $CFG->wwwroot . '/lti/register.php';
	    $use->{"f6820326-5ea3-4a02-840d-7f91e75eb01b"} = $launch;
	    $orig->use = $use;
	    $entry->original = $orig;

	    $output[] = $entry;
*/
	
	public static final String TITLE_SCHEMA = "1f2625c2-615f-11e3-bf13-d231feb1dc81";
	public static final String TITLE = "title";
	public static final String TEXT_SCHEMA = "b7856963-4078-4698-8e95-8feceafe78da";
	public static final String TEXT = "text";
	public static final String CONTACT_SCHEMA = "d59e3a1f-c034-4309-a282-60228089194e";
	public static final String CONTACT = "contact";
	public static final String CONTACT_NAME = "name";
	public static final String CONTACT_EMAIL = "email";
	public static final String LAUNCH_SCHEMA = "f6820326-5ea3-4a02-840d-7f91e75eb01b";
	public static final String LAUNCH = "launch";
	public static final String ICON_SCHEMA = "d25b3012-1832-4843-9ecf-3002d3434155";
	public static final String ICON = "icon_path";

	private static final String EMPTY_JSON_OBJECT = "{\n}\n";

}
