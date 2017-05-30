package org.arquillian.smart.testing.surefire.provider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.String.format;

public class InfoPrinter {

    int length;
    private List<String> strategies;
    private boolean isSelecting;
    private String title = "Smart testing";
    private String surroundingCharacter = "=";

    public InfoPrinter(List<String> strategies, boolean isSelecting) {
        this.strategies = strategies;
        this.isSelecting = isSelecting;
    }

    public void print() {
        String strategiesLine = format("Applied strategies: %s", strategies);
        String usageLine = format("Applied usage: [%s]", isSelecting ? "selecting" : "ordering");
        length = getLongestSize(title, strategiesLine, usageLine);
        System.out.println(getStringToPrint(strategiesLine, usageLine));
    }

    private int getLongestSize(String... lines) {
        return Arrays.stream(lines).mapToInt(line -> line.length()).max().getAsInt();
    }

    private String getStringToPrint(String... lines) {
        StringBuffer sb = new StringBuffer();
        addAndFillLine(sb, title, surroundingCharacter);
        addAndFillLine(sb, "", " ");
        Arrays.stream(lines).forEach(line -> addAndFillLine(sb, line, " "));
        addAndFillLine(sb, "", " ");
        fillWith(sb, length + 10, surroundingCharacter);
        return sb.toString();
    }

    private void addAndFillLine(StringBuffer sb, String content, String charToFill) {
        addThreeSurroundingChars(sb);
        int missingChars = length - content.length();
        int missingLeading = missingChars / 2;
        fillWith(sb, missingLeading, charToFill);
        sb.append("  ").append(content).append("  ");
        fillWith(sb, missingChars - missingLeading, charToFill);
        addThreeSurroundingChars(sb);
        sb.append("\n");
    }

    private void addThreeSurroundingChars(StringBuffer sb) {
        fillWith(sb, 3, surroundingCharacter);
    }

    private void fillWith(StringBuffer sb, int count, String character) {
        IntStream.range(0, count).forEach(i -> sb.append(character));
    }
}
