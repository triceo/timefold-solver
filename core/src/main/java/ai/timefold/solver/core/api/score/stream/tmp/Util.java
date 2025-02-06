package ai.timefold.solver.core.api.score.stream.tmp;

final class Util {

    public static String convertCamelCaseToSentence(String camelCaseText) {
        if (camelCaseText == null || camelCaseText.isEmpty()) {
            return "";
        }
        var result = new StringBuilder();
        result.append(Character.toUpperCase(camelCaseText.charAt(0)));
        for (var i = 1; i < camelCaseText.length(); i++) {
            char currentChar = camelCaseText.charAt(i);
            if (Character.isUpperCase(currentChar)) {
                result.append(' ');
                result.append(Character.toLowerCase(currentChar));
            } else {
                result.append(currentChar);
            }
        }
        return result.toString();
    }

    private Util() {
        // No external instances.
    }

}