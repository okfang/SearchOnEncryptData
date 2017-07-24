/**
 * @author tanglabhdl
 * 线性反馈移位寄存器，生成流密码Si
 */
public class LFSR {
	
	private int[] bigkey = new int[200];
	private String lfsrStream = "";
	
	public void genStream(int n,int[] iv) {
		// TODO 获取线性移位寄存器的触发器设置和初始向量，生成指定bit的流密码
		int i;
		for(i = 0;i < iv.length;i++ ){
			bigkey[i] = iv[i]; 
		}
		for(;i < bigkey.length; i++){
			bigkey[i]=(bigkey[i-2]+bigkey[i-3])%2;
		}
		for(i = 0; i < n/2; i++){
			  lfsrStream += Integer.toBinaryString(bigkey[i]);
		}
	}
	
	public String getLFSRStream() {
		// get方法
		return lfsrStream;
	}

}
