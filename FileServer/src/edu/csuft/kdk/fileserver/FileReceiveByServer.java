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
 * 定义文件上传的具体操作，交给线程池执行的一个任务
 * 
 * @author mockingbird (kdk777@163.com)
 * 
 */
// 实现Runnable接口
public class FileReceiveByServer implements Runnable {

	/*配置JDBC相关信息*/
	static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/fileserver?characterEncoding=utf-8&useSSL=true";

	//数据库的账号和密码
	static final String USER = "root";
	static final String PASS = "1234";

	Connection conn = null;
	Statement stmt = null;

	boolean flag = true;

	/**
	 * 服务器与客户端通信的连接(管道)
	 */
	Socket socket;

	public FileReceiveByServer(Socket newSocket) {
		this.socket = newSocket;
	}

	@Override
	public void run() {
		// 客户端上传的文件名
		String name = null;
		// 客户端上传的文件长度
		String length = null;
		// 客户端IP地址
		InetAddress ipAddress = socket.getInetAddress();
		String ip = String.valueOf(ipAddress);
		
		// 存储文件的路径
		String path = "files";
		// 根据文件内容的散列值生成服务器端保存的文件名
		String fileName = "";


		/*这里需要拆分文件名、文件大小、文件数据*/
		//接受数据
		//容量可变的内存数组
		ByteArrayOutputStream ram = new ByteArrayOutputStream();

		byte[] buf = new byte[1024 * 4];
		int size;
		try (InputStream in = socket.getInputStream()) {
			byte[] namebyte = new byte[512];
			byte[] lengthbyte = new byte[32];
			in.read(namebyte);
			in.read(lengthbyte);
			//服务器接受到的文件名称
			name = new String(namebyte).trim();
			//服务器接受到的文件长度
			length = new String(lengthbyte).trim();

			//这里是文件数据部分
			while (-1 != (size = in.read(buf))) {
				ram.write(buf, 0, size);
			}
		} catch (Exception e) {
			
		}

		//获得所有文件数据
		byte[] data = ram.toByteArray();
		

		//生产文件的消息摘要(SHA-256),使用摘要信息做文件名
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
			fileName = new BigInteger(1, hash).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		/*这里实现秒传，就是判断上面生成的文件名为散列值形式的fileName是否存在在服务器中。若有即无需上传，但数据库仍需要记录客户端上传信息。*/
		File file = new File(path); //存放路径
		String[] fileNameInServer = file.list();//文件名列表
		
		//开始遍历服务器文件
		for (int i = 0; i < fileNameInServer.length; i++) {
			if (fileName.equals(fileNameInServer[i])) {
				/*这里设置为客户端(用户)无需知道文件是否秒传*/
				System.out.println("下面文件已经存在，无需重复上传(即\"秒传\")"+fileName);
				flag = false; //若有重复，则无需重新上传的flag
			}
		}

		if (flag == true) {
			// 写入新的文件
			try(FileOutputStream out = new FileOutputStream(new File(path, fileName))) {
				out.write(data);
				out.flush();
			} catch (Exception e) {
				
			}
		}

		/*客户端文件上传的信息写入服务器端的数据库中*/
		try {
			Class.forName(JDBC_DRIVER);
			conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/fileserver?serverTimezone=GMT", USER, PASS);
			stmt = conn.createStatement();
			String sql = "insert into fileinfo(name,size,hash,ip) values (?,?,?,?)"; //使用占位符
			PreparedStatement sta = conn.prepareStatement(sql);
			sta.setString(1, name);
			sta.setString(2, length);
			sta.setString(3, fileName);
			sta.setString(4, ip);

			int rows = sta.executeUpdate();
			if (rows > 0) {
				System.out.println("写入数据库成功!");
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
