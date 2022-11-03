package org.yildiz;

import org.yildiz.instabot.InstaBot;

import java.io.FileWriter;
import java.io.IOException;

public class Main {


    public static void main(String[] args) {
        System.setProperty("webdriver.chrome.driver", "C:\\instabot\\chromedriver.exe");
        final String USERNAME = "user_name";
        final String PASSWORD = "password";
        final int PIC_LIMIT = 100;
        InstaBot instabot = new InstaBot();
        writeToFile("instabot-result.txt", instabot.startBot(USERNAME, PASSWORD, PIC_LIMIT));
    }

    public static void writeToFile(String fileName, String content) {
        try {
            FileWriter myWriter = new FileWriter(fileName);
            myWriter.write(content);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}