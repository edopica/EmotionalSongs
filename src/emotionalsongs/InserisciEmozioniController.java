/*Progetto di 
 * Edoardo Picazio 748815 VA
 * Federico Ligas 749063 VA
 */
package emotionalsongs;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.util.Callback;

/**
 * Classe che permette all' utente di inserire le emozioni provate , votarle 
 * e eventualmente inserire un commento per un' emozione inserita
 * @author Federico Ligas
 * @author Edoardo Picazio
 *
 */
public class InserisciEmozioniController extends Controller implements Initializable {
	
    @FXML private TabPane tabpane;
	
	@FXML private Tab firstTab,secondTab ; 
	/**
	 * </>tabellaEmozioni</> : tabella che contiene le emozioni , i voti e un link di testo ai commenti
	 */
	@FXML private TableView<ArrayList<StringProperty>> tabellaEmozioni ; 
	
	@FXML private Button backToSongs , backToEmotions , saveEmotions , saveComment ; 
	/**
	 * </>areaCommento</> : area di Testo che permette di inserire il commento
	 */
	@FXML private TextArea areaCommento ; 
	
	@FXML private Label caratteriRimasti ; 
	/**
	 * </>idCanzone</> : identificatore della canzone a cui l'utente vuole associare delle emozioni
	 */
	private int idCanzone ; 
	/**
	 * </>commentoVisibile</> : lista di booleani che indica se il commento può essere inserito
	 * <p> i valori contenuti rappresentano le righe della tabella e assumono true se l'emozione è stata votata altrimenti false 
	 */
	private ArrayList<SimpleBooleanProperty> commentoVisibile ;
	/**
	 * </>listaCommenti</> : lista dei commenti inserite
	 */
	private String[] listaCommenti ;
	/**
	 * </>playlist</> : playlist a cui appartiene la canzone da valutare 
	 */
	private Playlist playlist ; 
	/**
	 * </>listaVoti</> : lista dei voti inseriti 
	 */
	private int[] listaVoti ;
	
	private int indiceCommentoCorrente ;
	/**
	 * </>utente</> : utente che sta inserendo l'emozione 
	 */
	private Login utente; 

	
	/**
	 * costruttore di base 
	 */
	public InserisciEmozioniController(Login utente, int idCanzone , Playlist playlist ) {
		
		listaVoti  = new int[9] ;
		listaCommenti = new String[9];
		// inizializziamo con i valori di default 
		for(int i=0;i<9;i++) {
			listaCommenti[i]  = "";
			listaVoti[i] = 0 ;
		}
		commentoVisibile = new ArrayList<SimpleBooleanProperty>();
		
		this.playlist = playlist;
		this.idCanzone = idCanzone;
		this.utente = utente; 
		
	}
	

