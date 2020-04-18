package name.ignat.minerva.io.write;

import java.util.function.Function;

/**
 * @author Dan Ignat
 */
public interface WriteMapper<T> extends Function<T, String[]>
{
}
