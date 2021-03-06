/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testivy;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
//import testivy.MonCanvas.Geste;

/**
 *
 * @author Thomas
 */
public class TestIvy {
    Ivy bus;
    Stroke stroke;
    final String FILE = "./test.csv";

    private enum State {
        IDLE,
        CREATE,
        CREATE_CLICKED, 
        CREATE_VOIX,
        DELETE,
        DELETE_CLICKED,
        DELETE_VOIX
    }
    private State state;


    //Cette enumeration va servir pour dire a l'application quel traitement prendre quand il recoit un 
    //click apres avoir reco une commande vocale.
    private enum VoiceRecog {
        COLOR,
        POSITION,
        NOTHING
    } 
    private VoiceRecog commandeReconnu;

    private String forme;


    private Timer timer;
    Point position;
    Point tempPos;
    String couleur;

    List<Geste> gestes; 
    StoreGeste sg;
        
    String nomObjetTest;
    public TestIvy() throws IvyException {
        bus = new Ivy("Test Ivy", "", null);
        init();
        try {
            bus.start("127.255.255.255:2010");
        } catch (IvyException e) {
            e.printStackTrace();
        }
        
        stroke = new Stroke();
        MonCanvas canvas = new MonCanvas(stroke.getPoints());
        canvas.openFrame();
        canvas.setGestes(gestes);
        
        System.out.println("Start Monitoring");
        bus.bindMsg("^Palette:MousePressed x=(.*) y=(.*)", (IvyClient arg0, String[] arg1) -> {
            System.out.println("Event Souris Pressed x:" + arg1[0] + " - y" + arg1[1]);
            switch (state){
                case IDLE :
                    //demarre le dessin de forme
                    stroke = new Stroke();
                    canvas.setPoints(stroke.getPoints());
                    stroke.addPoint(Integer.parseInt(arg1[0]), Integer.parseInt(arg1[1]));
                    break;
                case CREATE : 
                    //On fait rien
                    break;
                case CREATE_CLICKED :
                    //On fait rien
                    break;
                case CREATE_VOIX :
                    //On fait rien
                    break; 
            }
        });
        
        bus.bindMsg("^Palette:MouseDragged x=(.*) y=(.*)", (IvyClient arg0, String[] arg1) -> {
            System.out.println("Event Souris Dragged x:" + arg1[0] + " - y" + arg1[1]);
            
            switch (state){
                case IDLE :
                    //continue le dessin de forme
                    stroke.addPoint(Integer.parseInt(arg1[0]), Integer.parseInt(arg1[1]));
                    break;
                case CREATE : 
                    //On fait rien
                    break;
                case CREATE_CLICKED :
                    //On fait rien
                    break;
                case CREATE_VOIX :
                    //On fait rien
                    break; 
            }
        });
        
        bus.bindMsg("^Palette:MouseReleased x=(.*) y=(.*)", (IvyClient arg0, String[] arg1) -> {
            System.out.println("Event Souris Dragged x:" + arg1[0] + " - y" + arg1[1]);
           
            canvas.repaint();
            switch (state){
                case IDLE :
                    //termine le dessin de forme
                     stroke.addPoint(Integer.parseInt(arg1[0]), Integer.parseInt(arg1[1]));
                    stroke.normalize();
                    canvas.setNormPoints(stroke.getPoints());
                    //sg.addGestetoCSV(new Geste(stroke.getPoints(), "Deplacer"), FILE);
                    
                   Geste reco = canvas.recoGeste(stroke.getPoints());
                    if (reco != null){
                        switch (reco.getNom()){
                        case "Rectangle" :
                            //Set la forme a creer en tant que rectangle et changer d'etat
                            forme = "Rectangle";
                            this.state = State.CREATE;
                            timer.schedule(new HandleTimerTask(), 6000);
                            break;
                        case "Ellipse":
                            //Set la forme a creer en tant que ellipse et changer d'etat
                            forme = "Ellipse";
                            this.state = State.CREATE;
                            timer.schedule(new HandleTimerTask(), 6000);
                            break;
                        case "Supprimer" :
                            state = State.DELETE;
                            timer = new Timer();
                            timer.schedule(new HandleTimerTask(), 6000);
                            break;
                        default :
                            break;
                        }
                    } else 
                        System.out.println("Geste non reconnu");
                    break;

                case CREATE : 
                    //On fait rien
                    break;
                case CREATE_CLICKED :
                    //On fait rien
                    break;
                case CREATE_VOIX :
                    //On fait rien
                    break; 
            }
        });
        
        bus.bindMsg("^Palette:MouseClicked x=(.*) y=(.*)", (IvyClient arg0, String[] arg1) -> {
            System.out.println("Event Souris Pressed x:" + arg1[0] + " - y" + arg1[1]);
            switch (state){
                case IDLE :
                    //Interdit
                    break;
                case CREATE : 
                    //On releve la position et on change d'etat
                    tempPos.x = Integer.parseInt(arg1[0]);
                    tempPos.y = Integer.parseInt(arg1[1]);
                    state = State.CREATE_CLICKED;
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(new HandleTimerTask(), 6000);
                    break;
                case CREATE_CLICKED :
                    //On remplace la position
                    //timer.cancel();
                    tempPos.x = Integer.parseInt(arg1[0]);
                    tempPos.y = Integer.parseInt(arg1[1]);
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(new HandleTimerTask(), 6000);
                    break;
                case CREATE_VOIX :
                    //On traite le click.
                    //timer.cancel();
                    tempPos.x = Integer.parseInt(arg1[0]);
                    tempPos.y = Integer.parseInt(arg1[1]);
                
                    try {
                        traiterClick();
                    } catch (IvyException ex) {
                        Logger.getLogger(TestIvy.class.getName()).log(Level.SEVERE, null, ex);
                    }
                
                    //commandeReconnu = VoiceRecog.NOTHING;
                    //state = State.CREATE;
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(new HandleTimerTask(), 6000);
                    break; 
                case DELETE : 
                    state = State.DELETE_CLICKED;
                    tempPos.x = Integer.parseInt(arg1[0]);
                    tempPos.y = Integer.parseInt(arg1[1]);
                    try {
                        traiterClickDelete();
                    } catch (IvyException ex) {
                        Logger.getLogger(TestIvy.class.getName()).log(Level.SEVERE, null, ex);
                    }
                
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(new HandleTimerTask(), 6000);
                    break; 
                case DELETE_CLICKED :
                    tempPos.x = Integer.parseInt(arg1[0]);
                    tempPos.y = Integer.parseInt(arg1[1]);
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(new HandleTimerTask(), 6000);
                    break; 
                case DELETE_VOIX : 
                    tempPos.x = Integer.parseInt(arg1[0]);
                    tempPos.y = Integer.parseInt(arg1[1]);
                    try {
                        traiterClickDelete();
                    } catch (IvyException ex) {
                        Logger.getLogger(TestIvy.class.getName()).log(Level.SEVERE, null, ex);
                    }
                
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(new HandleTimerTask(), 6000);
                    break; 
                    
 
            }
        });
        
        
        bus.bindMsg("^Palette:ResultatTesterPoint x=(.*) y=(.*) nom=(.*)", (IvyClient arg0, String[] arg1) -> {
            switch (state){
                case IDLE :
                    //Interdit
                    break;
                case CREATE : 
                    //Interdit
                    break;
                case CREATE_CLICKED :
                    //Interddit
                    
                    break;
                case CREATE_VOIX :
                    //On releve le nom de l'element.
                    nomObjetTest = arg1[2];
                    break; 
                case DELETE :
                    break;
                case DELETE_CLICKED : 
                case DELETE_VOIX : 
                    nomObjetTest = arg1[2];
                    break;
                    
            }
        });
        
        bus.bindMsg("^Palette:FinTesterPoint x=(.*) y=(.*)", (IvyClient arg0, String[] arg1) -> {
            switch (state){
                case IDLE :
                    //Interdit
                    break;
                case CREATE : 
                    //On releve la position et on change d'etat
                    break;
                case CREATE_CLICKED :
                    //On remplace la position
                    
                    break;
                case CREATE_VOIX :
                    //On traite le click.
                    switch (commandeReconnu){
                        case COLOR : 
                
                            try {
                                bus.sendMsg("Palette:DemanderInfo nom=" + nomObjetTest);
                            } catch (IvyException ex) {
                                Logger.getLogger(TestIvy.class.getName()).log(Level.SEVERE, null, ex);
                            }
                
                        case POSITION :
                        case NOTHING :
                            break;
                    }
                    break; 
                case DELETE : 
                    break;
                case DELETE_CLICKED:
                case DELETE_VOIX : 
                    try {
                        bus.sendMsg("Palette:SupprimerObjet nom=" + nomObjetTest);
                    } catch (IvyException ex) {
                        Logger.getLogger(TestIvy.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    state = State.IDLE;
                    timer.cancel();
                    initVars();
            }
        });
        
       
        bus.bindMsg("^Palette:Info nom=(.*) x=(.*) y=(.*) longueur=(.*) hauteur=(.*) couleurFond=(.*) couleurContour=(.*)", (IvyClient arg0, String[] arg1) -> {
            switch (state){
                case IDLE :
                    //Interdit
                    break;
                case CREATE : 
                    //Interdit
                    break;
                case CREATE_CLICKED :
                    //Interdit
                    
                    break;
                case CREATE_VOIX :
                    //On traite le click.
                    couleur = arg1[5];
                    
                    if (!position.equals(new Point(0,0)) && (couleur != "default")){
                        createShape();
                        initVars();
                        state = State.IDLE;
                        //timer.cancel();
                    } else {
                        //timer.cancel();
                        timer.cancel();
                        timer = new Timer();
                        timer.schedule(new HandleTimerTask(), 6000);
                        state = State.CREATE;
                    }
                    break; 
                default: 
                    break;
            }
        });
        
        bus.bindMsg("^sra5 Text=(.*) Confidence=(.*)", (IvyClient arg0, String[] arg1) -> {
            switch (state){
                case IDLE :
                    //Interdit
                    break;
                case CREATE : 
                    //Interdit
                    switch (arg1[0]) {
                        case "ici" :
                            commandeReconnu = VoiceRecog.POSITION;
                            state = State.CREATE_VOIX;
                            //timer.cancel();
                            timer.cancel();
                            timer = new Timer();
                            timer.schedule(new HandleTimerTask(), 6000);
                            break;
                        case "couleur":
                        case "element" :
                            commandeReconnu = VoiceRecog.COLOR;
                            state = State.CREATE_VOIX;
                            
                            //timer.cancel();
                            timer.cancel();
                            timer = new Timer();
                            timer.schedule(new HandleTimerTask(), 6000);
                            break;
                        case "rouge":
                            couleur = "RED";
                            break;
                        case "violet":
                            couleur = "PURPLE";
                            break;
                        case "vert":
                            couleur = "GREEN";
                            break;
                        case "bleu":
                            couleur = "BLUE";
                            break;
                        case "noir":
                            couleur = "BLACK";
                            break;
                        case "jaune":
                            couleur = "YELLOW";
                            break;
                        default :
                             break;
                    }
                    break;
                case CREATE_CLICKED :
                    
                    switch (arg1[0]) {
                        case "ici" :
                            position = tempPos;
                            //timer.cancel();
                            state = State.CREATE;
                            if (couleur != "default"){
                                createShape();
                                initVars();
                                state = State.IDLE;
                            } else {
                                timer.cancel();
                                timer = new Timer();
                                timer.schedule(new HandleTimerTask(), 6000);
                                state = State.CREATE;
                            }
                            break;
                        case "element" :
                            commandeReconnu = VoiceRecog.COLOR;
                
                            try {
                                bus.sendMsg("Palette:TesterPoint x=" + tempPos.x + " y=" + tempPos.y);
                            } catch (IvyException ex) {
                                Logger.getLogger(TestIvy.class.getName()).log(Level.SEVERE, null, ex);
                            }
                
                            //timer.cancel();
                            timer.schedule(new HandleTimerTask(), 6000);
                            break;

                        default :
                             break;
                    }
                    break;
                case CREATE_VOIX :
                    //On traite le click.
                    switch (arg1[0]) {
                        case "ici" :
                            commandeReconnu = VoiceRecog.POSITION;
                            //timer.cancel();
                            timer.cancel();
                            timer = new Timer();
                            timer.schedule(new HandleTimerTask(), 6000);
                            break;
                        case "element" :
                            commandeReconnu = VoiceRecog.COLOR;
                            //timer.cancel();
                            timer.cancel();
                            timer = new Timer();
                            timer.schedule(new HandleTimerTask(), 6000);
                            break;
                        default :
                             break;
                    }
                    break; 
                case DELETE :
                    switch (arg1[0]){
                        case "element" :
                            commandeReconnu = VoiceRecog.POSITION;
                            state = State.DELETE_VOIX; 
                            break;
                        default:
                            break;
                    }
                    break;
                case DELETE_CLICKED :
                    try {
                        bus.sendMsg("Palette:TesterPoint x=" + tempPos.x + " y=" + tempPos.y);
                    } catch (IvyException ex) {
                        Logger.getLogger(TestIvy.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(new HandleTimerTask(), 6000);
                    break;
                case DELETE_VOIX : 
                    break;
            }
        });
        
    }
    
    public class HandleTimerTask extends TimerTask {
        public void run() {
           switch (state){
               case IDLE : 
                   //Interdit
                   break;
               case CREATE :
                   createShape();
                   initVars();
                   state = State.IDLE;
                   break;

               case CREATE_CLICKED : 
                    //On supprime la position releve avec le click
                    tempPos = new Point(0,0);
                    //timer.cancel();
                    state = State.CREATE;
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(new HandleTimerTask(), 6000);
                   break;
               case CREATE_VOIX : 
                    //On revient dans l'Etat CREATE et on annule la commande reconnu
                    //timer.cancel();
                    state = State.CREATE;
                    commandeReconnu = VoiceRecog.NOTHING;
                    timer.cancel();
                    timer = new Timer();
                    timer.schedule(new HandleTimerTask(), 6000);
                   break;
               case DELETE :
                   state = State.IDLE;
                   timer.cancel();
                   break;
               case DELETE_CLICKED :
                   tempPos = new Point(0,0);
                   state = State.DELETE;
                   timer.cancel();
                   break;
               case DELETE_VOIX :
                   state = State.DELETE;
                   commandeReconnu = VoiceRecog.NOTHING;
                   timer.cancel();
               
           }
        }
    }
    
    
    /**
     * Intialise les differents variables et elements du code.
     */
    private void init() {
        initVars();
        // =================
        // Test Store & Read
        // =================
        
        sg = new StoreGeste();
        gestes = new ArrayList<>();
        /*List<Point2D.Double> points = new ArrayList<>();
        for (int i = 0 ; i < 10 ; i++)
            points.add(new Point2D.Double(i, i));
        
        gestes.add(new Geste(points, "ligne droite"));
        sg.storeWorkflowCSV(gestes, FILE);
        System.out.println(Arrays.toString(gestes.toArray()));
        sg.addGestetoCSV(new Geste(points, "ligne droite2"), FILE);*/
        gestes = sg.readGestesCSV(FILE);
    }
    
    /**
     * @param args the command line arguments
     * @throws fr.dgac.ivy.IvyException
     */
    public static void main(String[] args) throws IvyException {
            TestIvy testIvy;
            testIvy = new TestIvy();
            
            
    }
    
    
    public void initVars(){
        state = State.IDLE;
        commandeReconnu = VoiceRecog.NOTHING;
        timer = new Timer();
        //On met la couleur et la position par defaut.
        position = new Point(0, 0);
        tempPos = new Point(0, 0);
        couleur = "default";
    }
    public void traiterClick() throws IvyException{
        
        
        switch(commandeReconnu){
            case COLOR:
                bus.sendMsg("Palette:TesterPoint x=" + tempPos.x + " y=" + tempPos.y);
                
                break;
            case POSITION :
                position = tempPos;
                commandeReconnu = VoiceRecog.NOTHING;
                state= State.CREATE;
                break;
            case NOTHING : 
                break;
                
        }
    }
    
    public void traiterClickDelete() throws IvyException{
        switch (commandeReconnu){
            case POSITION : 
                bus.sendMsg("Palette:TesterPoint x=" + tempPos.x + " y=" + tempPos.y);
                break;
            case NOTHING :
            case COLOR : 
                break;
        }
    }
    
    public void createShape(){
        try {
            //Creation de la forme reco avec les parametres obtenus ou defaut
            if (position == null){
                position.setLocation(0, 0);
            }
            if (couleur == "default" || couleur == null){
                couleur = "RED";
            }
            
            switch (forme){
                case "Rectangle":
                    bus.sendMsg("Palette:CreerRectangle x=" + position.x + " y=" + position.y + " couleurFond=" + couleur);
                    break;
                case "Ellipse":
                    bus.sendMsg("Palette:CreerEllipse x=" + position.x + " y=" + position.y + " couleurFond=" + couleur);
                    break;
            }
            
        } catch (IvyException ex) {
            Logger.getLogger(TestIvy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
