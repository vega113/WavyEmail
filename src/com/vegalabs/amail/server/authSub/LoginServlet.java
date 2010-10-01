/* Copyright (c) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.vegalabs.amail.server.authSub;

import com.google.gdata.client.http.AuthSubUtil;
import com.google.inject.Singleton;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Logging into this application is trivial and just consists of visiting
 * this servlet.  This servlet will set an authentication cookie for the
 * user and redirect to the page to authorize to Google.  Typically, the
 * user will be authenticated to the server via the login cookie prior to
 * accessing this servlet.
 *
 * 
 */
@Singleton
public class LoginServlet extends HttpServlet {

  // On successfully acquiring a token, the servlet will redirect the user to
  // the following next URL
  /*package*/ static final String NEXT_URL = "/SuccessMessageServlet";
  final public static String FINANCE_SCOPE_URL = "http://finance.google.com/finance/feeds/";
  final public static String CONTACTS_SCOPE_URL = "http://www.google.com/m8/feeds/";
  
  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
  throws ServletException, IOException {
	  // Save any persistent data to user session
		String financeFeedRootUrl =  CONTACTS_SCOPE_URL;
	    String authSubToken = null;

	    // Form continue URL
	    StringBuffer continueUrl = req.getRequestURL();
	    int index = continueUrl.lastIndexOf("/");
	    continueUrl.delete(index, continueUrl.length());

	    // If the user doesn't have an AuthSub token yet, redirect the user to
	    // the Google page to request an AuthSub token. Otherwise redirect to
	    // the main page
	    if (authSubToken == null) {
	      continueUrl.append("/HandleTokenServlet");
	      
	    String user = req.getParameter("user");
	    if(user != null)
	    	continueUrl.append("?user=" + URLEncoder.encode(user, "UTF-8"));

	      // Check whether to use https for authentication
	      boolean secure = (Utility.getPrivateKey() != null);
	      String authSubLogin;
	      authSubLogin = AuthSubUtil.getRequestUrl(continueUrl.toString(),financeFeedRootUrl, secure,  true /*session*/);

	      resp.sendRedirect(authSubLogin);
	    } else {
	      continueUrl.append(NEXT_URL);
	      resp.sendRedirect(continueUrl.toString());
	    }
  }
}
