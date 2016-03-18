package gui;

import java.io.File;

public class UIAutomator {

	
	public static File dumpWindowXML()
	{
		// 1. "adb shell uiautomator dump"
		// 3. "adb pull <dumped file> <local path>"
		// 4. return the file
		
		Common.adb("shell uiautomator dump --compressed");
		// Assuming the dumped file is at /sdcard/window_dump.xml
		File tempF = new File("temp/window_dump.xml");
		tempF.getParentFile().mkdirs();
		Common.adb("pull /sdcard/window_dump.xml temp/window_dump.xml");
		
		return tempF;
	}
	
	
}
