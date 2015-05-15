/**
 * For Publishing sites in Morpheus
 */

function publishSite(siteId) { 

  var reqUrl = '/direct/site/'+siteId+"/edit"; 
  var resp = $PBJQ.ajax({ 
    type: 'POST', 
    data: 'published=true', 
    url: reqUrl, 
    success: function() { location.reload(); } 
  }).responseText; 

}
