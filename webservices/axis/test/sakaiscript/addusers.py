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

scriptsoap = WSDL.SOAPProxy(script_url)


usersfile = open("nobel_laureates.txt")
lines = usersfile.readlines()

for line in lines:
    line = line.rstrip("\n")
    parts = line.split(",")
    newuser = scriptsoap.addNewUser(sessionid, parts[0], parts[1], parts[2], "", "", "password" )
    print str(newuser)