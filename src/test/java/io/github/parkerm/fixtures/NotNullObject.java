package io.github.parkerm.fixtures;

import javax.validation.constraints.NotNull;

public class NotNullObject {

    @NotNull
    private String str;

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }
}
