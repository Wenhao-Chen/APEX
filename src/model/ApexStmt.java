package model;

import java.io.Serializable;
import java.util.ArrayList;

public class ApexStmt implements Serializable{

	
	private static final long serialVersionUID = 1L;

	private String smaliStmt = "";
	private int id;
	private ApexMethod containingMethod;
	private StmtDebugInfo debugInfo = null;
	private ArrayList<String> array_or_switch_data = new ArrayList<String>();
	private boolean isInTryBlock = false;
	
	
	public ApexStmt(String smaliStmt, int id, ApexMethod m)
	{
		this.smaliStmt = smaliStmt.trim();
		this.id = id;
		this.containingMethod = m;
	}
	
	public String getSmaliStmt()
	{
		return this.smaliStmt;
	}
	
	public int getID()
	{
		return this.id;
	}

	public ApexMethod getContainingMethod()
	{
		return this.containingMethod;
	}
	
	void set_array_or_switch_data(ArrayList<String> dataChunk)
	{
		this.array_or_switch_data = new ArrayList<String>(dataChunk);
	}
	
	public ArrayList<String> get_array_or_switch_data()
	{
		return this.array_or_switch_data;
	}
	
	void addDebugInfo(String newDebugInfo)
	{
		this.debugInfo.add(newDebugInfo);
	}
	
	void setDebugInfo(ArrayList<String> debugInfo)
	{
		this.debugInfo = new StmtDebugInfo(debugInfo);
	}
	
	public boolean hasBlockLabel()
	{
		return !this.debugInfo.getBlockLabel().isEmpty();
	}
	
	void setIsInTryBlock(boolean isInTryBlock)
	{
		this.isInTryBlock = isInTryBlock;
	}
	
	public boolean isInTryBlock()
	{
		return this.isInTryBlock;
	}
	
	public boolean isInCatchBlock()
	{
		return this.getBlockName().contains(":catch_");
	}
	
	public void copyBlockLabel(ApexStmt s)
	{
		this.debugInfo.copyBlockLabel(s.debugInfo);
	}
	
	public ArrayList<String> getBody()
	{
		ArrayList<String> result = new ArrayList<String>();
		result.addAll(this.debugInfo.getPreStmtSection());
		result.add("    " + this.smaliStmt);
		result.addAll(this.debugInfo.getPostStmtSection());
		return result;
	}
	
	public String getUniqueID()
	{
		return this.containingMethod.getSignature()+ ":" + this.id;
	}
	
	public String getBytecodeOperator()
	{
		return smaliStmt.contains(" ")? 
				 smaliStmt.substring(0, smaliStmt.indexOf(" "))
				:smaliStmt;
	}
	
	public String getBlockName()
	{
		String result = "";
		for (String s : this.debugInfo.getBlockLabel())
		{
			if (s.contains(" "))
				result += s.trim();
			else
				result += s;
		}
		return result;
	}
	
	public boolean isFirstStmtOfBlock()
	{
		return this.debugInfo.isFirstStmtOfBlock();
	}
	
	public boolean isFirstStmtOfMethod()
	{
		return (this.id == 0);
	}
	
	public boolean isReturnStmt()
	{
		return (this.getBytecodeOperator().startsWith("return"));
	}
	
	public boolean isThrowStmt()
	{
		return (this.getBytecodeOperator().startsWith("throw"));
	}
	
	public boolean isIfStmt()
	{
		return (this.getBytecodeOperator().startsWith("if"));
	}
	
	public boolean isSwitchStmt()
	{
		return (this.getBytecodeOperator().endsWith("switch"));
	}
	
	public boolean isGotoStmt()
	{
		return (this.getBytecodeOperator().startsWith("goto"));
	}
	
	public boolean isInvokeStmt()
	{
		return (this.getBytecodeOperator().startsWith("invoke"));
	}
	
	public boolean hasSourceLineNumber()
	{
		return (this.debugInfo.getSourceLineNumber() > 0);
	}
	
	public int getSourceLineNumber()
	{
		return this.debugInfo.getSourceLineNumber();
	}
	
	public boolean hasNoBlockLabel()
	{
		return this.debugInfo.getBlockLabel().isEmpty();
	}
	
	public String getTryStartLabel()
	{
		return this.debugInfo.getTryStartLabel();
	}
	
	public String getTryEndLabel()
	{
		return this.debugInfo.getTryEndLabel();
	}
	
}
