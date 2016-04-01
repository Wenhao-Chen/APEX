package parsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DEXParser {

	public static String[] OpCodes = {
		"nop",
		"move",
		"move/from16",
		"move/16",
		"move-wide",
		"move-wide/from16",
		"move-wide/16",
		"move-object",
		"move-object/from16",
		"move-object/16",
		"move-result",
		"move-result-wide",
		"move-result-object",
		"move-exception",
		"return-void",
		"return",
		"return-wide",
		"return-object",
		"const/4",
		"const/16",
		"const",
		"const/high16",
		"const-wide/16",
		"const-wide/32",
		"const-wide",
		"const-wide/high16",
		"const-string",
		"const-string/jumbo",
		"const-class",
		"monitor-enter",
		"monitor-exit",
		"check-cast",
		"instance-of",
		"array-length",
		"new-instance",
		"new-array",
		"filled-new-array",
		"filled-new-array/range",
		"fill-array-data",
		"throw",
		"goto",
		"goto/16",
		"goto/32",
		"packed-switch",
		"sparse-switch",
		"cmpl-float",
		"cmpg-float",
		"cmpl-double",
		"cmpg-double",
		"cmp-long",
		"if-eq",
		"if-ne",
		"if-lt",
		"if-ge",
		"if-gt",
		"if-le",
		"if-eqz",
		"if-nez",
		"if-ltz",
		"if-gez",
		"if-gtz",
		"if-lez",
		"aget",
		"aget-wide",
		"aget-object",
		"aget-boolean",
		"aget-byte",
		"aget-char",
		"aget-short",
		"aput",
		"aput-wide",
		"aput-object",
		"aput-boolean",
		"aput-byte",
		"aput-char",
		"aput-short",
		"iget",
		"iget-wide",
		"iget-object",
		"iget-boolean",
		"iget-byte",
		"iget-char",
		"iget-short",
		"iput",
		"iput-wide",
		"iput-object",
		"iput-boolean",
		"iput-byte",
		"iput-char",
		"iput-short",
		"sget",
		"sget-wide",
		"sget-object",
		"sget-boolean",
		"sget-byte",
		"sget-char",
		"sget-short",
		"sput",
		"sput-wide",
		"sput-object",
		"sput-boolean",
		"sput-byte",
		"sput-char",
		"sput-short",
		"invoke-virtual",
		"invoke-super",
		"invoke-direct",
		"invoke-static",
		"invoke-interface",
		"invoke-virtual/range",
		"invoke-virtual/range",
		"invoke-super/range",
		"invoke-direct/range",
		"invoke-static/range",
		"invoke-interface/range",
		"neg-int",
		"not-int",
		"neg-long",
		"not-long",
		"neg-float",
		"neg-double",
		"int-to-long",
		"int-to-float",
		"int-to-double",
		"long-to-int",
		"long-to-float",
		"long-to-double",
		"float-to-int",
		"float-to-long",
		"float-to-double",
		"double-to-int",
		"double-to-long",
		"double-to-float",
		"int-to-byte",
		"int-to-char",
		"int-to-short",
		"add-int",
		"sub-int",
		"mul-int",
		"div-int",
		"rem-int",
		"and-int",
		"or-int",
		"xor-int",
		"shl-int",
		"shr-int",
		"ushr-int",
		"add-long",
		"sub-long",
		"mul-long",
		"div-long",
		"rem-long",
		"and-long",
		"or-long",
		"xor-long",
		"shl-long",
		"shr-long",
		"ushr-long",
		"add-float",
		"sub-float",
		"mul-float",
		"div-float",
		"rem-float",
		"add-double",
		"sub-double",
		"mul-double",
		"div-double",
		"rem-double",
		"add-int/2addr",
		"sub-int/2addr",
		"mul-int/2addr",
		"div-int/2addr",
		"rem-int/2addr",
		"and-int/2addr",
		"or-int/2addr",
		"xor-int/2addr",
		"shl-int/2addr",
		"shr-int/2addr",
		"ushr-int/2addr",
		"add-long/2addr",
		"sub-long/2addr",
		"mul-long/2addr",
		"div-long/2addr",
		"rem-long/2addr",
		"and-long/2addr",
		"or-long/2addr",
		"xor-long/2addr",
		"shl-long/2addr",
		"shr-long/2addr",
		"ushr-long/2addr",
		"add-float/2addr",
		"sub-float/2addr",
		"mul-float/2addr",
		"div-float/2addr",
		"rem-float/2addr",
		"add-double/2addr",
		"sub-double/2addr",
		"mul-double/2addr",
		"div-double/2addr",
		"rem-double/2addr",
		"add-int/lit16",
		"rsub-int/lit16",
		"mul-int/lit16",
		"div-int/lit16",
		"rem-int/lit16",
		"and-int/lit16",
		"or-int/lit16",
		"xor-int/lit16",
		"add-int/lit8",
		"rsub-int/lit8",
		"mul-int/lit8",
		"div-int/lit8",
		"rem-int/lit8",
		"and-int/lit8",
		"or-int/lit8",
		"xor-int/lit8",
		"shl-int/lit8",
		"shr-int/lit8",
		"ushr-int/lit8"
	};

	private static List<String> arithmaticalOp = 
			new ArrayList<String>
			(
				Arrays.asList
				(
					/**
					 * rsub: reverse subtraction
					 * */
					new String[]
					{
						"not", "neg", "add", "sub", "rsub", "mul", "div",
						"rem", "and", "or", "xor", "shl", "shr", "ushr", "to"
					}
				)
			);
	
	private static List<String> primitiveTypes = 
			new ArrayList<String>
			(
				Arrays.asList
				(
					new String[]
					{
						"V", "Z", "B", "S", "C",
						"I", "J", "F", "D"
					}
				)
			);
	
	public static boolean isArithmaticalOperator(String operator)
	{
		return DEXParser.arithmaticalOp.contains(operator);
	}
	
	public static boolean isPrimitiveType(String type)
	{
		return DEXParser.primitiveTypes.contains(type);
	}
	
	
	public static String javaTypeToDexType(String javaType)
	{
		switch (javaType)
		{
			case "void":	return "V";
			case "boolean":	return "Z";
			case "byte":	return "B";
			case "short":	return "S";
			case "char":	return "C";
			case "int":		return "I";
			case "long":	return "J";
			case "float":	return "F";
			case "double":	return "D";
		}
		if (javaType.endsWith("[]"))
		{
			String elementJavaType = javaType.substring(0, javaType.length()-2);
			String elementDexType = javaTypeToDexType(elementJavaType);
			return "[" + elementDexType;
		}
		return "L" + javaType.replace(".", "/") + ";";
	}
	
	public static String dexTypeToJavaType(String dexType)
	{
		switch (dexType)
		{
			case "V":	return "void";
			case "Z":	return "boolean";
			case "B":	return "byte";
			case "S":	return "short";
			case "C":	return "char";
			case "I":	return "int";
			case "L":	return "long";
			case "F":	return "float";
			case "D":	return "double";
		}
		if (dexType.startsWith("["))
		{
			String elementDexType = dexType.substring(1);
			String elementJavaType = dexTypeToJavaType(elementDexType);
			return elementJavaType + "[]";
		}
		return dexType.substring(1, dexType.length()-1).replace("/", ".");
	}
	
	
	
}
