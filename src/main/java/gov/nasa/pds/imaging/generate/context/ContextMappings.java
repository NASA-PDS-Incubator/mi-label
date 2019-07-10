	package gov.nasa.pds.imaging.generate.context;

import gov.nasa.pds.imaging.generate.TemplateException;
import gov.nasa.pds.imaging.generate.label.PDSObject;
import gov.nasa.pds.imaging.generate.util.XMLUtil;
import gov.nasa.pds.imaging.generate.util.Debugger;

import java.util.HashMap;

public class ContextMappings {

    public static final String XML_FILENAME = "context-classes.xml";

    /** XML element name holding the key value **/
    public static final String XML_TAG = "class";

    public HashMap<String, PDSContext> contextMap = new HashMap<String, PDSContext>();
    
    public ContextMappings() throws TemplateException, Exception {
        for (final String cl : XMLUtil.getClassList(ContextMappings.class.getResourceAsStream(XML_FILENAME), XML_TAG)) {
            final PDSContext context = (PDSContext) Class.forName(cl).newInstance();
            this.contextMap.put(context.getContext(), context);
        }
    }

    /**
     * Populates the contextMap with those classes specified in the context
     * mappings XML file.
     * 
     * @throws TemplateException
     * @throws Exception
     */
    public ContextMappings(final PDSObject pdsObject)
            throws TemplateException, Exception {
    	Debugger.debug("Generator.ContextMappings()");
        for (final String cl : XMLUtil.getClassList(ContextMappings.class.getResourceAsStream(XML_FILENAME),
                XML_TAG)) {
        	Object instance = Class.forName(cl).newInstance();
        	if (instance instanceof PDSObjectContext) {
	            final PDSObjectContext context = (PDSObjectContext) instance;
	            context.setParameters(pdsObject);
	            context.setMappings();
	            this.contextMap.put(context.getContext(), context);
        	} else if (instance instanceof PDSContext) {
	            final PDSContext context = (PDSContext) instance;
	            this.contextMap.put(context.getContext(), context);
        	}
        }
    }

    public void addMapping(final String key, final PDSContext value) {
        this.contextMap.put(key, value);
    }

}
