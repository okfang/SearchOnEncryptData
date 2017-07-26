import com.sun.org.apache.bcel.internal.generic.SIPUSH;

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
    //加密后每个单词的长度（byte）
    static final int N = 32;
    //Fki的长度
    static final int M = 20;


    public static void main(String[] args) {
        doEncrypt(sourcePath);
        doDecrypt();
        doSearch(".");
    }

    /**
     *将目录下所有文件加密
     * @param sourcePath 待加密文件目录
     * */
    public static void doEncrypt(String sourcePath){
        File dir = new File(sourcePath);
        //遍历文件夹下面的每个文件
        if (dir.exists() && dir.isDirectory()){
            for (File sourceFile: dir.listFiles()){
                BufferedOutputStream bos = null;
                try {
                    bos = new BufferedOutputStream(new FileOutputStream(encryptPath+sourceFile.getName()));
                    //获取加密好的单词集
                    List<byte[]> plainSequence = getEncryptSequence(sourceFile.getAbsolutePath());
                    //获取加密用的随机数
                    List<byte[]> randomSequence = getRandomSequence(N-M, plainSequence.size());

                    //逐个单词加密
                    for (int i = 0; i< plainSequence.size(); i++){
                        //每个单词
                        byte[] encryptedWord = plainSequence.get(i);
                        //前n-m byte的 Li部分，用于生成Ki
                        byte[] Li = Arrays.copyOfRange(encryptedWord,0,N-M);
                        byte[] Si = randomSequence.get(i);
                        byte[] Ki = getKi(Li);
                        //根据Si, Ki 获取流密码
                        byte[] flowCipher = getFlowCipher(Si,Ki);
                        byte[] cryptWord = new byte[N];

                        //明文序列和流密码异或，生成密文
                        for (int j = 0; j< encryptedWord.length;j++){
                            cryptWord[j] = (byte) (encryptedWord[j] ^ flowCipher[j]);
                        }
//System.out.println("cryptword:"+Arrays.toString(cryptWord));
//System.out.println("flowcipher:"+Arrays.toString(flowCipher));
//System.out.println("Si:"+Arrays.toString(Si));
//System.out.println("Li:"+Arrays.toString(Li));
//System.out.println("Ki:"+Arrays.toString(Ki));
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
    * @return 包含该单词的文件列表
    */
    public static List<String> doSearch(String word){
        //保存包含目标单词的所有文件路径
        List<String> fileList = new ArrayList<String>();
        //处理要搜索的单词
        byte[] searchWord = parseWord(word);
        //获取前半部分用于生成Ki
        byte[] Li = Arrays.copyOf(searchWord,N-M);
        //生成用于解密的Ki
        byte[] Ki = getKi(Li);
        File dir = new File(encryptPath);
        //获取文件目录下所有加密文件
        if (dir.isDirectory()){
            File[] files = dir.listFiles();
            for (File encyptFile: files) {
                byte[] flowCipher = null;
                try {
                    FileInputStream fis = new FileInputStream(encyptFile);
                    byte[] wordSequence = new byte[N];
                    byte[] result = new byte[N];
                    int ret = 0;
                    boolean isFound = false;
                    // 逐个文件搜索匹配
                    while ((ret = fis.read(wordSequence)) != -1) {
                        for (int i = 0; i < wordSequence.length; i++) {
                            result[i] = (byte) (wordSequence[i] ^ searchWord[i]);
                        }
                        //验证结果是否符合流密码解密。
                        if (varify(result,Ki)) {
                            fileList.add(encyptFile.getAbsolutePath());
                            System.out.println("find a file:"+encyptFile.getAbsolutePath());
                            break;
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileList.isEmpty()){
                System.out.println("no such file");
            }
            for (String file: fileList){
                System.out.println(file);
            }
        }
        return fileList;
    }

    /**
     * 解密文件操作
     * 要解密的文件目录
     * */
    public static void doDecrypt(){
        //解密后存放路径
        File dir = new File(encryptPath);
        if (dir.isDirectory()){
            for (File decryptFile: dir.listFiles()){
                List<byte[]> words = new ArrayList<byte[]>();

                FileInputStream fis =  null;
                FileWriter fw = null;
                try {
                    fis = new FileInputStream(decryptFile);
                    fw = new FileWriter(new File(decryptPath+decryptFile.getName()));
                    //获取文档的单词个数
                    int wordCount = (int)decryptFile.length()/N;
                    List<byte[]> randomSequence = RandomSequenceGenerator.getRandomSequence(N-M,wordCount);
                    byte[] encryptWord = new byte[N];
                    int ret = 0;
                    //读取加密文件，用流密码解密
                    int index = 0;
                    while ((ret = fis.read(encryptWord))!= -1){
                        //首先计算Li
                        byte[] Si = randomSequence.get(index++);
                        //解出加密单词的左半部分
                        byte[] Li = new byte[N-M];
                        for (int i = 0; i< Si.length;i++){
                            Li[i] = (byte)(encryptWord[i] ^ Si[i]);
                        }
                        //计算Ki
                        byte[] Ki = getKi(Li);
                        //计算解密用的流密码
                        byte[] flowCipher = getFlowCipher(Si,Ki);
                        //计算单词的明文
                        byte[] plainWord = new byte[N];
                        for (int i =0; i<encryptWord.length; i++){
                            //流密码解密
                            plainWord[i] =(byte)(encryptWord[i]^flowCipher[i]);
                        }
//System.out.println("decryptword:"+Arrays.toString(plainWord));
//System.out.println("decrypt word:"+Arrays.toString(plainWord));
//System.out.println("flowcipher:"+Arrays.toString(flowCipher));
//System.out.println("Si:"+Arrays.toString(Si));
//System.out.println("Li:"+Arrays.toString(Li));
//System.out.println("Ki:"+Arrays.toString(Ki));
                        words.add(plainWord);
                    };
                    String txt = wordToArticle(words);
                    fw.write(txt);
                    fw.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fw != null) {
                        try {
                            fw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }


    }

    /**
     *
     * */
    private static String wordToArticle(List<byte[]> words) {
        for (byte[] word: words){
            System.out.println("wordToArticle:"+Arrays.toString(word));
        }
        List<String> decryptWords = PlainTextParser.decrypt_words(words);
        String txt = PlainTextParser.words_to_article(decryptWords);
        System.out.println(txt);
        return  txt;
    }

    /**
     * 验证单词是否匹配
     * */
    private static boolean varify(byte[] result,byte[] Ki) {
        byte[] Li = Arrays.copyOfRange(result,0,N-M);
        byte[] Ri = Arrays.copyOfRange(result,N-M,N);
        //需要伪随机函数
        byte[] flowCipher = getFlowCipher(Li,Ki);
        byte[] Fki = Arrays.copyOfRange(flowCipher,N-M,N);
        System.out.println("Fkilen:"+Fki.length);
        System.out.println("Fki:"+Arrays.toString(Fki));
        System.out.println("Ri:"+Arrays.toString(Ri));
        boolean isMatched = true;
        for (int i = 0; i< M; i++){
            if (Ri[i] != Fki[i]){
                isMatched = false;
                break;
            }
        }
        return isMatched;
    }

    //到时使用代理模式，将别的代码封装起来


    private static byte[] decryptWord(byte[] b) {
        return PlainTextParser.bytesAESdecode(b, PlainTextParser.KEY_E);
    }


    private static byte[] getFlowCipher(byte[] Si, byte[] Ki){
        return RandomSequenceGenerator.getFlowCipher(Si,Ki);
    }


    private static byte[] getKi(byte[] Li) {
        return RandomSequenceGenerator.getKi(Li);
    }

    private static List<byte[]> getEncryptSequence(String filePath) {
        List<byte[]> encryptSequence = PlainTextParser.extend_encrypt_words(PlainTextParser.cut_words(filePath));
        return encryptSequence;
    }

    private static byte[] parseWord(String word) {
        byte[] word_bytes = word.getBytes();
        byte[] word_block = new byte[20];
        Arrays.fill(word_block, (byte)0);
        if (word_bytes.length >  20) {
            word_bytes = Arrays.copyOf(word_bytes, 20);
        }
        System.arraycopy(word_bytes, 0, word_block, 0, word_bytes.length);
        System.out.println("parse word:"+Arrays.toString(PlainTextParser.bytesAESencode(word_block,PlainTextParser.KEY_E)));
        return PlainTextParser.bytesAESencode(word_block,PlainTextParser.KEY_E);
    }

    public static List<byte[]> getRandomSequence(int len,int sequence_size) throws IOException {
        return RandomSequenceGenerator.getRandomSequence(len, sequence_size);
    }
}
