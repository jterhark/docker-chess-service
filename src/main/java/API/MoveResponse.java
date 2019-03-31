package API;

public class MoveResponse{
    public String status;
    public String[] board;
    public String boardFen;
    public String boardPGN;


    public MoveResponse(){}

    public MoveResponse(String status, String[] board, String boardFen, String boardPGN){
        this.status = status;
        this.board = board;
        this.boardFen = boardFen;
        this.boardPGN = boardPGN;
    }
}
