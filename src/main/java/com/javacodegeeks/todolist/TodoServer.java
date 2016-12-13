package com.javacodegeeks.todolist;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeUnit;
import java.util.Random;



import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnection;
import com.lambdaworks.redis.ValueScanCursor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;



public class TodoServer {

	public static void main(String[] args) throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.createContext("/", new MyHandler(System.getenv("REDIS_PORT")));
		server.setExecutor(null); // creates a default executor


		Connection con = null;
        Statement st = null;
        ResultSet rs = null;
		Statement stmt = null;
		ResultSet rs2 = null;

        String url = "jdbc:mysql://mysql:3306/todo";
        String user = "dbuser";
        String password = "password";
		String sql = "";

		TimeUnit.SECONDS.sleep(30); // wait for docker container to finish initializing
		Random rand = new Random();


        try {
            
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
			stmt = con.createStatement();
           // rs = st.executeQuery("SELECT VERSION()");
		   st.executeUpdate("INSERT INTO todo (todo)" +
                   " VALUES ('Hello')");
			

/*
            if (rs.next()) {
                
                System.out.println(rs.getString(1));
            }
			*/

        } catch (SQLException ex) {
        
            Logger lgr = Logger.getLogger(TodoServer.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);

        } finally {
            
            try {
                
                if (rs != null) {
                    rs.close();
                }
                
                if (st != null) {
                    st.close();
                }
                
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                
                Logger lgr = Logger.getLogger(TodoServer.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }

		 try {
            
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
			sql = "SELECT todo FROM todo";
     		 rs = st.executeQuery(sql);
        
			


            if (rs.next()) {
                
                System.out.println(rs.getString(1));
            }
			

        } catch (SQLException ex) {
        
            Logger lgr = Logger.getLogger(TodoServer.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);

        } finally {
            
            try {
                
                if (rs != null) {
                    rs.close();
                }
                
                if (st != null) {
                    st.close();
                }
                
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                
                Logger lgr = Logger.getLogger(TodoServer.class.getName());
                lgr.log(Level.WARNING, ex.getMessage(), ex);
            }
        }


	
         


		server.start();

	}

	static class MyHandler implements HttpHandler {

		private RedisClient redisClient;
		private RedisConnection connection;
		private ObjectMapper mapper;

		public MyHandler(String redisURL) throws MalformedURLException {

			String hostPortURL = redisURL.substring("tcp://".length());
			int separator = hostPortURL.indexOf(':');
			redisClient = new RedisClient(hostPortURL.substring(0, separator),
					Integer.parseInt(hostPortURL.substring(separator + 1)));
			connection = redisClient.connect();
			mapper = new ObjectMapper();
		}

		public void handle(HttpExchange t) throws IOException {
			String method = t.getRequestMethod();
			OutputStream os = t.getResponseBody();
			String response = "";
			
			if (t.getRequestURI().getPath().equals("/todos")) {
				if (method.equals("GET")) {
					ValueScanCursor cursor = connection.sscan("todos");
					List tasks = cursor.getValues();
					response = mapper.writeValueAsString(tasks);

				} else if (method.equals("PUT")) {

					connection.sadd("todos", IOUtils.toString(t.getRequestBody()));
				}
			}

			t.sendResponseHeaders(200, response.length());
			os.write(response.getBytes());
			os.close();
		}



	

		@Override
		public void finalize() {
			connection.close();
			redisClient.shutdown();
		}
	}
}