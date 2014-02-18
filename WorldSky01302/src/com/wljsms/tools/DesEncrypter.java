package com.wljsms.tools;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;



//cxy ��� Androidʵ��DES���ַ���� http://zhanglfat.iteye.com/blog/1669678 Ȼ�����base64�����д
public class DesEncrypter {
	
	static String password="tcxyctxy";//���������������룬���������������ظ�����
	static String charset="UTF-8";

	/*<���ַ����Des���ܣ����ַ�ת��Ϊ�ֽ��������>*/
	public static String encode(String str) 
	{
		byte[] datasource = null;
		try {
			datasource = str.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		byte[] datadest=desCrypto(datasource,password+password);
		return Base64.encode(datadest);
	}
	
	public static String decode(String base64String) 
	{
		byte[] datadest = Base64.decode(base64String);
		byte[] datasource = new byte[0];
		try {
			datasource = decrypt(datadest,password+password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String str = new String(datasource);
		return str;
	}
	
	/*<���ַ����Des���ܣ����ַ�ת��Ϊ�ֽ��������>*/
	public static byte[] desCrypto(byte[] datasource, String password)
	{
		try
		{
			SecureRandom random = new SecureRandom();
			DESKeySpec desKey = new DESKeySpec(password.getBytes());

			// ����һ���ܳ׹�����Ȼ�������DESKeySpecת����
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey securekey = keyFactory.generateSecret(desKey);

			// Cipher����ʵ����ɼ��ܲ���
			Cipher cipher = Cipher.getInstance("DES");

			// ���ܳ׳�ʼ��Cipher����
			cipher.init(Cipher.ENCRYPT_MODE, securekey, random);

			// ���ڣ���ȡ��ݲ�����
			// ��ʽִ�м��ܲ���
			return cipher.doFinal(datasource);
		}

		catch (Throwable e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/* <�����ܵ������ֽ�����ת��Ϊ�����ֽ�����> */
	public static byte[] decrypt(byte[] src, String password)
	throws Exception
	{
		// DES�㷨Ҫ����һ�������ε������Դ
		SecureRandom random = new SecureRandom();

		// ����һ��DESKeySpec����
		DESKeySpec desKey = new DESKeySpec(password.getBytes());

		// ����һ���ܳ׹���
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");

		// ��DESKeySpec����ת����SecretKey����
		SecretKey securekey = keyFactory.generateSecret(desKey);

		// Cipher����ʵ����ɽ��ܲ���
		Cipher cipher = Cipher.getInstance("DES");

		// ���ܳ׳�ʼ��Cipher����
		cipher.init(Cipher.DECRYPT_MODE, securekey, random);

		// ����ʼ���ܲ���
		return cipher.doFinal(src);
	}
}