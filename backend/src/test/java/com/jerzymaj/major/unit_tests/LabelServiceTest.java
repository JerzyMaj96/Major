package com.jerzymaj.major.unit_tests;

import com.jerzymaj.major.Dtos.CreateLabelDto;
import com.jerzymaj.major.Dtos.UpdateLabelDto;
import com.jerzymaj.major.models.Label;
import com.jerzymaj.major.repos.LabelRepository;
import com.jerzymaj.major.services.LabelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class LabelServiceTest {

    @Mock
    private LabelRepository labelRepository;

    @InjectMocks
    private LabelService labelService;

    private Label label;

    @BeforeEach
    public void setUp() {
        label = Label.builder()
                .id(1L)
                .name("Test Label")
                .color("Blue")
                .build();

        lenient().when(labelRepository.save(any(Label.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void shouldCreateLabel_IfSuccess() {
        CreateLabelDto createLabelDto = new CreateLabelDto("Backend", "Red");

        when(labelRepository.existsByName(createLabelDto.name())).thenReturn(false);


        Label actualResult = labelService.createLabel(createLabelDto);

        assertThat(actualResult.getName()).isEqualTo(createLabelDto.name());
        assertThat(actualResult.getColor()).isEqualTo(createLabelDto.color());
        verify(labelRepository, times(1)).save(any(Label.class));
    }

    @Test
    public void shouldGetLabelById_IfSuccess() {
        when(labelRepository.findById(label.getId())).thenReturn(Optional.of(label));

        Label actualResult = labelService.getLabelById(label.getId());

        assertThat(actualResult.getName()).isEqualTo(label.getName());
        assertThat(actualResult.getColor()).isEqualTo(label.getColor());
    }

    @Test
    public void shouldGetAllLabels_IfSuccess() {
        when(labelRepository.findAll()).thenReturn(List.of(label));

        List<Label> actualResult = labelService.getAllLabels();

        assertThat(actualResult).isEqualTo(List.of(label));
        assertThat(actualResult).hasSize(1);
    }

    @Test
    public void shouldUpdateLabel_IfSuccess() {
        UpdateLabelDto updateLabelDto = new UpdateLabelDto("Backend", "Green");

        when(labelRepository.findById(label.getId())).thenReturn(Optional.of(label));

        Label actualResult = labelService.updateLabel(label.getId(), updateLabelDto);

        assertThat(actualResult.getName()).isEqualTo(updateLabelDto.name());
        assertThat(actualResult.getColor()).isEqualTo(updateLabelDto.color());
    }

    @Test
    public void shouldDeleteLabel_IfSuccess() {
        when(labelRepository.findById(label.getId())).thenReturn(Optional.of(label));

        labelService.deleteLabelById(label.getId());

        verify(labelRepository, times(1)).delete(label);
    }
}
