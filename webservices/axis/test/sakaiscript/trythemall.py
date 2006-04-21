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

scriptsoap.addNewSite(sessionid, siteid, "Try All The Tools", "A site containing all the tools that come with the demo", \
                    "All the shipped tools", "", "", True, "access", True, True, "", "test" )
 
toolsfile = open("all_the_tools.txt")
lines = toolsfile.readlines()

for line in lines:
    line = line.rstrip("\n")
    parts = line.split(",")
    result = scriptsoap.addNewPageToSite(sessionid, siteid, parts[0], 0)
    print "AddPage:" + parts[0] + "\t" + result
    result2 = scriptsoap.addNewToolToPage(sessionid, siteid, parts[0], parts[0], parts[1], "")
    print "AddTool:" + parts[1] + "\t" + result2

