package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApexClass implements Serializable{

	
	private static final long serialVersionUID = 1L;
	
	private String smaliPath;
	private String declaration;
	private String superClass;
	private String sourceFileName;
	private List<String> interfaces = new ArrayList<String>();
	private List<String> annotations = new ArrayList<String>();
	private List<ApexField> fields = new ArrayList<ApexField>();
	private List<ApexMethod> methods = new ArrayList<ApexMethod>();
	
	public ApexClass()
	{}
	
	public ApexClass(String declaration)
	{
		this.declaration = declaration;
	}
	
	public String getDeclaration()
	{
		return declaration;
	}

	public void setSmaliPath(String smaliPath)
	{
		this.smaliPath = smaliPath;
	}
	
	public String getSmaliPath()
	{
		return this.smaliPath;
	}
	
	public void parseSuperClass(String line)
	{
		this.superClass = line.substring(line.lastIndexOf(" ")+1);
	}
	
	public String getSuperClass()
	{
		return this.superClass;
	}
	
	public void parseSourceFile(String line)
	{
		this.sourceFileName = line.substring(line.lastIndexOf(".source ")+8).replace("\"", "");
	}
	
	public String getSourceFileName()
	{
		return this.sourceFileName;
	}
	
	public void parseInterface(String line)
	{
		this.interfaces = new ArrayList<String>();
		String interfaceName = line.substring(line.indexOf(".implements ")+12);
		this.interfaces.add(interfaceName);
	}
	
	public List<String> getInterfaces()
	{
		return this.interfaces;
	}
	
	public void parseAnnotation(ArrayList<String> classAnnotation)
	{
		this.annotations = new ArrayList<String>();
		this.annotations.addAll(classAnnotation);
		this.annotations.add("");
		//TODO There are 4 types of class annotations:
		//".annotation system Ldalvik/annotation/MemberClasses;"
		//".annotation system Ldalvik/annotation/InnerClass;"
		//".annotation system Ldalvik/annotation/EnclosingMethod;"
		//".annotation system Ldalvik/annotation/EnclosingClass;"
	}
	
	public List<String> getAnnotations()
	{
		return this.annotations;
	}
	
	public void addField(ApexField f)
	{
		this.fields.add(f);
	}
	
	public List<ApexField> getFields()
	{
		return this.fields;
	}
	
	public void addMethod(ApexMethod m)
	{
		this.methods.add(m);
	}
	
	public List<ApexMethod> getMethods()
	{
		return this.methods;
	}
	
	public String getJavaName()
	{
		String result = declaration.substring(
				declaration.lastIndexOf(" ")+2, 
				declaration.length()-1);
		return result.replace("/", ".");
	}
	
	public String getDexName() {
		return declaration.substring(
				declaration.lastIndexOf(" ")+1, 
				declaration.length());
	}
	
	public boolean isPublic()
	{
		return declaration.contains(" public ");
	}
	
	public boolean isPrivate()
	{
		return declaration.contains(" private ");
	}
	
	public boolean isInterface()
	{
		return declaration.contains(" interface ");
	}
	
	public boolean isFinal()
	{
		return declaration.contains(" final ");
	}
	
	public boolean isAbstract()
	{
		return declaration.contains(" abstract ");
	}
	
	public boolean isProtected()
	{
		return declaration.contains(" protected ");
	}

	
	public ArrayList<String> getBody()
	{
		ArrayList<String> result = new ArrayList<String>();
		result.add(this.declaration);
		result.add(".super " + this.superClass);
		if (!this.sourceFileName.equals(""))
			result.add(".source \"" + this.sourceFileName + "\"");
		result.add("");
		if (this.interfaces.size()>0)
		{
			for (String s : this.interfaces)
				result.add(".implements " + s);
			result.add("");
		}
		if (this.annotations.size()>0)
		{
			for (String s : this.annotations)
				result.add(s);
		}
		for (ApexField f : this.fields)
		{
			result.addAll(f.getFullDeclaration());
			result.add("");
		}
		for (ApexMethod m : this.methods)
		{
			result.addAll(m.getBody());
			result.add("");
		}
		return result;
	}
	
	public ApexMethod getMethodBySignature(String methodSignature)
	{
		for (ApexMethod m : this.methods)
		{
			if (m.getSignature().equals(methodSignature))
				return m;
		}
		return null;
	}
	
	public ApexMethod getMethodBySubsignature(String methodSubsig)
	{
		for (ApexMethod m : this.methods)
		{
			if (m.getSubSignature().equals(methodSubsig))
				return m;
		}
		return null;
	}
	
	public ApexField getFieldBySubsignature(String fieldSubSig)
	{
		for (ApexField f : this.fields)
		{
			if (f.getSubSignature().equals(fieldSubSig))
				return f;
		}
		return null;
	}
	
	public ApexField getFieldBySignature(String fieldSignature)
	{
		for (ApexField f : this.fields)
		{
			if (f.getSignature().equals(fieldSignature))
				return f;
		}
		return null;
	}

	public ApexStmt getStmtWithLineNumber(int lineNumber) {
		for (ApexMethod m : this.methods)
		{
			ApexStmt s = m.getStatementByLineNumber(lineNumber);
			if (s != null)
				return s;
		}
		return null;
	}
}
