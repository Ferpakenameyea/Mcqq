package com.nexora.onebot.api;

import java.util.Optional;

import com.google.gson.annotations.SerializedName;

public class ImageResponse {
    private String file;
    private String url;
    @SerializedName("file_size")
    private String fileSize;
    @SerializedName("file_name")
    private String fileName;
    private String base64;

    public Optional<String> getBase64() {
        return Optional.ofNullable(base64);
    }

    public Optional<String> getFile() {
        return Optional.ofNullable(file);
    }

    public Optional<String> getFileName() {
        return Optional.ofNullable(fileName);
    }
    
    public Optional<Integer> getFileSize() {
        return Optional.ofNullable(fileSize).map(Integer::parseInt);
    }

    public Optional<String> getUrl() {
        return Optional.ofNullable(url);
    }
}
