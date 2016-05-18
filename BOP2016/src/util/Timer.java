package util;

public class Timer {
	public static long st;
	
	public static void init() {
		st = System.currentTimeMillis();
	}
	
	public static void log(String text) {
		System.out.println(Thread.currentThread().getName() + ": " + text 
				+ " " + (System.currentTimeMillis() - st) / 1000.0);;
	}
	
	public static void log() {
		System.out.println(Thread.currentThread().getName() + ": " 
				+ " " + (System.currentTimeMillis() - st) / 1000.0);;
	}
}
