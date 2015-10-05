package utils.sacha.impl;

import utils.org.eclipse.core.internal.localstore.SafeChunkyInputStream;
import utils.sacha.classloader.enrich.EnrichableClassloader;
import utils.sacha.classloader.factory.ClassloaderFactory;
import utils.sacha.interfaces.IEclipseConfigurable;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class AbstractConfigurator implements IEclipseConfigurable {

	private File projectDir = null;
	private String projectName = null;
	private File metadataFolder = null;

	@Override
	public void setEclipseProject(String projectName) {
		this.projectName = projectName;
		if(projectDir!=null)
			refreshProjectDir();
	}

	@Override
	public void setEclipseMetadataFolder(String metadataLoc) {
		File tmp = new File(metadataLoc);
		if (tmp.exists() && tmp.isDirectory() && tmp.canRead())
			this.metadataFolder = tmp;
		else
			throw new IllegalArgumentException(metadataLoc + " is not a correct absolute path to an eclipse metadata folder");
		if(projectDir!=null)
			refreshProjectDir();
	}

	private void refreshProjectDir() {
		projectDir=getProjectLocation();
	}

	public File getProjectDir() {
		if(projectDir==null)
			refreshProjectDir();
		if(projectDir==null)
			throw new IllegalArgumentException("you have to use setEclipseProject and setEclipseMetadataFolder before");
		return projectDir;
	}
	
	public File getMetadataFolder() {
		if(metadataFolder==null)
			throw new IllegalArgumentException("you have to use setEclipseMetadataFolder before");
		return metadataFolder;
	}

	 /**
     * Get the project location for a project in the eclipse metadata.
     * 
     * @param workspaceLocation the location of the workspace
     * @param project the project subdirectory in the metadata
     * @return the full path to the project.
     * @throws IOException failures to read location file
     * @throws URISyntaxException failures to read location file
     */
	@SuppressWarnings("resource")
	private File getProjectLocation(){
		if(projectName==null || metadataFolder==null){
			throw new IllegalArgumentException("you have to use setEclipseProject and setEclipseMetadataFolder before");
		}
		String locationPath = metadataFolder.getAbsolutePath()+"/.plugins/org.eclipse.core.resources/.projects/"+projectName+"/.location";
		File location = new File(locationPath);
		if (location.exists()) {
			SafeChunkyInputStream fileInputStream = null;
			try {
				fileInputStream = new SafeChunkyInputStream(location);
				DataInputStream dataInputStream = new DataInputStream(fileInputStream);
				String file = dataInputStream.readUTF().trim();

				if (file.length() > 0) {
					if (!file.startsWith("URI//")) {
						throw new IOException(location.getAbsolutePath() + " contains unexpected data: " + file);
					}
					file = file.substring("URI//".length());
					return new File(new URI(file));
				}
			}catch(Throwable t){
				throw new RuntimeException(t);
			}finally {
				try {
					if (fileInputStream != null)
						fileInputStream.close();
				} catch (IOException ioe) {
					// NOOP
				}
			}
		}
		locationPath = metadataFolder.getParentFile().getAbsolutePath()+"/"+projectName;
		location = new File(locationPath);
		if (location.exists()) {
			return location;
		}
		throw new IllegalArgumentException("cannot find project");
	}
	
	protected EnrichableClassloader getEnrichableClassloader(){
		EnrichableClassloader eClassloader = ClassloaderFactory.getEnrichableClassloader();
		eClassloader.addEclipseMetadata(getMetadataFolder());
		eClassloader.addEclipseProject(getProjectDir().getAbsolutePath());
		return eClassloader;
	}

}
