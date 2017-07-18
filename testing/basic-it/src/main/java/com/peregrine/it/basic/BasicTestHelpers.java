package com.peregrine.it.basic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.peregrine.util.PerConstants.JCR_CONTENT;
import static com.peregrine.util.PerConstants.JCR_PRIMARY_TYPE;
import static com.peregrine.util.PerConstants.JCR_TITLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by schaefa on 6/28/17.
 */
public class BasicTestHelpers {

    public static final String ADMIN_PREFIX_URL = "/api/admin/";

    private static final Logger logger = LoggerFactory.getLogger(BasicTestHelpers.class.getName());

    public static Map listResourceAsJson(SlingClient client, String path, int level) throws ClientException, IOException {
        return convertToMap(
            client.doGet(path + "." + level + ".json", 200)
        );
    }

    public static SlingHttpResponse listResource(SlingClient client, String path,int level) throws ClientException {
        return client.doGet(path + "." + level + ".json", 200);
    }

    public static SlingHttpResponse createFolder(SlingClient client, String path, String name, int expectedStatus) throws ClientException {
        String url = ADMIN_PREFIX_URL + "createFolder.json" + path;
        logger.info("Create Folder with URL: '{}' and Name: '{}'", url, name);
        HttpEntity formEntry = FormEntityBuilder.create().addParameter("name", name).build();
        return client.doPost(url, formEntry, expectedStatus);
    }

    public static void createFolderStructure(SlingClient client, String path) throws ClientException, IOException {
        String[] folders = path.split("/");
        logger.info("Folders: '{}'", Arrays.asList(folders));
        String folderPath = "";
        for(String folder: folders) {
            if(folder != null && !folder.isEmpty()) {
                String fullFolderPath = folderPath + "/" + folder;
                logger.info("Full Folder Path: '{}'", fullFolderPath);
                SlingHttpResponse response;
                try {
                    response = client.doGet(fullFolderPath + ".0.json", 200);
                    // Check response
                    Map listResponse = convertToMap(response.getContent());
                    String primaryType = listResponse.get(JCR_PRIMARY_TYPE) + "";
                    if(
                        "sling:OrderedFolder".equals(primaryType) ||
                        "sling:Folder".equals(primaryType) ||
                        "nt:folder".equals(primaryType)
                    ) {
                        logger.info("Folder is ok: '{}'", listResponse);
                    } else {
                        logger.error("Folder response was not recognized: '{}'", listResponse);
                        fail("Folder response was not recognized: " + listResponse);
                    }
                } catch(ClientException e) {
                    if(e.getHttpStatusCode() == 404) {
                        // Create folder
                        response = createFolder(client, folderPath, folder, 200);
                        // Check response
                        Map listResponse = convertToMap(response.getContent());
                        checkResponse(listResponse, "type", "folder", "status", "created", "name", folder);
                    } else {
                        throw e;
                    }
                }
                logger.info("Create Folder Structure, folder path: '{}', response: '{}'", fullFolderPath, response.getContent());
                folderPath = fullFolderPath;
            }
        }
    }

    public static Map checkResponse(SlingHttpResponse response, String ... nameThenExpected) throws IOException {
        Map answer = convertToMap(response.getContent());
        checkResponse(answer, nameThenExpected);
        return answer;
    }

    public static void checkResponse(Map response, String ... nameThenExpected) {
        for(int i = 0; i < nameThenExpected.length - 1; i = i + 2) {
            String name = nameThenExpected[i];
            String expected = nameThenExpected[i + 1];
            String actual = response.get(name) + "";
            if(!actual.equals(expected)) {
                logger.info("Property: '{}' yielded: '{}' but did not match expected value: '{}'", name, actual, expected);
                fail("Property: " + name +  " yielded unexpected value: " + actual + " but this was expected: " + expected);
            }
        }
    }

    public static Map convertToMap(SlingHttpResponse response) throws IOException {
        return convertToMap(response == null ? null : response.getContent());
    }

    public static Map convertToMap(String json) throws IOException {
        Map answer = new LinkedHashMap();
        if(json != null) {
            ObjectMapper mapper = new ObjectMapper();
            answer = mapper.readValue(json, LinkedHashMap.class);
        }
        return answer;
    }

    public static void checkPages(SlingHttpResponse listResponse, String ... expectedPageNames) throws IOException {
        checkPages(listResponse, false, expectedPageNames);
    }

