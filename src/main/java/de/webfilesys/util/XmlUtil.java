package de.webfilesys.util;

import java.io.IOException;
import java.io.Writer;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtil
{
    public static Element getChildByTagName(Element e,String tagname)
    {
        if (e==null )
        {
            return null;
        }

        NodeList children=e.getElementsByTagName(tagname);

        if ((children==null) || (children.getLength()==0))
        {
            return(null);
        }

        return((Element) children.item(0));
    }

    public static String getChildText(Element e,String tagname )
    {
        return getElementText(getChildByTagName(e,tagname));
    }

    public static String getElementText(Element e)
    {
        if (e==null)
        {
            return("");
        }

        NodeList children=e.getChildNodes();

        if (children==null)
        {
            return("");
        }

        StringBuffer text = new StringBuffer();

        int listLength=children.getLength();

        for (int i=0;i<listLength;i++)
        {
            Node node=children.item(i);

            int nodeType = node.getNodeType();

            if ((nodeType==Node.TEXT_NODE) || (nodeType==Node.CDATA_SECTION_NODE))
            {
                String nodeValue=node.getNodeValue();
                if (nodeValue!=null)
                {
                    text.append(nodeValue);
                }
            }
        }

        return(text.toString().trim());
    }

    public static void setElementText(Element e,String newValue)
    {
        setElementText(e,newValue,false);
    }

    /**
     * For compatibility reasons the following is required:
     * If the value of a text node is to be changed, but a CDATA section with this name
     * already exists, the CDATA section is removed an a text node is created or changed.
     *
     * If the value of a CDATA section is to be changed, but a text node with this name
     * already exists, the text node is removed an a CDATA section is created or changed.
     *
     */
    public static void setElementText(Element e,String newValue,boolean cdata)
    {
        if (e==null)
        {
            return;
        }

        Node node=null;

        NodeList children=e.getChildNodes();

        if (children!=null)
        {
            Node childToRemove=null;
            boolean changed=false;

            int listLength=children.getLength();

            for (int i=0;i<listLength;i++)
            {
                node=children.item(i);

                int nodeType=node.getNodeType();

                if (nodeType==Node.TEXT_NODE)
                {
                    if (cdata)
                    {
                        childToRemove=node;
                    }
                    else
                    {
                        node.setNodeValue(newValue);
                        changed=true;
                    }
                }

                if (nodeType==Node.CDATA_SECTION_NODE)
                {
                    if (!cdata)
                    {
                        childToRemove=node;
                    }
                    else
                    {
                        node.setNodeValue(newValue);
                        changed=true;
                    }

                }
            }

            if (childToRemove!=null)
            {
                // System.out.println("removing child " + childToRemove.getNodeValue());
                childToRemove.setNodeValue("");
                e.removeChild(childToRemove);
            }

            if (changed)
            {
                return;
            }
        }

        Document doc=e.getOwnerDocument();

        if (cdata)
        {
            node=doc.createCDATASection(newValue);
        }
        else
        {
            node=doc.createTextNode(newValue);
        }

        e.appendChild(node);
    }


    public static void setChildText(Element e,String tagname,String newValue)
    {
        setChildText(e,tagname,newValue,false);
    }

    public static void setChildText(Element e,String tagname,String newValue,boolean cdata)
    {
        Element child=getChildByTagName(e,tagname);

        if (child==null)
        {
            Document doc=e.getOwnerDocument();
            child=doc.createElement(tagname);
            e.appendChild(child);
        }

        setElementText(child,newValue,cdata);
    }

    public static void removeAllChilds(Element parentElem)
    {
        NodeList children = parentElem.getChildNodes();

        if (children != null)
        {
            int listLength = children.getLength();
            for (int i = listLength - 1; i >= 0; i--)
            {
                Element child = (Element) children.item(i);
                parentElem.removeChild(child);
            }
        }
    }
    
    public static void writeToStream(Element rootElement,Writer outputWriter)
    {
        OutputFormat outputFormat = new OutputFormat();
        outputFormat.setEncoding("UTF-8");
        outputFormat.setLineWidth(0);
        outputFormat.setPreserveSpace(true);
        
        XMLSerializer output = new XMLSerializer(outputWriter,outputFormat);
        try
        {
            output.serialize(rootElement);
        }
        catch (IOException ioex)
        {
            System.out.println(ioex.getMessage());
        }
    }

	public static void writeToStream(Document doc, Writer outputWriter)
	{
		OutputFormat outputFormat = new OutputFormat();
		outputFormat.setEncoding("UTF-8");
		outputFormat.setLineWidth(0);
		outputFormat.setPreserveSpace(true);
        
		XMLSerializer output = new XMLSerializer(outputWriter,outputFormat);
		try
		{
			output.serialize(doc);
		}
		catch (IOException ioex)
		{
			System.out.println(ioex.getMessage());
		}
	}
}
