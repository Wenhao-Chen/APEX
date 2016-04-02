package visualization;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Set;

import model.ApexApp;
import model.ApexAppBuilder;
import model.ApexClass;
import model.ApexCodeBlock;
import model.ApexMethod;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;

import cfg.ControlFlowGraph;
import cfg.Vertex;

public class CFG{


	private ApexApp app;
	public static String apkPath = "/home/wenhaoc/workspace/android-studio/ApexApp/app/build/outputs/apk/app-debug.apk";
	
	public static void main(String[] args)
	{
		CFG cfg = new CFG();
		
	}
	
	public CFG()
	{
		try
		{
		app = ApexAppBuilder.fromAPK(apkPath);
		File graphDir = new File("/home/wenhaoc/graphs");
		graphDir.mkdirs();
		for (File f : graphDir.listFiles())
			f.delete();
		for (ApexClass c : app.getClasses())
		{
			if (c.isLibraryClass())
				continue;
			for (ApexMethod m : c.getMethods())
			{
/*				DOTExporter<ApexStmt, String> exporter = new DOTExporter<ApexStmt, String>(
						new IntegerNameProvider<ApexStmt>(),
						new StringNameProvider<ApexStmt>(),
						new StringEdgeNameProvider<String>());*/
				PrintWriter out = new PrintWriter(new FileWriter("/home/wenhaoc/graphs/"+m.getName() + ".dot"));
				ControlFlowGraph cfg = new ControlFlowGraph(m);
				for (Vertex v : cfg.getVertices())
				{
					if (v.getApexStmt().isIfStmt())
						v.setColor("red");
					if (v.getApexStmt().isReturnStmt())
						v.setColor("green");
				}
				for (int i = 0; i < cfg.getBlockVertices().size(); i++)
				{
					if (i%2 == 0)
						cfg.getBlockVertices().get(i).setColor("blue");
					else
						cfg.getBlockVertices().get(i).setColor("red");
				}
				DirectedGraph<ApexCodeBlock, String> blockCFG = cfg.getBlockJGrapht();
				highlightLoops(blockCFG);
				out.write(cfg.getBlockDotGraph());
				//DirectedGraph g = m.getControlFlowGraph();
				System.out.println("exporting " + m.getSignature());
				CycleDetector cycle = new CycleDetector(blockCFG);

				//System.out.println(g.edgeSet());
				//exporter.export(out, g);
				out.flush();
				out.close();
			}
		}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private <V, E> void highlightLoops(DirectedGraph<V, E> g)
	{
		CycleDetector cycle = new CycleDetector(g);
		Set<V> s = cycle.findCycles();
		for (V v : s)
		{
			
		}
	}
	
	
	
	
}
