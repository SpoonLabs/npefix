package utils.sacha.interfaces;

public interface ISpooner extends IEclipseConfigurable {

	void spoon();

	void setOutputFolder(String folderPath);

	void setProcessors(String... processorNames);
	
	void setProcessors(Class... processor);

	/** must be set before spooning */
	void setSourceFolder(String... sources);

	void setGraphicalOutput(boolean b);
}
