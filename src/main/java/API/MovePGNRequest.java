package API;

public class MovePGNRequest {
    public String id;
    public String pgn;

    public MovePGNRequest(){}

    public MovePGNRequest(String id, String pgn){
        this.id = id;
        this.pgn = pgn;
    }

}
