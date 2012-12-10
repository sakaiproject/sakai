<?php

function getAddStructure() {

  $cur_url = curPageURL();
  $endpoint = str_replace("ext/lori_json.php","tool.php",$cur_url);

$addStructureCore = array(
  'resources' => array(
     array(
      'type' => 'folder',
      'name' => 'Pre-Work Folder',
      'resources' => array(
        array(
          'type' => 'lti',
          'name' => 'Pre-Test',
          'launch' => $endpoint,
		  'custom' => array (
            'lor_resource_id' => '4fa81c849378a'
          ) , 
          'iconUrl' => 'https://lti-lor.org/img/globe.png' 
        ), 
        array(
          'type' => 'lti',
          'name' => 'Welcome Video',
          'launch' => $endpoint,
		  'custom' => array (
            'lor_resource_id' => '2984329fd98df98'
          ) , 
          'iconUrl' => 'https://lti-lor.org/img/arrow.png' 
        )
      ),
    ),
    array(
      'type' => 'lti',
      'name' => 'Final',
      'launch' => $endpoint,
      'custom' => array (
        'lor_resource_id' => '543222058dge83'
      ) , 
      'iconUrl' => 'https://lti-lor.org/img/bell.png'
    )
  )
);

  return $addStructureCore;
}
