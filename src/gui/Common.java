package gui;

import java.util.ArrayList;

import main.Settings;

public class Common {

	
	public static Process adb(String command)
	{
		return exec(Settings.adbPath + " " + command);
	}
	
	
	
	public static ArrayList<String> readOutStream(Process p)
	{
		ArrayList<String> result = new ArrayList<String>();
		if (p == null)
			return result;
		
		return result;
	}
	
	private static Process exec(String command)
	{
		return exec(command, true);
	}
	
	private static Process exec(String command, boolean waitFor)
	{
		try
		{
			Process p = Runtime.getRuntime().exec(command);
			if (waitFor)
				p.waitFor();
			return p;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
}
