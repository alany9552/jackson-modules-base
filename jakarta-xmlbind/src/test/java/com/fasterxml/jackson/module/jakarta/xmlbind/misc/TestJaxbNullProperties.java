package com.fasterxml.jackson.module.jakarta.xmlbind.misc;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.module.jakarta.xmlbind.ModuleTestBase;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;

/**
 * Unit tests to ensure that handling of writing of null properties (or not)
 * works when using JAXB annotation introspector.
 */
public class TestJaxbNullProperties
    extends ModuleTestBase
{
    static class Bean
    {
       public String empty;

       public String x = "y";
    }

    @XmlRootElement
    static class BeanWithNillable {
        public Nillable X;
    }

    @XmlRootElement
    static class Nillable {
        @XmlElement (name="Z", nillable=true)
        Integer Z;

        public Nillable() { }
        public Nillable(int i) {
            Z = Integer.valueOf(i);
        }
    } 

    @XmlRootElement
    static class NonNillableZ {
        @XmlElement(name="z", nillable=false)
        public Integer z;

        public NonNillableZ() { }
        public NonNillableZ(int i) {
            z = Integer.valueOf(i);
        }
    } 

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */

    private final ObjectMapper MAPPER = getJaxbMapper();
    
    public void testWriteNulls() throws Exception
    {
        BeanWithNillable bean = new BeanWithNillable();
        bean.X = new Nillable();
        assertEquals("{\"X\":{\"Z\":null}}", MAPPER.writeValueAsString(bean));
    }

    public void testNullProps() throws Exception
    {
        ObjectMapper mapper = getJaxbMapperBuilder()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();
        assertEquals("{\"x\":\"y\"}", mapper.writeValueAsString(new Bean()));
    }

    public void testNillability() throws Exception
    {
        ObjectMapper mapper = getJaxbMapper();
        // by default, something not marked as nillable will still be written if null
        assertEquals("{\"z\":null}", mapper.writeValueAsString(new NonNillableZ()));
        assertEquals("{\"z\":3}", mapper.writeValueAsString(new NonNillableZ(3)));

        // but we can change that...
        mapper = getJaxbMapperBuilder()
                .annotationIntrospector(new JakartaXmlBindAnnotationIntrospector()
                        .setNonNillableInclusion(JsonInclude.Include.NON_NULL)
                    )
                .addModule(new JakartaXmlBindAnnotationModule().setNonNillableInclusion(JsonInclude.Include.NON_NULL))
                .build();
        assertEquals("{}", mapper.writeValueAsString(new NonNillableZ()));
        assertEquals("{\"z\":3}", mapper.writeValueAsString(new NonNillableZ(3)));
    }
}
