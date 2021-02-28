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
        
        private enum State {
            IDLE,
            CREATE,
            CREATE_CLICKED, 
            CREATE_VOIX
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
        bus = new Ivy("Test Ivy", "Palette:CreerRectangle", null);
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
                    //continue le dessin de forme
                     stroke.addPoint(Integer.parseInt(arg1[0]), Integer.parseInt(arg1[1]));
                    stroke.normalize();
                    canvas.setNormPoints(stroke.getPoints());
                   /*Geste reco = canvas.recoGeste(stroke.getPoints());
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
                        default :
                            break;
                    }*/
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
                    timer.cancel();
                    tempPos.x = Integer.parseInt(arg1[0]);
                    tempPos.y = Integer.parseInt(arg1[1]);
                    state = State.CREATE_CLICKED;
                    timer.schedule(new HandleTimerTask(), 6000);
                    break;
                case CREATE_CLICKED :
                    //On remplace la position
                    timer.cancel();
                    tempPos.x = Integer.parseInt(arg1[0]);
                    tempPos.y = Integer.parseInt(arg1[1]);
                    timer.schedule(new HandleTimerTask(), 6000);
                    break;
                case CREATE_VOIX :
                    //On traite le click.
                    timer.cancel();
                    tempPos.x = Integer.parseInt(arg1[0]);
                    tempPos.y = Integer.parseInt(arg1[1]);
                
                    try {
                        traiterClick();
                    } catch (IvyException ex) {
                        Logger.getLogger(TestIvy.class.getName()).log(Level.SEVERE, null, ex);
                    }
                
                    state = State.CREATE;
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
                    //On releve la position et on change d'etat
                    break;
                case CREATE_CLICKED :
                    //On remplace la position
                    
                    break;
                case CREATE_VOIX :
                    //On traite le click.
                    nomObjetTest = arg1[3];
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
            }
        });
        bus.bindMsg("^Palette:Info nom=(.*) x=(.*) y=(.*) longeur=(.*)"
                + " hauteur=(.*) couleurFond=(.*) couleurContour=(.*)", (IvyClient arg0, String[] arg1) -> {
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
                    
                    if (position != null && (couleur != null && !couleur.isEmpty())){
                        createShape();
                        state = State.IDLE;
                        timer.cancel();
                    } else {
                        timer.cancel();
                        timer.schedule(new HandleTimerTask(), 6000);
                        state = State.CREATE;
                    }
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
                        case "HERE" :
                            commandeReconnu = VoiceRecog.POSITION;
                            state = State.CREATE_VOIX;
                            timer.cancel();
                            timer.schedule(new HandleTimerTask(), 6000);
                            break;
                        case "COLOR" :
                            commandeReconnu = VoiceRecog.COLOR;
                            state = State.CREATE_VOIX;
                            timer.cancel();
                            timer.schedule(new HandleTimerTask(), 6000);
                            break;
                        default :
                             break;
                    }
                    break;
                case CREATE_CLICKED :
                    //Interdit
                    switch (arg1[0]) {
                        case "HERE" :
                            position = tempPos;
                            timer.cancel();
                            state = State.CREATE;
                            if (couleur != null && !couleur.isEmpty()){
                                createShape();
                                state = State.IDLE;
                            } else {
                                timer.schedule(new HandleTimerTask(), 6000);
                                state = State.CREATE;
                            }
                            break;
                        case "COLOR" :
                            commandeReconnu = VoiceRecog.COLOR;
                
                            try {
                                bus.sendMsg("Palette:TesterPoint x=" + tempPos.x + "y=" + tempPos.y);
                            } catch (IvyException ex) {
                                Logger.getLogger(TestIvy.class.getName()).log(Level.SEVERE, null, ex);
                            }
                
                            timer.cancel();
                            timer.schedule(new HandleTimerTask(), 6000);
                            break;

                        default :
                             break;
                    }
                    break;
                case CREATE_VOIX :
                    //On traite le click.
                    switch (arg1[0]) {
                        case "HERE" :
                            commandeReconnu = VoiceRecog.POSITION;
                            timer.cancel();
                            timer.schedule(new HandleTimerTask(), 6000);
                            break;
                        case "COLOR" :
                            commandeReconnu = VoiceRecog.COLOR;
                            timer.cancel();
                            timer.schedule(new HandleTimerTask(), 6000);
                            break;
                        default :
                             break;
                    }
                    break; 
            }
        });
        
        
        
        bus.sendMsg("Palette:CreerEllipse");
    }
    
    public class HandleTimerTask extends TimerTask {
        public void run() {
           switch (state){
               case IDLE : 
                   //Interdit
                   break;
               case CREATE :
                   createShape();
                   break;

               case CREATE_CLICKED : 
                   //On supprime la position releve avec le click
                   tempPos = null;
                   timer.cancel();
                   state = State.CREATE;
                   timer.schedule(new HandleTimerTask(), 6000);
                   break;
               case CREATE_VOIX : 
                   //On revient dans l'Etat CREATE et on annule la commande reconnu
                   timer.cancel();
                   state = State.CREATE;
                   commandeReconnu = VoiceRecog.NOTHING;
                   timer.schedule(new HandleTimerTask(), 6000);
                   break;
           }
        }
    }
    
    
    /**
     * Intialise les differents variables et elements du code.
     */
    private void init() {
        state = State.IDLE;
        commandeReconnu = VoiceRecog.NOTHING;
        timer = new Timer();
        //On met la couleur et la position par defaut.
        position = new Point(0, 0);
        // =================
        // Test Store & Read
        // =================
        final String FILE = "./test.csv";
        sg = new StoreGeste();
        gestes = new ArrayList<>();
        List<Point2D.Double> points = new ArrayList<>();
        for (int i = 0 ; i < 10 ; i++)
            points.add(new Point2D.Double(i, i));
        
        gestes.add(new Geste(points, "ligne droite"));
        sg.storeWorkflowCSV(gestes, FILE);
        gestes = sg.readGestesCSV(FILE);
        System.out.println(Arrays.toString(gestes.toArray()));
        sg.addGestetoCSV(new Geste(points, "ligne droite2"), FILE);
    }
    
    /**
     * @param args the command line arguments
     * @throws fr.dgac.ivy.IvyException
     */
    public static void main(String[] args) throws IvyException {
            TestIvy testIvy;
            testIvy = new TestIvy();
            
            
    }
    
    public void traiterClick() throws IvyException{
        
        
        switch(commandeReconnu){
            case COLOR:
                bus.sendMsg("Palette:TesterPoint x=" + tempPos.x + "y=" + tempPos.y);
                break;
            case POSITION :
                position = tempPos;
                break;
            case NOTHING : 
                break;
                
        }
    }
    
    public void createShape(){
        try {
            //Creation de la forme reco avec les parametres obtenus ou defaut
            if (position == null){
                position.setLocation(0, 0);
            }
            if (couleur == "" || couleur == null){
                couleur = Color.RED.toString();
            }
            bus.sendMsg("Palette:CreerRectangle x=" + position.x + " y=" + position.y + " couleurFond=" + couleur);
        } catch (IvyException ex) {
            Logger.getLogger(TestIvy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
