package fr.inria.spirals.npefix.main.spoon;

import fr.inria.spirals.npefix.transformer.processors.BeforeDerefAdder;
import fr.inria.spirals.npefix.transformer.processors.ForceNullInit;
import fr.inria.spirals.npefix.transformer.processors.IfSplitter;
import fr.inria.spirals.npefix.transformer.processors.MethodEncapsulation;
import fr.inria.spirals.npefix.transformer.processors.TargetModifier;
import fr.inria.spirals.npefix.transformer.processors.TryRegister;
import fr.inria.spirals.npefix.transformer.processors.VarRetrieveAssign;
import fr.inria.spirals.npefix.transformer.processors.VarRetrieveInit;
import fr.inria.spirals.npefix.transformer.processors.VariableFor;
import utils.sacha.impl.DefaultSpooner;
import utils.sacha.interfaces.ISpooner;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainSpoon {

	public static void main(String[] args) {
		try{
			BufferedReader br = null;
			Object selection = null;
			String[] arg = null;
			try {
				File targetsFile = new File("targets");
				if(targetsFile.exists()){
					Map<String, String[]> targets = new HashMap<String, String[]>();
					List<String> options = new ArrayList<>();
					br = new BufferedReader(new InputStreamReader(
							new FileInputStream(targetsFile)));
					String line;
					while ((line = br.readLine()) != null) {
						if(line.startsWith("#"))continue;
						String[] lineB = line.split(":",2);
						String[] arguments = lineB[1].split("\\s");
						targets.put(lineB[0],arguments);
						options.add(lineB[0]);
					}
					Collections.sort(options);
					Object[] selectionValues = options.toArray();
				    selection = JOptionPane.showInputDialog(null, "Which project?", "spoon", 
				    		JOptionPane.QUESTION_MESSAGE, null, selectionValues, "test");
				    arg=targets.get(selection);
				}
			} finally {
				if(br!=null)
					br.close();
			}
			ISpooner spooner = new DefaultSpooner();
			//project config
			spooner.setEclipseProject(arg[0]);
			spooner.setEclipseMetadataFolder("/home/thomas/workspace/.metadata");
			String[] srcs = arg[1].split(":");
			//spoon config
			spooner.setSourceFolder(srcs);
			spooner.setProcessors(
					IfSplitter.class,
					ForceNullInit.class,
					BeforeDerefAdder.class,
					TargetModifier.class,
					TryRegister.class,
					MethodEncapsulation.class,
					VariableFor.class,
					VarRetrieveAssign.class,
					VarRetrieveInit.class
					);
			spooner.setOutputFolder(arg[2]);
			if(arg[0].equals("test"))
				spooner.setGraphicalOutput(true);
			cleanOutput(new File(arg[2]));

			spooner.spoon();
			System.err.println("spoon done");
		}catch(Throwable t){
			t.printStackTrace();
		}
	}
	
	private static void cleanOutput(File outputFolder) {
		if (outputFolder.exists()) {
			recursifDeleteJavaFiles(outputFolder);
		}
	}

	private static boolean recursifDeleteJavaFiles(File file) {
		boolean delete = true;
		if (file.exists()) {
			if (file.isDirectory())
				for (File child : file.listFiles()) {
					delete &= recursifDeleteJavaFiles(child);
				}
			if(!delete || (file.isFile() && !file.getName().endsWith(".java"))){
				return false;
			}
			file.delete();
		}
		return delete;
	}

}
