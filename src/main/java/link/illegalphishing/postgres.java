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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class postgres {
	Connection con;
	String db;
	String table = "urlshort";
	String template = "template";
	String hits = "hits";
	
	public postgres(String db, String username, String password) {
	      try {
	    	  this.db = db;
	          Class.forName("org.postgresql.Driver");
	          con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/"+db, username, password);
	       } catch (Exception e) {
	          e.printStackTrace();
	          System.exit(-1);
	       }
	}
	
	public void startup_db() throws SQLException {
		Statement stmt = this.con.createStatement();
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS "+this.table+"(id SERIAL PRIMARY KEY NOT NULL, astring TEXT NOT NULL, domain TEXT NOT NULL, url TEXT NOT NULL, ip TEXT NOT NULL, expdate TIMESTAMP)");
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS "+this.template+"(id SERIAL PRIMARY KEY NOT NULL, path TEXT NOT NULL, body TEXT NOT NULL)");
		stmt.executeUpdate("CREATE TABLE IF NOT EXISTS "+this.hits+"(id SERIAL PRIMARY KEY NOT NULL, domain TEXT NOT NULL, astring TEXT NOT NULL, useragent TEXT NOT NULL, referral TEXT, ip TEXT NOT NULL, date TIMESTAMP NOT NULL)");
		stmt.close();
	}
	
	public void registerHit(String domain, String astring, String useragent, String referral, String ip) throws SQLException {
		PreparedStatement stmt = this.con.prepareStatement("INSERT INTO "+this.hits+" (domain, astring, useragent, referral, ip, date) VALUES(?,?,?,?,?,?)");
		stmt.setString(1, domain);
		stmt.setString(2, astring);
		if(useragent != null)
			stmt.setString(3, useragent);
		else
			stmt.setString(3, "");
		if(referral != null)
			stmt.setString(4, referral);
		else
			stmt.setString(4, "");
		stmt.setString(5, ip);
		stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
		stmt.executeUpdate();
		stmt.close();
	}
	
	public String add(String domain, String url, String ip) throws SQLException {
		String rs = link.illegalphishing.server.RandomString(7);
		return this.add(domain, rs, url, 0, ip);
	}
	
	public String add(String domain, String url, long expdate, String ip) throws SQLException {
		String rs = link.illegalphishing.server.RandomString(7);
		return this.add(domain, rs, url, expdate, ip);
	}
	
	public String add(String domain, String astring, String url, long expdate, String ip) throws SQLException {
		PreparedStatement stmt = this.con.prepareStatement("INSERT INTO "+this.table+" (astring, domain, url, expdate, ip) VALUES(?,?,?,?,?)");
		stmt.setString(1, astring);
		stmt.setString(2, domain);
		stmt.setString(3,  url);
		if(expdate != 0)
			stmt.setTimestamp(4, new Timestamp(expdate));
		else
			stmt.setNull(4, java.sql.Types.TIMESTAMP);
		stmt.setString(5, ip);
		stmt.executeUpdate();
		stmt.close();
		return astring;
	}
	
	public void rem(String astring) throws SQLException {
		PreparedStatement stmt = this.con.prepareStatement("DELETE FROM "+this.table+" WHERE astring=?");
		stmt.setString(1, astring);
		stmt.executeUpdate();
		stmt.close();
	}
	
	public String get(String domain, String astring) throws SQLException {
		PreparedStatement stmt = this.con.prepareStatement("SELECT url FROM "+this.table+" WHERE astring=? AND domain=?");
		stmt.setString(1,  astring);
		stmt.setString(2, domain);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			String r = rs.getString(1);
			stmt.close();
			return r;
		}
		else {
			stmt.close();
			return null;
		}
	}
	
	public String getTemplate(String path) throws SQLException {
		PreparedStatement stmt = this.con.prepareStatement("SELECT body FROM "+this.template+" WHERE path=?");
		stmt.setString(1,  path);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			String r = rs.getString(1);
			stmt.close();
			return r;
		}
		else {
			stmt.close();
			return null;
		}
	}
	
	private boolean existsTemplate(String path) throws SQLException {
		PreparedStatement stmt = this.con.prepareStatement("SELECT count(id) FROM "+this.template+" WHERE path=?");
		stmt.setString(1,  path);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			int r = rs.getInt(1);
			stmt.close();
			if(r>0) return true;
			else return false;
		}
		else {
			stmt.close();
			return false;
		}
	}
	
	private void remTemplate(String path) throws SQLException {
		PreparedStatement stmt = this.con.prepareStatement("DELETE FROM "+this.template+" WHERE path=?");
		stmt.setString(1, path);
		stmt.executeUpdate();
		stmt.close();
	}
	
	public void setTemplate(String path, String body) throws SQLException {
		if(this.existsTemplate(path))
			this.remTemplate(path);
		PreparedStatement stmt = this.con.prepareStatement("INSERT INTO "+this.template+" (path, body) VALUES(?,?)");
		stmt.setString(1, path);
		stmt.setString(2,  body);
		stmt.executeUpdate();
		stmt.close();
	}
	
	public long getCount() throws SQLException {
		PreparedStatement stmt = this.con.prepareStatement("SELECT count(id) FROM "+this.table);
		ResultSet rs = stmt.executeQuery();
		if(rs.next()) {
			long r = rs.getLong(1);
			stmt.close();
			return r;
		}
		else {
			stmt.close();
			return 0;
		}
	}
	
	/* public Map<String,String> getStats() {
		Map<String,String> ret = new TreeMap<String,String>();
		
		
		return ret;
	} */
	
	public void purge() throws SQLException {
		Statement stmt = this.con.createStatement();
		stmt.executeUpdate("DELETE FROM "+this.table+" WHERE expdate < now();");
		stmt.close();
	}

}
