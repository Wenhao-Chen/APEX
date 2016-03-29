package instrumentation;

import gui.Communication;
import gui.Input;
import gui.LayoutHierarchy;
import gui.UIAutomator;
import gui.View;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import model.ApexApp;
import model.ApexAppBuilder;
import model.tools.Apktool;
import model.tools.Jarsigner;

public class Instrumentor {

	
	public static String apkPath = "/home/wenhaoc/workspace/android-studio/ApexApp/app/build/outputs/apk/app-debug.apk";
	
	public static void main(String[] args)
	{
		ApexApp app = ApexAppBuilder.fromAPK(apkPath);
		writeLoggerSmali(app);
		app.instrument();
		Apktool.buildAPK(app.getOutputDir() + File.separator + "apktool/", app.getUnsignedApkPath());
		Jarsigner.signAPK(app.getUnsignedApkPath(), app.getInstrumentedApkPath());
		Communication.installApp(app.getInstrumentedApkPath());
		Communication.startActivity(app.getPackageName(), app.getMainActivity().getJavaName());
		UIAutomator.dump();
		LayoutHierarchy h = new LayoutHierarchy(UIAutomator.pullDump());
		for (View v : h.getClickableLeafViews())
		{
			int x = (int) v.getBoundsRect().getCenterX();
			int y = (int) v.getBoundsRect().getCenterY();
			Communication.clearLogcat();
			System.out.println("Tapping " + v.getTextOrID() + " at (" + x + "," + y + ")");
			Input.tap(x, y);
			pause(500);
			for (String s : Communication.readLogcat())
			{
				System.out.println("  >" + s);
			}
		}
	}
	
	
	public static void pause(int millis)
	{
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeLoggerSmali(ApexApp app)
	{
		try
		{
			String sep = File.separator;
			String loggerName = app.getInstrumentedLoggerClassDexName();
			String nameToPath = loggerName.substring(1, loggerName.length()-1).replace("/", File.separator) + ".smali";
			String path = app.getOutputDir() + sep + "apktool" + sep + "smali" + sep + nameToPath;
			new File(path).getParentFile().mkdirs();
			PrintWriter out = new PrintWriter(new FileWriter(path));
			out.write(LoggerSmali.getSmaliCode(loggerName));
			out.flush();
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
}
