import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class PlainTextParser {
	
	final static int N = 20*8;                //20位字符的bit位数
	final static int MAX_LEN = N/8;          //word填充后长度
	static String KEY_E = "26b21eb5c65f7126363e59b333327ec8548320df";//E密钥
//	static String SEED = "26b21eb5c65f7126363e59b333327ec8548320df"; //随机数种子
//	static SecureRandom random = new SecureRandom();
	
	//流密码生成
//	public static byte[] random_gen(int len) {
//		byte bytes[] = new byte[len];
//		random.nextBytes(bytes);
//		return bytes;
//	}

	public static void main(String[] args) {
////		random.setSeed(hex2bytes(SEED)); //设定随机数生成器种子
		
		String filePath = "/home/zk/eclipse-workspace/searchoncrypt/src/test";
		
		//分词
		List<String> words = cut_words(filePath);
		//打印
		for (String word: words) {
			System.out.println(word);
		}
		//扩充词并异或
		List<byte[]> enwords = extend_encrypt_words(words);
		//解密词
		List<String> dewords = decrypt_words(enwords);
		for (String item: dewords) {
			System.out.println(item);
		}
		System.out.println(words_to_article(dewords));
		
	}
	
	/**
	 * words转化为文章
	 * @param words
	 * @return
	 */
	
	public static String words_to_article(List<String> words) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String word: words) {
			if (first || word.matches("^\\pP$")) {
				sb.append(word);
				first = false;
			}
			else {
				sb.append(" ");
				sb.append(word);
			}
		}
		return sb.toString();
	}
	
	/**
	 * 将word填充到N位，再异或加密
	 * @param words
	 * @return
	 */
	public static List<byte[]> extend_encrypt_words(List<String> words) {
		List<byte[]> encryptwords = new ArrayList<byte[]>();
		
		for (String word: words) {
			System.out.println(word);
			byte[] word_bytes = word.getBytes();
			byte[] word_block = new byte[MAX_LEN];
			Arrays.fill(word_block, (byte)0);
			if (word_bytes.length >  MAX_LEN) {
				word_bytes = Arrays.copyOf(word_bytes, MAX_LEN);
			}
			System.arraycopy(word_bytes, 0, word_block, 0, word_bytes.length);
			
			encryptwords.add(bytesAESencode(word_block, KEY_E));
				
		}
		return encryptwords;
	}
	
	public static byte[] encrypt_words(byte[] word, byte[] key) {
		return bytesXor(word, key);
	}
	
	/**
	 * 还原得到的加密word
	 * @param words
	 * @return
	 */
	public static List<String> decrypt_words(List<byte[]> words) {
		List<String> decryptwords = new ArrayList<String>();
		for (byte[] item: words) {
			String tmp = new String(bytesAESdecode(item, KEY_E));
            tmp = tmp.trim();
			decryptwords.add(tmp);
		}
		return decryptwords;
	}

	/**
	 * byte[]字节数组异或
	 * @param a
	 * @param b
	 * @return
	 */
	public static byte[] bytesXor(byte[] a, byte[] b) {
		byte[] newbytes = new byte[a.length];
		for (int i = 0; i < a.length; i++) {
			newbytes[i] = (byte)(a[i] ^ b[i]);
		}
		return newbytes;
		
	}
	
	/**
	 * 随机密钥生成
	 * @param mykey
	 * @return
	 */
	public static Key genAESkey(String mykey) {
		if (null == mykey || mykey.length() == 0) {
            throw new NullPointerException("key is null");
        }
        SecretKeySpec key2 = null;
        SecureRandom random;
		
        try {
        	random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(mykey.getBytes());
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, random);
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            key2 = new SecretKeySpec(enCodeFormat, "AES");
        } catch (NoSuchAlgorithmException e) {
        	e.printStackTrace();
        }
        return key2;
		
	}
	
	/**
	 * 字节数组AES加密
	 * @param word
	 * @param mykey
	 * @return
	 */
	public static byte[] bytesAESencode(byte[] word, String mykey) {

		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, genAESkey(mykey));
			byte[] result = cipher.doFinal(word);
			return result;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 字节数组AES解密
	 * @param word
	 * @param mykey
	 * @return
	 */
	public static byte[] bytesAESdecode(byte[] word, String mykey) {
		
		try {
			
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, genAESkey(mykey));
			byte[] result = cipher.doFinal(word);
			return result;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 字节数组转16进制字符串
	 * @param bytes
	 * @return
	 */
	public static String bytes2hex(byte[] bytes) {
		BigInteger bi = new BigInteger(1, bytes);  
        String hex = bi.toString(16); 
        return hex;
	}
	
	/**
	 * 16进制字符串转字节数组
	 * @param hex
	 * @return
	 */
	public static byte[] hex2bytes(String hex) {
		BigInteger bi = new BigInteger(hex, 16);  
        byte[] bytes = bi.toByteArray(); 
        return bytes;
	}
	
    /**
     * 文本以空格分割，处理标点
     * @param filepath
     * @return
     */
	public static List<String> cut_words(String filepath) {
		File file = new File(filepath);
		String fileContent = "";
		List<String> final_words = new ArrayList<String>();
		
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String tmp = "";
			while ((tmp = br.readLine()) != null) {
				fileContent += tmp;
			}
			br.close();
			
			String[] words = fileContent.split("\\s+");
			System.out.println(fileContent);
			
			String re = "(\\pP)";
			Pattern p = Pattern.compile(re);
			
			
			for(String word: words){
				
				String tmp_word = word;
				while (!tmp_word.equals("")) {
					Matcher m = p.matcher(tmp_word);
					if(!m.find() || m.group(1).equals("-")) {
						final_words.add(tmp_word);
						break;
					}
					int idxstart = m.start();
					String sub = tmp_word.substring(0, idxstart);
					if (!sub.equals("")){
						final_words.add(sub);
					}
					final_words.add(m.group(1));
					tmp_word = tmp_word.substring(idxstart+1);
				}
			}
			final_words.get(0).trim();
			final_words.get(final_words.size()-1).trim();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return final_words;
	}

}
