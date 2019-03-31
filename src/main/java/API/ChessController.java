package API;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
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
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@Validated
@RequestMapping("/chess")
public class ChessController {

    private static HashMap<String, Session> Sessions = new HashMap<>();

    @RequestMapping(value = "/newgame", method = POST)
    public ResponseEntity<NewGameResponse> newGame(@Valid @RequestBody NewGameRequest req){
        String id = generateSessionId();
        Session session = new Session(req.level, req.color, null);
        Sessions.put(id, session);
        try {
            Game g = buildGame(session);
            if(req.color.equals("black")){
                g.doComputerMove();
            }
            session.board = buildFenString(g);
            NewGameResponse res = new NewGameResponse(id, "ok", buildBoardStringArray(session), session.board);
            return ResponseEntity.ok(res);

        }catch (ReadGameError rge){
            return ResponseEntity.status(500).body(new NewGameResponse(null,"cannot read new game!", null, null));
        }
    }

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
            g.importGame(session.board, DataTransferFactory.getImporterInstance(TransferFormat.FEN));
        }

        return g;
    }

    private static String buildFenString(Game game){
        return DataTransferFactory
                .getExporterInstance(TransferFormat.FEN)
                .exportData(game);
    }


    private String generateSessionId(){
        String s;
        do{
            s = java.util.UUID.randomUUID().toString();
        }while(Sessions.containsKey(s));
        return s;
    }

    private static HashMap<Character, String> unicodeMappings = new HashMap<Character, String>(){{
        put('r', "\u265C");
        put('n', "\u265E");
        put('b', "\u265D");
        put('q', "\u265B");
        put('k', "\u265A");
        put('p', "\u265F");
        put('R', "\u2656");
        put('N', "\u2658");
        put('B', "\u2657");
        put('Q', "\u2655");
        put('K', "\u2654");
        put('P', "\u2659");
    }};

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
//            builder.append("|\n-----------------\n");
            result.add("- ----------------");
        }

        result.add("P - white");
        result.add("p - black");
        result.add("You are " + session.personColor);

        return result.toArray(new String[0]);
    }

}

class Session{
    public int level;
    public String personColor;
    public String board;

    public Session(int level, String personColor, String board){
        this.level = level;
        this.personColor = personColor;
        this.board = board;
    }
}

class NewGameRequest{

    @Pattern(regexp = "(^black$)|(^white$)", message = "color must be black or white")
    public String color;

    @Min(1)
    @Max(3)
    public int level;
}

class NewGameResponse{
    public final String id;
    public final String status;
    public final String[] board;
    public final String boardFen;

    public NewGameResponse(String id, String status, String[] board, String boardFen){
        this.id = id;
        this.status = status;
        this.board = board;
        this.boardFen = boardFen;
    }
}

class MoveRequest{

}

class MoveResponse{

}

class QuitRequest{

}

class QuitResponse{

}



//class


