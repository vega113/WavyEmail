
package com.vegalabs.amail.server.authSub;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.vegalabs.general.server.command.Command;
import com.vegalabs.general.server.rpc.JsonRpcRequest;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;

@Singleton
public class SuccessMessageServlet extends HttpServlet {
	
	protected Injector injector = null;
	
	private static Logger LOG = Logger.getLogger(SuccessMessageServlet.class.getName());

	@Inject
	public SuccessMessageServlet(Injector injector){
		this.injector = injector;
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// Retrieve the authentication cookie to identify user
		String principal = req.getParameter("user");
		principal = URLDecoder.decode(principal, "UTF-8");
		LOG.info("principal: " + principal);
		// Check that the user has an AuthSub token
		JSONObject postDataJson = new JSONObject();
		JSONObject paramsJson = new JSONObject();
		
		try{
			postDataJson.put("method", "IMPORT_CONTACTS");
			paramsJson.put("userId", principal);
			postDataJson.put("params", paramsJson);
			JSONObject json = handleGetRequest(postDataJson.toString());
			if(!json.isNull("error")){
				resp.getWriter().print("Error, please retry!");
				resp.getWriter().flush();
			}else{
				resp.getWriter().print("<html><head><title>Contacts for " + principal + " are  successfully imported!</title></head><body>");
				resp.getWriter().print("<h4><br>Please click on the \"Reload contacts\" button for the changes to take effect.<br>  You can close this window now.</h4> ");
				resp.getWriter().print("</body></html>");
				resp.getWriter().flush();
			}
		}catch (JSONException e) {
			 LOG.severe(e.getMessage());
		}
		// Handle a GET request
	}

	private JSONObject handleGetRequest(String postBody) {
		 JSONObject json = new JSONObject();
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		  JsonRpcRequest jsonRpcRequest = gson.fromJson(postBody, JsonRpcRequest.class);
		  if (jsonRpcRequest != null) {
			  String method = jsonRpcRequest.getMethod();
			  if (method != null) {
				  LOG.info("processing method " + method);
				  Class<? extends Command> commandClass = com.vegalabs.amail.server.admin.CommandType.valueOfIngoreCase(method).getClazz();
				  Command command = injector.getInstance(commandClass);
				  command.setParams(jsonRpcRequest.getParams());
				  try{
					  try {
						  json = command.execute();
					  } catch (JSONException e1) {
						  json.put("error", e1.getMessage());
					  } catch (IllegalArgumentException e2) {
						  json.put("error", e2.getMessage());
					  }
				  }catch(JSONException e3){
					  LOG.severe(e3.getMessage());
				  }
			  }
		  }
		return json;
	}

}
