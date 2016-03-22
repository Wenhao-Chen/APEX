package model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApexApp implements Serializable{


	private static final long serialVersionUID = 1L;
	
	
	private String packageName;
	private List<ApexClass> classes = new ArrayList<ApexClass>();
	private String apkPath;
	private String outputDir;
	
	ApexApp() {}
	
	
	
	public String getAPKPath()
	{
		return apkPath;
	}
	
	public void setAPKPath(String apkPath)
	{
		this.apkPath = apkPath;
	}
	
	public String getPackageName() 
	{
		return packageName;
	}

	public void setPackageName(String packageName) 
	{
		this.packageName = packageName;
	}


	public List<ApexClass> getClasses() 
	{
		return classes;
	}

	public void addClass(ApexClass c) 
	{
		this.classes.add(c);
	}

	public String getOutputDir()
	{
		return outputDir;
	}

	public void setOutputDir(String outputDir)
	{
		this.outputDir = outputDir;
	}
	
	public ApexClass getClassByJavaName(String classJavaName)
	{
		for (ApexClass c : classes)
			if (c.getJavaName().equals(classJavaName))
				return c;
		return null;
	}

	public ApexClass getClassByDexName(String classDexName)
	{
		for (ApexClass c : classes)
			if (c.getDexName().equals(classDexName))
				return c;
		return null;
	}
	
	public ApexMethod getMethod(String methodSignature)
	{
		if (!methodSignature.contains("->"))
			return null;
		String className = methodSignature.substring(0, methodSignature.indexOf("->"));
		ApexClass c = getClassByDexName(className);
		if (c != null)
		{
			return c.getMethodBySignature(methodSignature);
		}
		return null;
	}
	
	public ApexField getField(String fieldSignature)
	{
		if (!fieldSignature.contains("->"))
			return null;
		String className = fieldSignature.substring(0, fieldSignature.indexOf("->"));
		String subSig = fieldSignature.substring(fieldSignature.indexOf("->")+2);
		ApexClass c = getClassByDexName(className);
		if (c != null)
			return c.getFieldBySubsignature(subSig);
		return null;
	}
	
	public String getInstrumentedApkPath() {
		String result = this.outputDir + File.separator;
		result += apkPath.substring(apkPath.lastIndexOf(File.separator)+1, apkPath.lastIndexOf(".apk"));
		result += "_instrumented.apk";
		
		return result;
	}

	public String getUnsignedApkPath() {
		String result = this.outputDir + File.separator;
		result += apkPath.substring(apkPath.lastIndexOf(File.separator)+1, apkPath.lastIndexOf(".apk"));
		result += "_unsigned.apk";
		return result;
	}
	
	/**
	 * Find statement using method signature and bytecode index
	 * @return the ApexStmt object
	 * @param stmtInfo Statement unique ID with format of <method signature>:<statement id>, e.g., "Lcom/example/MainActivity;->test()V:23"
	 * 
	 * */
	public ApexStmt getStmtByUniqueID(String stmtInfo) {
		String methodSig = stmtInfo.split(":")[0];
		int stmtID = Integer.parseInt(stmtInfo.split(":")[1]);
		ApexMethod m = this.getMethod(methodSig);
		if (stmtID >= 0 && m != null && m.getStatements().size() > stmtID)
			return m.getStatements().get(stmtID);
		return null;
	}
	
	public ApexStmt getStmtByLineNumber(String stmtInfo)
	{
		String className = stmtInfo.split(":")[0];
		int lineNumber = Integer.parseInt(stmtInfo.split(":")[1]);
		ApexClass c = this.getClassByJavaName(className);
		if (c != null)
		{
			return c.getStmtWithLineNumber(lineNumber);
		}
		return null;
	}
}
