package utils.sacha.impl;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import utils.sacha.interfaces.IGeneralToJava;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

public class GeneralToJavaCore extends AbstractConfigurator implements IGeneralToJava{

	@Override
	public void changeToJava() {
		changeNature();
		createPrefs();
		createDefaultClasspath();
	}

	private void changeNature() {
		try{
			File project = new File(getProjectDir(),".project");
			if(!project.exists())
				throw new IllegalArgumentException("missing .project file "+project);
			FileInputStream cpReader = new FileInputStream(project);
			Element cpElement;
			try {
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				cpElement = parser.parse(new InputSource(cpReader)).getDocumentElement();
			} finally {
				cpReader.close();
			}
			
			NodeList list = cpElement.getElementsByTagName("buildSpec");
			if(list.getLength()!=1)
				throw new IllegalArgumentException("incorrect .project file "+project);
			Node buildSpec = list.item(0);

			if(cpElement.getElementsByTagName("buildCommand").getLength()==0){
				Element buildCommand = buildSpec.getOwnerDocument().createElement("buildCommand");
				buildSpec.appendChild(buildCommand);
				Element name = buildSpec.getOwnerDocument().createElement("name");
				name.setTextContent("org.eclipse.jdt.core.javabuilder");
				buildCommand.appendChild(name);
			}else{
				throw new IllegalArgumentException("already exist a build command in .project file "+project);
			}

			list = cpElement.getElementsByTagName("natures");
			if(list.getLength()!=1)
				throw new IllegalArgumentException("incorrect .project file "+project);
			Node natures = list.item(0);
			
			if(cpElement.getElementsByTagName("nature").getLength()==0){
				Element nature = natures.getOwnerDocument().createElement("nature");
				nature.setTextContent("org.eclipse.jdt.core.javanature");
				natures.appendChild(nature);
			}else{
				throw new IllegalArgumentException("already exist a nature in .project file "+project);
			}
			
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			DOMSource source = new DOMSource(cpElement);
			StreamResult result = new StreamResult(new FileWriter(project));
			transformer.transform(source, result);

		}catch(Throwable t){
			throw t instanceof RuntimeException?(RuntimeException)t : new RuntimeException(t);
		}
	}

	private void createDefaultClasspath() {
		try{
			File classpath = new File(getProjectDir(),".classpath");
			BufferedWriter out=null;
			try {
				FileWriter fstream = new FileWriter(classpath);
				out = new BufferedWriter(fstream);
				out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
						"<classpath>\n" +
						"\t<classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.7\"/>\n" +
						"\t<classpathentry kind=\"output\" path=\"bin\"/>\n" +
						"</classpath>");
			} finally {
				out.close();
			}
		}catch(Throwable t){
			throw t instanceof RuntimeException?(RuntimeException)t : new RuntimeException(t);
		}
	}
	
	private void createPrefs() {
		try{
			File prefs = new File(getProjectDir().getAbsoluteFile()+"/.settings/org.eclipse.jdt.core.prefs");
			if(!prefs.getParentFile().exists())
				prefs.getParentFile().mkdir();
			BufferedWriter out=null;
			try {
				FileWriter fstream = new FileWriter(prefs);
				out = new BufferedWriter(fstream);
				out.write("org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.7\neclipse.preferences.version=1" +
						"\norg.eclipse.jdt.core.compiler.source=1.7\norg.eclipse.jdt.core.compiler.compliance=1.7");
			} finally {
				out.close();
			}
		}catch(Throwable t){
			throw t instanceof RuntimeException?(RuntimeException)t : new RuntimeException(t);
		}
	}

}
