/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.jee.servlet.writer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.PromptContext;
import org.ohmage.domain.PromptProperty;
import org.ohmage.request.SurveyResponseReadAwRequest;


/**
 * Strategy for outputting CSV output for the new data point API.
 * 
 * @author selsky
 */
public class SurveyResponseReadCsvColumnOutputBuilder implements SurveyResponseReadColumnOutputBuilder {
	private static Logger _logger = Logger.getLogger(SurveyResponseReadCsvColumnOutputBuilder.class);
	
	private static String newLine = System.getProperty("line.separator");
	
	public String createMultiResultOutput(int totalNumberOfResults,
			                              SurveyResponseReadAwRequest req,
			                              Map<String, PromptContext> promptContextMap,
			                              Map<String, List<Object>> columnMap) throws JSONException {
		
		_logger.info("Generating multi-result CSV output");
		
		Set<String> columnMapKeySet = columnMap.keySet();
		Set<String> promptContextKeySet = promptContextMap.keySet();
		StringBuilder builder = new StringBuilder();
		
		if(! req.isSuppressMetadata()) {
			builder.append("# begin metadata").append(newLine);
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("result", "success");
			jsonObject.put("campaign_urn", req.getCampaignUrn());
			jsonObject.put("number_of_prompts", totalNumberOfResults);
			jsonObject.put("number_of_surveys", columnMap.get(columnMapKeySet.toArray()[0]).size());
			
			builder.append(jsonObject.toString().replace(",", ";")).append(newLine)	
			       .append("# end metadata").append(newLine).append("# begin prompt contexts").append(newLine);
			
			for(String key : promptContextKeySet) {
				JSONObject prompt = new JSONObject();
				JSONObject context = new JSONObject();
				context.put("unit", promptContextMap.get(key).getUnit() == null ?  JSONObject.NULL : promptContextMap.get(key).getUnit());
				context.put("prompt_type", promptContextMap.get(key).getType());
				context.put("display_type", promptContextMap.get(key).getDisplayType());
				context.put("display_label", promptContextMap.get(key).getDisplayLabel());
				context.put("text", promptContextMap.get(key).getText());
				if(null != promptContextMap.get(key).getChoiceGlossary()) {	
					context.put("choice_glossary", toJson(promptContextMap.get(key).getChoiceGlossary()));
				} else {
					context.put("choice_glossary", JSONObject.NULL);
				}
				prompt.put(key, context); 
				builder.append(prompt.toString().replace(",", ";")).append(newLine);
			}
			builder.append("# end prompt contexts").append(newLine).append("# begin data").append(newLine);
		}
		
		// Build the column headers
		// For the CSV output, user advocates have requested that the column names be made shorter 
		int s = columnMapKeySet.size();
		int i = 0;
		for(String key : columnMapKeySet) {
			String shortHeader = null;
			if(key.startsWith("urn:ohmage:context")) {
				shortHeader = key.replace("urn:ohmage:context", "sys");				
			} else if(key.startsWith("urn:ohmage:prompt:id")) {
				shortHeader = key.replace("urn:ohmage:prompt:id:", "");
			} else if(key.startsWith("urn:ohmage")) {
				shortHeader = key.replace("urn:ohmage:", "");
			}
			builder.append(shortHeader);
			if(i < s - 1) {
				builder.append(",");
			}
			i++;
		}
		builder.append(newLine);
		
		// Build data output row by row
		int listSize = columnMap.get(columnMapKeySet.toArray()[0]).size();
		for(i = 0; i < listSize; i++) {
			int j = 0;
			for(String key : columnMapKeySet) {
				Object value = columnMap.get(key).get(i);
				if(null == value) {
					builder.append("null");
				} else {
					if(value instanceof JSONObject) { //single_choice_custom, multi_choice_custom, launch_context 
						builder.append(((JSONObject) value).toString().replace(",", ";"));
					} else if(value instanceof JSONArray) { // multi_choice
						builder.append(((JSONArray) value).toString().replace(",", ";"));
					} else {
						builder.append(value);
					}
				}
				if(j < columnMapKeySet.size() - 1) {
					builder.append(",");
				}
				j++;
			}
			builder.append(newLine);
		}
		
		if(! req.isSuppressMetadata()) {
			builder.append("# end data").append(newLine);
		}
		
		return builder.toString();
	}
	
	public String createZeroResultOutput(SurveyResponseReadAwRequest req, Map<String, List<Object>> columnMap) 
		throws JSONException  {
		
		_logger.info("Generating zero-result CSV output");
		
		StringBuilder builder = new StringBuilder();
		
		if(! req.isSuppressMetadata()) {
			builder.append("# begin metadata").append(newLine);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("result", "success");
			jsonObject.put("number_of_prompts", 0);
			jsonObject.put("number_of_surveys", 0);
			jsonObject.put("campaign_urn", req.getCampaignUrn());
			builder.append(jsonObject.toString().replace(",", ";")).append(newLine)
			       .append("# end metadata").append(newLine)
			       .append("# begin prompt contexts").append(newLine).append("# end prompt contexts").append(newLine)
			       .append("# begin data").append(newLine);
		}
		
		int s = columnMap.keySet().size();
		
		// Logic that is completely redundant with the method above
		int i = 0;
		for(String key : columnMap.keySet()) {
			String shortHeader = null;
			if(key.startsWith("urn:ohmage:context")) {
				shortHeader = key.replace("urn:ohmage:context", "sys");				
			} else if(key.startsWith("urn:ohmage:prompt:id")) {
				shortHeader = key.replace("urn:ohmage:prompt:id:", "");
			} else if(key.startsWith("urn:ohmage")) {
				shortHeader = key.replace("urn:ohmage:", "");
			}
			builder.append(shortHeader);
			if(i < s - 1) {
				builder.append(",");
			}
			i++;
		}
		builder.append(newLine);
		
		if(! req.isSuppressMetadata()) {
			builder.append("# end data").append(newLine);
		}
		
		return builder.toString();
	}
	
	private Object toJson(Map<String, PromptProperty> ppMap) throws JSONException {
		JSONObject main = new JSONObject();
		Iterator<String> it = ppMap.keySet().iterator();
		while(it.hasNext()) {
			PromptProperty pp = ppMap.get(it.next());
			JSONObject item = new JSONObject();
			item.put("value", pp.getValue());
			item.put("label", pp.getLabel());
			main.put(pp.getKey(), item);
		}
		return main;
	}
}