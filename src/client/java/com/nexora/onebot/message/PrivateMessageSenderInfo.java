package com.nexora.onebot.message;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

import com.google.gson.annotations.SerializedName;

public final class PrivateMessageSenderInfo {
    @SerializedName("user_id")
    private Long userId;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("sex")
    private Sex sex;

    @SerializedName("age")
    private Integer age;

    public OptionalInt getAge() {
        return age == null ? OptionalInt.empty() : OptionalInt.of(age);
    }
    
    public Optional<String> getNickname() {
        return Optional.ofNullable(nickname); 
    }
    
    public Sex getSex() {
        if (sex == null) {
            return Sex.UNKNOWN;
        }

        return sex;
    }

    public OptionalLong getUserId() {
        return userId == null ? OptionalLong.empty() : OptionalLong.of(userId);
    }
}
