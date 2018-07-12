package edu.csuft.kdk.fileserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 网盘服务器
 * 
 * @author mockingbird (kdk777@163.com)
 * 
 * */
public class Server {
	
	/**
	 * 服务端套接字
	 */
	ServerSocket serverSocket;
	
	/**
	 * 服务端端口号
	 */
	int port = 9000;
	
	/**
	 * 线程池
	 */
	ExecutorService pool;
	
	public void start() {
		pool = Executors.newCachedThreadPool();//无限大小的线程池
		try {
			serverSocket = new ServerSocket(port);
			while(true) {
				Socket socket = serverSocket.accept(); //这里套接字socket，相当于建立管道；每次accept()接受一个请求；
				pool.execute(new FileReceiveByServer(socket));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
