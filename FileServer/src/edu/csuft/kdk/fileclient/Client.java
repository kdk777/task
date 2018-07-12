package edu.csuft.kdk.fileclient;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;

/**
 * 网盘客户端
 * 
 * @author mockingbird (kdk777@163.com)
 * 
 */
public class Client {

	/**
	 * 套接字：封装了网络通信中的底层细节(like插座) 输出流：发送数据 输入流：接受数据
	 */
	Socket socket;

	/**
	 * 服务器的地址
	 */
	String address = ""; // 若为本机为：127.0.0.1

	/**
	 * 服务器的端口号
	 */
	int port = 9000;

	//上传文件的路径
	String fileAddress;

	JFrame frm = new JFrame("客户端上传/下载界面");
	JPanel p1, p2, p3;
	JLabel l1, l2, l3, l4, l5, l6;
	JTextField t1, t2, t3;
	JButton btn1, btn2, btn3, btn4;
	JFileChooser chooser;

	public void init() {
		frm.setBounds(100, 100, 510, 288);
		frm.setLayout(null);

		p1 = new JPanel();
		p1.setBounds(0, 0, 510, 40);
		p1.setLayout(null);

		l1 = new JLabel("请输入服务器端IP:");
		l1.setBounds(20, 10, 120, 25);
		p1.setBackground(Color.yellow);
		p1.add(l1);

		t1 = new JTextField();
		t1.setBounds(150, 10, 130, 25);
		t1.setText("127.0.0.1");
		p1.add(t1);

		l2 = new JLabel("(默认端口9000)");
		l2.setBounds(295, 10, 120, 25);
		p1.add(l2);

		/************ 以下是文件上传部分的界面代码 ******************/
		
		p2 = new JPanel();
		p2.setBounds(0, 45, 510, 100);
		p2.setBackground(Color.cyan);
		p2.setLayout(null);

		l3 = new JLabel("从客户端选择需要上传文件:");
		l3.setBounds(20, 15, 160, 25);
		p2.add(l3);

		btn1 = new JButton("点击选择");
		btn1.setBounds(190, 15, 100, 25);
		p2.add(btn1);

		l4 = new JLabel("所上传文件为:");
		l4.setBounds(20, 55, 120, 25);
		p2.add(l4);

		t2 = new JTextField();
		t2.setBounds(110, 55, 250, 25);
		t2.setEditable(false);
		p2.add(t2);

		btn2 = new JButton("确认上传");
		btn2.setBounds(380, 55, 100, 25);
		p2.add(btn2);

		/************ 以下是文件下载部分的界面代码 ******************/
		p3 = new JPanel();
		p3.setBounds(0, 150, 510, 100);
		p3.setBackground(Color.pink);
		p3.setLayout(null);

		l5 = new JLabel("从服务器查找需要下载文件:");
		l5.setBounds(20, 20, 160, 25);
		p3.add(l5);

		btn3 = new JButton("点击查找");
		btn3.setBounds(190, 20, 100, 25);
		p3.add(btn3);

		l5 = new JLabel("所下载文件为:");
		l5.setBounds(20, 60, 120, 25);
		p3.add(l5);

		t3 = new JTextField();
		t3.setBounds(110, 60, 250, 25);
		t3.setEditable(false);
		p3.add(t3);

		btn4 = new JButton("确认下载");
		btn4.setBounds(380, 60, 100, 25);
		p3.add(btn4);

		JLabel l7=new JLabel("(下载部分未作实现要求)");
		l7.setBounds(315, 20, 160, 25);
		p3.add(l7);

		
		frm.add(p1);
		frm.add(p2);
		frm.add(p3);

		
		
		/*************************/
		/*********事 件 部 分*********/
		/*************************/
		//按钮btn1的点击事件
		btn1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if (!"请先选择文件!".equals(t2.getText())) {
					t2.setForeground(Color.black);
				}
				
				chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				//设置起始路径为桌面
				FileSystemView fsv = FileSystemView.getFileSystemView();
				chooser.setCurrentDirectory(fsv.getHomeDirectory());
				
				//显示chooser
				chooser.showDialog(new JLabel(), "选择");

				File file = chooser.getSelectedFile();
				//这里其实只能为文件类型，因为前面设置为FILES_ONLY
				if (file != null) {
					if (file.isFile()) {
						fileAddress = file.getAbsolutePath();
					}
					t2.setForeground(Color.black);
					t2.setText(file.getAbsolutePath());
				}
			}
		});

		
		//按钮btn2的点击事件
		btn2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				if ("".equals(t2.getText()) || t2.getForeground() == Color.red) {
					t2.setText("请先选择文件!");
					t2.setForeground(Color.red);
				}

				else {
					try {
						address = t1.getText();
						socket = new Socket(address, port);
						
						/*这里需要合并文件名、文件大小、文件数据*/
						File tempFile = new File(fileAddress.trim());
						String fileName = tempFile.getName();
						String fileSize = String.valueOf(tempFile.length());
						
						FileInputStream filestream;      //文件数据
						ByteArrayInputStream nameStream; //文件名
						ByteArrayInputStream lengStream; //文件大小
						SequenceInputStream siStream;
						OutputStream out=socket.getOutputStream();
						
						//0.这是文件文件数据
						filestream = new FileInputStream(tempFile); //获取文件数据
					
						//1.这是文件名的部分
						byte[] fileName1 = fileName.getBytes();
						byte[] fileName2 = new byte[512];
						for (int i = 0; i < fileName1.length; i++)
						{
							fileName2[i] = fileName1[i];
						}
						nameStream = new ByteArrayInputStream(fileName2);

						//2.这是文件长度的部分
						byte[] fileLenth1 = String.valueOf(fileSize).getBytes();
						byte[] fileLenth2 = new byte[32];
						for (int j = 0; j < fileLenth1.length; j++)
						{
							fileLenth2[j] = fileLenth1[j];
						}
						lengStream = new ByteArrayInputStream(fileLenth2);
						
						
						//这是文件流合并部分
						Vector<InputStream> vec = new Vector<>();
						vec.addElement(nameStream);
						vec.addElement(lengStream);
						vec.addElement(filestream);
						Enumeration<InputStream> enumr = vec.elements();
						siStream = new SequenceInputStream(enumr);
						
						//输入流siStream：数据读到内存中
						BufferedInputStream in = new BufferedInputStream(siStream, 1024 * 32);
						
						byte[] buf = new byte[1024 * 4]; //缓冲大小取决于硬盘文件系统； 只有文件格式才有换行(一行一行读取)
						int size;
						//buf每个单元为4K;当没有数据时,返回"*-1"; 这里注意脏数据(上次的残留数据)问题
						while(-1 != (size = in.read(buf))) {
							// 使用套接字获得输出流，【发送】数据
							out.write(buf, 0, size); //out=socket.getOutputStream();
							// 刷新缓冲区
							out.flush();
						}

					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (socket != null) {
							try {
								socket.close();
							} catch (IOException e) {
								socket = null;
							}
						}
					}
				}
			}
		});

		frm.setVisible(true);
	}
}
