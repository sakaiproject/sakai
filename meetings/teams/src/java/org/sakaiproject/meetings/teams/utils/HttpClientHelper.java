// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package org.sakaiproject.meetings.teams.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

class HttpClientHelper {

    private HttpClientHelper() {
    }

    static String getResponseStringFromConn(HttpURLConnection conn) throws IOException {

        BufferedReader reader;
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder stringBuilder= new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }

        return stringBuilder.toString();
    }

    static JSONObject processResponse(int responseCode, String response) throws JSONException {

        JSONObject responseJson = new JSONObject();
        responseJson.put("responseCode", responseCode);

        if (response.equalsIgnoreCase("")) {
            responseJson.put("responseMsg", "");
        } else {
            responseJson.put("responseMsg", new JSONObject(response));
        }
        return responseJson;
    }
}
