package name.ignat.minerva.model.source;

import javax.annotation.concurrent.Immutable;

import lombok.Value;
import lombok.experimental.NonFinal;

/**
 * @author Dan Ignat
 */
@Immutable
@Value @NonFinal
public abstract class FileSource
{
    private String path;
}
