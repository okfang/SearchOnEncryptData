import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author tanglabhdl
 * 伪随机序列生成器
 */
public class GenStream{
	
	final static int AllCOUNT = 10;         //总共需要的流密码数量，这里可以设置所需要的密钥数量
	final static int N = 20;                //n的长度 实际位数为N*8 bit
	static byte[] seed = {0x55,0x34,0x45,0x75,0x1,0x0,0x1,0x2,0x3,0x23};  //置线性反馈移位寄存器的初始向量seed
	static byte[] test = {0x23,0x74,0x32,0x09,0x5,0x5,0x3,0x8,0x32,0x33};  //测试Li用，这里应该输入截取word的前n-m bit
	static List<byte[]> StreamCrp = new ArrayList<byte[]>();
	
	public static void main(String[] args) {
		
		List<byte[]> allSi = new ArrayList<byte[]>();
		GenStream si =new GenStream();                   //生成前n-m bit的Si部分
		allSi = si.genStream(AllCOUNT , seed);         //输入初始向量seed，以及n的值
		
		for(int i = 0; i < AllCOUNT; i++){
			System.out.println("allSi"+Arrays.toString(allSi.get(i)));
		}//打印Si
		
		System.out.println();
		List<byte[]> allLi = new ArrayList<byte[]>();
		List<byte[]> allki = new ArrayList<byte[]>();
		allLi = si.genStream(AllCOUNT, test);
		allki = si.genSmallKey(allLi);
		
		for(int i = 0; i < AllCOUNT; i++){
			System.out.println("allKi"+Arrays.toString(allki.get(i)));
		}//打印Ki
		
		System.out.println();
		List<byte[]> allNext = new ArrayList<byte[]>();
		allNext = si.pseuFunc(allSi, allki);
		
		for(int i = 0; i < AllCOUNT; i++){
			System.out.println("allNext"+Arrays.toString(allNext.get(i)));
		}//打印next
		
		System.out.println();
		StreamCrp = si.getStream(allSi, allNext);
		
		for(int i = 0; i < AllCOUNT; i++){
			System.out.println("AllStreamCrypto"+Arrays.toString(StreamCrp.get(i)));
		}//打印所有生成的流密码

	}	
	
	public List<byte[]> genStream(int n,byte[] seed){
		// TODO 获取线性移位寄存器的触发器设置和初始向量，生成n-m bit的流密码
		int i,j;
		List<byte[]> bigkey = new ArrayList<byte[]>();
		byte[] temp0 = new byte[seed.length];
		for(i = 0;i < seed.length; i++){
			temp0[i] = seed[i];
		}
		
		for(i = 0;i < n; i++){
			byte[] temp1 = new byte[temp0.length];
			for(j = 0;j < temp0.length; j++){
				temp1[j]= (byte) (temp0[(j+2)%temp0.length]^temp0[(j+5)%temp0.length]);
			}
			for(j = 0;j < temp0.length; j++){
				temp0[j] = temp1[j];
			}
			bigkey.add(temp1);
		}
		return bigkey;
	}
	
	public List<byte[]> genSmallKey(List<byte[]> li){
		// TODO 伪随机函数f，输入一个li(n-m bit)，产生一个密钥K（n-m bit）
		List<byte[]> smallkey = new ArrayList<byte[]>();
		int i,j;
		smallkey.addAll(li);
		for(i = 0; i < AllCOUNT; i++){
			for(j = 0; j < smallkey.get(i).length; j++){
				if(Math.random()>=0.5){
					smallkey.get(i)[j] = (byte) ~ smallkey.get(i)[j];
				}
			}
			//System.out.println(Arrays.toString(smallkey.get(i)));
		}
		//System.out.println(smallkey);
		//如有需要可以将key保留到文件.txt中
		return smallkey;
	}
	
	public List<byte[]> pseuFunc(List<byte[]> Si,List<byte[]> Ki){
		// TODO 伪随机函数F，输入si(n-m bit)与key(n-m bit)，输出 m bit
		
		List<byte[]> next = new ArrayList<byte[]>();
		
		int m = N - Si.get(0).length;//n-(n-m)
		int i,j;
		for( i = 0;i < AllCOUNT; i++){
			byte[] temp = new byte[m];
			for(j = 0; j < m; j++){
				temp[j] =  (byte) (Si.get(i)[j] ^ Ki.get(i)[j]);
			}
			next.add(temp);
		}
		return next;
	}
	
	public List<byte[]> getStream(List<byte[]> Si,List<byte[]> Ki){
		// TODO 拼接形成最终n bit序列
		List<byte[]> streamCry = new ArrayList<byte[]>();
		for(int i = 0; i < Si.size(); i++){
			byte[] temp = new byte[N];
			System.arraycopy(Si.get(i), 0, temp, 0, Si.get(i).length);
			System.arraycopy(Ki.get(i), 0, temp, Si.get(i).length, Ki.get(i).length);
			streamCry.add(temp);
		}
		return streamCry;
	}
	
	
}
