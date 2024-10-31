package model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"id", "code", "name", "sign"})
public class Currency {
    @Setter
    private int ID;
    private final String code;
    private final String name;
    private final String sign;
}

