package com.studymaan.modules.study.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class StudyForm {

    public static final String VALID_PATH_PATTERN = "^[ㄱ-ㅎ가-힣a-z0-9_-]{2,20}$";

    @NotBlank
    @Length(min = 2, max = 50)
    @Pattern(regexp = VALID_PATH_PATTERN)
    private String path;

    @NotBlank
    @Length(max = 50)
    private String title;

    @NotBlank
    @Length(max = 100)
    private String shortDescription;

    @NotBlank
    private String fullDescription;
}
