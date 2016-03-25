package gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import main.Settings;

public class Communication {

	
	public static Process adb(String command)
	{
		return exec(Settings.adbPath + " " + command);
	}
	
	public static Process installApp(String apkPath)
	{
		return exec(Settings.adbPath + " install -r " + apkPath);
	}
	
	public static Process startActivity(String packageName, String activityName)
	{
		return exec(Settings.adbPath + " shell am start -W -n " + packageName + "/" + activityName);
	}
			
	public static ArrayList<String> readOutStream(Process p)
	{
		ArrayList<String> result = new ArrayList<String>();
		if (p == null)
			return result;
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = in.readLine())!=null)
			{
				result.add(line);
			}
			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public static ArrayList<String> readErrorStream(Process p)
	{
		ArrayList<String> result = new ArrayList<String>();
		if (p == null)
			return result;
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line;
			while ((line = in.readLine())!=null)
			{
				result.add(line);
			}
			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
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
