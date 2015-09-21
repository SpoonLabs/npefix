package sacha.interfaces;

public interface ISpooner extends IEclipseConfigurable {

	void spoon();

	void setOutputFolder(String folderPath);

	void setProcessors(String... processorNames);

	/** must be set before spooning */
	void setSourceFolder(String... sources);

	void setGraphicalOutput(boolean b);
}
