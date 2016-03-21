package gui;

import java.io.File;
import java.util.ArrayList;

public class UIAutomator {

	public static final String localDumpFilePath = "temp/window_dump.xml";
	
	public static ArrayList<String> dump()
	{
		Process p = Communication.adb("shell uiautomator dump --compressed");
		return Communication.readOutStream(p);
	}
	
	public static File pullDump()
	{
		File tempF = new File(localDumpFilePath);
		tempF.getParentFile().mkdirs();
		Communication.adb("pull /sdcard/window_dump.xml " + localDumpFilePath);
		return tempF;
	}
	
	
}
