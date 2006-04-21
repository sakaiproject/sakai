import os
import sys
from SOAPpy import WSDL

username = "admin"
password = "admin"

server_url = "http://localhost:8080"

login_url = server_url + "/sakai-axis/SakaiLogin.jws?wsdl"
script_url = server_url + "/sakai-axis/SakaiScript.jws?wsdl"

login_proxy = WSDL.SOAPProxy(login_url)
script_proxy = WSDL.SOAPProxy(script_url)

loginsoap = WSDL.SOAPProxy(login_url)
sessionid = loginsoap.login(username, password)
#sessionid = "1233234324"

scriptsoap = WSDL.SOAPProxy(script_url)

for i in range(10000):
    newuser = scriptsoap.addNewUser(sessionid, "usern" + str(i), "usern", str(i), "", "", "password" )
    print str(newuser)
