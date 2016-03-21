package gui;

public class Input {

	
	public static void tap(int x, int y)
	{
		Communication.adb("shell input tap " + x + " " + y);
	}
	
}
