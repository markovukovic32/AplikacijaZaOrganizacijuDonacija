package util;

import java.util.regex.Pattern;

public class ValidatorUnosa {
    private static final String slovaPattern = "[A-PR-VZa-pr-vzšđžćčŠĐŽĆČ]*$";
    public static boolean validatorUnosa(String tekst) {
        if(Pattern.compile(slovaPattern).matcher(tekst).matches()){
            return true;
        }
        return false;
    }
}
