import com.sun.net.httpserver.HttpServer;
import java.util.concurrent.Executors;
import java.nio.charset.StandardCharsets;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class Server{

  public static void main(String[] args) {
    
    try{
      Integer port = Integer.parseInt(args[0]);
      System.setProperty("MD", args[1]);

      HttpServer http = HttpServer.create(new InetSocketAddress(port), 10);
      http.setExecutor(Executors.newCachedThreadPool());

      http.createContext("/api/v1", new HttpHandler(){
        public void handle(HttpExchange aHttp) throws IOException{
        	
        	String uri = aHttp.getRequestURI().getPath();
        	
        	if(uri.endsWith(".md")) {
        		System.setProperty("MD", uri.replace("/api/v1/",""));
        		aHttp.getResponseHeaders().set("Location", "/index.html");
        		aHttp.sendResponseHeaders(302, -1);
        	}else {
        		byte[] input = aHttp.getRequestBody().readAllBytes();
        		String md = new String(input, StandardCharsets.UTF_8); 
        		PrintWriter pw = new PrintWriter(System.getProperty("MD"),StandardCharsets.UTF_8);
        		pw.print(md);
        		pw.flush();
        		pw.close();
            Headers hres = aHttp.getResponseHeaders();
            hres.set("Content-Type", "application/json; charset=UTF-8");
            byte[] bytes = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
            aHttp.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = aHttp.getResponseBody()) {
              os.write(bytes);
              os.flush();
            }
        	}
          aHttp.close();
        }
      });


      http.createContext("/", new HttpHandler(){
        public void handle(HttpExchange aHttp)throws IOException{

          try{
            String uri = aHttp.getRequestURI().getPath().toLowerCase();
            uri = uri.equals("/") ? "./index.html" : "."+uri;

            Headers hres = aHttp.getResponseHeaders();

            String tipo = uri.endsWith("css") ? "text/css" 
              : uri.endsWith("js") ? "text/javascript" 
              : "text/html";
            
            
            hres.set("Content-Type", tipo+"; charset=UTF-8");
            
            byte[] bytes = Files.readAllBytes(Paths.get(uri));
            
            if(uri.equals("./index.html")){ 
              String conteudo = new String(
                  Files.readAllBytes(Paths.get(System.getProperty("MD"))), StandardCharsets.UTF_8);
                  
              bytes = new String(bytes).replace("</textarea>",conteudo+"</textarea>")
                .getBytes(StandardCharsets.UTF_8);
            }

			      aHttp.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = aHttp.getResponseBody()) {
				      os.write(bytes);
				      os.flush();
			      }
			    }catch(Exception e){
            e.printStackTrace();
          }
          aHttp.close();
        }
      });

      http.start();
      System.out.println("Rodando na porta "+port);
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  
}
