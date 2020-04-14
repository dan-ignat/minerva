package name.ignat.minerva.util;

import static org.apache.commons.lang3.StringUtils.split;
import static org.springframework.test.util.ReflectionTestUtils.getField;

import org.springframework.lang.Nullable;

public final class TestUtils
{
    @Nullable
    public static Object getNestedField(Object targetObject, String dottedName)
    {
        Object currentObject = targetObject;

        for (String name : split(dottedName, '.'))
        {
            currentObject = getField(currentObject, name);
        }

        return currentObject;
    }

    private TestUtils() { }
}
