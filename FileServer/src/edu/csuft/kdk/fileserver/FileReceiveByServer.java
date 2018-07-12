package edu.csuft.kdk.fileserver;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * �����ļ��ϴ��ľ�������������̳߳�ִ�е�һ������
 * 
 * @author mockingbird (kdk777@163.com)
 * 
 */
// ʵ��Runnable�ӿ�
public class FileReceiveByServer implements Runnable {

	/*����JDBC�����Ϣ*/
	static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/fileserver?characterEncoding=utf-8&useSSL=true";

	//���ݿ���˺ź�����
	static final String USER = "root";
	static final String PASS = "1234";

	Connection conn = null;
	Statement stmt = null;

	boolean flag = true;

	/**
	 * ��������ͻ���ͨ�ŵ�����(�ܵ�)
	 */
	Socket socket;

	public FileReceiveByServer(Socket newSocket) {
		this.socket = newSocket;
	}

	@Override
	public void run() {
		// �ͻ����ϴ����ļ���
		String name = null;
		// �ͻ����ϴ����ļ�����
		String length = null;
		// �ͻ���IP��ַ
		InetAddress ipAddress = socket.getInetAddress();
		String ip = String.valueOf(ipAddress);
		
		// �洢�ļ���·��
		String path = "files";
		// �����ļ����ݵ�ɢ��ֵ���ɷ������˱�����ļ���
		String fileName = "";


		/*������Ҫ����ļ������ļ���С���ļ�����*/
		//��������
		//�����ɱ���ڴ�����
		ByteArrayOutputStream ram = new ByteArrayOutputStream();

		byte[] buf = new byte[1024 * 4];
		int size;
		try (InputStream in = socket.getInputStream()) {
			byte[] namebyte = new byte[512];
			byte[] lengthbyte = new byte[32];
			in.read(namebyte);
			in.read(lengthbyte);
			//���������ܵ����ļ�����
			name = new String(namebyte).trim();
			//���������ܵ����ļ�����
			length = new String(lengthbyte).trim();

			//�������ļ����ݲ���
			while (-1 != (size = in.read(buf))) {
				ram.write(buf, 0, size);
			}
		} catch (Exception e) {
			
		}

		//��������ļ�����
		byte[] data = ram.toByteArray();
		

		//�����ļ�����ϢժҪ(SHA-256),ʹ��ժҪ��Ϣ���ļ���
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
			fileName = new BigInteger(1, hash).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		/*����ʵ���봫�������ж��������ɵ��ļ���Ϊɢ��ֵ��ʽ��fileName�Ƿ�����ڷ������С����м������ϴ��������ݿ�����Ҫ��¼�ͻ����ϴ���Ϣ��*/
		File file = new File(path); //���·��
		String[] fileNameInServer = file.list();//�ļ����б�
		
		//��ʼ�����������ļ�
		for (int i = 0; i < fileNameInServer.length; i++) {
			if (fileName.equals(fileNameInServer[i])) {
				/*��������Ϊ�ͻ���(�û�)����֪���ļ��Ƿ��봫*/
				System.out.println("�����ļ��Ѿ����ڣ������ظ��ϴ�(��\"�봫\")"+fileName);
				flag = false; //�����ظ��������������ϴ���flag
			}
		}

		if (flag == true) {
			// д���µ��ļ�
			try(FileOutputStream out = new FileOutputStream(new File(path, fileName))) {
				out.write(data);
				out.flush();
			} catch (Exception e) {
				
			}
		}

		/*�ͻ����ļ��ϴ�����Ϣд��������˵����ݿ���*/
		try {
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/fileserver?serverTimezone=GMT", USER, PASS);
			stmt = conn.createStatement();
			String sql = "insert into fileinfo(name,size,hash,ip) values (?,?,?,?)"; //ʹ��ռλ��
			PreparedStatement sta = conn.prepareStatement(sql);
			sta.setString(1, name);
			sta.setString(2, length);
			sta.setString(3, fileName);
			sta.setString(4, ip);

			int rows = sta.executeUpdate();
			if (rows > 0) {
				System.out.println("д�����ݿ�ɹ�!");
			}
			sta.close();

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
