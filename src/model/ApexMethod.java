package model;

import java.io.Serializable;
import java.util.ArrayList;

public class ApexMethod implements Serializable{

	
	private static final long serialVersionUID = 1L;
	
	private String declaration = "";
	private ApexClass declaringClass;
	private ArrayList<String> paramTypes = new ArrayList<String>();
	private int localRegisterCount = -1;
	private int instrumentedLocalRegisterCount = -1;
	private ArrayList<String> paramDeclarations = new ArrayList<String>();
	private ArrayList<String> catchInfo = new ArrayList<String>();
	private ArrayList<String> annotations = new ArrayList<String>();
	
	private ArrayList<ApexStmt> statements = new ArrayList<ApexStmt>();
	private ArrayList<ArrayList<String>> supplementalData = new ArrayList<ArrayList<String>>();
	private ArrayList<ApexCodeBlock> blocks = new ArrayList<ApexCodeBlock>();
	
	
	public ApexMethod(ArrayList<String> declaration, ApexClass c)
	{
		if (declaration.isEmpty())
			return;
		this.declaration = declaration.get(0);
		this.declaringClass = c;
		parseParams();
		parseBody(declaration);
	}

	private void parseParams()
	{
		String subSig = getSubSignature();
		String params = subSig.substring(subSig.indexOf("(") + 1, subSig.indexOf(")"));
		int index = 0;
		while (index < params.length())
		{
			char c = params.charAt(index++);
			if (c == 'L')
			{	// Non-primitive type
				String type = c + "";
				while (c != ';')
				{
					c = params.charAt(index++);
					type += c;
				}
				this.paramTypes.add(type);
			}
			else if (c == '[')
			{	// Array
				String type = c + "";
				while (c == '[')
				{
					c = params.charAt(index++);
					type += c;
				}
				if (c == 'L')
				{
					while (c != ';')
					{
						c = params.charAt(index++);
						type += c;
					}
				}
				this.paramTypes.add(type);
			}
			else
			{	// Primitive type
				this.paramTypes.add(c+"");
			}
		}
		if (!this.isStatic())
		{
			this.paramTypes.add(0, this.declaringClass.getDexName());
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseBody(ArrayList<String> body)
	{
		if (body.size() <= 2)
			return;
		int i = 1;
		ArrayList<String> debugInfo = new ArrayList<String>();
		ArrayList<ApexStmt> currentBlock = new ArrayList<ApexStmt>();
		boolean inTryBlock = false;
		while (i < body.size())
		{
			String line = body.get(i);
			if (line.equals("") || line.equals(".end method"))
			{}
			else if (line.startsWith("    .locals "))
			{
				this.localRegisterCount = Integer.parseInt(line.substring(line.lastIndexOf(" ")+1));
				this.instrumentedLocalRegisterCount = this.localRegisterCount;
			}
			else if (line.startsWith("    .param "))
			{
				this.paramDeclarations.add(line);
				if (body.get(i+1).startsWith("        .annotation "))
				{
					while (!line.equals("    .end param"))
					{
						line = body.get(++i);
						this.paramDeclarations.add(line);
					}
				}
			}
			else if (line.startsWith("    .annotation "))
			{
				this.annotations.add(line);
				while (!line.equals("    .end annotation"))
				{
					line = body.get(++i);
					this.annotations.add(line);
				}
				this.annotations.add("");
			}
			else if (line.startsWith("    .") || line.startsWith("    :") || line.startsWith("    #"))
			{
				if (line.startsWith("    :array_"))
				{
					String label = line.trim();
					ArrayList<String> chunk = new ArrayList<String>(debugInfo);
					debugInfo.clear();
					while (!line.equals("    .end array-data"))
					{
						chunk.add(line);
						line = body.get(++i);
					}
					chunk.add(line);
					this.supplementalData.add(chunk);
					for (ApexStmt s : this.statements)
					{
						if (s.getSmaliStmt().endsWith(label))
						{
							s.set_array_or_switch_data(chunk);
							break;
						}
					}
				}
				else if (line.startsWith("    :pswitch_data_"))
				{
					String label = line.trim();
					ArrayList<String> chunk = new ArrayList<String>(debugInfo);
					debugInfo.clear();
					while (!line.equals("    .end packed-switch"))
					{
						chunk.add(line);
						line = body.get(++i);
					}
					chunk.add(line);
					this.supplementalData.add(chunk);
					
					for (ApexStmt s : this.statements)
					{
						if (s.getSmaliStmt().endsWith(label))
						{
							s.set_array_or_switch_data(chunk);
							break;
						}
					}
				}
				else if (line.startsWith("    :sswitch_data_"))
				{
					String label = line.trim();
					ArrayList<String> chunk = new ArrayList<String>(debugInfo);
					debugInfo.clear();
					while (!line.equals("    .end sparse-switch"))
					{
						chunk.add(line);
						line = body.get(++i);
					}
					chunk.add(line);
					this.supplementalData.add(chunk);
					
					for (ApexStmt s : this.statements)
					{
						if (s.getSmaliStmt().endsWith(label))
						{
							s.set_array_or_switch_data(chunk);
							break;
						}
					}
				}
				else if (line.startsWith("    :try_start_"))
				{
					inTryBlock = true;
					debugInfo.add(line);
				}
				else if (line.startsWith("    :try_end_"))
				{
					this.statements.get(this.statements.size()-1).addDebugInfo(line);
					inTryBlock = false;
				}
				else if (line.startsWith("    .catch"))
				{
					this.statements.get(this.statements.size()-1).addDebugInfo(line);
					this.catchInfo.add(line);
				}
				else
				{
					debugInfo.add(line);
				}
			}
			else
			{
				ApexStmt s = new ApexStmt(line, this.statements.size(), this);
				s.setDebugInfo(debugInfo);
				debugInfo.clear();
				if (s.hasBlockLabel() && !currentBlock.isEmpty())
				{
					this.blocks.add(new ApexCodeBlock((ArrayList<ApexStmt>) currentBlock.clone()));
					currentBlock.clear();
				}
				if (!s.hasBlockLabel() && !this.statements.isEmpty())
				{
					s.copyBlockLabel(this.statements.get(this.statements.size()-1));
				}
				s.setIsInTryBlock(inTryBlock);
				this.statements.add(s);
				currentBlock.add(s);
				if (s.isGotoStmt() || s.isReturnStmt() || s.isThrowStmt() || s.isIfStmt() || s.isSwitchStmt())
				{
					this.blocks.add(new ApexCodeBlock((ArrayList<ApexStmt>) currentBlock.clone()));
					currentBlock.clear();
				}
			}
			i++;
		}
	}
	
	
	public ArrayList<String> getBody()
	{
		ArrayList<String> result = new ArrayList<String>();
		result.add(this.declaration);
		if (this.localRegisterCount > -1)
			result.add("    .locals " + this.localRegisterCount);
		result.addAll(this.paramDeclarations);
		if (this.annotations.size()>0)
		{
			result.addAll(this.annotations);
		}
		else
		{
			result.add("");
		}
		for (int i = 0; i < this.statements.size(); i++)
		{
			ApexStmt s = this.statements.get(i);
			result.addAll(s.getBody());
			if (i < this.statements.size()-1 || !this.supplementalData.isEmpty())
				result.add("");
		}
		for (int i = 0; i < this.supplementalData.size(); i++)
		{
			result.addAll(this.supplementalData.get(i));
			if (i < this.supplementalData.size()-1)
				result.add("");
		}
		result.add(".end method");
		return result;
	}
	
	public ArrayList<String> getInstrumentedBody()
	{
		ArrayList<String> result = new ArrayList<String>();
		result.add(this.declaration);
		if (this.localRegisterCount > -1)
			result.add("    .locals " + this);
		result.addAll(this.paramDeclarations);
		if (this.annotations.size()>0)
		{
			result.addAll(this.annotations);
		}
		else
		{
			result.add("");
		}
		for (int i = 0; i < this.statements.size(); i++)
		{
			ApexStmt s = this.statements.get(i);
			result.addAll(s.getBody());
			if (i < this.statements.size()-1 || !this.supplementalData.isEmpty())
				result.add("");
		}
		for (int i = 0; i < this.supplementalData.size(); i++)
		{
			result.addAll(this.supplementalData.get(i));
			if (i < this.supplementalData.size()-1)
				result.add("");
		}
		result.add(".end method");
		return result;
	}
	
	public String getDeclaration()
	{
		return this.declaration;
	}
	
	public ApexClass getDeclaringClass()
	{
		return this.declaringClass;
	}
	
	public String getSignature()
	{
		return this.declaringClass.getDexName() + "->" + this.getSubSignature();
	}
	
	public String getName()
	{
		String subSig = getSubSignature();
		return subSig.substring(0, subSig.indexOf("("));
	}
	
	public String getSubSignature()
	{
		return this.declaration.substring(this.declaration.lastIndexOf(" ")+1);
	}
	
	public boolean isStatic()
	{
		return this.declaration.contains(" static ");
	}
	
	public boolean isAbstract()
	{
		return this.declaration.contains(" abstract ");
	}
	
	public boolean isNative()
	{
		return this.declaration.contains(" native ");
	}
	
	public boolean isPrivate()
	{
		return this.declaration.contains(" private ");
	}
	
	public boolean isProtected()
	{
		return this.declaration.contains(" protected ");
	}
	
	public boolean throwsException()
	{
		for (String line : this.annotations)
		{
			if (line.equals("    .annotation system Ldalvik/annotation/Throws;"))
				return true;
		}
		return false;
	}
	
	public int getLocalRegisterCount() 
	{
		return localRegisterCount;
	}
	
	public void setInstrumentLocalRegisters(int newLocalCount)
	{
		this.instrumentedLocalRegisterCount = newLocalCount;
	}
	
	public int getInstrumentedLocalRegisterCount()
	{
		return this.instrumentedLocalRegisterCount;
	}
	
	public ArrayList<ApexStmt> getStatements() 
	{
		return this.statements;
	}
	
	public ArrayList<ApexCodeBlock> getCodeBlocks()
	{
		return this.blocks;
	}
	
	public ApexCodeBlock getCodeBlockContaining(ApexStmt s)
	{
		for (ApexCodeBlock block : blocks)
			if (block.contains(s))
				return block;
		return null;
	}

	public ArrayList<String> getParamTypes() 
	{
		return paramTypes;
	}
	
	public int getParamRegisterCount()
	{
		int result = 0;
		for (String pType : paramTypes)
		{
			if (pType.equals("J") || pType.equals("D"))
				result += 2;
			else
				result += 1;
		}
		return result;
	}
	
	public ApexStmt getStatement(int statementID)
	{
		if (statementID >= 0 && this.statements.size() > statementID)
			return this.statements.get(statementID);
		return null;
	}
	
	public ApexStmt getStatementByLineNumber(int lineNumber)
	{
		if (lineNumber < 1)
			return null;
		for (ApexStmt s : this.statements)
		{
			if (s.getSourceLineNumber() == lineNumber)
				return s;
		}
		return null;
	}
	
	public ApexStmt getFirstStmtOfBlock(String blockLabel)
	{
		for (ApexStmt s : this.statements)
		{
			if (s.isFirstStmtOfBlock() && 
					s.getBlockName().contains(blockLabel))
			{
				return s;
			}
		}
		return null;
	}
	
	public ApexStmt getTryStartStmt(String tryStartLabel)
	{
		for (ApexStmt s : this.statements)
		{
			if (s.getTryStartLabel().equals(tryStartLabel))
				return s;
		}
		return null;
	}
	
	public ApexStmt getTryEndStmt(String tryEndLabel)
	{
		for (ApexStmt s : this.statements)
		{
			if (s.getTryEndLabel().equals(tryEndLabel))
				return s;
		}
		return null;
	}
	
	public String getTryStartLabel(String catchLabel)
	{
		for (String info : this.catchInfo)
		{
			if (info.endsWith(catchLabel))
			{
				String tryRange = info.substring(info.indexOf("{")+1, info.indexOf("}"));
				String tryStart = tryRange.split(" .. ")[0];
				return tryStart;
			}
		}
		return "";
	}
	
	public String getTryEndLabel(String catchLabel)
	{
		for (String info : this.catchInfo)
		{
			if (info.endsWith(catchLabel))
			{
				String tryRange = info.substring(info.indexOf("{")+1, info.indexOf("}"));
				String tryEnd = tryRange.split(" .. ")[1];
				return tryEnd;
			}
		}
		return "";
	}
	
	public ArrayList<String> getDataChunk(String label)
	{
		ArrayList<String> result = new ArrayList<String>();
		for (ArrayList<String> chunk : this.supplementalData)
		{
			int index = 0;
			String line = "";
			while (!line.startsWith("    :"))
			{
				line = chunk.get(index++);
			}
			if (!line.trim().equals(label))
			{
				continue;
			}
			while (index < chunk.size())
			{
				result.add(chunk.get(index++));
			}
			return result;
		}
		return result;
	}
	
	
	
	public String toString()
	{
		return this.getSignature();
	}
	
	public boolean equals(ApexMethod m)
	{
		return this.getSignature().equals(m.getSignature());
	}
}
