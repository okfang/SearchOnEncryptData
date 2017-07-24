/**
 * @author tanglabhdl
 * 伪随机序列生成器
 */
public class GenStream{
	
	private String result = "";
	private String smallkey = "";
	private String li = "";
	private String result1 = "";
	private String result2 = "";
	
	public void inputFromHand(String str){
		// TODO 手动输入n bit方案,手动输入一个n bit单词，截取前n-m bit作为Li，此处将m=n/2
		result = str;
		li = result.substring(0,result.length()/2);
		//System.out.println(li);
	}
	
	public void genSmallKey(){
		// TODO 伪随机函数f，输入一个li(n-m bit)，产生一个密钥K（n-m bit）
		char[] templi = li.toCharArray();
		int[] tempkey = new int[li.length()];
		for(int i = 0; i < li.length() ; i++){
			if((Math.random())>=0.5){
				tempkey[i] = (templi[i]^'1'); 
			}
			else{
				tempkey[i] = (templi[i]^'0');
			}
		}
		for(int i=0; i < li.length(); i++){
			  smallkey += Integer.toBinaryString(tempkey[i]);
		}
		//System.out.println(smallkey);
		//如有需要可以将key保留到文件.txt中
	}
	
	public void pseuFunc(int n,int[] iv){
		// TODO 伪随机函数F，输入si(n-m bit)与key(n-m bit)，输出 m bit
		LFSR si =new LFSR();
		si.genStream(n , iv);
		result1 = si.getLFSRStream();
		
		int m = result.length()-smallkey.length();//n-(n-m)
		char[] chsi = result1.toCharArray();
		char[] chkey = smallkey.toCharArray();
		int[] temp = new int[m];
		for(int i = 0;i < m; i++){
			temp[i] = chsi[i % (smallkey.length())] ^ chkey[i % (smallkey.length())];
		}
		for(int i=0; i < m; i++){
			  result2 += Integer.toBinaryString(temp[i]);
		}
	}
	
	public String getStream(){
		// TODO 拼接形成最终n bit序列
		return result1 +" "+ result2;
	}
	
}
