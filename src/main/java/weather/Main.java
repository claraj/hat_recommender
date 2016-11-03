package weather;

import org.json.JSONException;
import org.json.JSONObject;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


/**
 * Created by clara on 11/3/16.
 *
 * Request current weather from Weather Underground
 *
 * Do something with the response.
 * As a simple example, if cooler than 40F, suggest woolly hat
 *
 * See Weather Underground documentation for requesting future conditions,
 * for example you may want to know if it will rain today.
 * https://www.wunderground.com/weather/api/d/docs
 *
 */
public class Main {

    public static void main(String[] args) {

        String key = readKey();

        if (key == null) {
            System.out.println("Fix key error. Exiting program");
            quit();
        }

        //Example URL for Minneapolis

        //http://api.wunderground.com/api/KEYGOESHERE/conditions/q/MN/Minneapolis.json

        String baseURL = "http://api.wunderground.com/api/%s/conditions/q/MN/Minneapolis.json";

        String url = String.format(baseURL, key);

        //Use AsyncHttpClient to make web request
        //docs at...
        //https://github.com/AsyncHttpClient/async-http-client

        //TODO handle errors - what if you have no internet connection?

        AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
        asyncHttpClient.prepareGet(url).execute(new AsyncCompletionHandler<Response>(){

            @Override
            public Response onCompleted(Response response) throws Exception {

                String responseBody = response.getResponseBody();
                System.out.println(responseBody);

                /*  The response is a string, formatted as JSON. https://en.wikipedia.org/wiki/JSON
                The JSON contains an attribute "current_observation" and this contains various items relating to the current conditions, temp, UV, precipitation, windchil...

                To deal with it, you've got three choices, in order of amount of new things you'll need to learn,

                 1. Parse the string, look for items you want, read the data. This is tricky and error prone though.
                 2. Use Java's JSON processing classes
                 3. Use GSON. You'll define a Java class that mirrors the structure of the JSON.
                 GSON will then turn the JSON response into a Java object, with variables matching the JSON attribute
                 names, so you can simply say myJSONobject.current_conditions.temp_f to get the temp.
                 Example here, http://www.javacreed.com/simple-gson-example/ and/or let me know if you need help getting started

                 Suggest approach 2 or 3. Let's try 2, using the org.json library
                 https://github.com/stleary/JSON-java


                */

                //TODO handle JSON processing errors
                //One error condition is that Weather Underground returns JSON but it's not structured as you expect
                //Try changing your key in key.txt and see what the response is.

                try {

                    //Convert the response String into a JSON object
                    JSONObject jsonObject = new JSONObject(responseBody);
                    //This JSONObject contains a JSON object with attribute current_observation
                    JSONObject currentObservation = jsonObject.getJSONObject("current_observation");
                    //You can think of JSON as hashmaps of hashmaps and lists
                    //currentobservations has an attribute "temp_f" and from reading the docs, the value of the temp_f attribute is a double
                    double currentTemp = currentObservation.getDouble("temp_f");

                    System.out.println("Current temp is " + currentTemp);

                    recommendHat(currentTemp);
                }
                catch (JSONException je) {

                    System.out.println("Error processing response, unable to make a recommendation");
                    return null;

                }

                return null;
            }
        });

    }

    private static void quit() {
        System.exit(0);
    }


    private static void recommendHat(double currentTemp) {

        if (currentTemp < 60) {
            System.out.println("Recommend woolly hat");
        } else {
            System.out.println("A hat is optional");
        }

        quit();

    }

    private static String readKey() {

        try {
            BufferedReader reader = new BufferedReader(new FileReader("key.txt"));
            String key = reader.readLine();
            if (key == null) {
                System.out.println("Key not found in file. Paste your Weather Underground key as the first line of key.txt");
                return null;
            }

            return key;

        } catch (IOException ioe) {
            System.out.println("Key file not found. Please provide a file called key.txt in the root directory of the project");
            System.exit(-1);
            return null;
        }
    }

}
