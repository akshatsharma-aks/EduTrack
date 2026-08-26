package com.edutrack.dto;

public class OptionResponse {

    private Long id;

    private String optionText;


    public OptionResponse(
            Long id,
            String optionText
    ) {

        this.id = id;
        this.optionText =
                optionText;
    }


    public Long getId() {
        return id;
    }

    public String getOptionText() {
        return optionText;
    }
}