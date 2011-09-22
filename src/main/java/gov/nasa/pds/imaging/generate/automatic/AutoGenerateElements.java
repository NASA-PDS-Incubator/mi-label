package gov.nasa.pds.imaging.generate.automatic;

import gov.nasa.pds.imaging.generate.TemplateException;
import gov.nasa.pds.imaging.generate.automatic.elements.Element;
import gov.nasa.pds.imaging.generate.context.PDSContext;
import gov.nasa.pds.imaging.generate.util.XMLUtil;

import java.util.HashMap;
import java.util.Map;

public class AutoGenerateElements implements PDSContext {
    /** Specifies the CONTEXT to be mapped to the Velocity Templates. **/
    public static final String CONTEXT = "generate";

    /** The XML File Path for the file with the generated value mappings **/
    public static final String XML_FILENAME = "generated-mappings.xml";

    /** XML element name holding the key value **/
    public static final String XML_KEY = "context";

    /** XML element name holding the mapped value **/
    public static final String XML_VALUE = "class";

    /** Map that will hole the String -> Class mappings specified in the XML **/
    public Map<String, Class<?>> genValsMap = new HashMap<String, Class<?>>();

    private String filePath;
    private String confPath;

    public AutoGenerateElements() {
    }

    public void addMapping(final String key, final Class<?> value) {
        this.genValsMap.put(key, value);
    }

    @Override
    public String get(final String value) throws TemplateException {
        Element el;
        try {
            el = (Element) this.genValsMap.get(value).newInstance();
            el.setParameters(this.filePath);
            return el.getValue();
        } catch (final InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final NullPointerException e) {
            return "Object Not Found";
            // throw new TemplateException("Generated value: " + value +
            // " Not expected.  Verify class mapping exists.");
        }
        return null;
    }

    @Override
    public String getContext() {
        return CONTEXT;
    }

    @Override
    public String getUnits(final String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setConfigPath(final String path) {
        this.confPath = path;
    }

    @Override
    public void setInputPath(final String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void setMappings() throws Exception {
        this.genValsMap.putAll(XMLUtil.getGeneratedMappings(this.confPath + "/"
                + XML_FILENAME, XML_KEY, XML_VALUE));
    }

}
