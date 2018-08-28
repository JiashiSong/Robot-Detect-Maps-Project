import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Communication extends Thread {

	private Rover rover;
	
	public Communication(Rover rover) {
		this.rover = rover;
	}
	
	public void run() {
		ServerSocket serv;
		try {
			serv = new ServerSocket(19232);
			Socket socket = serv.accept();
			rover.setHostAddress((InetSocketAddress) socket.getRemoteSocketAddress());
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// the listener with the while readline
			String line;
			while ((line = reader.readLine()) != "STOP" && rover.isRunning()) {
				String[] command = line.split(" ");
				switch (command[0]) {
				case "UP-PRESS":
					rover.forward();
					break;
				case "UP-RELEASE":
					rover.stop();
					break;
				case "DOWN-PRESS":
					rover.backward();
					break;
				case "DOWN-RELEASE":
					rover.stop();
					break;
				case "LEFT-PRESS":
					rover.turnL();
					break;
				case "LEFT-RELEASE":
					rover.stop();
					break;
				case "RIGHT-PRESS":
					rover.turnR();
					break;
				case "RIGHT-RELEASE":
					rover.stop();
					break;
				case "EXIT":
					rover.stopRunning();
					break;
				case "STATE-AUTO":
					rover.stop();
					rover.setAutoControl();
					break;
				case "STATE-MANUAL":
					rover.stop();
					rover.setManualControl();
					break;
				case "ADDNGZ":
					rover.addNGZ(Double.valueOf(command[1]),Double.valueOf(command[2]),Double.valueOf(command[3]),Double.valueOf(command[4]));
					break;
				case "REMOVENGZ":
					rover.removeNGZ(Double.valueOf(command[1]),Double.valueOf(command[2]),Double.valueOf(command[3]),Double.valueOf(command[4]));
					break;
				default:
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
