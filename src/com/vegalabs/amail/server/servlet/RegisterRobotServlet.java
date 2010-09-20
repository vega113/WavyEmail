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
		String verificationToken = "AOijR2dn-RKQWKY0_QZFo5T0yJ2c0HF2wIZXfMRG3kRzP8YLVescict0B5bgLwW6t8YFSk_MPcOJykDNL2u_5ef8FIcvtYs58Q39UNIIioycyb-OR4GEUnh20nxlL8IRTWTBpVQKtLieefuvJNAdnkf2B-uhpj-Vqw==";
		String securityToken = "8528";
		String securityTokenFromServer = req.getParameter("st");
		if(securityToken.equals(securityTokenFromServer)){
			PrintWriter pw = resp.getWriter();
			pw.print(verificationToken);
			pw.flush();
		}
	}

}
