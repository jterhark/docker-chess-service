package API;

public class NewGameResponse{
    public  String id;
    public  String status;
    public  String[] board;
    public  String boardFen;
    public String boardPGN;

    public NewGameResponse(){}

    public NewGameResponse(String id, String status, String[] board, String boardFen, String boardPGN){
        this.id = id;
        this.status = status;
        this.board = board;
        this.boardFen = boardFen;
        this.boardPGN = boardPGN;
    }
}
