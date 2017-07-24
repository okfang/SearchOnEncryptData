/**
 * @author tanglabhdl
 * 测试伪随机序列生成器和伪随机函数模块
 */
public class Test {
	public static void main(String[] args) {

		int[] iv = {1,0,1,1,1,0};               //设置线性反馈移位寄存器的初始向量
		int n = 32;                             //设置n bit的大小
		String li = "01001001110101010101000001111101";     //设置输入的n bit的Li
		GenStream strCry = new GenStream();
		strCry.inputFromHand(li);
		strCry.genSmallKey();
		strCry.pseuFunc(n, iv);

		System.out.println("�����룺"+strCry.getStream());
		System.out.println(strCry.getStream().length());

	}	
}
