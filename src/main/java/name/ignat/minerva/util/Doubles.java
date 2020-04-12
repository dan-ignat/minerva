package name.ignat.minerva.util;

public final class Doubles
{
    public static String toMinimalString(double d)
    {
        int i = (int) d;

        return d == i ? String.valueOf(i) : String.valueOf(d);
    }

    private Doubles() { }
}
