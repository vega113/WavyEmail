package com.vegalabs.amail.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

@Singleton
public class RegisterRobotServlet extends HttpServlet {
	Logger LOG = Logger.getLogger(RegisterRobotServlet.class.getName());
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		LOG.info("RegisterRobotServlet.doGet");
		String verificationToken = "AOijR2dLavenB5EYtDMFI11DFK2MH_c_6HAgcJsOvncq8McX2FNCisWOjbvxFu2-gx3Zpw91YqnkM5ZswYj6yrLRvf7qIfyBW60g19D3MzbSoa2eNIeDL3G-B3Qb_2QBwkyxp_BA49iWg6Xb2FsDY9L5qhXClrIaCw==";
		String securityToken = "7543";
		String securityTokenFromServer = req.getParameter("st");
		if(securityToken.equals(securityTokenFromServer)){
			PrintWriter pw = resp.getWriter();
			pw.print(verificationToken);
			pw.flush();
		}
	}

}
