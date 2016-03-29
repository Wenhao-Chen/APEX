package model.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.Settings;

public class Apktool {
	
	
	// NOTE: when this error pop up from apktool, it is usually because the test app's API level is higher
	// than apktool's built-in framework res file. Solve this by installing newest version of framework-res.apk. 
	// The project's 'tools/' folder contains an API level 23 framework-res.apk.
	private static final String noResFoundErrorMessage = "error: Error retrieving parent for item: No resource found that matches the given name";
	
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
						+ " -o " + outPath
						+ " " + sourceDir;
		System.out.println("Compiling instrumented smali code into APK file...");
		try {
			Process pc = Runtime.getRuntime().exec(command);
			BufferedReader in = new BufferedReader(new InputStreamReader(pc.getInputStream()));
			BufferedReader in_err = new BufferedReader(new InputStreamReader(pc.getErrorStream()));
			String line;
			while ((line = in.readLine()) != null)
				System.out.println("   " + line);
			Map<String, List<String>> missingRes = new HashMap<String, List<String>>();
			while ((line = in_err.readLine()) != null)
			{
				System.out.println("   " + line);
				if (line.contains(noResFoundErrorMessage))
				{
					String filePath = line.substring(line.indexOf("W: ")+3, line.indexOf(": "+noResFoundErrorMessage));
					filePath = filePath.substring(0, filePath.indexOf(":"));
					String resName = line.substring(line.indexOf(noResFoundErrorMessage)+noResFoundErrorMessage.length());
					resName = resName.substring(resName.indexOf("'")+1, resName.lastIndexOf("'"));
					List<String> resNames = missingRes.get(filePath);
					if (resNames == null)
						resNames = new ArrayList<String>();
					resNames.add(resName);
					missingRes.put(filePath, resNames);
				}
			}
			in.close();
			in_err.close();
			if (!missingRes.isEmpty())
			{
				System.out.println("\nApktool cannot find some resource references. Installing newer version of framework to try again...");
				installFramework(Settings.frameworkResPath);
				//fixResNameErrors(missingRes);
				buildAPK(sourceDir, outPath);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void installFramework(String frameworkPath)
	{
		String command = 
					"java -jar " + Settings.apktoolPath
							+ " if " + frameworkPath;
			
			System.out.println("Installing " + frameworkPath + " for Apktool...");
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
	
	public static void fixResNameErrors(Map<String, List<String>> missingRes) throws Exception
	{
		for (Map.Entry<String, List<String>> entry : missingRes.entrySet())
		{
			String filePath = entry.getKey();
			List<String> resNames = entry.getValue();
			List<String> xmlLines = new ArrayList<String>();
			BufferedReader in = new BufferedReader(new FileReader(filePath));
			String line;
			while ((line = in.readLine())!=null)
			{
				xmlLines.add(line);
			}
			in.close();
			PrintWriter out = new PrintWriter(new FileWriter(filePath));
			for (String s : xmlLines)
			{
				out.write(s + "\n");
				if (s.equals("<resources>"))
				{
					for (String resName : resNames)
						out.write("    <style name=\"" + resName +"\"></style>" + "\n");
				}
			}
			out.flush();
			out.close();
		}
	}
}
