package ro.pub.cs.systems.pdsd.practicaltest02;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import ro.pub.cs.systems.pdsd.practicaltest02.Alarm.AlarmStatus;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class PracticalTest02MainActivity extends Activity {
	
	private EditText portText = null;
	private Button startServerBtn = null;
	private EditText hour = null;
	private EditText minute = null;
	private Button setBtn = null;
	private Button resetBtn = null;
	private Button pollBtn = null;
	private TextView responseText;
	
	private Server server;
	int port;
	
	
	private class Server extends Thread {
		
		private ServerSocket ssocket = null;
		private HashMap<String, Alarm> alarms;
		
		public Server(int port) {
			
			try {
				
				ssocket = new ServerSocket(port);
				Log.d("Server", "Socket binded on port: " + port);
			} catch (IOException e) {

				Log.d("Server", "Socket binding failed: " + e.getMessage());
			}
			
			alarms = new HashMap<String, Alarm>();
		}
		
		@Override
		public void run() {
			
			if(ssocket != null) {
				
				while(!isInterrupted()) {
					
					try {
						
						Socket socket = ssocket.accept();
						resolve(socket);
						
					} catch (IOException e) {
						
						Log.d("Server", "Accept failed: " + e.getMessage());
					}
				}
			}
		}
		
		private void resolve(Socket socket) {
			
			String clientId = socket.getInetAddress().getHostAddress();
			
			try {
				
				BufferedReader reader = Utilities.getReader(socket);
				PrintWriter writer = Utilities.getWriter(socket);
				
				String command = reader.readLine();
				String response = "";
				
				if(command.equals("reset")) {
					
					alarms.remove(clientId);
					writer.println("Reset done");
				}
				
				if(command.equals("poll")) {
					Log.d("Server", "Poll ");
					if(!alarms.containsKey(clientId)) {
						
						response = "none";
					} else {
					
						if(alarms.get(clientId).getStatus() == AlarmStatus.Active) {
							
							response = "active";
						} else {
						
							HashMap<String, Integer> crtInfo = getCurrentHourAndMin();
							if(!crtInfo.containsKey("hour") || !crtInfo.containsKey("minute")) {
								
								response = "error";
							}
							else {
								
								int crtHour = crtInfo.get("hour");
								int crtMin = crtInfo.get("minute");
								Alarm alarm = alarms.get(clientId);
								
								if(alarm.getHour() < crtHour || (alarm.getHour() == crtHour && alarm.getMinute() < crtMin)) {
									alarm.setStatus(AlarmStatus.Active);
									response = "active";
								} else {
									
									response = "inactive";
								}
							}
						}
					}
					
					writer.println(response);
				}
				
				if(command.startsWith("set")) {
					
					String[] tokens = command.split(",");
					alarms.put(clientId, new Alarm(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2])));
					writer.println("Alarm Set");
				}
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
		}
		
		private HashMap<String, Integer> getCurrentHourAndMin() {
			
			HashMap<String, Integer> result = new HashMap<String, Integer>();
			
			try {
				  HttpClient httpClient = new DefaultHttpClient();
				  HttpGet httpGet = new HttpGet("http://www.timeapi.org/utc/now");				  
				  ResponseHandler<String> responseHandler = new BasicResponseHandler();
				  String content = httpClient.execute(httpGet, responseHandler);
				  if (content != null) {  
					  //extract hour and minute;
					  //2015-05-25T10:20:25+01:00
					  
					  Log.d("Server", "Poll content: " + content);
					  
					  int start = content.indexOf("T");
					  int end = content.indexOf("+");
					  String time = content.substring(start + 1, end);
					  String[] tokens = time.split(":");
					  Log.d("Server", Arrays.toString(tokens));
					  result.put("hour", Integer.parseInt(tokens[0]));
					  result.put("minute", Integer.parseInt(tokens[1]));
				  } else {
					  
					  
				  }
				  
				  
				  
				} catch (Exception exception) {
					
				    exception.printStackTrace();
				}
			
			return result;
		}
		
		private void stopThread() {
			
			if(!isInterrupted()) {
				interrupt();
				if(ssocket != null) {
					
					try {
						ssocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	private class Client extends Thread {
		
		String command;
		int port;
		
		
		
		 public Client(String command, int port) {
			super();
			this.command = command;
			this.port = port;
		}



		@Override
		public void run() {
			
			try {
				Socket socket = new Socket("localhost", port);
				
				BufferedReader reader = Utilities.getReader(socket);
				PrintWriter writer = Utilities.getWriter(socket);
				
				writer.println(command);
				final String response = reader.readLine();
				
				responseText.post(new Runnable() {
					
					@Override
					public void run() {
						
						responseText.setText(response);
					}
				});
				
				//Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG);
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		 
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02_main);
        
        portText = (EditText)findViewById(R.id.port);
        startServerBtn = (Button)findViewById(R.id.startServer);
        hour = (EditText)findViewById(R.id.hour);
        minute = (EditText)findViewById(R.id.minute);
        setBtn = (Button)findViewById(R.id.set);
        resetBtn = (Button)findViewById(R.id.reset);
        pollBtn = (Button)findViewById(R.id.poll);
        responseText = (TextView)findViewById(R.id.response);
        
        
        startServerBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				port = Integer.parseInt(portText.getText().toString());
				
				server = new Server(port);
				server.start();
			}
		});
        
        setBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				EditText hour = (EditText)findViewById(R.id.hour);
		        EditText minute = (EditText)findViewById(R.id.minute);
		        
		        Client client = new Client("set," + hour.getText().toString() + "," + minute.getText().toString() , port);
		        client.start();
			}
		});
        
        pollBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        
		        Client client = new Client("poll", port);
		        client.start();
			}
		});
        
        resetBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
		        
		        Client client = new Client("reset", port);
		        client.start();
			}
		});
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.practical_test02_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onDestroy() {
    	
    	if(server != null) {
    		
    		server.stopThread();
    	}
    	
    	super.onDestroy();
    }
}
