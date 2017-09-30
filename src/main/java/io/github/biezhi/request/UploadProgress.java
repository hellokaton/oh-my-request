package io.github.biezhi.request;

/**
 * Callback interface for reporting upload progress for a request.
 */
public interface UploadProgress {
    /**
     * Callback invoked as data is uploaded by the request.
     *
     * @param uploaded The number of bytes already uploaded
     * @param total    The total number of bytes that will be uploaded or -1 if
     *                 the length is unknown.
     */
    void onUpload(long uploaded, long total);

    UploadProgress DEFAULT = (uploaded, total) -> {
    };
}