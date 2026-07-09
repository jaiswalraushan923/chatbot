package com.example.chatbot.ai;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
//import com.google.cloud.vertexai.generativeai.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;


//@Component
//public class GeminiClient {
//
//    @Value("${vertex.project-id}")
//    private String projectId;
//
//    @Value("${vertex.location}")
//    private String location;
//
//    @Value("${vertex.model}")
//    private String modelName;
//
//    @Value("${vertex.credentials-path}")
//    private String credentialsPath;
//
//    public String generate(String prompt) {
//
//        try {
//
//            GoogleCredentials credentials =
//                    GoogleCredentials.fromStream(
//                            new FileInputStream(credentialsPath)
//                    );
//
//            VertexAI vertexAI =
//                    new VertexAI(projectId, location, credentials);
//
//            GenerativeModel model =
//                    new GenerativeModel(modelName, vertexAI);
//
//            GenerateContentResponse response =
//                    model.generateContent(prompt);
//
//            return response.getCandidates(0)
//                    .getContent()
//                    .getParts(0)
//                    .getText();
//
//        } catch (IOException e) {
//            throw new RuntimeException("Error calling Gemini", e);
//        }
//    }
//}

@Component
public class GeminiClient {

    @Value("${vertex.project-id}")
    private String projectId;

    @Value("${vertex.location}")
    private String location;

    @Value("${vertex.model}")
    private String modelName;


    public String generate(String prompt) {

        try (VertexAI vertexAI = new VertexAI(projectId, location)) {

            GenerativeModel model = new GenerativeModel(modelName, vertexAI);

            GenerateContentResponse response = model.generateContent(prompt);

            return response.getCandidates(0)
                    .getContent()
                    .getParts(0)
                    .getText();

        } catch (IOException e) {
            throw new RuntimeException("Error calling Gemini", e);
        }
    }
}
