package smartlife.monitorwearables.util;


import android.widget.EditText;

public class ValidatorUtils {


    public static boolean isValidPassword(String pass) {
        if (pass != null && pass.length() >= 8) {
            return true;
        }
        return false;
    }


    public static boolean isValidEmail(String email) {
        boolean isValid = false;
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean isEmpty(EditText editText) {
        String input = editText.getText().toString().trim();
        return input.length() == 0;
    }

}
