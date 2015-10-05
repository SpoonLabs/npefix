package utils.sacha.project.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import utils.sacha.impl.AbstractConfigurator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MavenModulesMerger extends AbstractConfigurator implements IMavenMerger{

	@Override
	public void merge() {
		try{
			File projectFolder = getProjectDir();
			File maven = new File(projectFolder, "pom.xml");
			if(!maven.exists() || !maven.canRead())
				throw new IllegalArgumentException("cannot access file :"+maven);
			File classpath = new File(projectFolder, ".classpath");
			if(!classpath.exists() || !classpath.canRead() || ! classpath.canWrite())
				throw new IllegalArgumentException("cannot access file :"+classpath);
			
			FileInputStream mvnReader = new FileInputStream(maven);
			Element mvnElement;
			try {
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				mvnElement = parser.parse(new InputSource(mvnReader)).getDocumentElement();
			} finally {
				mvnReader.close();
			}
			NodeList list = mvnElement.getElementsByTagName("module");
			int length = list.getLength();
			List<File> modules = new ArrayList<>();
			Node node;
			for (int i = 0; i < length; ++i) {
				node = list.item(i);
				modules.add(new File(projectFolder,node.getTextContent()));
			}

			Set<String> libraries = new HashSet<>();
			Set<String> vars = new HashSet<>();
			Set<String> sourceFolders = new HashSet<>();
			
			for (File module : modules) {
				File meClasspath = new File(module,".classpath");
				FileInputStream meReader = new FileInputStream(meClasspath);
				Element meElement;
				try {
					DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					meElement = parser.parse(new InputSource(meReader)).getDocumentElement();
				} finally {
					meReader.close();
				}
				NodeList list1 = meElement.getElementsByTagName("classpathentry");
				int length1 = list1.getLength();
				Node node1;
				for (int i1 = 0; i1 < length1; ++i1) {
					node1 = list1.item(i1);
					if("lib".equalsIgnoreCase(node1.getAttributes().getNamedItem("kind").getNodeValue())){
						String currentPathString = node1.getAttributes().getNamedItem("path").getNodeValue();
						if(!(currentPathString.startsWith("/")))
							currentPathString=module.getAbsolutePath()+File.separator+currentPathString;
						libraries.add(currentPathString);
					}else if("var".equalsIgnoreCase(node1.getAttributes().getNamedItem("kind").getNodeValue())){
						String currentPathString = node1.getAttributes().getNamedItem("path").getNodeValue();
						vars.add(currentPathString);
					}else if("src".equalsIgnoreCase(node1.getAttributes().getNamedItem("kind").getNodeValue())){
						String currentPathString = node1.getAttributes().getNamedItem("path").getNodeValue();
						if(!currentPathString.startsWith("/")){
							sourceFolders.add(module.getName()+File.separator+currentPathString);
						}
					}
				}
			}
			
			FileInputStream cpReader = new FileInputStream(classpath);
			Element cpElement;
			try {
				DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				cpElement = parser.parse(new InputSource(cpReader)).getDocumentElement();
			} finally {
				cpReader.close();
			}

			NodeList list1 = cpElement.getElementsByTagName("classpathentry");
			int length1 = list1.getLength();
			Node node1;
			List<Node> removable = new ArrayList<>();
			for (int i1 = 0; i1 < length1; ++i1) {
				node1 = list1.item(i1);
				if("lib".equalsIgnoreCase(node1.getAttributes().getNamedItem("kind").getNodeValue())){
					removable.add(node1);
				}else if("var".equalsIgnoreCase(node1.getAttributes().getNamedItem("kind").getNodeValue())){
					removable.add(node1);
				}else if("src".equalsIgnoreCase(node1.getAttributes().getNamedItem("kind").getNodeValue())){
					removable.add(node1);
				}
			}
			for (Node node2 : removable) {
				cpElement.removeChild(node2);
			}
			
			for (String lib : libraries) {
				cpElement.appendChild(createLineBreak(cpElement.getOwnerDocument()));
				cpElement.appendChild(createNode("lib",lib,cpElement.getOwnerDocument()));
			}
			for (String var : vars) {
				cpElement.appendChild(createLineBreak(cpElement.getOwnerDocument()));
				cpElement.appendChild(createNode("var",var,cpElement.getOwnerDocument()));
			}
			for (String src : sourceFolders) {
				cpElement.appendChild(createLineBreak(cpElement.getOwnerDocument()));
				cpElement.appendChild(createNode("src",src,cpElement.getOwnerDocument()));
			}
			cpElement.appendChild(cpElement.getOwnerDocument().createTextNode("\n"));
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();

			DOMSource source = new DOMSource(cpElement);
			StreamResult result = new StreamResult(new FileOutputStream(classpath));
			transformer.transform(source, result);
 
		}catch(Throwable t){
			throw t instanceof RuntimeException?(RuntimeException)t : new RuntimeException(t);
		}
	}
	
	private Node createLineBreak(Document document) {
		return document.createTextNode("\n\t");
	}

	private Node createNode(String type, String value, Document document) {
		Element node = document.createElement("classpathentry");
		node.setAttribute("kind", type);
		node.setAttribute("path", value);
		return node;
	}

}
