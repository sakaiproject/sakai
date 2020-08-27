# Sakai Web API

This project hosts various web api endpoints for comsumption in client side tools such as curl, or
scripting languages such as Python, Ruby, etc. The endpoints mount under /api and have been designed
to be rest comliant and predicatable.

# Login:

curl -c cookie.txt "localhost/api/login?username=joe&password=joe"

you should see a uuid printed out. This is your Sakai session.

# Get my announcements:

curl -b cookie.txt "localhost/api/users/me/announcements"

you should get a JSON document

# Get my calendar events

curl -b cookie.txt "localhost/api/users/me/calendar"

you should get a JSON document

# Swagger API docs

If you build this project, you'll be able to access the Swagger API docs at:

http://localhost:8080/api/swagger-ui.html

or something like that, anyway.






