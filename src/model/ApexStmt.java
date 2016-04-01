package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ApexStmt implements Serializable{

	
	private static final long serialVersionUID = 1L;

	private String smaliStmt = "";
	private int id;
	private ApexMethod containingMethod;
	private StmtDebugInfo debugInfo = null;
	private ArrayList<String> array_or_switch_data = new ArrayList<String>();
	private boolean isInTryBlock = false;
	
	private ArrayList<String> instrumentedStmts_before = new ArrayList<String>();
	private ArrayList<String> instrumentedStmts_after = new ArrayList<String>();
	
	
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
	
	public ApexStmt getGotoTargetStmt()
	{
		if (!this.isGotoStmt())
			return null;
		String targetLabel = this.smaliStmt.substring(this.smaliStmt.indexOf(":"));
		return this.containingMethod.getFirstStmtOfBlock(targetLabel);
	}
	
	public ApexStmt getIfJumpTargetStmt()
	{
		if (!this.isIfStmt())
			return null;
		String targetLabel = this.smaliStmt.substring(this.smaliStmt.indexOf(":"));
		return this.containingMethod.getFirstStmtOfBlock(targetLabel);
	}
	
	public ApexStmt getFlowThroughStmt()
	{
		if (id < this.containingMethod.getStatements().size()-1)
			return this.containingMethod.getStatement(id+1);
		return null;
	}
	
	public Map<Integer, String> getSwitchMap()
	{
		Map<Integer, String> switchMap = new HashMap<Integer, String>();
		if (this.getBytecodeOperator().equals("sparse-switch"))
		{
			for (String line : this.array_or_switch_data)
			{
				if (!line.contains(" -> "))
					continue;
				String link = line.trim();
				String hexValue = link.substring(0, link.indexOf(" -> "));
				String caseTargetLabel = link.substring(link.indexOf(" -> ")+4);
				int decValue = Integer.parseInt(hexValue.replace("0x", ""), 16);
				switchMap.put(decValue, caseTargetLabel);
			}
		}
		else if (this.getBytecodeOperator().equals("packed-switch"))
		{
			int caseValue = 0;
			for (int i = 0; i < this.array_or_switch_data.size(); i++)
			{
				String line = this.array_or_switch_data.get(i);
				if (line.startsWith("    .packed-switch"))
				{
					String initValueString = line.substring(line.lastIndexOf(" ")+1).replace("0x", "");
					caseValue = Integer.parseInt(initValueString, 16);
				}
				else if (line.startsWith("        :"))
				{
					switchMap.put(caseValue++, line.trim());
				}
			}
		}
		return switchMap;
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
	
	public void addPrecedingStmt(String stmtToAdd)
	{
		this.instrumentedStmts_before.add(stmtToAdd);
	}
	
	public void addSucceedingStmt(String stmtToAdd)
	{
		this.instrumentedStmts_after.add(stmtToAdd);
	}
	
	public ArrayList<String> getInstrumentedPrecedingStmts()
	{
		return this.instrumentedStmts_before;
	}
	
	public ArrayList<String> getInstrumentedSucceedingStmts()
	{
		return this.instrumentedStmts_after;
	}
	
	public ArrayList<String> getInstrumentedBody()
	{
		ArrayList<String> result = new ArrayList<String>();
		result.addAll(this.debugInfo.getPreStmtSection());
		if (!this.getInstrumentedPrecedingStmts().isEmpty())
		{
			result.addAll(this.getInstrumentedPrecedingStmts());
			result.add("");
		}
		result.add("    #id " + this.id);
		result.add("    " + this.smaliStmt);
		result.addAll(this.debugInfo.getPostStmtSection());
		if (!this.getInstrumentedSucceedingStmts().isEmpty())
		{
			result.add("");
			result.addAll(this.getInstrumentedSucceedingStmts());
		}
		return result;
	}
	
	public String toDotGraphString()
	{
		return id + "\t"  + smaliStmt.replace("\"", "\\\"");
	}
	
	public boolean equals(ApexStmt s)
	{
		return (this.containingMethod.equals(s.containingMethod) && this.id == s.id);
	}
	
}
