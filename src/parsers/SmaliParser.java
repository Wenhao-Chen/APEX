package parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import model.ApexClass;
import model.ApexField;
import model.ApexMethod;

public class SmaliParser {


	public static ApexClass parse(File f) throws IOException
	{
		ApexClass c = null;
		BufferedReader in = new BufferedReader(new FileReader(f));
		String line;
		while ((line = in.readLine())!=null)
		{
			if (line.startsWith(".class "))
			{
				c = new ApexClass(line);
			}
			else if (line.startsWith(".super "))
			{
				c.parseSuperClass(line);
			}
			else if (line.startsWith(".source "))
			{
				c.parseSourceFile(line);
			}
			else if (line.startsWith(".implements "))
			{
				c.parseInterface(line);
			}
			else if (line.startsWith(".annotation "))
			{
				ArrayList<String> classAnnotation = new ArrayList<String>();
				classAnnotation.add(line);
				while ((line = in.readLine())!= null && !line.equals(".end annotation"))
				{
					classAnnotation.add(line);
				}
				if (line != null && line.equals(".end annotation"))
					classAnnotation.add(line);
				c.parseAnnotation(classAnnotation);
			}
			else if (line.startsWith(".field "))
			{
				ArrayList<String> fieldDeclaration = new ArrayList<String>();
				fieldDeclaration.add(line);
				line = in.readLine();
				if (line != null && line.startsWith("    .annotation"))
				{
					fieldDeclaration.add(line);
					while ((line = in.readLine())!= null && !line.equals(".end field"))
					{
						fieldDeclaration.add(line);
					}
					if (line != null && line.equals(".end field"))
						fieldDeclaration.add(line);
				}
				c.addField(new ApexField(fieldDeclaration, c));
			}
			else if (line.startsWith(".method "))
			{
				ArrayList<String> methodDeclaration = new ArrayList<String>();
				methodDeclaration.add(line);
				while ((line = in.readLine())!= null && !line.equals(".end method"))
				{
					methodDeclaration.add(line);
				}
				if (line != null && line.equals(".end method"))
					methodDeclaration.add(line);
				c.addMethod(new ApexMethod(methodDeclaration, c));
			}
		}
		in.close();
		c.setSmaliPath(f.getAbsolutePath());
		moveSmaliFile(f, c);
		return c;
	}
	
	// put a copy of the original smali files into "/oldSmali" folder
	// because later instrumentation will overwrite the "/smali" folder
	private static void moveSmaliFile(File f, ApexClass c)
	{
		try
		{
			String dexName = c.getDexName();
			String fullPath = f.getAbsolutePath();
			String rightPart = dexName.substring(1, dexName.length()-1).replace("/", File.separator) + ".smali";
			String leftPart = fullPath.substring(0, fullPath.indexOf(rightPart)).replace("/", File.separator);
			leftPart = leftPart.substring(0, leftPart.lastIndexOf("smali")) + "oldSmali" + File.separator;
			String newFullPath = leftPart + rightPart;
			
			File outFile = new File(newFullPath);
			outFile.getParentFile().mkdirs();
			
			PrintWriter out = new PrintWriter(new FileWriter(newFullPath));
			BufferedReader in = new BufferedReader(new FileReader(f));
			String line;
			while ((line = in.readLine())!=null)
			{
				out.write(line + "\n");
			}
			out.flush();
			in.close();
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
}
