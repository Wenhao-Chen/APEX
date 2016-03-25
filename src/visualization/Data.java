package visualization;

import gui.LayoutHierarchy;
import gui.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import model.ApexApp;

public class Data {

	public ApexApp app;
	public LayoutHierarchy currentHierarchy;
	public View selectedView;
	public File screenCapFile;
	public String apkPath = "temp/net.mandaria.tippytipper.apk";
	
	public List<LayoutHierarchy> layoutList = new ArrayList<LayoutHierarchy>();
}
