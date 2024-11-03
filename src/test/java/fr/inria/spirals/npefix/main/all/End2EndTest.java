package fr.inria.spirals.npefix.main.all;

import org.junit.Ignore;
import org.junit.Test;

public class End2EndTest {
	@Test
	@Ignore
	public void test() throws Exception {
		// test end 2 end for helping @Peanu11 in https://github.com/SpoonLabs/npefix/issues/29

		// execute command ls
		Runtime.getRuntime().exec("rm -rf npefix-src");

		fr.inria.spirals.npefix.main.run.Main.main(new String[] { "-s"
				, "../npe-dataset/pdfbox_2965/src/"
				, "-c"
				, "../npe-dataset/pdfbox_2965/target/classes/:../npe-dataset/pdfbox_2965/target/test-classes/:/home/martin/.m2/repository/org/apache/pdfbox/fontbox/2.0.0-RC1/fontbox-2.0.0-RC1.jar:/home/martin/.m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:/home/martin/.m2/repository/org/bouncycastle/bcpkix-jdk15on/1.50/bcpkix-jdk15on-1.50.jar:/home/martin/.m2/repository/org/bouncycastle/bcprov-jdk15on/1.50/bcprov-jdk15on-1.50.jar:/home/martin/.m2/repository/junit/junit/4.12/junit-4.12.jar:/home/martin/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar"
				, "-t"
				, "org.apache.pdfbox.pdmodel.interactive.form.PDAcroFormTest"
		});


	}
}
