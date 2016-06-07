package com.rgroult.visualreview;

/**
 * Created by rgroult on 06/06/16.
 */

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Date;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class VisualReviewClient {
    private static  String VR_SERVER = "";

    public static void main(String[] args) {
        System.out.println("started on " + new Date());
        try {
            if (args.length != 4){
                help("VisualReviewClient");
                System.exit(-1);
            }

            //parse args
            HashMap<String,String> argsMap = new HashMap<String,String>();
            for(int i=0;i<args.length;i+=2){
                argsMap.put(args[i],args[i+1]);
            }

            VR_SERVER = argsMap.get("-server");
            String directory = argsMap.get("-screenshots_dir");
            parseLanguagesDirectory(directory);
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
        System.out.println("ended on " + new Date());
    }

    public  static void help(String progName){
        System.out.println("usage :");
        System.out.println(progName +" -server <http://visualReviewHost:port> -screenshots_dir <path to screenshots directory>");
    }

    public static void parseLanguagesDirectory(String directoryPath) throws IOException {
        System.out.println("Listing directory: "+directoryPath);
        HashMap<String,Integer> runBySuites = new HashMap<String,Integer>();
        File dir = new File(directoryPath);
        File[] languagesDirectoryList = dir.listFiles();
        for(File f : languagesDirectoryList){
            if (f.isDirectory()){
                System.out.println("found language: "+f.getName());
                parseScreenshotsDirectory(f,runBySuites);
            }
        }
    }

    public static int getRunID(String suiteName,HashMap<String,Integer> runBySuites) throws IOException {
        if (!runBySuites.containsKey(suiteName)){
            int runId = createRun("TestProject", suiteName);
            runBySuites.put(suiteName,runId);
        }
        return runBySuites.get(suiteName);
    }

    public static void parseScreenshotsDirectory(File languageDirectory,HashMap<String,Integer> runBySuites) throws IOException {
        //list directory
        File[] screenshotsDirectoryList = languageDirectory.listFiles();
        HashMap<String,File> imageList = new HashMap<String,File>();
        HashMap<String,File> jsonList = new HashMap<String,File>();
        for(File f : screenshotsDirectoryList){
            if (f.isFile() && f.getName().endsWith(".png")){
                imageList.put(f.getName(),f);
            }else  if (f.isFile() && f.getName().endsWith(".json")){
                jsonList.put(f.getName(),f);
            }else {
                System.out.println("found unused file :"+f.getName());
            }
        }
        //list all images
        for(File imageFile : imageList.values()){
            String filename  = imageFile.getName().substring(0, imageFile.getName().lastIndexOf("."));
            //extract deviceName
            String deviceName = filename.substring(0, filename.indexOf("-"));
            String screenshotName = filename.substring(filename.indexOf("-")+1);
            File jsonFile = jsonList.get(filename+".json");
            String metaString = "{}";
            String maskString = "{}";
            String suiteName = "default-" + languageDirectory.getName();
            String resolution = "XXXxXXX";
            String deviceVersion = "Unknown";
            if (jsonFile != null) {
                try {
                    JSONParser parser = new JSONParser();
                    JSONObject obj = (JSONObject) parser.parse(new FileReader(jsonFile));

                    JSONObject metaDict =/*(HashMap) */obj;
                    if (metaDict.containsKey("suiteName")) {
                        suiteName = metaDict.get("suiteName") + "-" + languageDirectory.getName();
                        metaDict.remove("suiteName");
                    }
                    if (metaDict.containsKey("resolution")) {
                        resolution = metaDict.get("resolution").toString();
                        metaDict.remove("resolution");
                    }
                    if (metaDict.containsKey("version")) {
                        deviceVersion = metaDict.get("version").toString();
                        metaDict.remove("version");
                    }
                    if (metaDict.containsKey("mask")) {
                        maskString = ((JSONObject)metaDict.get("mask")).toJSONString();
                        metaDict.remove("mask");
                    }
                    metaString = obj.toJSONString();

                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
            System.out.println("Sending "+screenshotName + " for suite "+suiteName);
            takeAndSendScreenshot(getRunID(suiteName,runBySuites),imageFile.getAbsolutePath(),screenshotName,deviceName,deviceVersion,resolution,metaString,maskString);
        }
    }

    /**
     * Convert input stream to string.
     * This method is very useful to convert the stream of the HTTP response into a string.
     *
     * @param inputStream the input stream
     * @return the string representing the input stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        //inputStream.close();
        return result;

    }

    public static String getAnalysis (int runID) throws IOException{
        System.out.println("-------------------------------");
        System.out.println("VisualReview Analysis: " + runID);
        System.out.println("-------------------------------");
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(VR_SERVER + "/api/runs/"+runID+"/analysis");

        CloseableHttpResponse response = httpclient.execute(httpGet);
        try {
            System.out.println("response from server when analyzing run: " + response.getStatusLine());
            HttpEntity responseEntity = response.getEntity();

            String result = convertInputStreamToString(responseEntity.getContent());
//			JsonFactory factory = new JsonFactory();
//			JsonParser parser = factory.createParser(response.getEntity().getContent());
//			if (parser.nextToken() != JsonToken.START_OBJECT) {
//				throw new IOException("Expected data to start with an Object");
//			}
//
//			while (parser.nextToken() != JsonToken.END_OBJECT) {
//				String fieldName = parser.getCurrentName();
//				JsonToken token = parser.nextToken(); // moves to value
//				System.out.println("--> fieldName: " + fieldName + "/value:" + token.asString());
//			}
            EntityUtils.consume(responseEntity);
            return result;
        } finally {
            response.close();
        }
    }

    /**
     * Creates a new run on the VisualReview server with the given project name
     * and suite name.
     *
     * @return the new run's RunID, which can be used to upload screenshots to
     * @throws IOException
     */
    public static int createRun(String projectName, String suiteName) throws IOException {
        System.out.println("-------------------------------");
        System.out.println("VisualReview Runs: " + projectName + "/" + suiteName);
        System.out.println("-------------------------------");
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(VR_SERVER+ "/api/runs");
        StringEntity input = new StringEntity("{\"projectName\":\"" + projectName + "\",\"suiteName\":\"" + suiteName + "\"}");
        input.setContentType("application/json");

        httpPost.setEntity(input);
        CloseableHttpResponse response = httpclient.execute(httpPost);

        try {
            System.out.println("response from server when creating run: " + response.getStatusLine());
            HttpEntity responseEntity = response.getEntity();

            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(response.getEntity().getContent());
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IOException("Expected data to start with an Object");
            }

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();
                parser.nextToken(); // moves to value
                if (fieldName != null && fieldName.equals("id")) {
                    return Integer.parseInt(parser.getValueAsString());
                }
            }
            EntityUtils.consume(responseEntity);
        } finally {
            response.close();
        }

        throw new RuntimeException("something went wrong while creating suite..");
    }

    public static void takeAndSendScreenshot(int runId, String screenshotPath, String screenshotName,
                                             String deviceName, String deviceVersion, String resolution, String meta,String mask) throws IOException {
//		System.out.println("-------------------------------");
//		System.out.println("VisualReview Screenshots: " + runId);
//		System.out.println("-------------------------------");

        byte[] screenshotData = extractBytes(screenshotPath);

        JsonFactory factory = new JsonFactory();
        StringWriter jsonString = new StringWriter();
        JsonGenerator generator = factory.createGenerator(jsonString);
        generator.writeStartObject();
        generator.writeStringField("platform", deviceName);
        generator.writeStringField("resolution", resolution);
        generator.writeStringField("version", deviceName+" "+deviceVersion);
        generator.writeEndObject();
        generator.flush();


        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(VR_SERVER+ "/api/runs/" + runId + "/screenshots");

        HttpEntity input = MultipartEntityBuilder.create()
                .addBinaryBody("file", screenshotData, ContentType.parse("image/png"), screenshotPath)
                .addTextBody("screenshotName", screenshotName, ContentType.TEXT_PLAIN)
                .addTextBody("properties", jsonString.toString(), ContentType.APPLICATION_JSON)
                .addTextBody("meta", meta, ContentType.APPLICATION_JSON)
                .addTextBody("mask", mask, ContentType.APPLICATION_JSON)
                .build();

        httpPost.setEntity(input);
        CloseableHttpResponse response = httpclient.execute(httpPost);

        try {
            System.out.println("response from server when uploading screenshot: " + response.getStatusLine());
            if (response.getStatusLine().getStatusCode() != 201){
                System.out.println("response error: "+EntityUtils.toString(response.getEntity()));
            }
        } finally {
            EntityUtils.consume(response.getEntity());
            response.close();
        }
    }

    private static byte[]  extractBytes (String pathName) throws IOException {
        BufferedImage originalImage = ImageIO.read(new File(pathName));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "png", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        return imageInByte;
    }
}

