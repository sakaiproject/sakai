AVOIDING OUT OF MEMORY CONDITIONS
=================================

Many of the modern app server technologies make use of objects that persist longer,
and therefore many Sakai deployments have found it beneficial to adjust the JVM 
with flags to specify enough PERMANENT SPACE to hold these objects.  If an 
OutOfMemory error happens even though there is a lot of available HEAP space, it's 
very likely caused by the setting of PERMANENT GENERATION SPACE. Programs that 
dynamically generate and load many classes (e.g. Hibernate, Struts, JSF, Spring etc.) 
usually need a larger permanent generation than the default 32MB maximum.  The 
permanent generation is sized independently from the other generations because it's 
where the JVM allocates classes, methods, and other "reflection" objects.  

Specify flag -XX:PermSize=16m to allow the JVM to start with enough memory so that 
it doesn't have to pause apps to allocate more memory. Specify -XX:MaxPermSize for 
the size of the permanent generation to be greater than the default 32MB maximum.  

When running on servers with multiple cpus, you'd MULTIPLY the memory by the number 
of CPU's.  For example, to run a 4 way CPU using the default you'd set the flag to 
-XX:MaxPermSize=128m so the JVM would have a maximum of 131072K bytes to grow into. 

