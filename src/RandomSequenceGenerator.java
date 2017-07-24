import java.io.*;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tjoe on 2017/7/24.
 */
public class RandomSequenceGenerator {
    static private SecureRandom random = new SecureRandom();

    public static List<byte[]> genRandomSequence(int len, int sequence_size) throws IOException {
        File seedFile = new File(System.getProperty("user.dir")+"\\seed.txt");
        byte[] seed = null;
        if (seedFile.exists()){
            System.out.println("file exists");
            FileInputStream fileInputStream = new FileInputStream(seedFile);
            int ret = 0;
            byte[] buffer = new byte[len];
            while ((ret=fileInputStream.read(buffer) ) != -1){
                seed = buffer;
            }
            fileInputStream.close();
        }else{
            System.out.println("create file");
            seed = random.generateSeed(len);
            FileOutputStream fileOutputStream = new FileOutputStream(seedFile);
            fileOutputStream.write(seed);
            fileOutputStream.flush();
            fileOutputStream.close();
        }
        random.setSeed(seed);
        List<byte[]> randomSquence = new ArrayList<byte[]>();
       for (int i =0; i< sequence_size; i++){
           byte bytes[] = new byte[len];
           random.nextBytes(bytes);
           randomSquence.add(bytes);
       }
        return randomSquence;

	}
    public static void main(String[] args) throws IOException {
        System.out.println();
        List<byte[]> randomSequence = genRandomSequence(20,10);
        for (byte[] randomNum : randomSequence){
            System.out.println(Arrays.toString(randomNum));
        }
    }
}
