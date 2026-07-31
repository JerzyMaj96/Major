package com.jerzymaj.major.mappers;

import com.jerzymaj.major.Dtos.LabelDto;
import com.jerzymaj.major.models.Label;

public final class LabelMapper {

    private LabelMapper() {}

    public static LabelDto toDto(Label label) {
        return new LabelDto(
                label.getId(),
                label.getName(),
                label.getColor()
        );
    }
}
