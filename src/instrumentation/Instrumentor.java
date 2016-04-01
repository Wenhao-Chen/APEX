package instrumentation;

import gui.Communication;
import gui.Input;
import gui.LayoutHierarchy;
import gui.UIAutomator;
import gui.View;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import model.ApexApp;
import model.ApexAppBuilder;
import model.ApexClass;
import model.ApexMethod;
import model.ApexStmt;
import model.tools.Apktool;
import model.tools.Jarsigner;

public class Instrumentor {

	
	public static String apkPath = "/home/wenhaoc/workspace/android-studio/ApexApp/app/build/outputs/apk/app-debug.apk";
	
	public static void main(String[] args)
	{
		ApexApp app = ApexAppBuilder.fromAPK(apkPath);
		addLoggerClass(app);
		app.instrument();
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
	
	public static void instrument(ApexApp app)
	{
		addLoggerClass(app);
		for (ApexClass c : app.getClasses())
		{
			if (c.isLibraryClass())
				continue;
			instrument(app, c);
			File f = new File(c.getSmaliPath());
			f.getParentFile().mkdirs();
			try
			{
				PrintWriter out = new PrintWriter(new FileWriter(f));
				for (String s : c.getInstrumentedBody())
				{
					out.write(s + "\n");
				}
				out.flush();
				out.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		File unsignedApp = new File(app.getUnsignedApkPath());
		File signedApp = new File(app.getInstrumentedApkPath());
		if (unsignedApp.exists())
			unsignedApp.delete();
		if (signedApp.exists())
			signedApp.delete();
		Apktool.buildAPK(app.getOutputDir() + File.separator + "apktool/", app.getUnsignedApkPath());
		Jarsigner.signAPK(app.getUnsignedApkPath(), app.getInstrumentedApkPath());
	}
	
	public static void instrument(ApexApp app, ApexClass c)
	{
		for (ApexMethod m : c.getMethods())
		{
			instrument(app, c, m);
		}
	}
	
	public static void instrument(ApexApp app, ApexClass c, ApexMethod m)
	{
		for (ApexStmt s : m.getStatements())
		{
			instrument(app, c, m, s);
		}
	}
	
	public static void instrument(ApexApp app, ApexClass c, ApexMethod m, ApexStmt s)
	{
		if (s.isFirstStmtOfMethod())
		{
			addBefore(app, s, "Method_Starting," + m.getSignature());
		}
		
		if (s.isFirstStmtOfBlock())
		{
			addBefore(app, s, "exec_log,new_block," + s.getUniqueID());
		}
		
		if (s.isIfStmt())
		{
			addBefore(app, s, "exec_log,if," + s.getUniqueID());
			addAfter(app, s, "exec_log,flew_through");
		}
		else if (s.isSwitchStmt())
		{
			addBefore(app, s, "exec_log,switch," + s.getUniqueID());
			addAfter(app, s, "exec_log,flew_through");
		}
		
		if (s.isReturnStmt())
		{
			addBefore(app, s, "Method_Returning," + m.getSignature());
		}
		else if (s.isThrowStmt())
		{
			addBefore(app, s, "Method_Throwing," + m.getSignature());
		}
	}
	
	private static void addBefore(ApexApp app, ApexStmt s, String message)
	{
		ArrayList<String> stmts = generateLogStmts(app, s, message);
		if (s.getBytecodeOperator().startsWith("move-result"))	// move-result and previous statement must not be separated
		{
			for (String stmt : stmts)
			{
				s.addSucceedingStmt(stmt);
			}
		}
		else
		{
			for (String stmt : stmts)
			{
				s.addPrecedingStmt(stmt);
			}
		}
	}
	
	private static void addAfter(ApexApp app, ApexStmt s, String message)
	{
		ArrayList<String> stmts = generateLogStmts(app, s, message);
		for (String stmt : stmts)
		{
			s.addSucceedingStmt(stmt);
		}
	}
	
	
	private static ArrayList<String> generateLogStmts(ApexApp app, ApexStmt s, String message)
	{
		ArrayList<String> result = new ArrayList<String>();
		
		//TODO find a register
		String regName = findOneRegister(app, s);
		String constStringStmt = "    const-string " + regName + ", \"" + message + "\"";
		String invokeStmt = "    invoke-static {" + regName + "}, " + 
								app.getInstrumentedLoggerClassDexName() + "->d(Ljava/lang/String;)V";
		
		//TODO if register is occupied, create field in the class to store its value
		
		return result;
	}

	
	private static String findOneRegister(ApexApp app, ApexStmt s)
	{
		
		return "";
	}
	
	
	public static void addLoggerClass(ApexApp app)
	{
		try
		{
			String sep = File.separator;
			String loggerName = app.getInstrumentedLoggerClassDexName();
			String nameToPath = loggerName.substring(1, loggerName.length()-1).replace("/", sep) + ".smali";
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
