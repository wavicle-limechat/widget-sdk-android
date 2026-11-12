package android.webkit;

/** Test only: WebResourceError has a package-private constructor, so the fake lives in its package. */
public class FakeWebResourceError extends WebResourceError {
    private final int code;
    private final String description;

    public FakeWebResourceError(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public int getErrorCode() {
        return code;
    }

    @Override
    public CharSequence getDescription() {
        return description;
    }
}
