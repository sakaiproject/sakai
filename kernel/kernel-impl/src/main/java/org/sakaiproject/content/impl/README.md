# Content

The content api and implementation handles resources in Sakai, the database entries and the binary
assets themselves.

## File Conversion Service

The File Conversion Service in Sakai takes a set of content resource ids and submits them for
conversion to PDF, to an online instance of a headless Libre Office Online server. As these files
are converted, they are stored in content hosting and linked to the original content resource via
resource properties.

Setting up LibreOffice as a document converter for Sakai will massively improve the quality of the
document preview functionality in the new grader. LibreOffice can be setup as a full blown server
serving  multiple nodes or as a Docker image running on each Sakai node. This guide will focus on
the Docker image per Sakai node approach.

### Installation of LOOL (Libre Office Online)

1. Install Docker on your Sakai node. This may well be your development machine if you're just
trying this out. Visit https://docker.com  and install the version for your OS.

2. Download and run the libreoffice/online:master Docker image. On linux or OSX you'd use this
command: 

        docker run -d -p 9980:9980 -e "extra_params=--o:ssl.enable=false" libreoffice/online:master

    Details on the environment variables you can supply can be found at
    https://hub.docker.com/r/libreoffice/online/.

    Now you will have a LIbreOffice Online server running in a Docker container and listening on port
    9980\. On linux you can verify this with this command:

        netstat -nlp | grep 9980.

3. In your sakai.properties or local.properties, add these properties:

        fileconversion.submit.enabled=true
        fileconversion.conversion.enabled=true

    The first one, fileconversion.submit.enabled, allows the submitting of new jobs. You'd typically
    have this set to true on all of your Sakai nodes so that users on each can submit their attachments
    for conversion.

    The second, fileconversion.conversion.enabled, turns on the actual conversion job, the code that
    sends the attachment to LibreOffice for conversion. This should only be turned on on *one* of your
    Sakai nodes. Otherwise, you'll get race conditions on the shared state in the
    FILE\_CONVERSION\_QUEUE table.

4. Restart Tomcat. The file conversion service in Sakai should now talk to your dockerised
    LibreOffice Online server. This is currently setup to work with attachments in Sakai. Try this out
    in the Assignments tool by submitting a large powerpoint file. Within a few minutes that file
    should be available as a PDF and you'll be able to view it in the browser from the new grader.

    You can configure the Libre Office server's url with this property: 

        fileconversion.converterurl

    OOTB, it defaults to http://localhost:9980.

### Performance

There are a couple of sakai properties to help if you encounter issues with performance/reliability.

1. **fileconversion.pausemillis** : This defaults to 1000 and is the pause that occurs between each
document in the queue being sent off to the Libre server. If you're seeing pegging on the Libre
server, try upping this to throttle the requests coming from Sakai.

2. **fileconversion.queueintervalminutes**: This defaults to 1 and defines the length of time between
each read of the conversion queue in Sakai. Obviously, this will also affect the load on the Libre
server, together with pausemillis, throttling the rate of requests. This one is more aimed at
Sakai's load though.

3. **fileconversion.workerthreads**: Again, more a Sakai property. The conversion service uses a
thread pool and this defaults to 5 workers.

4. **fileconversion.maxattempts**: Defaults to 5 attempts to send a doc to the server before stopping.
You may want to set this higher when testing, debugging, or if you have a very unreliable Libre setup.

### Supported File Types

Out of the box, the conversion service supports these file extensions:

- application/msword
- application/vnd.openxmlformats-officedocument.wordprocessingml.document
- application/vnd.oasis.opendocument.text
- application/vnd.oasis.opendocument.presentation
- application/pdf
- application/vnd.ms-powerpoint
- application/vnd.openxmlformats-officedocument.presentationml.presentation

You can configure the service to convert any of the types that Libre Office supports by using this
property:

        fileconversion.fromtypes

... which is a comma separated list of mime types.

### Troubleshooting

If you are connecting to your LibreOffice server from another host, you may need to pass some extra
options to the docker container, around permitted client connections. Take a look at this github
issue:

https://github.com/CollaboraOnline/Docker-CODE/issues/49

You can curl documents to your LibreOffice server, and see what you get back. It really helps when
you're stuck. Get the name of a file off your filesystem, a docx or whatever. Let's say that is
called /tmp/my.doc.x:

            curl -X POST -F data=@/tmp/my.docx http://my.loolserver.org:9980/lool/convert-to/pdf > my.pdf &

You should see some data in that PDF file and you should be able to open it in your favourite PDF
viewer. If you have issues you can play around with curl's debug and response printing.
