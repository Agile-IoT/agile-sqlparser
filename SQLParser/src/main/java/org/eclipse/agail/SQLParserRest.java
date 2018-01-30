package org.eclipse.agail;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class SQLParserRest extends AbstractHandler
{
	private String readBody(HttpServletRequest request) throws IOException {
	  StringBuilder buffer = new StringBuilder();
	  BufferedReader reader = request.getReader();
	  String line;
	  while ((line = reader.readLine()) != null) {
	       buffer.append(line);
	  }
	  return  buffer.toString();	
	}
	
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
        throws ServletException, IOException
    {
    	Statement statement;
    	String result = "";
    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			String body = readBody(request);
			Map <String,String> map = mapper.readValue(body, new TypeReference<Map<String, String>>(){});
			if(map.containsKey("query")) {
				System.out.println("parsing query"+map.get("query"));
	    		try {
	    			statement = CCJSqlParserUtil.parse(map.get("query"));
	    	    	TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
	    	    	Map<String,List<String>> tableList = tablesNamesFinder.getTableList(statement);
	    	    	result = mapper.writeValueAsString(tableList);
	    	    	response.setContentType( "application/json");
	    	        response.setStatus(HttpServletResponse.SC_OK);
	    	    	
	    		} catch (JSQLParserException e) {
	    			response.setContentType( "application/json");
	    	        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	    	        Map<String,String> error = new HashMap<String,String>();
	    	        error.put("Exception", e.toString()); 
	    	        result = mapper.writeValueAsString(error);
	    	    	e.printStackTrace();
	    		}
	    	}
	    	else {
	    		response.setContentType( "application/json");
		        response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
		        Map<String,String> error = new HashMap<String,String>();
		        error.put("Exception", "This API receives a JSON Body (POST) with the query"); 
		        result = mapper.writeValueAsString(error);
	    		
	    	}
			
		} catch (IOException e1) {
			response.setContentType( "application/json");
	        response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
	        Map<String,String> error = new HashMap<String,String>();
	        error.put("Exception", "This API receives a JSON Body (POST) with the query"); 
	        try {
				result = new ObjectMapper().writeValueAsString(error);
			} catch (JsonProcessingException e) {
				e1.printStackTrace();
			}
		}   	
    			
		baseRequest.setHandled(true);
        response.getWriter().println(result);
    	
        
    }

    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        server.setHandler(new SQLParserRest());

        server.start();
        server.join();
    }
}
