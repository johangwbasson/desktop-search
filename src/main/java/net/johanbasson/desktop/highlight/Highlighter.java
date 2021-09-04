package net.johanbasson.desktop.highlight;

import java.util.ArrayList;
import java.util.List;

public class Highlighter {

    private static final int EXTRACT_CHARS = 20;

    public static List<String> highlight(String content, String keyword, int max) {
        String updated = content.replaceAll(keyword, "<b>" + keyword +"</b>");
        int no = 0;
        int pos = 0;
        int startPos = 0;
        int lastPos = 0;
        int endPos = 0;
        List<String> frags = new ArrayList<>();
        boolean con = true;
        do {
            pos = updated.indexOf("<b>", pos);
            endPos = updated.indexOf("</b>", pos);

            if (pos != -1 && endPos != -1) {
                if (pos - EXTRACT_CHARS < 0) {
                    startPos = 0;
                } else {
                    startPos = pos - EXTRACT_CHARS;
                }

                if (endPos + EXTRACT_CHARS > updated.length()) {
                    lastPos = updated.length();
                } else {
                    lastPos = endPos + EXTRACT_CHARS;
                }

                frags.add(updated.substring(startPos, lastPos));
            }

            con = frags.size() < max && pos != -1;

            pos = pos + 1;
        } while (con);
        return frags;
    }



    public static void main(String[] args) {

        String content = "@Component\n" +
                "public class ConnectorKeyPanel extends JPanel2 {\n" +
                "\n" +
                "\tprivate JLabel extractKeysLabel = new JLabel(\"→ Extracting Local Connector keys.\", SwingConstants.LEFT);\n" +
                "\tprivate JLabel matchKeysLabel = new JLabel(\"→ Matching Local Connector keys with desired Connectors.\", SwingConstants.LEFT);\n" +
                "\tprivate JLabel generateCloudKeyLabel = new JLabel(\"→ Generating new Cloud keys for Local Connector.\", SwingConstants.LEFT);\n" +
                "\tprivate JLabel writeFileLabel = new JLabel(\"→ Writing hierarchy to file.\", SwingConstants.LEFT);\n" +
                "\n" +
                "\tpublic ConnectorKeyPanel() {\n" +
                "\n" +
                "\t\tBoxLayout baseBoxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);\n" +
                "\t\tthis.setLayout(baseBoxLayout);\n" +
                "\t\tJPanel2 stepPanel = new JPanel2();\n" +
                "\t\tGridLayout layoutManager = new GridLayout(4, 1);\n" +
                "\t\tlayoutManager.setHgap(0);\n" +
                "\t\tlayoutManager.setVgap(10);\n" +
                "\t\tstepPanel.setLayout(layoutManager);\n" +
                "\t\tstepPanel.setBorder(new EmptyBorder(TOP_BORDER, LEFT_BORDER, INTERNAL_BORDER, RIGHT_BORDER));\n" +
                "\n" +
                "\t\tstepPanel.add(this.extractKeysLabel);\n" +
                "\t\tstepPanel.add(this.matchKeysLabel);\n" +
                "\t\tstepPanel.add(this.generateCloudKeyLabel);\n" +
                "\t\tstepPanel.add(this.writeFileLabel);\n" +
                "\t\tthis.add(stepPanel);\n" +
                "\t}\n" +
                "\n" +
                "\t@EventListener\n" +
                "\tpublic void setStep(StepStartedEvent step) {\n" +
                "\t\tSwingUtilities.invokeLater(() -> setActive(labelForStep(step.getStep())));\n" +
                "\t}\n" +
                "\n" +
                "\t@EventListener\n" +
                "\tpublic void stepSuccess(StepSucceededEvent step) {\n" +
                "\t\tSwingUtilities.invokeLater(() -> setSuccess(labelForStep(step.getStep())));\n" +
                "\t}\n" +
                "\n" +
                "\t@EventListener\n" +
                "\tpublic void stepFailed(StepFailedEvent step) {\n" +
                "\t\tSwingUtilities.invokeLater(() -> setFailed(labelForStep(step.getStep())));\n" +
                "\t}\n" +
                "\n" +
                "\tprivate JLabel labelForStep(Step step) {\n" +
                "\t\tswitch (step) {\n" +
                "\t\t\tcase EXTRACT_KEYS:\n" +
                "\t\t\t\treturn extractKeysLabel;\n" +
                "\t\t\tcase MATCH_KEYS:\n" +
                "\t\t\t\treturn matchKeysLabel;\n" +
                "\t\t\tcase GEN_CLOUD_KEYS:\n" +
                "\t\t\t\treturn generateCloudKeyLabel;\n" +
                "\t\t\tcase WRITE_FILE:\n" +
                "\t\t\t\treturn writeFileLabel;\n" +
                "\t\t}\n" +
                "\t\tthrow new RuntimeException(\"Unrecognised step!\");\n" +
                "\t}\n" +
                "\n" +
                "\tpublic enum Step {\n" +
                "\t\tEXTRACT_KEYS,\n" +
                "\t\tMATCH_KEYS,\n" +
                "\t\tGEN_CLOUD_KEYS,\n" +
                "\t\tWRITE_FILE\n" +
                "\t}\n" +
                "\n" +
                "}";


        highlight(content, "Label", 10).forEach(item -> System.out.println(item));


    }
}
