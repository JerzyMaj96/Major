package com.jerzymaj.major.services;

import com.jerzymaj.major.Dtos.CreateLabelDto;
import com.jerzymaj.major.Dtos.UpdateLabelDto;
import com.jerzymaj.major.exceptions.ExistingLabelNameException;
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
        if (labelRepository.existsByName(createLabelDto.name())) {
            throw new ExistingLabelNameException("Label with name " + createLabelDto.name() + " already exists");
        }

        Label label = Label.builder()
                .name(createLabelDto.name())
                .color(createLabelDto.color())
                .build();
        return labelRepository.save(label);
    }

    public Label updateLabel(Long labelId, UpdateLabelDto updateLabelDto) {
        Label label = getLabelById(labelId);

        if (updateLabelDto.name() != null &&
                !updateLabelDto.name().equals(label.getName()) &&
                labelRepository.existsByName(updateLabelDto.name())) {
            throw new ExistingLabelNameException("Label with name " + updateLabelDto.name() + " already exists");
        }

        label.setName(updateLabelDto.name() != null ? updateLabelDto.name() : label.getName());
        label.setColor(updateLabelDto.color() != null ? updateLabelDto.color() : label.getColor());

        return labelRepository.save(label);
    }

    public void deleteLabelById(Long labelId) {
        Label label = getLabelById(labelId);
        labelRepository.delete(label);
    }
}