    public static void checkPages(SlingHttpResponse listResponse, boolean ignoreTitle, String ... expectedPageNames) throws IOException {
        List<String> expectedPageNamesList = new ArrayList<String>();
        if(expectedPageNames != null) {
            for(String name: expectedPageNames) {
                expectedPageNamesList.add(name);
            }
        }
        Map targetFolderMap = convertToMap(listResponse);
        Set<Entry> entries = targetFolderMap.entrySet();
        for(Entry entry: entries) {
            String fieldName = entry.getKey() + "";
            logger.info("Field Name: '{}'", fieldName);
            Object value = entry.getValue();
            logger.info("Item Value: '{}'", value);
            if(value instanceof Map) {
                if(JCR_CONTENT.equals(fieldName)) {
                } else {
                    String expectedPage = expectedPageNamesList.get(0);
                    assertEquals("Unexpected Page (order)", expectedPage, fieldName);
                    Map pageMap = (Map) value;
                    assertEquals("Wrong Page Resource Type", "per:Page", pageMap.get(JCR_PRIMARY_TYPE) + "");
                    Map pageContentMap = (Map) pageMap.get(JCR_CONTENT);
                    assertNotNull("Page Content Node missing", pageContentMap);
                    assertEquals("Unexpected Page Content Type", "per:PageContent", pageContentMap.get(JCR_PRIMARY_TYPE) + "");
                    if(!ignoreTitle) {
                        assertEquals("Unexpected Page Title", expectedPage, pageContentMap.get(JCR_TITLE) + "");
                    }
                    // Found and is ok -> remove
                    expectedPageNamesList.remove(0);
                }
            }
        }
    }

    public static void checkFolders(SlingClient client, String parentPath, String ... expectedFolderNames) throws IOException, ClientException {
        Map targetFolderMap = listResourceAsJson(client, parentPath, 3);
        List<String> expectedFolderNamesList = new ArrayList<String>();
        if(expectedFolderNames != null) {
            for(String name: expectedFolderNames) {
                expectedFolderNamesList.add(name);
            }
        }
        Set<Entry> entries = targetFolderMap.entrySet();
        for(Entry entry: entries) {
            String fieldName = entry.getKey() + "";
            logger.info("Field Name: '{}'", fieldName);
            Object value = entry.getValue();
            logger.info("Item Value: '{}'", value);
            if(value instanceof Map) {
                if(JCR_CONTENT.equals(fieldName)) {
                } else {
                    String expectedFolderName = expectedFolderNamesList.get(0);
                    assertEquals("Unexpected Folder (order)", expectedFolderName, fieldName);
                    Map pageMap = (Map) value;
                    assertEquals("Wrong Folder Resource Type", "sling:OrderedFolder", pageMap.get(JCR_PRIMARY_TYPE) + "");
                    // Found and is ok -> remove
                    expectedFolderNamesList.remove(0);
                }
            }
        }
    }

    public static void createFolders(SlingClient client, String rootFolder, String...folderNames) throws ClientException, IOException {
        for(String folderName: folderNames) {
            createFolder(client, rootFolder, folderName, 200);
            checkFolderExists(client, rootFolder, folderName);
        }
    }

    public static void checkFolderExists(SlingClient client, String rootPath, String folderName) throws IOException, ClientException {
        Map folder = listResourceAsJson(client, rootPath + "/" + folderName, 1);
        String primaryType = folder.get(JCR_PRIMARY_TYPE) + "";
        assertEquals("Wrong Primary Type", "sling:OrderedFolder", primaryType);
    }

    public static Map<String, Map> extractChildNodes(Map source) {
        Map<String, Map> answer = new LinkedHashMap<String, Map>();
        Set<Entry> entries = source.entrySet();
        for(Entry entry: entries) {
            String fieldName = entry.getKey() + "";
            logger.info("Field Name: '{}'", fieldName);
            Object value = entry.getValue();
            logger.info("Item Value: '{}'", value);
            if(value instanceof Map) {
                if(!JCR_CONTENT.equals(fieldName)) {
                    answer.put(fieldName, (Map) value);
                }
            }
        }
        return answer;
    }

//    public static enum CheckType { allProvided, everythingMatches, }
    public static void checkResourceByJson(SlingClient client, String path, int levels, String expectedJson, boolean checkProvided) throws ClientException, IOException {
        SlingHttpResponse response = client.doGet(path + "." + levels + ".json", 200);
        assertEquals("Unexpected Mime Type", "application/json;charset=utf-8", response.getFirstHeader("Content-Type").getValue());
        String jsonResponse = response.getContent();
        ObjectMapper mapper = new ObjectMapper();
        Map expected = mapper.readValue(expectedJson, Map.class);
        Map actual = mapper.readValue(jsonResponse, Map.class);
        compareJson(expected, actual);
    }

    public static void compareJson(Map<Object, Object> expected, Map actual) throws IOException {
        for(Entry<Object, Object> entry: expected.entrySet()) {
            Object key = entry.getKey() + "";
            assertTrue("Did not find Property with Name: " + key, actual.containsKey(key));
            Object value = entry.getValue();
            if(value instanceof Boolean) {
                assertEquals("Boolean Property mismatch. Name: " + key, value, actual.get(key));
            } else if(value instanceof Number) {
                assertEquals("Number Property mismatch. Name: " + key, value, actual.get(key));
            } else if(value instanceof String) {
                assertEquals("String Property mismatch. Name: " + key, value, actual.get(key));
            } else if(value instanceof Object[]) {
                fail("Sling Json reponse should not contain an array. Name: " + key);
            } else if(value instanceof Map) {
                Map expectedChild = (Map) value;
                Map actualChild = (Map) actual.get(key);
                assertNotNull("Child: " + key + " not found as child in response", actualChild);
                compareJson(expectedChild, actualChild);
            } else {
                fail("Unkown type of value: " + value.getClass());
            }
        }
    }
}