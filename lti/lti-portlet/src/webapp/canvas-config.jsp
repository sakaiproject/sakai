<%@ page language="java" contentType="text/xml; charset=UTF-8" %><?xml version="1.0" encoding="UTF-8"?>
<cartridge_basiclti_link xmlns="http://www.imsglobal.org/xsd/imslticc_v1p0" xmlns:blti="http://www.imsglobal.org/xsd/imsbasiclti_v1p0" xmlns:lticm="http://www.imsglobal.org/xsd/imslticm_v1p0" xmlns:lticp="http://www.imsglobal.org/xsd/imslticp_v1p0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.imsglobal.org/xsd/imslticc_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticc_v1p0.xsd http://www.imsglobal.org/xsd/imsbasiclti_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imsbasiclti_v1p0p1.xsd http://www.imsglobal.org/xsd/imslticm_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticm_v1p0.xsd http://www.imsglobal.org/xsd/imslticp_v1p0 http://www.imsglobal.org/xsd/lti/ltiv1p0/imslticp_v1p0.xsd">
  <blti:title><%= request.getAttribute("title") %></blti:title>
  <blti:description><%= request.getAttribute("description") %></blti:description>
  <blti:launch_url><%= request.getAttribute("launch") %></blti:launch_url>
  <blti:custom>
    <lticm:property name="sub_canvas_account_id">$Canvas.account.id</lticm:property>
    <lticm:property name="sub_canvas_account_name">$Canvas.account.name</lticm:property>
    <lticm:property name="sub_canvas_account_sis_sourceId">$Canvas.account.sisSourceId</lticm:property>
    <lticm:property name="sub_canvas_api_domain">$Canvas.api.domain</lticm:property>
    <lticm:property name="sub_canvas_assignment_id">$Canvas.assignment.id</lticm:property>
    <lticm:property name="sub_canvas_assignment_points_possible">$Canvas.assignment.pointsPossible</lticm:property>
    <lticm:property name="sub_canvas_assignment_title">$Canvas.assignment.title</lticm:property>
    <lticm:property name="sub_canvas_course_id">$Canvas.course.id</lticm:property>
    <lticm:property name="sub_canvas_course_sis_source_id">$Canvas.course.sisSourceId</lticm:property>
    <lticm:property name="sub_canvas_enrollment_enrollment_state">$Canvas.enrollment.enrollmentState</lticm:property>
    <lticm:property name="sub_canvas_membership_concluded_roles">$Canvas.membership.concludedRoles</lticm:property>
    <lticm:property name="sub_canvas_membership_roles">$Canvas.membership.roles</lticm:property>
    <lticm:property name="sub_canvas_root_account.id">$Canvas.root_account.id</lticm:property>
    <lticm:property name="sub_canvas_root_account_sis_source_id">$Canvas.root_account.sisSourceId</lticm:property>
    <lticm:property name="sub_canvas_user_id">$Canvas.user.id</lticm:property>
    <lticm:property name="sub_canvas_user_login_id">$Canvas.user.loginId</lticm:property>
    <lticm:property name="sub_canvas_user_sis_source_id">$Canvas.user.sisSourceId</lticm:property>
    <lticm:property name="sub_canvas_xapi_url">$Canvas.xapi.url</lticm:property>
    <lticm:property name="person_address_timezone">$Person.address.timezone</lticm:property>
    <lticm:property name="person_email_primary">$Person.email.primary</lticm:property>
    <lticm:property name="person_name_family">$Person.name.family</lticm:property>
    <lticm:property name="person_name_full">$Person.name.full</lticm:property>
    <lticm:property name="person_name_given">$Person.name.given</lticm:property>
    <lticm:property name="user_image">$User.image</lticm:property>
  </blti:custom>
  <blti:extensions platform="canvas.instructure.com">
     <lticm:property name="privacy_level">public</lticm:property>
<lticm:property name="domain"><%= request.getAttribute("domain") %></lticm:property>
    <lticm:property name="icon_url"><%= request.getAttribute("icon") %></lticm:property>
    <lticm:options name="link_selection">
      <lticm:property name="message_type">ContentItemSelectionRequest</lticm:property>
      <lticm:property name="url"><%= request.getAttribute("launch") %></lticm:property>
    </lticm:options>
    <lticm:options name="assignment_selection">
      <lticm:property name="message_type">ContentItemSelectionRequest</lticm:property>
      <lticm:property name="url"><%= request.getAttribute("launch") %></lticm:property>
    </lticm:options>
    <lticm:options name="homework_submission">
      <lticm:property name="message_type">ContentItemSelectionRequest</lticm:property>
      <lticm:property name="url"><%= request.getAttribute("launch") %></lticm:property>
    </lticm:options>
    <lticm:options name="editor_button">
      <lticm:property name="message_type">ContentItemSelectionRequest</lticm:property>
      <lticm:property name="url"><%= request.getAttribute("launch") %></lticm:property>
    </lticm:options>
    <lticm:property name="selection_height">500</lticm:property>
    <lticm:property name="selection_width">500</lticm:property>
    <lticm:property name="text"><%= request.getAttribute("title") %></lticm:property>
  </blti:extensions>
</cartridge_basiclti_link>
