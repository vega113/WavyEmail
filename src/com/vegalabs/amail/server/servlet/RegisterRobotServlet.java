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
		String verificationToken = "AOijR2f5RywlQbiGHTG2DU3W4QM61DuaS0AAN28XCcyUZeT8-2F4-DmE0-6R-ASYde2TrcrovI39-ff4BD6FgWVOO6gSGlZcA4HOGbSXdHEgDBr1IgdFC7dfkC51R5JpPnsP93oqDm8ZDOajqfx6nmEo0wgy5n6cIQ==";
		String securityToken = "861";
		String securityTokenFromServer = req.getParameter("st");
		if(securityToken.equals(securityTokenFromServer)){
			PrintWriter pw = resp.getWriter();
			pw.print(verificationToken);
			pw.flush();
		}
	}

}
