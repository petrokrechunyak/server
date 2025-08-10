package com.alphabetas.bot.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Iterator;


public final class ConnectionUtils {

    public static String getResponseMessage(HttpURLConnection connection) {

        StringBuilder content;
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonFactory factory = mapper.getJsonFactory();
            try
            {
                JsonParser parser = factory.createJsonParser(content.toString());
                JsonNode actualObj = mapper.readTree(parser);
                Iterator<JsonNode> iterator = actualObj.elements();
                while (iterator.hasNext()) {
                    System.out.println(iterator.next());
                }
            }
            catch(Exception e)
            {
                System.out.println("Error: "+e.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }

        return null;
    }

}
