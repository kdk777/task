package edu.csuft.kdk.fileserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ���̷�����
 * 
 * @author mockingbird (kdk777@163.com)
 * 
 * */
public class Server {
	
	/**
	 * ������׽���
	 */
	ServerSocket serverSocket;
	
	/**
	 * ����˶˿ں�
	 */
	int port = 9000;
	
	/**
	 * �̳߳�
	 */
	ExecutorService pool;
	
	public void start() {
		pool = Executors.newCachedThreadPool();//���޴�С���̳߳�
		try {
			serverSocket = new ServerSocket(port);
			while(true) {
				Socket socket = serverSocket.accept(); //�����׽���socket���൱�ڽ����ܵ���ÿ��accept()����һ������
				pool.execute(new FileReceiveByServer(socket));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
