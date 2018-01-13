package smartlife.monitorwearables;


import android.text.Editable;
import android.widget.EditText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import smartlife.monitorwearables.util.ValidatorUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidatorUtilsUnitTest {

    @Mock private EditText editText;

    @Test
    public void validate_shouldReturnTrueForValidPassword() {
        assertThat(ValidatorUtils.isValidPassword("123abckddi"), is(true));
    }


    @Test
    public void validate_shouldReturnFalseForShortPassword() {
        assertThat(ValidatorUtils.isValidPassword("123"), is(false));
    }

    @Test
    public void validate_shouldReturnFalseIfPasswordIsNull() {
        assertThat(ValidatorUtils.isValidPassword(null), is(false));
    }

    @Test
    public void validate_shouldReturnFalseIfPasswordIsEmpty() {
        assertThat(ValidatorUtils.isValidPassword(""), is(false));
    }

    @Test
    public void validate_shouldReturnTrueIfInputFiledIsEmpty() {
        final String value = null;
        Mockito.doCallRealMethod().when(editText).setText(value);
        Mockito.doCallRealMethod().when(editText).getText();
        editText.setText(value);
        assertThat(ValidatorUtils.isEmpty(editText), is(true));
    }
}