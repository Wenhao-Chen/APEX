package model;

import java.io.Serializable;
import java.util.ArrayList;

public class ApexField implements Serializable{

	
	private static final long serialVersionUID = 1L;
	
	private String declaration = "";
	private ArrayList<String> fullDeclaration = new ArrayList<String>();
	
	private ApexClass declaringClass;
		
	public ApexField(ArrayList<String> declaration, ApexClass c)
	{
		if (declaration.isEmpty())
			return;
		this.fullDeclaration = new ArrayList<String>(declaration);
		this.declaration = declaration.get(0);
		this.declaringClass = c;
	}

	public String getDeclarationLine()
	{
		return this.declaration;
	}
	
	public ArrayList<String> getFullDeclaration()
	{
		return this.fullDeclaration;
	}
	
	public ApexClass getDeclaringClass()
	{
		return declaringClass;
	}
	
	public String getSubSignature()
	{
		return getDeclarationLine().substring(getDeclarationLine().lastIndexOf(" ")+1);
	}
	
	public String getSignature()
	{
		return this.declaringClass.getDexName() + "->" + this.getSubSignature();
	}
	
	public String getName() 
	{
		String subSignature = getSubSignature();
		return subSignature.substring(0, subSignature.indexOf(":"));
	}
	
	public String getType() 
	{
		String subSignature = getSubSignature();
		return subSignature.substring(subSignature.indexOf(":")+1);
	}
	
	public boolean hasInitialValueInDeclaration()
	{
		return this.declaration.contains(" = ");
	}
	
	public boolean isPublic() 
	{
		return declaration.contains(" public ");
	}
	
	public boolean isPrivate() 
	{
		return declaration.contains(" private ");
	}
	
	public boolean isProtected() 
	{
		return declaration.contains(" protected ");
	}
	
	public boolean isFinal() 
	{
		return declaration.contains(" final ");
	}
	
	public boolean isStatic() 
	{
		return declaration.contains(" static ");
	}
	
	public boolean isSynthetic() 
	{
		return declaration.contains(" synthetic ");
	}
	
	
	
}
