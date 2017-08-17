package remote.remote;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

@SuppressWarnings("deprecation")
public class Main {

	private Socket socket;
	String HOST = 
//			"localhost";
			"138.197.15.195";
	int PORT = 3002;
	String PASSWORD;
	
	String ip;
	
	public static void main(String[] args){
		new Main();
	}
	
	int w = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	int h = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
	boolean looping = true;
	Robot robot;
	BufferedReader in;
	
	public static String getIp() throws Exception {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            String ip = in.readLine();
            return ip;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
	
	public Main(){
		Scanner input = new Scanner(System.in);
		System.out.print("Enter your password: ");
		PASSWORD = input.nextLine();	
		input.close();
		try{
			socket = new Socket(HOST,(PORT+1));
			robot = new Robot();
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println(PASSWORD);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			try{ip = getIp();}catch(Exception e){e.printStackTrace();}
		    if(HOST.equalsIgnoreCase("localhost")) ip = "127.0.0.1";
			System.out.println("Computer IP: " + ip);
			
			new Thread(new Runnable(){
				public void run() {
					try{
						do{
							BufferedImage image = robot.createScreenCapture(new Rectangle(0,0,w,h));
							sendImage(image);
							System.out.println("Sending screenshot...");
					        Thread.sleep(300);
						}while(looping);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				
			}).start();
			new Thread(new Runnable(){
				public void run(){
					while(true){
						String line;
						try {
							while((line = in.readLine()) != null){
								handleInput(line);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
				
	        
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
	}
	
	public void handleInput(String line){
		Data json = new Gson().fromJson(line,Data.class);
		System.out.println("Receiving: " + json.type);
		
		try{
			switch(json.type){
				case "mousepress":
					pressMouse(json.button, json.x, json.y);
					break;
				case "mouserelease":
					releaseMouse(json.button);
					break;
				case "mousemove":
					moveMouse(json.x,json.y);
				case "mousedrag":
					dragMouse(json.x,json.y);
					break;
				case "keypress":
					pressKey(json.keyCode);
					break;
				case "keyrelease":
					releaseKey(json.keyCode);
					break;
				case "mousescroll":
					scrollMouse(json.amount);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		 	
	}
	
	public void pressMouse(String button, double x, double y) throws Exception{
		int b;
		if(button.equals("left") || button.equals("0")){
			b = InputEvent.BUTTON1_DOWN_MASK;
		}else if(button.equals("right")){
			b = InputEvent.BUTTON3_DOWN_MASK;
		}else{
			b = InputEvent.BUTTON2_DOWN_MASK;
		} 
		
		robot.mouseMove((int) x, (int) y);
		robot.mousePress(b);
	}
	
	public void releaseMouse(String button) throws Exception{
		int b;
		if(button.equals("left") || button.equals("0")){
			b = InputEvent.BUTTON1_DOWN_MASK;
		}else if(button.equals("right")){
			b = InputEvent.BUTTON3_DOWN_MASK;
		}else{
			b = InputEvent.BUTTON2_DOWN_MASK;
		}
		
		robot.mouseRelease(b);
	}
	
	public void scrollMouse(double amount) throws Exception{
		robot.mouseWheel((int) amount);
	}
	
	public void moveMouse(double x, double y) throws Exception{
		robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		robot.mouseMove((int) x, (int) y);
	}
	
	public void dragMouse(double x, double y) throws Exception{
		robot.mouseMove((int) x, (int) y);
	}
	
	public void pressKey(int keyCode) throws Exception{
		keyCode = convertKeyCode(keyCode);
		robot.keyPress(keyCode);
	}
	
	public void releaseKey(int keyCode) throws Exception{
		keyCode = convertKeyCode(keyCode);
		robot.keyRelease(keyCode);
	}
	
	public int convertKeyCode(int keyCode){
		if(keyCode == 13) keyCode = KeyEvent.VK_ENTER;
		if(keyCode == 186) keyCode = KeyEvent.VK_SEMICOLON;
		if(keyCode == 190) keyCode = KeyEvent.VK_PERIOD;
		if(keyCode == 187) keyCode = KeyEvent.VK_EQUALS;
		if(keyCode == 219) keyCode = KeyEvent.VK_OPEN_BRACKET;
		if(keyCode == 221) keyCode = KeyEvent.VK_CLOSE_BRACKET;
		if(keyCode == 188) keyCode = KeyEvent.VK_COMMA;
		if(keyCode == 191) keyCode = KeyEvent.VK_SLASH;
		if(keyCode == 220) keyCode = KeyEvent.VK_BACK_SLASH;
		if(keyCode == 189) keyCode = KeyEvent.VK_MINUS;
		return keyCode;
	}

	public void sendImage( BufferedImage image ) throws IOException
	{
	    
	    File file = new File(ip+".jpg");
	    ImageIO.write(image, "jpg", file);
	    @SuppressWarnings({ "resource" })
		HttpClient client = new DefaultHttpClient();
	    HttpPost post = new HttpPost("http://" + HOST + ":" + PORT + "/upload");
	    ContentBody fileBody = new FileBody(file);
	    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
	    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
	    builder.addPart("userfile", fileBody);
	    HttpEntity entity = builder.build();
	    post.setEntity(entity);
	    HttpResponse response = client.execute(post);
	    String jsonText = EntityUtils.toString(response.getEntity());
	}
}

class Response{
	public String response;
}
