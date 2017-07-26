import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tjoe on 2017/7/24.
 */
public class RandomSequenceGenerator {
    static final int N = 32;
    static final int M = 20;
    //带密钥的哈希函数SHA1，20 bytes
    static final String MAC_HAME = "HmacSHA1";
//    static private SecureRandom random = new SecureRandom();
    //用于生成Ki的密钥
    static private String key = "lovelovelove";

    /**
     * @param n 随机序列的长度
     * @param sequence_size 需要的随机数的个数
     * */
    public static List<byte[]> getRandomSequence(int n, int sequence_size) {
        //生成伪随机序列的种子
        byte[] seed = getSeed();
        SecureRandom random = new SecureRandom();
        random.setSeed(seed);
        List<byte[]> randomSquence = new ArrayList<byte[]>();
       for (int i =0; i< sequence_size; i++){
           byte bytes[] = new byte[n];
           random.nextBytes(bytes);
           randomSquence.add(bytes);
       }
        return randomSquence;

	}
    /**
     * 产生流密码，这里将原来20 bytes的加密哈希值截断为10 bytes
     * @param Si 对应的随机数
     * @param Ki 对应单词生词的密钥
     * @return 对应单词的流密码 10 byte
    * */
    public static byte[] getFlowCipher(byte[] Si, byte[] Ki){
        byte[] flowCipher = new byte[N];
        byte[] Fi = new byte[M];
        //生成Fi
        try {
            SecretKey secretKey = new SecretKeySpec(Ki,MAC_HAME);
            Mac mac = Mac.getInstance(MAC_HAME);
            mac.init(secretKey);
            Fi = mac.doFinal(Si);
            System.arraycopy(Si,0,flowCipher,0,Si.length);
            System.arraycopy(Fi,0,flowCipher,Si.length, Fi.length);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return flowCipher;
    }

    public static byte[] getSeed(){
        SecureRandom random = new SecureRandom();
        int seed_len = 20;
        File seedFile = new File(System.getProperty("user.dir")+"\\seed.txt");
        byte[] seed = new byte[0];
        try {
            seed = null;
            if (seedFile.exists()){
                System.out.println("file exists");
                FileInputStream fileInputStream = new FileInputStream(seedFile);
                int ret = 0;
                byte[] buffer = new byte[seed_len];
                while ((ret=fileInputStream.read(buffer) ) != -1){
                    seed = buffer;
                }
                fileInputStream.close();
            }else{
                System.out.println("create file");
                seed = random.generateSeed(seed_len);
                FileOutputStream fileOutputStream = new FileOutputStream(seedFile);
                fileOutputStream.write(seed);
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return seed;
    }

    public static void main(String[] args) throws IOException {
        System.out.println();
        List<byte[]> randomSequence = getRandomSequence(20,10);
        for (byte[] randomNum : randomSequence){
            System.out.println(Arrays.toString(randomNum));
        }
        List<byte[]> randomSequence2 = getRandomSequence(20,10);
        for (byte[] randomNum : randomSequence2){
            System.out.println(Arrays.toString(randomNum));
        }
    }

    /**
     * @param Li 单词的左半部分
     * @return 和单词有关的Ki(20 byte)，生成伪随机函数的密钥
     *
     * */
    public static byte[] getKi(byte[] Li) {
        SecretKey secretKey = new SecretKeySpec(key.getBytes(),MAC_HAME);
        byte[] Ki = null;
        try {
            Mac mac = Mac.getInstance(MAC_HAME);
            mac.init(secretKey);
            Ki = mac.doFinal(Li);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return Ki;
    }
}
