import pl.art.lach.mateusz.javaopenchess.core.*;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.DataTransferFactory;
import pl.art.lach.mateusz.javaopenchess.core.data_transfer.TransferFormat;
import pl.art.lach.mateusz.javaopenchess.core.exceptions.ReadGameError;


//scratches
public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        try {
            game.importGame("rnbqkbnr/p1ppppp1/1p6/7p/P1P5/8/1P1PPPPP/RNBQKBNR w KQkq h6 0 3", DataTransferFactory.getImporterInstance(TransferFormat.FEN));
        }catch (ReadGameError re){
            re.printStackTrace();
        }
        System.out.println(game.isIsEndOfGame());


    }

}
