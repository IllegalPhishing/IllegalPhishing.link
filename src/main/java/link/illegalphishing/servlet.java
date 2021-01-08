/* Copyright (C) 2021 William Welna (wwelna@occultusterra.com)
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/

package link.illegalphishing;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.FileHandler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class servlet extends HttpServlet {
	private static final long serialVersionUID = -2214761468251488783L;
	java.util.logging.Logger logger =  java.util.logging.Logger.getLogger(this.getClass().getName());
	postgres pg = new postgres(link.illegalphishing.server.db, link.illegalphishing.server.dbuser, link.illegalphishing.server.dbpass);
	
	protected String ServerString = "Jetty "+org.eclipse.jetty.server.Server.getVersion();
	
	public void init() {
		FileHandler fileHandler;
		try {
			fileHandler = new FileHandler(link.illegalphishing.server.logpath);
			logger.addHandler(fileHandler);
			pg.startup_db();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		//response.setStatus(500);
		response.addHeader("Server", ServerString);
		String UserAgent = request.getHeader("User-Agent");
		if(UserAgent == null) UserAgent = "<None>";
		logger.info("POST Request "+request.getRemoteAddr()+" "+request.getRequestURL()+" "+UserAgent);
		System.out.println(request.getParameterMap());
	}
	
	protected void doHead(HttpServletRequest request, HttpServletResponse response) {
		response.setStatus(500);
		response.addHeader("Server", ServerString);
		String UserAgent = request.getHeader("User-Agent");
		if(UserAgent == null) UserAgent = "<None>";
		logger.info("HEAD Request "+request.getRemoteAddr()+" "+request.getRequestURL()+" "+UserAgent);
		
	}
	
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
		response.setStatus(500);
		response.addHeader("Server", ServerString);
		String UserAgent = request.getHeader("User-Agent");
		if(UserAgent == null) UserAgent = "<None>";
		logger.info("DELETE Request "+request.getRemoteAddr()+" "+request.getRequestURL()+" "+UserAgent);
		
	}
	
	protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
		response.setStatus(500);
		response.addHeader("Server", ServerString);
		String UserAgent = request.getHeader("User-Agent");
		if(UserAgent == null) UserAgent = "<None>";
		logger.info("OPTIONS Request "+request.getRemoteAddr()+" "+request.getRequestURL()+" "+UserAgent);
		
	}
	
	protected void doPut(HttpServletRequest request, HttpServletResponse response) {
		response.setStatus(500);
		response.addHeader("Server", ServerString);
		String UserAgent = request.getHeader("User-Agent");
		if(UserAgent == null) UserAgent = "<None>";
		logger.info("PUT Request "+request.getRemoteAddr()+" "+request.getRequestURL()+" "+UserAgent);
		
	}
	
	protected void doTrace(HttpServletRequest request, HttpServletResponse response) {
		response.setStatus(500);
		response.addHeader("Server", ServerString);
		String UserAgent = request.getHeader("User-Agent");
		if(UserAgent == null) UserAgent = "<None>";
		logger.info("TRACE Request "+request.getRemoteAddr()+" "+request.getRequestURL()+" "+UserAgent);
		
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String Url = request.getRequestURI();
		String UserAgent = request.getHeader("User-Agent");
		String Domain = request.getHeader("Location").toLowerCase();
		String IP = request.getRemoteAddr();
		String Referral = request.getHeader("Referral");
		cookies c = new cookies(request, response);
		template templates = new template(this.pg);
		if(UserAgent == null) UserAgent = "<None>";
		response.addHeader("Server", ServerString);
		response.setContentType("text/html");
		logger.info("GET Request "+request.getRemoteAddr()+" "+request.getRequestURL()+" "+UserAgent);
		PrintWriter out = response.getWriter();
		try {
			if(Url.equals("/")) { // Splash Page
				String template = templates.getPath(Url)
				.replace("{URL-INPUT}", c.getHash("URL-INPUT"))
				.replace("{CUSTOM-INPUT}", c.getHash("CUSTOM-INPUT"))
				.replace("{SITE-INPUT}", c.getHash("SITE-INPUT"))
				.replace("{DATE-INPUT}", c.getHash("DATE-INPUT"))
				.replace("{TIME-INPUT}", c.getHash("TIME-INPUT"))
				.replace("{SUBMIT}", c.getHash("SUBMIT"));
				out.write(template);
			} else { // Do redirect & Check
				String redirurl = pg.get(Domain, Url.substring(1));
				if(redirurl != null) {
					response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
					response.setHeader("Location", redirurl);
					pg.registerHit(Domain, redirurl, UserAgent, Referral, IP);
				} else {
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
					out.write("<html><head><title>404 Error</title></head><body><center><h1>404 NOT FOUND</h1></center></body></html>");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void destroy() {
		
	}

}
