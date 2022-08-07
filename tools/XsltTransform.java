import javax.xml.transform.*;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

public class XsltTransform {

    private static final String APP_NAME = "XsltTransform";
    private static final String APP_VERSION = "1.0";
    

    private static void showUsage() {
        System.out.println(APP_NAME + " Version: " + APP_VERSION);
        System.out.println(String.format("Usage: %s <xlst-file.xsl> <input.xml> <output.html>", APP_NAME));
    }
    
    
    public static void main(String[] args) throws TransformerException {
        if (args.length != 3) {
            System.out.println("Error: Invalid parameters");
            System.out.println();
            showUsage();
            System.exit(1);;
        }
        Source xslt = new StreamSource(new File(args[0]));
        Source xml  = new StreamSource(new File(args[1]));
        Result out  = new StreamResult(new File(args[2]));

        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        Transformer transformer = factory.newTransformer(xslt);
        transformer.transform(xml, out);
        System.out.println("Info: XSLT transform completed");

        System.exit(0);
    }
}

