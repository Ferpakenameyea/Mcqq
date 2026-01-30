package com.nexora.onebot.message;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;

import com.google.gson.annotations.SerializedName;

public final class GroupMessageSenderInfo {
    @SerializedName("user_id")
    private Long userId;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("sex")
    private Sex sex;

    @SerializedName("age")
    private Integer age;

    @SerializedName("card")
    private String card;

    @SerializedName("area")
    private String area;

    @SerializedName("level")
    private String level;

    @SerializedName("role")
    private Role role;

    @SerializedName("title")
    private String title;

    public Optional<String> getArea() {
        return Optional.ofNullable(area);
    }

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public Optional<Role> getRole() {
        return Optional.ofNullable(role);
    }

    public Optional<String> getLevel() {
        return Optional.ofNullable(level);
    }

    public Optional<String> getCard() {
        return Optional.ofNullable(card);
    }

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

    public enum Role {
        @SerializedName("owner")
        OWNER,
        @SerializedName("admin")
        ADMIN,
        @SerializedName("member")
        MEMBER
    }
}
