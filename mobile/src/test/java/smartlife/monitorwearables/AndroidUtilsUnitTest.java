package smartlife.monitorwearables;


import android.widget.EditText;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.TimeZone;

import smartlife.monitorwearables.util.AndroidUtils;
import smartlife.monitorwearables.util.ValidatorUtils;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class AndroidUtilsUnitTest {

    @Test
    public void validate_shouldReturnLongForMongoDate() {
        String mongoDate = "2018-01-08T10:39:40.000Z";
        assertTrue((Long)AndroidUtils.parseMongoDateToLocal(mongoDate) instanceof Long);
    }

    @Test
    public void validate_shouldReturnCorrectMongoDateInMilliseconds() {
        String mongoDate = "2018-01-08T10:39:40.000Z";
        //add offset to match the local timezone
        int offsetInMilliseconds = TimeZone.getDefault().getOffset(new Date().getTime());
        //1515404380000 are milliseconds for the given mongo date
        long mongoDateInMilliseconds = Math.addExact(offsetInMilliseconds, Long.parseLong("1515404380000"));
        assertThat(AndroidUtils.parseMongoDateToLocal(mongoDate), is(mongoDateInMilliseconds));
    }

}