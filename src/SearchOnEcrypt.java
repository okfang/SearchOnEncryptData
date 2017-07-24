import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by tjoe on 2017/7/21.
 */
public class SearchOnEcrypt {

    static final String packagePath = System.getProperty("user.dir");
    static final String sourcePath = packagePath+"/sources/";
    static final String encryptPath = packagePath+"/encrypt/";
    static final String decryptPath = packagePath+"/decrypt/";
    static final int n =  20*8;
    static final int m = 32;

    private static PlainTextParser plainTextParser;
    private static GenStream FlowCipherGenerator;

    public static String byteToBit(byte b) {
        return ""
                + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
                + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
                + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
                + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }

    //初始化
    public static void init(){
        plainTextParser = new PlainTextParser();
        FlowCipherGenerator = new GenStream();
    }

    public static void main(String[] args) {
        init();
        doEncrypt();
    }

    //将所有文件加密
    public static void doEncrypt(){
        File dir = new File(sourcePath);
        if (dir.exists() && dir.isDirectory()){
            for (File sourceFile: dir.listFiles()){
                BufferedOutputStream bos = null;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(sourceFile));
                    List<byte[]> plainSequence = getEncryptSequence(sourceFile.getAbsolutePath());
                    //逐个明文加密
                    for (byte[] encryptedWord : plainSequence ){
                        byte[] flowCipher = getFlowCipher();
                        byte[] cryptWord = new byte[n];
                        //明文序列和流密码异或，生成密文
                        for (int i = 0; i< encryptedWord.length;i++){
                            cryptWord[i] = (byte) (encryptedWord[i] ^ flowCipher[i]);
                        }
                        //将密文写入文件
                        bos.write(cryptWord);
                    }
                    bos.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (bos != null){
                        try {
                            bos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }

    /**
    * 搜索单词
     * @param word 查询的单词
     * @param ki 用于解密的密钥
    * @return 包含该单词的文件列表
    */
    public static List<String> doSearch(String word, byte[] ki){
        List<String> fileList = new ArrayList<String>();
        byte[] parsedWord = parseWord(word);
        File dir = new File(encryptPath);
        //获取文件目录下所有加密文件
        if (dir.isDirectory()){
            File[] files = dir.listFiles();
            for (File encyptFile: files) {
                byte[] flowCipher = null;
                try {
                    FileInputStream fis = new FileInputStream(encyptFile);
                    byte[] wordSequence = new byte[m / 8];
                    byte[] result = new byte[m / 8];
                    int ret = 0;
                    boolean isFound = false;
                    // 逐个文件搜索匹配
                    while ((ret = fis.read(wordSequence)) != -1) {
                        for (int i = 0; i < wordSequence.length; i++) {
                            result[i] = (byte) (wordSequence[i] ^ parsedWord[i]);
                        }
                        //验证结果是否符合流密码解密。
                        if (varify(result)) {
                            fileList.add(encyptFile.getAbsolutePath());
                            break;
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileList;
    }

    /**
     * 解密操作
     * @param fileName 要解密的文件
     * */
    public static void doDecrypt(String fileName){
        //解密后存放路径
        File decryptSaveFile = new File(encryptPath+fileName);
        FileInputStream fis =  null;
        BufferedOutputStream bos = null;
        try {
            fis = new FileInputStream(decryptSaveFile);
            bos = new BufferedOutputStream(new FileOutputStream(decryptPath+fileName));
            byte[] encryptWord = new byte[n/8];
            byte[] plainWord = new byte[n/8];
            byte[] realPlainWord = null;
            byte[] flowCipher = null;
            int ret = 0;
            //读取加密文件，用流密码解密
            while ((ret = fis.read(encryptWord))!= -1){
                flowCipher = getFlowCipher();
                for (int i =0; i<encryptWord.length; i++){
                    //流密码解密
                    plainWord[i] =(byte)(encryptWord[i]^flowCipher[i]);
                }
                //对称解密
                realPlainWord = decryptWord(plainWord);
                // 解密后写入文件
                bos.write(realPlainWord);
            };
            bos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bos != null){
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 验证单词是否匹配
     * */
    private static boolean varify(byte[] result) {
        byte[] Li = Arrays.copyOfRange(result,0,m-n);
        byte[] Ri = Arrays.copyOfRange(result,m-n,m);
        //需要伪随机函数
        byte[] Fki = getPsudorandom(Li);
        boolean isMatched = true;
        for (int i = 0; i< m; i++){
            if (Ri[i] != Fki[i]){
                isMatched = false;
            }
        }
        return isMatched;
    }

    //到时使用代理模式，将别的代码封装起来

    private static byte[] decryptWord(byte[] b) {
        return new byte[n];
    }

    private static byte[] getPsudorandom(byte[] li) {
        return new byte[n-m];
    }

    private static byte[] getFlowCipher() {
        return new byte[10];
    }

    private static List<byte[]> getEncryptSequence(String filePath) {
        List<byte[]> encryptSequence = plainTextParser.extend_encrypt_words(plainTextParser.cut_words(filePath));
        return encryptSequence;
    }

    private static byte[] parseWord(String word) {
        return new byte[10];
    }
}
