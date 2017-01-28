package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import com.google.gson.Gson;

public class urlTest {

	public static void main(String[] args) throws ClientProtocolException, IOException {
    	Object resultado=null;
		String inputLine;
		StringBuffer responseBuffer = new StringBuffer();
		BufferedReader br;
		
		String urlBase = "https://api.genderize.io/";
		String resource = "?name=maria";
		
		HttpClient client = new DefaultHttpClient();
		
		HttpGet requestGet = new HttpGet(urlBase+resource);
		//requestGet.addHeader("Content-Type", "text/plain");
		requestGet.setHeader("Content-Type", "application/json");
		requestGet.addHeader("accept", "application/json");
		//requestGet.addHeader("accept-encoding", "gzip, deflate");
		//requestGet.addHeader("accept-language", "en-US,en;q=0.8");
		//requestGet.setHeader("Accept-Encoding", "text/plain");
		HttpResponse responseGet = client.execute(requestGet);
        int responseCode = responseGet.getStatusLine().getStatusCode();
		
        if (responseGet.getStatusLine().getStatusCode() == 200 || responseGet.getStatusLine().getStatusCode() == 204) {

            br = new BufferedReader(
                    new InputStreamReader((responseGet.getEntity().getContent())));


	        responseBuffer.setLength(0);
	        while ((inputLine = br.readLine()) != null) {
	        	System.out.println("line: "+inputLine);
	        	responseBuffer.append(inputLine);
	        }
	        
	        JSONObject joResult = new JSONObject(responseBuffer.toString());
	        
	        System.out.println(joResult.get("name"));
	        System.out.println(joResult.get("gender"));
	        System.out.println(joResult.get("probability"));
	        System.out.println(joResult.get("count"));
	        
	        
	        //resultado = new Gson().toJson(responseBuffer.toString());
	        //resultado = responseBuffer;
        }
        else{
            System.out.println(responseGet.getStatusLine().getStatusCode());

            throw new RuntimeException("Failed : HTTP error code : "
                    + responseGet.getStatusLine().getStatusCode());
        }
        
        resultado = new Gson().toJson(responseBuffer.toString());
        //resultado = responseBuffer.toString();
		//return resultado;
        


	}
	
	

}
