package net.johanbasson.desktop.extract.extractors;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;


public class Extractors {

    private static final Logger log = LoggerFactory.getLogger(Extractors.class);

    public static List<ExtractorMapping> load() throws IOException, ParseException {
        List<ExtractorMapping> mappings = new ArrayList<>();
        InputStream in = Extractors.class.getClassLoader().getResourceAsStream("extractors.json");
        JSONParser parser = new JSONParser();
        JSONArray array = (JSONArray) parser.parse(new InputStreamReader(in));
        Iterator it = array.iterator();
        while (it.hasNext()) {
            JSONObject obj = (JSONObject) it.next();
            String className = (String) obj.get("class");
            try {
                Class clazz = Class.forName(className);
                FileExtractor extractor = (FileExtractor) clazz.newInstance();
                JSONArray types = (JSONArray) obj.get("types");
                Set<String> contentTypes = new HashSet<>();
                Iterator typesIt = types.iterator();
                while (typesIt.hasNext()) {
                    String type = (String) typesIt.next();
                    contentTypes.add(type.toLowerCase().trim());
                }
                mappings.add(new ExtractorMapping(extractor, contentTypes));
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                log.error("Class not found", e);
            }
        }
        return mappings;
    }
}
