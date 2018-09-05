/*
import br.edu.ufrgs.inf.bpm.application.TestBpmnXml;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestStructuredText {

    private String structuredText;

    @Before
    public void initialize(){
        structuredText = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<text>\n" +
                "  <sentence hasBulletPoint=\"false\" newLineAmount=\"1\"\n" +
                "    resource=\"Resource 1\" tabAmount=\"0\" value=\"The process begins when the Resource 1 does an activity 1.\">\n" +
                "    <subsentence endIndex=\"57\" processElementType=\"activity\" startIndex=\"0\"/>\n" +
                "  </sentence>\n" +
                "  <sentence hasBulletPoint=\"false\" newLineAmount=\"0\" resource=\"\"\n" +
                "    tabAmount=\"0\" value=\"The process is split into 2 parallel branches:\">\n" +
                "    <subsentence endIndex=\"45\" processElementType=\"andsplit\" startIndex=\"0\"/>\n" +
                "  </sentence>\n" +
                "  <sentence hasBulletPoint=\"true\" newLineAmount=\"1\"\n" +
                "    resource=\"Resource 1\" tabAmount=\"1\" value=\"The Resource 1 does the activity 2.\">\n" +
                "    <subsentence endIndex=\"34\" processElementType=\"activity\" startIndex=\"0\"/>\n" +
                "  </sentence>\n" +
                "  <sentence hasBulletPoint=\"true\" newLineAmount=\"1\" resource=\"\"\n" +
                "    tabAmount=\"1\" value=\"One of the following branches is executed.\">\n" +
                "    <subsentence endIndex=\"41\" processElementType=\"xorsplit\" startIndex=\"0\"/>\n" +
                "  </sentence>\n" +
                "  <sentence hasBulletPoint=\"true\" newLineAmount=\"1\"\n" +
                "    resource=\"Resource 2\" tabAmount=\"2\" value=\"The Resource 2 does the activity 3.\">\n" +
                "    <subsentence endIndex=\"34\" processElementType=\"activity\" startIndex=\"0\"/>\n" +
                "  </sentence>\n" +
                "  <sentence hasBulletPoint=\"true\" newLineAmount=\"1\"\n" +
                "    resource=\"Resource 3\" tabAmount=\"2\" value=\"The Resource 3 does the activity 4.\">\n" +
                "    <subsentence endIndex=\"34\" processElementType=\"activity\" startIndex=\"0\"/>\n" +
                "  </sentence>\n" +
                "  <sentence hasBulletPoint=\"false\" newLineAmount=\"1\"\n" +
                "    resource=\"Resource 1\" tabAmount=\"0\" value=\"Once one of the following branches was executed and all 2 branches were executed, the Resource 1 does the activity 5.\">\n" +
                "    <subsentence endIndex=\"116\" processElementType=\"activity\" startIndex=\"82\"/>\n" +
                "    <subsentence endIndex=\"47\" processElementType=\"xorjoin\" startIndex=\"0\"/>\n" +
                "    <subsentence endIndex=\"80\" processElementType=\"andjoin\" startIndex=\"52\"/>\n" +
                "  </sentence>\n" +
                "  <sentence hasBulletPoint=\"false\" newLineAmount=\"0\" resource=\"\"\n" +
                "    tabAmount=\"0\" value=\"The process is finished.\">\n" +
                "    <subsentence endIndex=\"23\" processElementType=\"endevent\" startIndex=\"0\"/>\n" +
                "  </sentence>\n" +
                "</text>\n";
    }

    @Test
    public void shouldReturnEqualStruct(){
        Assert.assertEquals(TestBpmnXml.getStrucutedText(), structuredText);
    }

}
*/