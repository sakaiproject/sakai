package org.dojo.moxie;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

/** 
	Exposes a simple RESTian API for working with Moxie documents.
	The objects we expose are Moxie documents with their filenames:
	
	/somePageName1
	/somePageName2
	
	etc.
	
	To view a page, simply do a GET on it's page name. If a page
	does not exist you will get a 404 (Not Found); if it is an incorrect filename
	you will get a 403 (Forbidden).
	
	To see a list of all page's, simply do a GET on /*
	
	If your HTTP client has sent "text/html" as one of the things
	it accepts in it's Accept header, then a simple HTML page 
	will be returned that uses an unordered list
	of links to point to all of our pages:
	
	<html><body>
		<ul>
			<li><a href="somePageName1">somePageName1</a></li>
			<li><a href="somePageName2">somePageName2</a></li>
		</ul>
	</body></html>
	
	If your client doesn't send "text/html" but sends "text/javascript"
	than we return this list of page names as JSON:
	
	[
		"somePageName1", "somePageName2"
	]
	
	To create a new page or update an existing page, do a POST to 
	what the page name will be or is,
	such as /aNewPage1 or /updateMe. The payload can either be URL encoded form
	values or can be a simple HTML page. If it is a URL encoded
	form value, the Content-Type header must be 
	"application/x-www-form-urlencoded"; there should be one form
	value, with the key name 'content' and the values URL encoded.
	
	If the payload is a simple HTML page, the Content-Type should
	be "text/html" and the POSt content can just be plain, normal
	HTML.
	
	The server responds with either a 201 (Created); a
	403 (Forbidden) if the page already exists or has a malformed name,
	or 200 OK if the update was successful.
	
	To delete a page, we need to simulate a DELETE request to the / URL
	of the page name. Safari and Opera have issues with the DELETE method,
	so we use 'X-Method-Override: DELETE' on these. The server 
	returns a 410 (Gone) request if successful,
	404 (Not Found) if there was no page there originally, or
	403 (Not Allowed) if the file name is mangled.
	
	If clients can correctly send DELETE requests, you can bypass
	having to send 'X-Method-Override' and simply do a normal DELETE
	request as outlined above.
	
	We also expose /download/, called with a GET, which will download
	a JSON data structure with the contents of all of our documents.
	This JSON structure is an array of objects, where each object
	is an object with a 'fileName' member that has the file name
	of that document, and a 'content' entry with the content of that
	document. Example:
	
	[
		{fileName: "message1", content: "hello world"},
		{fileName: "message2", content: "goodbye world"}
	]
	
	@author Brad Neuberg, bkn3@columbia.edu
*/
public class MoxieServlet extends HttpServlet{
	private String jdbcURL, userName, password, driver;
	
	public MoxieServlet(String jdbcURL, String userName, 
						String password, String driver){
		this.jdbcURL = jdbcURL;
		this.userName = userName;
		this.password = password;
		this.driver = driver;
	}
	
