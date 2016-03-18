package gui;

import java.io.File;

public class UIAutomator {

	
	
	public File dumpWindowXML()
	{
		//TODO
		// 1. "adb shell uiautomator dump"
		// 2. read process out stream, find out where is the dumped file
		// 3. "adb pull <dumped file> <local path>"
		// 4. return the file
		Process p = Common.adb("shell uiautomator dump");
		
		return null;
	}
	
	
	
}
