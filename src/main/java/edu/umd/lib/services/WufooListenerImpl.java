package edu.umd.lib.services;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.Exchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WufooListenerImpl {

  /*********************************************
   * process the request and parses the field names and field values.
   ***/
  public void processRequest(Exchange exchange) {

    String message = exchange.getIn().getBody(String.class);
    Map<String, List<String>> parameters = getQueryParams(message);

    exchange.getOut().setBody("Thank you for the submission");
    System.out.println("Total Number of Parameters from the request:" + parameters.size());

    try {

      checkHandshake(parameters);
      Map<String, String> fields = getFields(parameters);
      getFieldStructure(parameters.get("FieldStructure"), fields);

    } catch (JSONException e) {
      e.printStackTrace();
    }

  }

  /***********************************************
   * Created key value pair from the url string
   ****/
  public Map<String, List<String>> getQueryParams(String paramaters) {

    try {

      Map<String, List<String>> params = new HashMap<String, List<String>>();
      String query = paramaters;

      for (String param : query.split("&")) {
        String[] pair = param.split("=");
        String key = URLDecoder.decode(pair[0], "UTF-8");
        String value = "";
        if (pair.length > 1) {
          value = URLDecoder.decode(pair[1], "UTF-8");
        }
        List<String> values = params.get(key);
        if (values == null) {
          values = new ArrayList<String>();
          params.put(key, values);
        }
        values.add(value);
      }
      // printingMap(params);
      return params;
    } catch (UnsupportedEncodingException ex) {
      throw new AssertionError(ex);
    }
  }

  /***********************************************
   * Checks for handshake key and validates if request is valid
   ****/
  public void checkHandshake(Map<String, List<String>> parameters) {
    String handshake = parameters.get("HandshakeKey").get(0);
    System.out.println("handshake:" + handshake);
  }

  /***********************************************
   * Get the fields and values
   ****/
  public Map<String, String> getFields(Map<String, List<String>> parameters) {
    Map<String, String> fields = new HashMap<String, String>();
    Set<String> parameterNames = parameters.keySet();
    for (String name : parameterNames) {
      if (name.contains("Field")) {
        fields.put(name, parameters.get(name).get(0));
      }
    }
    /* Removes field structure from fields map */
    fields.remove("FieldStructure");
    return fields;
  }

  /***********************************************
   * Get the field Structure
   ***/
  public JSONArray getFieldStructure(List<String> fieldStructure, Map<String, String> fields) throws JSONException {

    JSONArray json = new JSONArray(fieldStructure);
    JSONArray fieldsList = new JSONArray();

    for (int m = 0; m < json.length(); m++) {
      JSONObject jsonObject = new JSONObject(json.get(m).toString());
      fieldsList = (JSONArray) jsonObject.get("Fields");
    }
    for (int i = 0; i < fieldsList.length(); i++) {
      JSONObject field = fieldsList.getJSONObject(i);
      field.put("Value", fields.get(field.get("ID")));
    }
    System.out.println("Field List :" + fieldsList);
    return fieldsList;
  }

  /***********************************************
   * Method to print Hash maps
   ***/
  @SuppressWarnings("unchecked")
  public void printingMap(Map<String, ?> parameters) {

    for (Map.Entry<String, ?> entry : parameters.entrySet()) {
      if (entry.getValue() instanceof List<?>) {
        String value = "";
        for (int i = 0; i < ((Map<String, List<String>>) entry.getValue()).size(); i++) {
          value = value + ((List<String>) entry.getValue()).get(i);
        }
        System.out.println(entry.getKey() + ":" + value);
      } else {
        System.out.println(entry.getKey() + ":" + entry.getValue());
      }

    }
  }

}