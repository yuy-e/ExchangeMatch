package Exchange.Matching.server;

import java.io.OutputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;

public class XMLgenerator {
    private Element result;//root element of response
    private Document document;

    public XMLgenerator()  {
        try{
            DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
            DocumentBuilder bd=factory.newDocumentBuilder();
            document=bd.newDocument();
            result=document.createElement("results");
            document.appendChild(result);
        }catch(Exception e){
            e.getStackTrace();
        }
    }

    public Document getDocument(){
        return document;
    }
 

    public String DOMtoXML() throws TransformerException{
        // create the xml file
        // transform the DOM Object to an XML File
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(document);
        transformer.transform( source, result );
        String nodeString = sw.getBuffer().toString();
        return nodeString;

    }


    public Element lineXML(XMLObject XMLobject,String status){
        Element Status=document.createElement(status);
        if (status=="error"){
            Status.appendChild(document.createTextNode(XMLobject.getErrorMessage()));
        }
        Map<String,String> arrMap=XMLobject.getAttribute();
        for (String arr:arrMap.keySet()){
            Attr attr = document.createAttribute(arr);
            attr.setValue(arrMap.get(arr));
            Status.setAttributeNode(attr);
        }
        result.appendChild(Status);
        return Status;
    }

    public Element lineXML(Element root,TransactionId transactionId,String status){
        Element Status=document.createElement(status);
        Map<String,String> arrMap=transactionId.getChild();
        for (String arr:arrMap.keySet()){
            Attr attr = document.createAttribute(arr);
            attr.setValue(arrMap.get(arr));
            Status.setAttributeNode(attr);
        }
        root.appendChild(Status);
        return Status;
    }

}


