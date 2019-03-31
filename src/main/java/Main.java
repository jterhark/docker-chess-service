import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.art.lach.mateusz.javaopenchess.core.*;
import pl.art.lach.mateusz.javaopenchess.core.ai.AIFactory;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.DataExporter;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.DataTransferFactory;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.TransferFormat;
import pl.art.lach.mateusz.javaopenchess.core.players.PlayerType;
import pl.art.lach.mateusz.javaopenchess.utils.GameModes;
import pl.art.lach.mateusz.javaopenchess.utils.GameTypes;
import org.springframework.core.annotation.AliasFor;

public class Main {
    public static void main(String[] args) {
        Game g = new GameBuilder()
                .setBlackPlayerName("Computer")
                .setBlackPlayerType(PlayerType.COMPUTER)
                .setWhitePlayerName("Human")
                .setWhitePlayerType(PlayerType.LOCAL_USER)
                .setGameMode(GameModes.NEW_GAME)
                .setGameType(GameTypes.LOCAL).build();
        g.setAi(AIFactory.getAI(1));

        g.newGame();
        g.move(0, 1, 0, 2);
        g.move(1,1,1,2);
        DataExporter e = DataTransferFactory.getExporterInstance(TransferFormat.FEN);
        System.out.println(e.exportData(g));

    }

}
