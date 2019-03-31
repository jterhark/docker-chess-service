package API;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.art.lach.mateusz.javaopenchess.core.Game;
import pl.art.lach.mateusz.javaopenchess.core.GameBuilder;
import pl.art.lach.mateusz.javaopenchess.core.ai.AIFactory;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.DataTransferFactory;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.TransferFormat;
import pl.art.lach.mateusz.javaopenchess.core.exceptions.ReadGameError;
import pl.art.lach.mateusz.javaopenchess.core.players.PlayerType;
import pl.art.lach.mateusz.javaopenchess.utils.GameModes;
import pl.art.lach.mateusz.javaopenchess.utils.GameTypes;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@Validated
@RequestMapping("/chess")
public class ChessController {

    private static HashMap<String, Session> Sessions = new HashMap<>();

    //Create a new game
    @RequestMapping(value = "/newgame", method = POST)
    public ResponseEntity<NewGameResponse> newGame(@Valid @RequestBody NewGameRequest req){
        String id = generateSessionId();
        Session session = new Session(req.level, req.color, null);
        Sessions.put(id, session);
        try {
            Game g = buildGame(session);
            if(req.color.equals("black")){ //if player is black, computer moves first
                g.doComputerMove();
            }

            //build response
            session.board = buildFenString(g);
            session.pgnBoard = buildPGNString(g);
            NewGameResponse res = new NewGameResponse(id, "ok", buildBoardStringArray(session), session.board, session.pgnBoard);
            return ResponseEntity.ok(res);

        }catch (ReadGameError rge){
            return ResponseEntity.status(500).body(new NewGameResponse(null,"cannot read new game!", null, null, null));
        }
    }

    //Endpoint for communication between containers
    @RequestMapping(value = "/movePgn", method = POST)
    public ResponseEntity<MoveResponse> move(@Valid @RequestBody MovePGNRequest req){
        try{
            Session session = Sessions.get(req.id);
            session.pgnBoard = req.pgn;
            Game g = buildGame(session);
            return getMoveResponseResponseEntity(session, g);
        }catch (ReadGameError rge){
            return ResponseEntity.badRequest().body(null);
        }
    }

    //generate response and test if winner
    private ResponseEntity<MoveResponse> getMoveResponseResponseEntity(Session session, Game g) {
        if(g.isIsEndOfGame()){
            return ResponseEntity.ok(new MoveResponse("You Win!", buildBoardStringArray(session), session.board, session.pgnBoard));
        }else{
        g.doComputerMove();
        session.board = buildFenString(g);
        session.pgnBoard = buildPGNString(g);
        if(g.isIsEndOfGame()){
            return ResponseEntity.ok(new MoveResponse("Computer Wins!", buildBoardStringArray(session), session.board, session.pgnBoard));
        }else{
            return ResponseEntity.ok(new MoveResponse("ok", buildBoardStringArray(session), session.board, session.pgnBoard));
        }
    }
    }

    //Human move endpoint. Sumbmit a move, test if winner, and do a computer move
    @RequestMapping(value = "/move", method = POST)
    public ResponseEntity<MoveResponse> move(@Valid @RequestBody MoveRequest req){
        int fromX = ((int) req.from.charAt(0)) - 97;
        int fromY = Character.getNumericValue(req.from.charAt(1)) - 1;
        int destX = ((int) req.to.charAt(0)) - 97;
        int destY = Character.getNumericValue(req.to.charAt(1))-1;
        try{
            Session session = Sessions.get(req.id);
            boolean moved = false;
            if(!session.pgnBoard.contains("1.")){
                session.pgnBoard += "1. " + req.from.charAt(0) + inverseMappings.get(req.from.charAt(1)) + "-" + req.to.charAt(0) + inverseMappings.get(req.to.charAt(1)) + " ";
                moved = true;
            }
            Game g = buildGame(session);
            if(!moved)
                g.move(fromX, fromY, destX, destY);
            session.board = buildFenString(g);
            session.pgnBoard = buildPGNString(g);
            return getMoveResponseResponseEntity(session, g);
        }catch (ReadGameError rge){
            return ResponseEntity.status(500).body(new MoveResponse("There was an error reading your game!", null, null, null));
        }

    }