    /**
	 * Viene chiamato (una e una sola volta) dal controller appena dopo la scena è stata "caricata" con successo 
     * e inizializza gli elementi che sono contenuti nella scena .
     * @param arg0 : Il path usato per risolvere i path relativi per l'oggetto "radice" o il valore null se il path non &egrave noto
     * @param arg1 : Le risorse usate per localizzare l'oggetto "radice" , o null se la radice non viene trovata 
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		backToSongs.setOnAction(
				event -> switchTo(event,"SelezionaCanzone.fxml",new SelezionaCanzoneController(utente,playlist,null) )); 
		
		backToEmotions.setOnAction(
				event -> { 
					SingleSelectionModel<Tab> selectionModel  = tabpane.getSelectionModel();
				    selectionModel.select(0);
				    firstTab.setDisable(false);
				    secondTab.setDisable(true);
						
				});
		
		saveEmotions.setOnAction(
				event -> {
					try {
						salvaEmozioni(event);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} );
		
		saveComment.setOnAction(
				event -> salvaCommento(event) );
		
		createTable();
		
		tabellaEmozioni.setItems(getData());
		
		areaCommento.setTextFormatter(new TextFormatter<String>(change -> change.getControlNewText().length() <= 256 ? change : null));
		
		caratteriRimasti.textProperty().bind(new SimpleIntegerProperty(256).subtract(Bindings.length(areaCommento.textProperty())).asString());
		
	}
	
	private void createTable() {
		
		TableColumn<ArrayList<StringProperty>, String>  nameColumn = new TableColumn<>("Emozione");
		nameColumn.setCellValueFactory(cellData -> cellData.getValue().get(0));
		tabellaEmozioni.getColumns().add(nameColumn);
		
		TableColumn<ArrayList<StringProperty>, String> descriptionColumn = new TableColumn<>("Descrizione");
		descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().get(1));
		tabellaEmozioni.getColumns().add(descriptionColumn);
		
		addButtonsToTable();
		addHyperlinkToTable();
		
	}
	
	private ObservableList<ArrayList<StringProperty>> getData(){
		
		ObservableList<ArrayList<StringProperty>> datiTabella = FXCollections.observableArrayList() ; 
		for (int i = 0; i < 9 ; i++) {
	        ArrayList<StringProperty> row = new ArrayList<StringProperty>();
            
	        row.add(new SimpleStringProperty(Emozioni.getlistaEmozioni()[i].getNome()));
	        row.add(new SimpleStringProperty(Emozioni.getlistaEmozioni()[i].getDescrizione()));

	        datiTabella.add(row);
	    }
		
		return datiTabella ;    
		
	}
	/**
	 * aggiunge un link di testo alla tabella 
	 */
	private void addHyperlinkToTable() {
	    // usiamo la wrapper class di void per inizializzare colonna 
		
	    TableColumn<ArrayList<StringProperty>, Void> commentColumn = new TableColumn<ArrayList<StringProperty>, Void>("");

	    Callback<TableColumn<ArrayList<StringProperty>, Void>, TableCell<ArrayList<StringProperty>, Void>> cellFactory = new Callback<TableColumn<ArrayList<StringProperty>, Void>, TableCell<ArrayList<StringProperty>, Void>>() {

	        @Override
	        public TableCell<ArrayList<StringProperty>, Void> call(final TableColumn<ArrayList<StringProperty>, Void> param) {

	            final TableCell<ArrayList<StringProperty>, Void> cell = new TableCell<ArrayList<StringProperty>, Void>(){

	                private final Hyperlink linkToComment= new Hyperlink("Inserisci commento");
	                
	                {
	                /* all' interno di questa funzione lambda metteremo un metodo 
	                * crea un dialog che mostra le statistiche della canzone */ 
	                	linkToComment.setOnAction( event -> {
	                		
	                    	int indice  = getIndex();
							onHyperLinkCliked(event,indice);
							
	                    	// serve in modo che un hyperlink clickato non rimanga sottolineato
							linkToComment.setVisited(false);
	                    });
	                	
	                
	                 }
	                
	                /* update item è un metodo che viene chiamato per ogni cella dopo
	                 *  che è stato determinato il tipo da inserire e colloca l'elemento 
	                    che la cella deve contenere  */
	                @Override
	                public void updateItem(Void item, boolean empty) {
	                    super.updateItem(item, empty);
	                    if (empty) {
	                        setGraphic(null);
	                    } else {
	                    	TableRow<ArrayList<StringProperty>> row = getTableRow();
		                	
		                	if(row != null && !(row.isEmpty())) {
		                	  commentoVisibile.add(new SimpleBooleanProperty(true));
		                	  linkToComment.disableProperty().bind(commentoVisibile.get(getIndex()));
		                	  setGraphic(linkToComment);
		                	}
	                        
	                    }
	                }};

	            return cell;
	        }
	    };

	    // crea una CellValueFactory che contiene un hyperlink , per ogni riga della tabella 
	    commentColumn.setCellFactory(cellFactory);
	    commentColumn.setMinWidth(150);
	    tabellaEmozioni.getColumns().add(commentColumn);
		
	}
	/**
	 * aggiunge i bottoni per votare l'emozione  alla tabella 
	 */
	private void addButtonsToTable() {
	    // usiamo la wrapper class di void per inizializzare colonna 
		
	    TableColumn<ArrayList<StringProperty>, Void> buttonsColumn = new TableColumn<ArrayList<StringProperty>, Void>("voto");

	    Callback<TableColumn<ArrayList<StringProperty>, Void>, TableCell<ArrayList<StringProperty>, Void>> cellFactory = new Callback<TableColumn<ArrayList<StringProperty>, Void>, TableCell<ArrayList<StringProperty>, Void>>() {

	        @Override
	        public TableCell<ArrayList<StringProperty>, Void> call(final TableColumn<ArrayList<StringProperty>, Void> param) {

	            final TableCell<ArrayList<StringProperty>, Void> cell = new TableCell<ArrayList<StringProperty>, Void>(){

	            	private final Button firstButton = new Button("1");
	            	private final Button secondButton = new Button("2");
	            	private final Button thirdButton = new Button("3");
	            	private final Button forthButton = new Button("4");
	            	private final Button fifthButton = new Button("5");
	            	
	            	private final HBox buttons = new HBox(firstButton,secondButton,thirdButton,forthButton,fifthButton);
	            	
	            	{
	            		// getChildren --> lista :: scorrere lista :: tutti setting per buttone ( compresa azione ) 
	            		double r= 12;
	            		Circle buttonShape = new Circle(r);
	            		ObservableList<Node> buttonslist =  buttons.getChildren(); 
	            	    int i = 0  ;
	            	    
	            	    
	            		for(i=0;i<5;i++) {

	            			Button button = ((Button)buttonslist.get(i)) ; 
	            			button.setShape(buttonShape);
	            			button.setMinSize(2*r, 2*r); 
	            			button.setMaxSize(2*r, 2*r);
	            			button.setStyle("-fx-background-color: transparent;");
	            			
	            			button.setOnAction(event -> {
	            				
	            				int indice = getIndex();
		                    	Button buttoneClickato = (Button) event.getTarget();
		                    	HBox bottoniRiga = (HBox) buttoneClickato.getParent();
		                    	Integer votoInserito = Integer.valueOf(buttoneClickato.getText());
		                    	
		                    	
		                    	if(listaVoti[indice] == votoInserito) {
		                    		
		                    	   button.setStyle("-fx-background-color: transparent;");
		                    	   listaVoti[indice] = 0 ; 
		                    	   listaCommenti[indice] = "" ; 
		                    	   commentoVisibile.get(indice).setValue(true);
		                    	   
		                    	}
		                    	else {
		                    		
		                    	   if(!(listaVoti[indice] == 0 )) {
		                    		   
		                    		   ObservableList<Node> buttonslistriga =  bottoniRiga.getChildren(); 
		                    		   Button bottonePrecedente = ((Button)buttonslistriga.get(listaVoti[indice]-1)) ;
		                    		   bottonePrecedente.setStyle("-fx-background-color: transparent;");
		                    		   
		                    	   }
		                    	   
		                    	   commentoVisibile.get(indice).setValue(false);
		                    	   button.setStyle("-fx-background-color: white;");
		                    	   listaVoti[indice] = votoInserito ; 
		                    	}	
	            				
		                    });
	            			
	            			
	            		}
	            		
	                    buttons.setSpacing(2);
	                }
	                
	                @Override
	                public void updateItem(Void item, boolean empty) {
	                    super.updateItem(item, empty);
	                    if (empty) {
	                        setGraphic(null);
	                    } else {
	                        setGraphic(buttons);
	                    }
	                }};

	            return cell;
	        }
	    };

	    // crea una CellValueFactory che contiene un hyperlink , per ogni riga della tabella 
	    buttonsColumn.setCellFactory(cellFactory);

	    tabellaEmozioni.getColumns().add(buttonsColumn);
		
	}
	    

