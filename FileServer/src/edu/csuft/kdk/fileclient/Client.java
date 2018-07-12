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
 * ���̿ͻ���
 * 
 * @author mockingbird (kdk777@163.com)
 * 
 */
public class Client {

	/**
	 * �׽��֣���װ������ͨ���еĵײ�ϸ��(like����) ��������������� ����������������
	 */
	Socket socket;

	/**
	 * �������ĵ�ַ
	 */
	String address = ""; // ��Ϊ����Ϊ��127.0.0.1

	/**
	 * �������Ķ˿ں�
	 */
	int port = 9000;

	//�ϴ��ļ���·��
	String fileAddress;

	JFrame frm = new JFrame("�ͻ����ϴ�/���ؽ���");
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

		l1 = new JLabel("�������������IP:");
		l1.setBounds(20, 10, 120, 25);
		p1.setBackground(Color.yellow);
		p1.add(l1);

		t1 = new JTextField();
		t1.setBounds(150, 10, 130, 25);
		t1.setText("127.0.0.1");
		p1.add(t1);

		l2 = new JLabel("(Ĭ�϶˿�9000)");
		l2.setBounds(295, 10, 120, 25);
		p1.add(l2);

		/************ �������ļ��ϴ����ֵĽ������ ******************/
		
		p2 = new JPanel();
		p2.setBounds(0, 45, 510, 100);
		p2.setBackground(Color.cyan);
		p2.setLayout(null);

		l3 = new JLabel("�ӿͻ���ѡ����Ҫ�ϴ��ļ�:");
		l3.setBounds(20, 15, 160, 25);
		p2.add(l3);

		btn1 = new JButton("���ѡ��");
		btn1.setBounds(190, 15, 100, 25);
		p2.add(btn1);

		l4 = new JLabel("���ϴ��ļ�Ϊ:");
		l4.setBounds(20, 55, 120, 25);
		p2.add(l4);

		t2 = new JTextField();
		t2.setBounds(110, 55, 250, 25);
		t2.setEditable(false);
		p2.add(t2);

		btn2 = new JButton("ȷ���ϴ�");
		btn2.setBounds(380, 55, 100, 25);
		p2.add(btn2);

		/************ �������ļ����ز��ֵĽ������ ******************/
		p3 = new JPanel();
		p3.setBounds(0, 150, 510, 100);
		p3.setBackground(Color.pink);
		p3.setLayout(null);

		l5 = new JLabel("�ӷ�����������Ҫ�����ļ�:");
		l5.setBounds(20, 20, 160, 25);
		p3.add(l5);

		btn3 = new JButton("�������");
		btn3.setBounds(190, 20, 100, 25);
		p3.add(btn3);

		l5 = new JLabel("�������ļ�Ϊ:");
		l5.setBounds(20, 60, 120, 25);
		p3.add(l5);

		t3 = new JTextField();
		t3.setBounds(110, 60, 250, 25);
		t3.setEditable(false);
		p3.add(t3);

		btn4 = new JButton("ȷ������");
		btn4.setBounds(380, 60, 100, 25);
		p3.add(btn4);

		JLabel l7=new JLabel("(���ز���δ��ʵ��Ҫ��)");
		l7.setBounds(315, 20, 160, 25);
		p3.add(l7);

		
		frm.add(p1);
		frm.add(p2);
		frm.add(p3);

		
		
		/*************************/
		/*********�� �� �� ��*********/
		/*************************/
		//��ťbtn1�ĵ���¼�
		btn1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				if (!"����ѡ���ļ�!".equals(t2.getText())) {
					t2.setForeground(Color.black);
				}
				
				chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				//������ʼ·��Ϊ����
				FileSystemView fsv = FileSystemView.getFileSystemView();
				chooser.setCurrentDirectory(fsv.getHomeDirectory());
				
				//��ʾchooser
				chooser.showDialog(new JLabel(), "ѡ��");

				File file = chooser.getSelectedFile();
				//������ʵֻ��Ϊ�ļ����ͣ���Ϊǰ������ΪFILES_ONLY
				if (file != null) {
					if (file.isFile()) {
						fileAddress = file.getAbsolutePath();
					}
					t2.setForeground(Color.black);
					t2.setText(file.getAbsolutePath());
				}
			}
		});

		
		//��ťbtn2�ĵ���¼�
		btn2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				if ("".equals(t2.getText()) || t2.getForeground() == Color.red) {
					t2.setText("����ѡ���ļ�!");
					t2.setForeground(Color.red);
				}

				else {
					try {
						address = t1.getText();
						socket = new Socket(address, port);
						
						/*������Ҫ�ϲ��ļ������ļ���С���ļ�����*/
						File tempFile = new File(fileAddress.trim());
						String fileName = tempFile.getName();
						String fileSize = String.valueOf(tempFile.length());
						
						FileInputStream filestream;      //�ļ�����
						ByteArrayInputStream nameStream; //�ļ���
						ByteArrayInputStream lengStream; //�ļ���С
						SequenceInputStream siStream;
						OutputStream out=socket.getOutputStream();
						
						//0.�����ļ��ļ�����
						filestream = new FileInputStream(tempFile); //��ȡ�ļ�����
					
						//1.�����ļ����Ĳ���
						byte[] fileName1 = fileName.getBytes();
						byte[] fileName2 = new byte[512];
						for (int i = 0; i < fileName1.length; i++)
						{
							fileName2[i] = fileName1[i];
						}
						nameStream = new ByteArrayInputStream(fileName2);

						//2.�����ļ����ȵĲ���
						byte[] fileLenth1 = String.valueOf(fileSize).getBytes();
						byte[] fileLenth2 = new byte[32];
						for (int j = 0; j < fileLenth1.length; j++)
						{
							fileLenth2[j] = fileLenth1[j];
						}
						lengStream = new ByteArrayInputStream(fileLenth2);
						
						
						//�����ļ����ϲ�����
						Vector<InputStream> vec = new Vector<>();
						vec.addElement(nameStream);
						vec.addElement(lengStream);
						vec.addElement(filestream);
						Enumeration<InputStream> enumr = vec.elements();
						siStream = new SequenceInputStream(enumr);
						
						//������siStream�����ݶ����ڴ���
						BufferedInputStream in = new BufferedInputStream(siStream, 1024 * 32);
						
						byte[] buf = new byte[1024 * 4]; //�����Сȡ����Ӳ���ļ�ϵͳ�� ֻ���ļ���ʽ���л���(һ��һ�ж�ȡ)
						int size;
						//bufÿ����ԪΪ4K;��û������ʱ,����"*-1"; ����ע��������(�ϴεĲ�������)����
						while(-1 != (size = in.read(buf))) {
							// ʹ���׽��ֻ��������������͡�����
							out.write(buf, 0, size); //out=socket.getOutputStream();
							// ˢ�»�����
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
