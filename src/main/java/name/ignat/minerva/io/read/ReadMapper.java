package name.ignat.minerva.io.read;

import java.util.function.Function;

/**
 * @author Dan Ignat
 */
public interface ReadMapper<T> extends Function<String[], T>
{
}
