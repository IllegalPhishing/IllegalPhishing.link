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

import java.io.File;
import java.security.SecureRandom;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

@SuppressWarnings("deprecation")
public class server {
	// Server Options
	private static String port = "8080"; // default port
	public static String logpath = "servlet.log";
	// Postgres Credentials
	public static String dbuser = "scratch";
	public static String dbpass = "toomanysecrets";
	public static String db = "scratch";
	
	public static String RandomString(int len) { // No I, l, o, or O
		char[] map = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'm',
					  'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
					  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
					  'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
					  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
		SecureRandom r = new SecureRandom();
		StringBuilder result = new StringBuilder();
		int maplen = map.length;
		for(int x=0; x<len; ++x)
			result.append(map[((r.nextInt()&0xEFFFFF)%maplen)]);
		return result.toString();
	}
	
	public static void loadTemplate(String path, String filename) throws Exception {
		String body = Files.toString(new File(filename), Charsets.UTF_8);
		postgres pg = new postgres(link.illegalphishing.server.db, link.illegalphishing.server.dbuser, link.illegalphishing.server.dbpass);
		pg.setTemplate(path, body);
	}
	
	@SuppressWarnings({ "static-access" })
	public static void main( String[] args ) throws Exception {
		Options options = new Options();
		options.addOption(OptionBuilder.withArgName("port").hasArg().withDescription("Listening Port").create("port"));
		options.addOption(OptionBuilder.withArgName("logpath").hasArg().withDescription("Log Path").create("logpath"));
		options.addOption(OptionBuilder.withArgName("dbuser").hasArg().withDescription("Postgres DB Username").create("dbuser"));
		options.addOption(OptionBuilder.withArgName("dbpass").hasArg().withDescription("Postgres DB Password").create("dbpass"));
		options.addOption(OptionBuilder.withArgName("db").hasArg().withDescription("Postgres DB").create("db"));
		options.addOption(OptionBuilder.withArgName("template").hasArg().withDescription("Load from Template File").create("template"));
		options.addOption(OptionBuilder.withArgName("temppath").hasArg().withDescription("Template Path").create("temppath"));
		options.addOption(new Option("help", "Help"));
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);
		
		loadTemplate("/", "/home/wwelna/workspace/illegalphishing/home.html");
		if(cmd.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(new java.io.File(link.illegalphishing.server.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName(), options);
			System.exit(0);
		} else if(cmd.hasOption("port"))
			port = cmd.getOptionValue("port");
		else if(cmd.hasOption("dbuser"))
			dbuser = cmd.getOptionValue("dbuser");
		else if(cmd.hasOption("dbpass"))
			dbpass = cmd.getOptionValue("dbpass");
		else if(cmd.hasOption("db"))
			db = cmd.getOptionValue("db");
		else if(cmd.hasOption("template") && cmd.hasOption("temppath")) {
			loadTemplate(cmd.getOptionValue("temppath"), cmd.getOptionValue("template"));
			System.exit(0);
		}
		new Server();
		Server server = new Server();
		ServerConnector connector = new ServerConnector(server, 8, 8, new HttpConnectionFactory());
		
		connector.setPort(Integer.parseInt(port));
		connector.setAcceptQueueSize(128);
		server.addConnector(connector);
		
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setDirAllowed(true);
		
		ContextHandler context1 = new ContextHandler();
		context1.setContextPath("/res/");
		context1.setResourceBase("./res/");
		context1.setHandler(resourceHandler);
		
		ServletContextHandler context0 = new ServletContextHandler();
		context0.setContextPath("/");
		context0.addServlet(new ServletHolder(new servlet()), "/*");
		
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] {context1, context0});
		server.setHandler(contexts);
		
	    server.start();
	    server.dumpStdErr();
	    server.join();
	 }
}
