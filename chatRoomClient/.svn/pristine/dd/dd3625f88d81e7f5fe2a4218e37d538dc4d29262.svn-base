package utils;

import java.util.Scanner;

public class InputUtil {
	
	public static int inputIntType(Scanner sc){
		int choose = 0;
		while(true){
			try {
				choose = sc.nextInt();
				break;
			} catch (Exception e) {
				sc = new Scanner(System.in);
				System.out.println("输入类型不正确，请重新输入:");
			}
		}
		return choose;
	}
}
