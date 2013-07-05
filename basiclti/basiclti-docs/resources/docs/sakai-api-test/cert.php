<?php

$lmsdata_common = array(
  "launch_presentation_locale" => "en_us",
  "tool_consumer_info_product_family_code" => "sakai-unit",
  "tool_consumer_info_version" => "0.9",
  "tool_consumer_instance_guid" => "lmsng.school.edu",
  "tool_consumer_instance_description" => "University of School (LMSng)",
);

$lmsdata_users = array(
    0 => array(
      "user_id" => "user-0016",
      "roles" => "Instructor",  
      // "lis_person_name_full" => 'Siân Instructor',
      "lis_person_name_family" => 'Instructor',
      "lis_person_name_given" => 'Siân',
      "lis_person_contact_email_primary" => "sian@imscert.org",
      "lis_person_sourcedid" => "school.edu:user",
	),
    1 => array(
      "user_id" => "user-0029",
      "roles" => "Learner", 
      // "lis_person_name_full" => 'John Student',
      "lis_person_name_family" => 'Student',
      "lis_person_name_given" => 'John',
      "lis_person_contact_email_primary" => "john@imscert.org",
      "lis_person_sourcedid" => "school.edu:john",
	)
);

$lmsdata_courses = array(
    0 => array(
      "context_id" => "cid-00113",
      "context_title" => "Design of Personal Environments 1",
      "context_label" => "SI106",
	),
	1 => array(
      "context_id" => "cid-00213",
      "context_title" => "Design of Personal Environments 2",
      "context_label" => "SI206",
	)
);

$lmsdata_resources = array(
	// First course
    0 => array(
      "resource_link_id" => "res-0012612",
      "resource_link_title" => "My Weekly Wiki",
      "resource_link_description" => "This learning space is private",
      // "resource_link_title" => "My <em>Weekly</em> Wiki",
      // "resource_link_description" => "This learning space is <strong>private</strong>",
      ),
	1 => array(
      "resource_link_id" => "res-0028378",
      "resource_link_title" => "Prescribed text",
      "resource_link_description" => "This textbook is to accompany the course",
      ),
	// Second course
	2 => array(
      "resource_link_id" => "res-003372",
      "resource_link_title" => "My Learning Diary",
      "resource_link_description" => "Record your activities within this course",
      )
);

$lmsdata_tmp = array(
	0 => array_merge( 
		$lmsdata_common, $lmsdata_users[0], $lmsdata_courses[0], $lmsdata_resources[0],
		array("ext_note" => "Instructor from first course") ),
	1 => array_merge( 
		$lmsdata_common, $lmsdata_users[0], $lmsdata_courses[0], $lmsdata_resources[1],
		array("ext_note" => "Instructor from first course second resource") ),
	2 => array_merge( 
		$lmsdata_common, $lmsdata_users[0], $lmsdata_courses[1], $lmsdata_resources[2],
		array("ext_note" => "Instructor from second course") ),
	3 => array_merge( 
		$lmsdata_common, $lmsdata_users[1], $lmsdata_courses[1], $lmsdata_resources[2],
		array("ext_note" => "Learner from second course") ),
	// Copies for less privacy
	4 => array_merge( 
		$lmsdata_common, $lmsdata_users[1], $lmsdata_courses[1], $lmsdata_resources[2],
		array("ext_note" => "Learner from second course minus name") ),
	5 => array_merge( 
		$lmsdata_common, $lmsdata_users[1], $lmsdata_courses[1], $lmsdata_resources[2],
		array("ext_note" => "Learner from second course minus email") ),
	6 => array_merge( 
		$lmsdata_common, $lmsdata_users[1], $lmsdata_courses[1], $lmsdata_resources[2],
		array("ext_note" => "Learner from second course no info") ),
);

// Remove name from [4]
unset($lmsdata_tmp[4]["lis_person_name_family"]);
unset($lmsdata_tmp[4]["lis_person_name_given"]);

// Remove email from [5]
unset($lmsdata_tmp[5]["lis_person_contact_email_primary"]);

// Remove all from [6]
unset($lmsdata_tmp[6]["lis_person_name_family"]);
unset($lmsdata_tmp[6]["lis_person_name_given"]);
unset($lmsdata_tmp[6]["lis_person_contact_email_primary"]);

$lmsdata_cert = array();
foreach($lmsdata_tmp as $k => $v) {
	ksort($v);
	$lmsdata_cert[$k] = $v;
}
