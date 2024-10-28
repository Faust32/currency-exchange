package model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "code", "name", "sign"})
public class Currency {
    private int ID;
    private final String code;
    private final String name;
    private final String sign;

    public Currency(int ID, String code, String fullName, String sign) {
        this.ID = ID;
        this.code = code;
        this.name = fullName;
        this.sign = sign;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSign() {
        return sign;
    }

}

