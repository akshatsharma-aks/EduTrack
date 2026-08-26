package com.edutrack.dto;

public class OptionRequest {

    private String optionText;

    private Boolean correct;


    public OptionRequest() {
    }


    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(
            String optionText
    ) {
        this.optionText =
                optionText;
    }


    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(
            Boolean correct
    ) {
        this.correct = correct;
    }
}