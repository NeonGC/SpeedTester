package ru.neongc.tester;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

public class SpeedTester {

    private static final String DEST_FILE = "speedtest.exe";
    private static final String LOG_FILE = "log.txt";

    public static void main(String[] args) throws IOException {
        if (!new File(DEST_FILE).exists()) {
            String jarFileName = "SpeedTester-1.0.jar";
            if (!new File(jarFileName).exists()) jarFileName = "./target/"+jarFileName;

            JarResources jr = new JarResources(jarFileName);
            byte[] buff = jr.getResource("resources/speedtest.exe");
            Files.write(Paths.get(DEST_FILE), buff);
        }

        ProcessBuilder pb = new ProcessBuilder(DEST_FILE, "--format=json");
        Process p = pb.start();

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }

        String jsonString = sb.toString();

        if (jsonString.isEmpty()) {
            main(args);
            return;
        }

        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

        String date = new SimpleDateFormat("dd/MM/yyyy kk:mm").format(DatatypeConverter.parseDateTime(jsonObject.get("timestamp").getAsString()).getTime());
        int ping = jsonObject.getAsJsonObject("ping").get("latency").getAsInt();
        String download = String.format("%.2f", jsonObject.getAsJsonObject("download").get("bandwidth").getAsDouble()/125000);
        String upload = String.format("%.2f", jsonObject.getAsJsonObject("upload").get("bandwidth").getAsDouble()/125000);
        String serverName = jsonObject.getAsJsonObject("server").get("name").getAsString();
        String serverLocation = jsonObject.getAsJsonObject("server").get("location").getAsString();
        String resultUrl = jsonObject.getAsJsonObject("result").get("url").getAsString();

        StringBuilder data = new StringBuilder();
        int length = 20;
        data.append(addSpaces(length,"Date:")).append(date).append("\n");
        data.append(addSpaces(length,"Ping:")).append(ping).append("\n");
        data.append(addSpaces(length,"Download:")).append(download).append(" Mbps").append("\n");
        data.append(addSpaces(length,"Upload:")).append(upload).append(" Mbps").append("\n");
        data.append(addSpaces(length,"Server:")).append(serverName).append(", ").append(serverLocation).append("\n");
        data.append(addSpaces(length,"URL:")).append(resultUrl).append("\n");
        data.append(addSpaces(length,"Json:")).append(jsonString).append("\n");
        data.append("\n");
        new FileWriter(LOG_FILE, true).append(data).close();
        new File(DEST_FILE).deleteOnExit();
    }

    private static String addSpaces(int length, String str) {
        return String.format("%-" + length + "s", str);
    }
}
