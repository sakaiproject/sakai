<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> 
<html>
    <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
      <meta http-equiv="Content-Style-Type" content="text/css" />
      <%= request.getAttribute("sakai.html.head") %>
      <title>Sakai Search Title</title>
    </head>
    <body 
    onload="<%= request.getAttribute("sakai.html.body.onload") %> parent.updCourier(doubleDeep, ignoreCourier); callAllLoaders();" 
    >
      <table border="0" cellpadding="0" cellspacing="0" width="100%" class="toolTitle" summary="layout">
	<tr>
	  <td class="title">
	    Search			
	  </td>
	  <td class="action" id="pageName">
	  </td>
	</tr>
      </table>
  </body>
</html>
