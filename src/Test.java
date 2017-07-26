
import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Created by tjoe on 2017/7/25.
 */
public class Test {
    public static void main(String[] args){
        mac();
//        try {
//            String src = "hello";
//            // 生成KEY
//            SecureRandom random;
//            random = SecureRandom.getInstance("SHA1PRNG");
//            random.setSeed(src.getBytes());
//            KeyGenerator kgen = KeyGenerator.getInstance("AES");
//            kgen.init(128, random);
////            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
////            keyGenerator.init(128);
//            SecretKey secretKey = kgen.generateKey();
//            byte[] keyBytes = secretKey.getEncoded();
//
//            // key转换
//            SecretKey key = new SecretKeySpec(keyBytes, "AES");
//
//            // 加密
//            Cipher cipher = Cipher.getInstance("AES/EBC/PKCS5Padding");
//            cipher.init(Cipher.ENCRYPT_MODE, key);
//            byte[] result = cipher.doFinal(src.getBytes());
//            System.out.println(result.length);
//
//            // 解密
//            cipher.init(Cipher.DECRYPT_MODE, key);
//            result = cipher.doFinal(result);
//            System.out.println("jdk aes desrypt : " + new String(result));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        String mykey = "26b21eb5c65f7126363e59b333327ec8548320df";
        String src = "qqqqqqq";
        SecureRandom random;
        SecretKeySpec key;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(mykey.getBytes());
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, random);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            key = new SecretKeySpec(enCodeFormat, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding ");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptData = cipher.doFinal(src.getBytes());
            System.out.println(encryptData.length);


//            Cipher cipher2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(encryptData);
            System.out.println(new String(result));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

    }
    public static void mac() {
        try {
            String encryptKey = "key";
            String encryptText = "gogogogo";
            byte[] data=encryptKey.getBytes();
            SecretKey secretKey = new SecretKeySpec(data, "HmacMD5");
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(secretKey);
            byte[] text = encryptText.getBytes();
            byte[] str = mac.doFinal(text);
            System.out.println(str.length);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }
}
