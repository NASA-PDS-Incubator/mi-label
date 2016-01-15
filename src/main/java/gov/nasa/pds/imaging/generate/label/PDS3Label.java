//	Copyright 2013, by the California Institute of Technology.
//	ALL RIGHTS RESERVED. United States Government Sponsorship acknowledged.
//	Any commercial use must be negotiated with the Office of Technology 
//	Transfer at the California Institute of Technology.
//	
//	This software is subject to U. S. export control laws and regulations 
//	(22 C.F.R. 120-130 and 15 C.F.R. 730-774). To the extent that the software 
//	is subject to U.S. export control laws and regulations, the recipient has 
//	the responsibility to obtain export licenses or other export authority as 
//	may be required before exporting such information to foreign countries or 
//	providing access to foreign nationals.
//	
//	$Id$
//
package gov.nasa.pds.imaging.generate.label;

import gov.nasa.pds.imaging.generate.TemplateException;
import gov.nasa.pds.imaging.generate.collections.PDSTreeMap;
import gov.nasa.pds.imaging.generate.context.ContextUtil;
import gov.nasa.pds.imaging.generate.readers.PDS3LabelReader;
import gov.nasa.pds.imaging.generate.util.Debugger;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.stream.ImageInputStream;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Represents PDS3 Label object to provide the necessary functionality to
 *
 * @author jpadams
 * @author srlevoe
 *
 */
public class PDS3Label implements PDSObject {

    public static final String CONTEXT = "label";
    public ContextUtil ctxtUtil;

	private static final boolean debug = true;

    // Contains the DOM representation of
    // the PDS label as generated by the
    // PDSLabel2DOM parser
    private Document document;
    
    // Contains a flattened representation of
    // the PDS label. In this case, flattened
    // means everything has been normalized
    // to simple keyword=value pairs.
    private Map<String, Map> flatLabel;
    
    private List<String> pdsObjectTypes;
    private List<String> pdsObjectNames;
    
    private String filePath;
    
    private ImageInputStream imageInputStream;
    
    private Long image_start_byte = 0L;

    /**
     * Empty Constructor, set everything later on
     */
    public PDS3Label() {
        this.filePath = null;
        this.flatLabel = new PDSTreeMap();
    }
    
    /**
     * Constructor
     * 
     * Construct the PDS3label using a DOM object from somewhere else
     * 
     * @param document
     */
    public PDS3Label(Document document) {
    	Debugger.debug("PDS3Label constructor document " + document);
        this.filePath = "";
        this.imageInputStream = null;
        this.document = document;
        this.flatLabel = new PDSTreeMap();
    }

    /**
     * Constructor
     *
     * @param filePath
     */
    public PDS3Label(final String filePath) {
        this.filePath = filePath;
        this.flatLabel = new PDSTreeMap();
        
        this.pdsObjectNames = new ArrayList<String>();
    }

    /**
     * Retrieves the value for the specified key
     *
     * @param key
     * @return value for key
     */
    @Override
    public final Object get(final String key) {
        final Object node = getNode(key.toUpperCase());
        Debugger.debug("\n\n++ Get " + key + " ----->");
        if (node == null) {
          return null;
        } else if (node instanceof ItemNode) {
        	Debugger.debug("++++ node(2) ------>\n" + ((ItemNode) node).toString());
            return StringEscapeUtils.escapeXml(((ItemNode) node).toString());
        } else {
        	Debugger.debug("++ node(1) ------>\n" + node);
            return node;
        }
    }

    /**
     * Returns the variable to be used in the Velocity Template Engine to map to
     * this object.
     */
    @Override
    public final String getContext() {
        return CONTEXT;
    }

    @Override
    public final String getFilePath() {
        return this.filePath;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public final List getList(final String key) throws TemplateException {
    	if (getNode(key) == null) {
    		//return new GenerateEmptyList();
    		return new ItemNode(key,null);
    	} else {
    		return ((ItemNode) getNode(key)).getValues();
    	}
    }

    private final Object getNode(final String key) {
        // Handles call where . is embedded in key.
        // Mainly for IndexedGroup implementation.
        if (key.contains(".")) {
            final String[] links = key.split("\\.");
            // object->item
            // object->subobject->item
            if (links[0] == null) {
                return null;
            }
            LabelObject labelObj = (LabelObject) this.flatLabel.get(links[0]);
            if (labelObj == null) {
                return null;
            }
            Object obj = null;
            for (int i = 1; i < links.length; ++i) {
                obj = labelObj.get(links[i]);
                if (obj instanceof LabelObject) {
                    labelObj = (LabelObject) obj;
                }
            }
            return obj;
        }
        
        return this.flatLabel.get(key);
    }

    @Override
    public final String getUnits(final String key) {
        return ((ItemNode) getNode(key)).getUnits();
    }

    @Override
    public final void setParameters(PDSObject pdsObject) {
    	this.filePath = pdsObject.getFilePath();
    	this.imageInputStream = pdsObject.getImageInputStream();
    	Debugger.debug("PDS3Label.setParameters this.imageInputStream = "+ this.imageInputStream + " this.filePath = "+this.filePath+" ***");
    }
    
    public void setImageInputStream(ImageInputStream iis) {
    	imageInputStream = iis;
    }
    
    public ImageInputStream getImageInputStream() {
    	return imageInputStream ;
    }
    
    public void setImageStartByte(Long x) {
    	image_start_byte = x;
    }
    
    @Override
    public Long getImageStartByte() {
    	return image_start_byte;
    }

    @Override
    public void setMappings() {
    	Debugger.debug("+++++++++++++++++++++++++++\n"
    		+ "PDS3Label.setMapping()\n"
    		+ "document " + this.document + "\n"
			+ "+++++++++++++++++++++++++++");
        try {
            final PDS3LabelReader reader = new PDS3LabelReader();

            // Look to see if we a document was already set
        	if (this.document == null) {
				Debugger.debug("+++++++++++++++++++++++++++\n"
					+ "PDS3Label.setMapping() document is null parseLabel");
	            this.document = reader.parseLabel(this.filePath);
			} else { // we already have the document supplied in the constructor
        		Debugger.debug("+++++++++++++++++++++++++++\n"
        			+ "PDS3Label.setMapping() document is GOOD using");	
        	}
        	
            // start of traversal of DOM
            final Node root = this.document.getDocumentElement();

            this.flatLabel = reader.traverseDOM(root);
            this.pdsObjectNames = reader.getPDSObjectNames();
        } catch (final FileNotFoundException fnfe) {
            // TODO - create a logger
            fnfe.printStackTrace();
        }
    }
    
    /**
     * Added per request from mcayanan in order to be able to loop through the
     * PDS Objects that can be found in the label
     * 
     * @return
     */
    public final List<String> getPDSObjectNames() {
    	return this.pdsObjectNames;
    }
    
    @Override
    public final String toString() {
        final StringBuffer strBuff = new StringBuffer();
        final Set<String> keys = this.flatLabel.keySet();
        for (final String key : keys) {
            // String key = (String) iter.next();
            strBuff.append(key + " = " + this.flatLabel.get(key) + "\n");
        }
        return strBuff.toString();
    }

}
