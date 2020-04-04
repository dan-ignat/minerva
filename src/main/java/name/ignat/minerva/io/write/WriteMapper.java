package name.ignat.minerva.io.write;

import java.util.function.Function;

public interface WriteMapper<T> extends Function<T, String[]>
{
}
