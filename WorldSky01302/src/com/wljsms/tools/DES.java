package com.wljsms.tools;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DES {
    private static byte[] iv = {1,2,3,4,5,6,7,8};
    //加密方法
    public static String encryptDES(String encryptString, String encryptKey) throws Exception {
            IvParameterSpec zeroIv = new IvParameterSpec(iv);
            SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
            byte[] encryptedData = cipher.doFinal(encryptString.getBytes());
      
//            return Base64.encode(encryptedData);
            return byte2HexString(encryptedData);
    }
    //解密方法
    public static String decryptDES(String decryptString, String decryptKey) throws Exception {
//            byte[] byteMi = new Base64().decode(decryptString);
            byte[] byteMi = String2Byte(decryptString);
            IvParameterSpec zeroIv = new IvParameterSpec(iv);
            SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
            byte decryptedData[] = cipher.doFinal(byteMi);
      
            return new String(decryptedData);
    }
    /**
     * byte[]转换成字符串
     * @param b
     * @return
     */
    public static String byte2HexString(byte[] b)
    {
     StringBuffer sb = new StringBuffer();
     int length = b.length;
     for (int i = 0; i < b.length; i++) {
      String stmp = Integer.toHexString(b[i]&0xff);
      if(stmp.length() == 1)
       sb.append("0"+stmp);
      else
       sb.append(stmp);
     }
     return sb.toString();
    }
    
    /**
     * 16进制转换成byte[]
     * @param hexString
     * @return
     */
    public static byte[] String2Byte(String hexString)
    {
     if(hexString.length() % 2 ==1)
      return null;
     byte[] ret = new byte[hexString.length()/2];
     for (int i = 0; i < hexString.length(); i+=2) {
       ret[i/2] = Integer.decode("0x"+hexString.substring(i,i+2)).byteValue();
      }
     return ret;
    }
}