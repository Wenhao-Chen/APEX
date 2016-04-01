package instrumentation;

public class LoggerSmali {

	
	public static final String tagName = "APEXISU";
	
	public static final String body = 
	//".class public Lapex/app/Logger;" + "\n" + 
	".super Ljava/lang/Object;" + "\n" + 
	
	".method public constructor <init>()V" + "\n" + 
	"    .locals 0" + "\n" + 
	
	"    .prologue" + "\n" + 
	"    invoke-direct {p0}, Ljava/lang/Object;-><init>()V" + "\n" + 
	
	"    return-void" + "\n" + 
	".end method" + "\n" + 
	
	".method public static d(Ljava/lang/String;)V" + "\n" + 
	"    .locals 1" + "\n" + 
	
	"    .prologue" + "\n" + 
	"    const-string v0, \"" + tagName + "\"\n" + 
	
	"    invoke-static {v0, p0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I" + "\n" + 
	
	"    return-void" + "\n" + 
	".end method";
	
	public static String getSmaliCode(String className)
	{
		String line1 = ".class public " + className;
		return line1 + "\n" + body;
	}
	
	public static String getPrintMethodSubSignature()
	{
		return "d(Ljava/lang/String;)V";
	}
	
}
