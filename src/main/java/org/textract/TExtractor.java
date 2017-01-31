package org.textract;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TExtractor {


	private static final ResourceBundle GLOBAL_CONFIGURATION = ResourceBundle.getBundle("config");
	
	private HashMap<String, Field> hmMasterFields;
    private HashMap<String, HashMap<String, FieldSource>> hmFieldSource;
	
    public TExtractor() throws Exception {
    	init();
    }

    //TODO to complete obj piece
    public void init() throws Exception{
        String sourcesXmlFile = GLOBAL_CONFIGURATION.getString("xml.file.sources");

        XMLHelper xmlHelper = new XMLHelper();
        xmlHelper.load(new FileInputStream(new File(sourcesXmlFile)));
        Node[] roots = xmlHelper.getElements();
        Node rootTag = roots[0];
        NodeList sources = rootTag.getChildNodes();
        for(int i = 0; i<sources.getLength();i++){
              Node sourceNode = sources.item(i);
              if(sourceNode.getNodeType()==Node.ELEMENT_NODE)
              {
                    // its a source tag
            	  String sourceName = sourceNode.getAttributes().item(0).getNodeValue();
            	  HashMap<String, FieldSource> fieldMap = new HashMap<String, FieldSource>();
            	  for (int j = 0; j < sourceNode.getChildNodes().getLength(); j++) {
            		  Node fieldNode = sourceNode.getChildNodes().item(j);
                      if(fieldNode.getNodeType()==Node.ELEMENT_NODE){
	                	  FieldSource fs = new FieldSource();
	                	  NamedNodeMap fieldAttribs = fieldNode.getAttributes();
	                	  fs.setFieldName(fieldAttribs.getNamedItem("name").getNodeValue());
	                	  fs.setSearchType(Integer.parseInt(fieldAttribs.getNamedItem("searchtype").getNodeValue()));
	                	  switch (fs.getSearchType()) {
							case FieldSource.SEARCH_TYPE_SIMPLE_REGEX:
								fs.setRegex(fieldAttribs.getNamedItem("regex").getNodeValue());
								fs.setOffset(Integer.parseInt(fieldAttribs.getNamedItem("offset").getNodeValue()));
								break;
							case FieldSource.SEARCH_TYPE_OFFSET_LENGTH:
								fs.setOffset(Integer.parseInt(fieldAttribs.getNamedItem("offset").getNodeValue()));
								fs.setLength(Integer.parseInt(fieldAttribs.getNamedItem("length").getNodeValue()));
								break;
							case FieldSource.SEARCH_TYPE_OFFSET_TERMINATION:
								fs.setRegex(fieldAttribs.getNamedItem("regex").getNodeValue());
								fs.setTerminatingChar(fieldAttribs.getNamedItem("termination").getNodeValue());
								break;
							case FieldSource.SEARCH_TYPE_REGEX_OFFSET_LENGTH:
								fs.setRegex(fieldAttribs.getNamedItem("regex").getNodeValue());
								fs.setOffset(Integer.parseInt(fieldAttribs.getNamedItem("offset").getNodeValue()));
								fs.setLength(Integer.parseInt(fieldAttribs.getNamedItem("length").getNodeValue()));		
								fs.setTerminatingChar(fieldAttribs.getNamedItem("termination").getNodeValue());
								break;
							case FieldSource.SEARCH_TYPE_REGEX_OFFSET_TERMINATION:
								fs.setRegex(fieldAttribs.getNamedItem("regex").getNodeValue());
								fs.setOffset(Integer.parseInt(fieldAttribs.getNamedItem("offset").getNodeValue()));
								fs.setTerminatingChar(fieldAttribs.getNamedItem("termination").getNodeValue());
								break;
	                	  }
	                	  fieldMap.put(fs.getFieldName(), fs);
                      }
            	  }
            	  if(hmFieldSource == null)
            		  hmFieldSource = new HashMap<String, HashMap<String,FieldSource>>();
            	  hmFieldSource.put(sourceName,fieldMap);
              }
        }

        String candidateFieldsXmlFile  = GLOBAL_CONFIGURATION.getString("xml.file.fields");
        xmlHelper.load(new FileInputStream(new File(candidateFieldsXmlFile)));
        roots = xmlHelper.getElements();
        rootTag = roots[0];
        NodeList candidateFields = rootTag.getChildNodes();
        for(int i = 0; i<candidateFields.getLength();i++){
            Node fieldNode = candidateFields.item(i);
            if(fieldNode.getNodeType()==Node.ELEMENT_NODE) {
          	  	Field cf = new Field();
          	  	cf.setFieldName(fieldNode.getAttributes().getNamedItem("name").getNodeValue());
          	  	cf.setFieldType(Integer.parseInt(fieldNode.getAttributes().getNamedItem("type").getNodeValue()));
//          	  	NodeList fieldDetails = fieldNode.getChildNodes();
//          	  	for (int j = 0; j < fieldDetails.getLength(); j++) {
//					Node detailNode = fieldDetails.item(j);
//					if(detailNode.getNodeType() == Node.ELEMENT_NODE){
//						if("db".equalsIgnoreCase(detailNode.getNodeName())){
//							cf.setTableName(detailNode.getAttributes().getNamedItem("table").getNodeValue());
//							cf.setColumnName(detailNode.getAttributes().getNamedItem("col").getNodeValue());
//							cf.setColumnType(detailNode.getAttributes().getNamedItem("dbtype").getNodeValue());
//						}else if("ref".equalsIgnoreCase(detailNode.getNodeName())){
//							if(cf.getFieldType() == Field.FIELD_TYPE_REFERENCE){
//								cf.setReferenceQuery(detailNode.getAttributes().getNamedItem("query").getNodeValue());
//							}
//						}
//					}
//				}
          	  	if(hmMasterFields == null)
          	  		hmMasterFields = new HashMap<String, Field>();
          	  	hmMasterFields.put(cf.getFieldName(), cf);
            }
        }
        System.out.println("Initialized...");
    }
     
	public void doExtract(String source, String content) {
		System.out.println("Beginning extraction!");
        HashMap<String, String> fieldvalues = new HashMap<String, String>();
        HashMap<String, FieldSource> hm = hmFieldSource.get(source+"");
        for(String key : hm.keySet()){
        	FieldSource fs = hm.get(key);
        	fieldvalues.put(fs.getFieldName(),getFieldData(fs, content));       	
		}
        //TODO need to implement post-extraction cleansing
		System.out.println("EXTRACTED INFO\n "+fieldvalues);
    }

    private String getFieldData(FieldSource fs, String content) {
    	if(fs.getSearchType() == FieldSource.SEARCH_TYPE_SIMPLE_REGEX)
    	{
    		Pattern pat = Pattern.compile(fs.getRegex());
	    	Matcher mat = pat.matcher(content);
	    	String returnValue = null;
	        while (mat.find()){
	        	returnValue = mat.group();
	        }
	        return returnValue;
    	}
    	else if(fs.getSearchType() == FieldSource.SEARCH_TYPE_OFFSET_LENGTH){
    		return content.substring(fs.getOffset(), fs.getOffset()+fs.getLength());
	    }
    	else if(fs.getSearchType() == FieldSource.SEARCH_TYPE_OFFSET_TERMINATION){
    		return content.substring(fs.getOffset(), content.indexOf(fs.getTerminatingChar(), fs.getOffset()));    		
    	}
    	else if(fs.getSearchType() == FieldSource.SEARCH_TYPE_REGEX_OFFSET_LENGTH){
    		Pattern pat = Pattern.compile(fs.getRegex());
	    	Matcher mat = pat.matcher(content);
	    	int end=0;
	        while (mat.find()){
	        	end=mat.end();
	        }
	        content = content.substring(end);
	        return content.substring(fs.getOffset(), fs.getOffset()+fs.getLength());
	    }
    	else if(fs.getSearchType() == FieldSource.SEARCH_TYPE_REGEX_OFFSET_TERMINATION){
    		Pattern pat = Pattern.compile(fs.getRegex());
	    	Matcher mat = pat.matcher(content);
	    	int end=0;
	        while (mat.find()){
	        	end=mat.end();
	        }
	        content=content.substring(end);
	        String temp=null;
			try {
				if(fs.getTerminatingChar().startsWith("\\")){
					temp = content.substring(fs.getOffset(), content.indexOf("\n", fs.getOffset()));					
				}else{
					if(content.indexOf(fs.getTerminatingChar())!=-1){
						temp = content.substring(fs.getOffset(), content.indexOf(fs.getTerminatingChar(), fs.getOffset()));
						temp=temp.replaceAll("[+]","").trim();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	        return temp;
    	}
    	
    	return null;
    }
    
    public static void main(String[] args) throws Exception {
		TExtractor processor = new TExtractor();
		BufferedReader br = new BufferedReader(new FileReader("/home/dev/one.txt"));
		StringBuffer sb = new StringBuffer();
		String content = null;
		while((content = br.readLine())!=null){
			sb.append(content+"\n");
		}
		br.close();
		processor.doExtract("Source1", sb.toString());
	}

}
