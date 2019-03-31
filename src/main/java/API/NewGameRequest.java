package API;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

public class NewGameRequest{

    @Pattern(regexp = "(^black$)|(^white$)", message = "color must be black or white")
    public String color;

    @Min(1)
    @Max(3)
    public int level;

    public NewGameRequest(){}

    public NewGameRequest(String color, int level){
        this.color = color;
        this.level=level;
    }
}