    //load a game from a session or build a new game
    private static Game buildGame(Session session) throws ReadGameError {
        GameBuilder builder = new GameBuilder()
                .setGameType(GameTypes.LOCAL);
        boolean isNewGame = session.board == null || session.board.equals("");

        if(session.personColor.equals("white")){
            builder.setWhitePlayerName("Human")
                    .setWhitePlayerType(PlayerType.LOCAL_USER)
                    .setBlackPlayerName("Computer")
                    .setBlackPlayerType(PlayerType.COMPUTER);
        }else if(session.personColor.equals("black")){
            builder.setBlackPlayerName("Human")
                    .setBlackPlayerType(PlayerType.LOCAL_USER)
                    .setWhitePlayerName("Computer")
                    .setWhitePlayerType(PlayerType.COMPUTER);
        }

        if(isNewGame){
            builder.setGameMode(GameModes.NEW_GAME);
        }else{
            builder.setGameMode(GameModes.LOAD_GAME);
        }

        Game g = builder.build();
        g.setAi(AIFactory.getAI(session.level));

        if(!isNewGame){
            g.importGame(session.pgnBoard, DataTransferFactory.getImporterInstance(TransferFormat.PGN));
        }

        return g;
    }

    //export game to PGN string
    private static String buildPGNString(Game game){
        return game.exportGame(DataTransferFactory.getExporterInstance(TransferFormat.PGN));
    }

    //export game to FEN string (doesn't work)
    private static String buildFenString(Game game){
        return game.exportGame(DataTransferFactory.getExporterInstance(TransferFormat.FEN));
    }

    //use unique UUID as session id
    private String generateSessionId(){
        String s;
        do{
            s = java.util.UUID.randomUUID().toString();
        }while(Sessions.containsKey(s));
        return s;
    }

    //used for mapping the first move of a PGN format game
    private static HashMap<Character, Integer> inverseMappings = new HashMap<Character, Integer>(){{
        put('1',8);
        put('2',7);
        put('3',6);
        put('4',5);
        put('5',4);
        put('6',3);
        put('7',2);
        put('8',1);
    }};

    //unicode chess pieces, need to find a space the same width as these things for an empty square
//    private static HashMap<Character, String> unicodeMappings = new HashMap<Character, String>(){{
//        put('r', "\u265C");
//        put('n', "\u265E");
//        put('b', "\u265D");
//        put('q', "\u265B");
//        put('k', "\u265A");
//        put('p', "\u265F");
//        put('R', "\u2656");
//        put('N', "\u2658");
//        put('B', "\u2657");
//        put('Q', "\u2655");
//        put('K', "\u2654");
//        put('P', "\u2659");
//    }};

    //create a string array of the board for human readability
    private static String[] buildBoardStringArray(Session session){
        String fen = session.board;
        String[] lines=fen.split(" ");
        String[] board=lines[0].split("/");
        List<String> result = new ArrayList<>();
        result.add("  |a|b|c|d|e|f|g|h|");
        result.add("- ----------------");
        //StringBuilder builder = new StringBuilder("-----------------\n");
        StringBuilder builder = new StringBuilder();
        int row = 1;

        for(String s: board){
            builder.append(row);
            builder.append(' ');
            row++;
            for(char c: s.toCharArray()){
                if(Character.isDigit(c)){
                    for(int i = 0; i<Character.getNumericValue(c); ++i){
                        builder.append("| ");
                    }
                }else{
                    builder.append("|");
                    builder.append(c);
                }
            }
            builder.append('|');
            result.add(builder.toString());
            builder = new StringBuilder();
            result.add("- ----------------");
        }

        result.add("P - white");
        result.add("p - black");
        result.add("You are " + session.personColor);

        return result.toArray(new String[0]);
    }

}

//Session class
class Session{
    public int level;
    String personColor;
    String board;
    String pgnBoard;

    Session(int level, String personColor, String board){
        this.level = level;
        this.personColor = personColor;
        this.board = board;
    }
}

//class


