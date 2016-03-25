package model;

import java.io.File;

import main.Settings;
import model.tools.Apktool;
import parsers.SmaliParser;
import parsers.XMLParser;

public class ApexAppBuilder {

	public static boolean decodeRes = true;
	
	public static ApexApp fromAPK(String apkFilePath)
	{
		File f = new File(apkFilePath);
		if (!f.exists() || !f.isFile() || !apkFilePath.endsWith(".apk"))
		{
			System.out.println("[ERROR] Not a valid APK file: " + apkFilePath);
			return null;
		}
		
		String apktoolOutDir = Settings.outputDir.replace("/", File.separator) + 
				File.separator + f.getName() + File.separator + "apktool/";
		new File(apktoolOutDir).mkdirs();
		Apktool.extractAPK(f.getAbsolutePath(), apktoolOutDir, decodeRes);
		
		ApexApp app = new ApexApp();
		app.setOutputDir(apktoolOutDir);
		app.setAPKPath(apkFilePath);
		
		System.out.print("Parsing smali files. Might take a while...");
		parseSmaliFiles(new File(apktoolOutDir + File.separator + "smali/"), app);
		System.out.println(" Done.");
		
		System.out.print("Parsing AndroidManifest.xml...");
		XMLParser.parseManifest(new File(apktoolOutDir + File.separator + "AndroidManifest.xml"), app);
		System.out.println(" Done.");
		
		return app;
	}
	
	private static void parseSmaliFiles(File f, ApexApp app)
	{
		if (!f.exists())
			return;
		if (f.isDirectory())
		{
			File[] flist = f.listFiles();
			for (File ff : flist)
				parseSmaliFiles(ff, app);
		}
		else if (f.isFile() && f.getName().endsWith(".smali"))
		{
			try
			{
				app.addClass(SmaliParser.parse(f));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	
	
}
