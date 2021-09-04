package net.johanbasson.desktop.exclusions;

import com.hrakaroo.glob.GlobPattern;
import com.hrakaroo.glob.MatchingEngine;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Exclusions {
    private static final Logger log = LoggerFactory.getLogger(Exclusions.class);
    private static final List<MatchingEngine> exclusions = new ArrayList<>();

    static {
        try {
            InputStream in = Exclusions.class.getClassLoader().getResourceAsStream("exclusions");
            IOUtils.readLines(new InputStreamReader(in))
                    .forEach(item -> {
                        log.info("Exlusion: {}", item);
                        exclusions.add(GlobPattern.compile(item));
                    });


        } catch (Exception ex) {
            log.error("Unable to load exclusions", ex);
        }
    }

    public static boolean isExcluded(String path) {
        boolean isExcluded = false;
        for (MatchingEngine me : exclusions) {
            if (me.matches(path)) {
                isExcluded = true;
                break;
            }
        }
        log.info("EXCLUDED: [{}] - {} ", isExcluded, path);
        return isExcluded;
    }

    public static boolean isExcluded(File file) {
        boolean isExcluded = false;
        for (MatchingEngine me : exclusions) {
            if (me.matches(file.getName())) {
                isExcluded = true;
                break;
            }
        }
        log.info("EXCLUDED: [{}] - {} ", isExcluded, file.getAbsolutePath());
        return isExcluded;
    }

    public static void main(String[] args) {

        MatchingEngine mc = GlobPattern.compile("**/target/**");
        System.out.println(mc.matches("/home/johan/Spikes/ad-connector/key-rebuild-tool/target/maven-status/maven-compiler-plugin/compile/default-compile/createdFiles.lst"));
    }
}
