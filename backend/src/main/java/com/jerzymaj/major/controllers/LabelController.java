package com.jerzymaj.major.controllers;

import com.jerzymaj.major.Dtos.CreateLabelDto;
import com.jerzymaj.major.Dtos.LabelDto;
import com.jerzymaj.major.Dtos.UpdateLabelDto;
import com.jerzymaj.major.configuration.ApiRoutes;
import com.jerzymaj.major.mappers.LabelMapper;
import com.jerzymaj.major.services.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(ApiRoutes.BASE_API + "/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping
    public ResponseEntity<LabelDto> createLabel(@Valid @RequestBody CreateLabelDto createLabelDto) {

        LabelDto labelDto = LabelMapper.toDto(labelService.createLabel(createLabelDto));

        return ResponseEntity.created(URI.create(ApiRoutes.BASE_API + "/labels/" + labelDto.id())).body(labelDto);
    }

    @GetMapping
    public ResponseEntity<List<LabelDto>> retrieveAllLabels() {

        List<LabelDto> labelDtoList = labelService.getAllLabels().stream()
                .map(LabelMapper::toDto)
                .toList();

        return ResponseEntity.ok(labelDtoList);
    }

    @GetMapping("/{labelId}")
    public ResponseEntity<LabelDto> retrieveLabelById(@PathVariable("labelId") Long labelId) {

        LabelDto labelDto = LabelMapper.toDto(labelService.getLabelById(labelId));

        return ResponseEntity.ok(labelDto);
    }

    @PatchMapping("/{labelId}")
    public ResponseEntity<LabelDto> updateLabel(@PathVariable("labelId") Long labelId,
                                                @Valid @RequestBody UpdateLabelDto updateLabelDto) {

        LabelDto labelDto = LabelMapper.toDto(labelService.updateLabel(labelId, updateLabelDto));

        return ResponseEntity.ok(labelDto);
    }

    @DeleteMapping("/{labelId}")
    public ResponseEntity<Void> deleteLabel(@PathVariable("labelId") Long labelId) {

        labelService.deleteLabelById(labelId);

        return ResponseEntity.noContent().build();
    }
}
