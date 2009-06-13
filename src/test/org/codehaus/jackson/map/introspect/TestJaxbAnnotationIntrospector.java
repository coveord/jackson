package org.codehaus.jackson.map.introspect;

import junit.framework.TestCase;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import java.io.StringWriter;
import java.util.List;
import java.util.Arrays;

/**
 * @author Ryan Heaton
 */
public class TestJaxbAnnotationIntrospector
        extends TestCase
{

    /**
     * tests getting serializer/deserializer instances.
     */
    public void testSerializeDeserializeWithJaxbAnnotations() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getSerializationConfig().setAnnotationIntrospector(new JaxbAnnotationIntrospector());
        mapper.getDeserializationConfig().setAnnotationIntrospector(new JaxbAnnotationIntrospector());
        mapper.getSerializationConfig().set(SerializationConfig.Feature.INDENT_OUTPUT, true);
        JaxbExample ex = new JaxbExample();
        QName qname = new QName("urn:hi", "hello");
        ex.setQname(qname);
        ex.setAttributeProperty("attributeValue");
        ex.setElementProperty("elementValue");
        ex.setWrappedElementProperty(Arrays.asList("wrappedElementValue"));
        ex.setEnumProperty(EnumExample.VALUE1);
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, ex);
        writer.flush();
        writer.close();

        String json = writer.toString();

        // uncomment to see what the json looks like.
        // System.out.println(json);

        //make sure the json is written out correctly.
        JsonNode node = mapper.readValue(json, JsonNode.class);
        assertEquals(qname.toString(), node.get("qname").getValueAsText());
        assertEquals("attributeValue", node.get("myattribute").getValueAsText());
        assertEquals("elementValue", node.get("myelement").getValueAsText());
        assertEquals(1, node.get("mywrapped").size());
        assertEquals("wrappedElementValue", node.get("mywrapped").get(0).getValueAsText());
        assertEquals("Value One", node.get("enumProperty").getValueAsText());

        //now make sure it gets deserialized correctly.
        JaxbExample readEx = mapper.readValue(json, JaxbExample.class);
        assertEquals(ex.qname, readEx.qname);
        assertEquals(ex.attributeProperty, readEx.attributeProperty);
        assertEquals(ex.elementProperty, readEx.elementProperty);
        assertEquals(ex.wrappedElementProperty, readEx.wrappedElementProperty);
        assertEquals(ex.enumProperty, readEx.enumProperty);
    }

    public static enum EnumExample {

        @XmlEnumValue("Value One")
        VALUE1
    }

    public static class JaxbExample
    {

        private String attributeProperty;
        private String elementProperty;
        private List<String> wrappedElementProperty;
        private EnumExample enumProperty;
        private QName qname;

        @XmlJavaTypeAdapter(QNameAdapter.class)
        public QName getQname()
        {
            return qname;
        }

        public void setQname(QName qname)
        {
            this.qname = qname;
        }

        @XmlAttribute(name="myattribute")
        public String getAttributeProperty()
        {
            return attributeProperty;
        }

        public void setAttributeProperty(String attributeProperty)
        {
            this.attributeProperty = attributeProperty;
        }

        @XmlElement(name="myelement")
        public String getElementProperty()
        {
            return elementProperty;
        }

        public void setElementProperty(String elementProperty)
        {
            this.elementProperty = elementProperty;
        }

        @XmlElementWrapper(name="mywrapped")
        public List<String> getWrappedElementProperty()
        {
            return wrappedElementProperty;
        }

        public void setWrappedElementProperty(List<String> wrappedElementProperty)
        {
            this.wrappedElementProperty = wrappedElementProperty;
        }

        public EnumExample getEnumProperty()
        {
            return enumProperty;
        }

        public void setEnumProperty(EnumExample enumProperty)
        {
            this.enumProperty = enumProperty;
        }
    }

    public static class QNameAdapter extends XmlAdapter<String, QName> {

        @Override
        public QName unmarshal(String v)
                throws Exception
        {
            return QName.valueOf(v);
        }

        @Override
        public String marshal(QName v)
                throws Exception
        {
            return v.toString();
        }
    }

}