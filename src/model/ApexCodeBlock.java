package model;

import java.util.ArrayList;

public class ApexCodeBlock {

	
	private ArrayList<ApexStmt> stmts = new ArrayList<ApexStmt>();
	
	ApexCodeBlock(ArrayList<ApexStmt> stmts)
	{
		this.stmts = stmts;
	}
	
	public ArrayList<ApexStmt> getApexStmts()
	{
		return stmts;
	}
	
	public String toDotGraphString()
	{
		String result = "";
		for (int i = 0; i < stmts.size(); i++)
		{
			result += stmts.get(i).toDotGraphString() + "\\l";
		}
		return result;
	}
	
	
	public boolean contains(ApexStmt s)
	{
		for (ApexStmt stmt : stmts)
		{
			if (stmt.equals(s))
				return true;
		}
		return false;
	}
	
	
	
}