	public void init() throws ServletException{
		// initialize our database and the class that allows us to 
		// gain access to our documents
		try{
			Documents.initialize(jdbcURL, userName, password, driver);
		}catch(MoxieException e){
			throw new ServletException(e);
		}
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse res)
							throws IOException, ServletException{
		try{
			String path = req.getPathInfo();
			if(path == null){
				path = "/*";
			}
			
			// dispatch our action
			if(path.equals("/*")){
				list(req, res);
			}else if(path.equals("/download") || path.equals("/download")){
				download(req, res);
			}else{
				viewItem(req, res);
			}
		}catch(MoxieException e){
			e.printStackTrace();
			throw new ServletException(e);
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res)
							throws IOException, ServletException{
		try{
			String methodOverride = req.getHeader("X-Method-Override");
			
			// dispatch our action
			if(methodOverride == null){
				updateItem(req, res);
			}else if(methodOverride.equals("DELETE")){
				deleteItem(req, res);
			}
		}catch(MoxieException e){
			e.printStackTrace();
			throw new ServletException(e);
		}
	}
	
	public void doDelete(HttpServletRequest req, HttpServletResponse res)
							throws IOException, ServletException{
		try{
			deleteItem(req, res);
		}catch(MoxieException e){
			throw new ServletException(e);
		}
	}
	
	private void viewItem(HttpServletRequest req, HttpServletResponse res)
							throws IOException, ServletException, MoxieException{
		// get the file name
		String fileName = getFileName(req, res);
	
		// white list the file name
		if(Document.validFileName(fileName) == false){ // invalid file name
			// HTTP Status Code 403
			res.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
	
		// see if the file exists
		if(Documents.exists(fileName) == false){
			// HTTP Status Code 404
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		// try to get it
		Document doc = Documents.findByFileName(fileName);
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		out.write(doc.content);
	}
	
	private void newItem(HttpServletRequest req, HttpServletResponse res)
							throws IOException, ServletException, MoxieException{
		// get the file name
		String fileName = getFileName(req, res);
	
		// white list the file name
		if(Document.validFileName(fileName) == false){ // invalid file name
			// HTTP Status Code 403
			res.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		// populate it's Document values
		String content = getRequestAsString(req);
		if(content == null){
			res.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		Document doc = new Document(null, fileName, new Date().getTime(), new Date().getTime(),
									content);
									
		// create it
		Documents.newItem(doc);
		
		// send back a 201 Created response with the correct
		// return values
		res.setStatus(HttpServletResponse.SC_CREATED);
		res.setHeader("Location", fileName);
		res.setContentType("text/html");
	}
	
	private void deleteItem(HttpServletRequest req, HttpServletResponse res)
							throws IOException, ServletException, MoxieException{
		// get the file name
		String fileName = getFileName(req, res);
	
		// white list the file name
		if(Document.validFileName(fileName) == false){ // invalid file name
			// HTTP Status Code 403
			res.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
	
		// see if the file exists
		if(Documents.exists(fileName) == false){
			// HTTP Status Code 404
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		// get the original document
		Document doc = Documents.findByFileName(fileName);
		
		// delete it
		Documents.deleteItem(doc.getId());
		
		// send back a 410 Gone response
		res.setStatus(HttpServletResponse.SC_GONE);
	}
	
	private void updateItem(HttpServletRequest req, HttpServletResponse res)
							throws IOException, ServletException, MoxieException{
		// get the file name
		String fileName = getFileName(req, res);
	
		// white list the file name
		if(Document.validFileName(fileName) == false){ // invalid file name
			// HTTP Status Code 403
			res.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
	
		// see if the file exists; if it doesn't, we 
		// will create a new document
		if(Documents.exists(fileName) == false){
			newItem(req, res);
			return;
		}
		
		// get the original document
		Document doc = Documents.findByFileName(fileName);

		// get our new content
		String content = getRequestAsString(req);							
		if(content == null){
			res.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
																																												
		// update our values
		doc.setLastUpdated(new Date().getTime());
		doc.setContent(content);
		
		// save them
		Documents.updateItem(doc);
		
		// send back a 200 OK response with the correct
		// return values
		res.setStatus(HttpServletResponse.SC_OK);
	}
	
	private void list(HttpServletRequest req, HttpServletResponse res)
							throws IOException, ServletException, MoxieException{
		// get our file names
		List<Document> allDocs = Documents.list();
		
		// determine what kind of representation to return
		String accepts = req.getHeader("Accept");
		if(accepts == null 
			|| accepts.indexOf("text/html") != -1){ // return HTML
			listReturnHTML(allDocs, req, res);
		}else{
			listReturnJSON(allDocs, req, res);
		}
	}
	
	private void download(HttpServletRequest req, HttpServletResponse res)
							throws IOException, ServletException, MoxieException{
		// get our file names
		List<Document> allDocs = Documents.list();
		
		// send our JSON response back
		res.setContentType("text/javascript");
		PrintWriter out = res.getWriter();
		
		out.write("[\n");
		
		// loop through each document
		Iterator<Document> iter = allDocs.iterator();
		while(iter.hasNext()){
			Document d = iter.next();
			out.write("{");
			
			// FIXME: Use a real JSON serialization library
			// write out the file name
			out.write("fileName: \"" + d.fileName + "\", ");
			
			// escape our double quotes
			Pattern quotePattern = Pattern.compile("[\"]", Pattern.MULTILINE);
			Matcher m = quotePattern.matcher(d.content);
			String content = m.replaceAll("\\\\\""); 
			
			// escape multi line strings
			Pattern linePattern = Pattern.compile("\n|\r", Pattern.MULTILINE);
			m = linePattern.matcher(content);
			content = m.replaceAll("\\\\\n");
			
			// write out our contents
			out.write("content: \"" + content + "\"");
			
			out.write("}");
			
			// add a comma if we are not the last one
			if(iter.hasNext() == true){
				out.write(", \n");
			}else{
				out.write("\n");
			}
		}
		
		out.write("]\n");
	}
	
	private String getFileName(HttpServletRequest req, HttpServletResponse res) 
										throws MoxieException{
		// get the file to view
		String fileName = req.getPathInfo();
		
		// strip off the leading slash
		fileName = fileName.substring(1);
		
		return fileName;
	}
	
	private String getRequestAsString(HttpServletRequest req) 
                                          throws IOException{
		// correctly decode this value
		String contentType = req.getHeader("Content-Type");
		if(contentType != null && contentType.equals("text/html")){
			// basic HTML in POST payload
			
			// FIXME: WARNING: The combination of a wrapped InputStream being
			// treated as a reader, with the deprecated readLine() method below
			// might mangle i18n text
			BufferedReader requestData = new BufferedReader(
						  new InputStreamReader(req.getInputStream()));
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			while ((line = requestData.readLine()) != null){
				stringBuffer.append(line);
			}
			
			String content = stringBuffer.toString();
			return content;
		}else{ // encoded form values -- application/x-www-form-urlencoded
			String content = req.getParameter("content");
			
			return content;
		}
   }
   
   private void listReturnHTML(List<Document> allDocs, 
								HttpServletRequest req, 
								HttpServletResponse res)
							throws IOException, ServletException, MoxieException{
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		out.write("<html><body><ul>");
		
		// loop through each file name and write it out as an A tag
		Iterator<Document> iter = allDocs.iterator();
		while(iter.hasNext()){
			Document d = iter.next();
			out.write("<li><a href=\"" + d.fileName + "\">"
						+ d.fileName + "</a></li>");
		}
		out.write("</ul></body></html>");
   }
   
   private void listReturnJSON(List<Document> allDocs, 
								HttpServletRequest req, 
								HttpServletResponse res)
							throws IOException, ServletException, MoxieException{
		res.setContentType("text/javascript");
		PrintWriter out = res.getWriter();
		
		out.write("[");
		
		// loop through each file name and write it out as an A tag
		Iterator<Document> iter = allDocs.iterator();
		while(iter.hasNext()){
			Document d = iter.next();
			// FIXME: Use a real JSON serialization library
			out.write("\"" + d.fileName + "\"");
			if(iter.hasNext() == true){
				out.write(", \n");
			}
		}
		
		out.write("]\n");
   }

}