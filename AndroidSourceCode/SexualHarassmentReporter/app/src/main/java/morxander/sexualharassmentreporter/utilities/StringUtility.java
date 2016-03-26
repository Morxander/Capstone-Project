package morxander.sexualharassmentreporter.utilities;

/**
 * Created by morxander on 2/29/16.
 */
public class StringUtility {
    // Source : http://stackoverflow.com/a/7362810/917222
    public static String UppercaseFirstLetters(String str)
    {
        boolean prevWasWhiteSp = true;
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isLetter(chars[i])) {
                if (prevWasWhiteSp) {
                    chars[i] = Character.toUpperCase(chars[i]);
                }
                prevWasWhiteSp = false;
            } else {
                prevWasWhiteSp = Character.isWhitespace(chars[i]);
            }
        }
        return new String(chars);
    }
}
