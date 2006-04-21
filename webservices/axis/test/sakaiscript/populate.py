import os
import sys
from SOAPpy import WSDL

siteid = "trythemall"
username = "admin"
password = "admin"

server_url = "http://localhost:8080"

login_url = server_url + "/sakai-axis/SakaiLogin.jws?wsdl"
script_url = server_url + "/sakai-axis/SakaiScript.jws?wsdl"

login_proxy = WSDL.SOAPProxy(login_url)
script_proxy = WSDL.SOAPProxy(script_url)

loginsoap = WSDL.SOAPProxy(login_url)
sessionid = loginsoap.login(username, password)

scriptsoap = WSDL.SOAPProxy(script_url)

usersfile = open("nobel_laureates.txt")
lines = usersfile.readlines()

for index, line in enumerate(lines):
	line = line.rstrip("\n")
	parts = line.split(",")
	
	if (index < 5):
		result = scriptsoap.addMemberToSiteWithRole(sessionid, siteid, parts[0], "maintain")
		print "Adding Instructor " + parts[0] + ": " + result
	else:
		result = scriptsoap.addMemberToSiteWithRole(sessionid, siteid, parts[0], "access")
		print "Adding Student " + parts[0] + ": " + result