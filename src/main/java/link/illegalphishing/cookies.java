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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;

public class cookies {
	private Cookie[] cookies;
	private String magick = null;
	private String Secret = "secret";
	
	public cookies(HttpServletRequest request, HttpServletResponse response) {
		this.cookies = request.getCookies();
		if(this.cookies != null) 
			for(Cookie c: this.cookies) {
				if(c.getName() == "magick") {
					this.magick = c.getValue();
					break;
				}
			}
		if(this.magick == null) {
			this.magick = link.illegalphishing.server.RandomString(16);
			Cookie c = new Cookie("magick", this.magick);
			c.setMaxAge(60*24); // 1 Day Expiration
			response.addCookie(c);
		}
	}
	
	public String getHash(String fieldname) throws Exception {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		String tohash = this.Secret+fieldname+this.magick;
		String hash = Hex.encodeHexString(md.digest(tohash.getBytes(StandardCharsets.UTF_8)));
		return hash;//StringUtils.left(hash, 12);
	}
	
	public String getMagick() {
		return this.magick;
	}
	
	

}
