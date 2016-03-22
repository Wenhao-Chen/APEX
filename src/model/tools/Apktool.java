package model.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;

import main.Settings;

public class Apktool {
	
	public static void extractAPK(String apkPath, String outDir, boolean decodeRes)
	{
		String command = 
			decodeRes?
				"java -jar " + Settings.apktoolPath
						+ " d -f -o " + outDir
						+ " " + apkPath
			: 	"java -jar " + Settings.apktoolPath
						+ " d -r -f -o " + outDir
						+ " " + apkPath;
		
		System.out.println("Using Apktool to extract " + apkPath + "...");
		try
		{
			Process pc = Runtime.getRuntime().exec(command);
			BufferedReader in = new BufferedReader(new InputStreamReader(pc.getInputStream()));
			BufferedReader in_err = new BufferedReader(new InputStreamReader(pc.getErrorStream()));
			String line;
			while ((line = in.readLine()) != null)
				System.out.println("   " + line);
			while ((line = in_err.readLine()) != null)
				System.out.println("   " + line);
			in.close();
			in_err.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void buildAPK(String sourceDir, String outPath)
	{
		String command = "java -jar " + Settings.apktoolPath
						+ " b -f"
						+ " -o " + outPath.replace("/", File.separator)
						+ " " + sourceDir.replace("/", File.separator);
		System.out.println("Compiling instrumented smali code into APK file...");
		try {
			Process pc = Runtime.getRuntime().exec(command);
			BufferedReader in = new BufferedReader(new InputStreamReader(pc.getInputStream()));
			BufferedReader in_err = new BufferedReader(new InputStreamReader(pc.getErrorStream()));
			String line;
			while ((line = in.readLine()) != null)
				System.out.println("   " + line);
			while ((line = in_err.readLine()) != null)
				System.out.println("   " + line);
			in.close();
			in_err.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