     /**
	 * porta l'utente alla scena per inserire un commento quando un link di testo viene clickato
	 * @param e : evento che scatena il metodo
	 * @param indice : indice della riga che contiene l'Hyperlink clickato 
	 * @throws IOException : un file non viene trovato 
	 */
    private void onHyperLinkCliked(ActionEvent event, int indice) {
    	
    	indiceCommentoCorrente = indice ; 
    	SingleSelectionModel<Tab> selectionModel  = tabpane.getSelectionModel();
	    selectionModel.select(1);
	    firstTab.setDisable(true);
	    secondTab.setDisable(false);
	    areaCommento.setText(listaCommenti[indiceCommentoCorrente]);
			
	}
	/**
	 * salva le emozioni inserite dall'utente nel file Emozioni.dati.csv 
	 * @param e : evento che scatena il metodo
	 * @throws IOException : il file non viene trovato 
	 */
	public void salvaEmozioni(ActionEvent event )throws IOException {
	
		boolean almenoUnVoto = false;
		for (int i = 0; i < listaVoti.length; i++){
		    if (listaVoti[i] != 0){
		    	almenoUnVoto  = true;
		        break;
		    }
		}
		if(almenoUnVoto) {
		  Song.RegistraEmozioni(utente,idCanzone,listaVoti,listaCommenti);
		  createAlert("Le emozioni sono state salvate");
		  switchTo(event,"SelezionaCanzone.fxml",new SelezionaCanzoneController(utente,playlist,null));
		}
		else
		  createAlert("Errore : E' necesssario associare \n        almeno un emozione");
        
	}
	
	/**
	 *  Salva il commento nella lista commenti se non è vuoto 
	 *  @param event : azione che scatena l'esecuzione del metodo
	 */
	public void salvaCommento(ActionEvent event ) {
		
		String commentoInserito = areaCommento.getText() ; 
        
        if (commentoInserito.isBlank()) {
        	createAlert("il commento non puo' essere vuoto");
        }
        else {
		    listaCommenti[indiceCommentoCorrente] = areaCommento.getText();
		    createAlert("Il commento e' stato salvato" );
        }
		
	
	}
	
}