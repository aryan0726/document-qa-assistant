package com.aryan.documentqa;

import org.springframework.boot.SpringApplication;

public class TestDocumentQaAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.from(DocumentQaAssistantApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
