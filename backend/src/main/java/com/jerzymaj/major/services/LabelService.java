package com.jerzymaj.major.services;

import com.jerzymaj.major.Dtos.CreateLabelDto;
import com.jerzymaj.major.Dtos.LabelDto;
import com.jerzymaj.major.exceptions.LabelNotFoundException;
import com.jerzymaj.major.models.Label;
import com.jerzymaj.major.repos.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;

    public List<Label> getAllLabels() {
        return labelRepository.findAll();
    }

    public Label getLabelById(Long labelId) {
        return labelRepository.findById(labelId)
                .orElseThrow(() -> new LabelNotFoundException("Label not found with id: " + labelId));
    }

    public Label createLabel(CreateLabelDto createLabelDto) {
        Label label = Label.builder()
                .name(createLabelDto.name())
                .color(createLabelDto.color())
                .build();
        return labelRepository.save(label);
    }

    public Label updateLabel(Long labelId, CreateLabelDto createLabelDto) {
        Label label = getLabelById(labelId);

        label.setName(createLabelDto.name() != null ? createLabelDto.name() : label.getName());
        label.setColor(createLabelDto.color() != null ? createLabelDto.color() : label.getColor());

        return labelRepository.save(label);
    }

    public void deleteLabelById(Long labelId) {
        Label label = getLabelById(labelId);
        labelRepository.delete(label);
    }
}
