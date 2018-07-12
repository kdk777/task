package edu.csuft.kdk.fileserver;

/**
 * 网盘服务器的启动器
 * 
 * @author mockingbird (kdk777@163.com)
 * 
 * */
public class ServerApp {
	public static void main(String[] args) {
		Server server = new Server();
		server.start();
	}
}