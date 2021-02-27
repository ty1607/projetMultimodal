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
        Color couleur;
        
        List<Geste> gestes; 
        StoreGeste sg;
        
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
                   Geste reco = canvas.recoGeste(stroke.getPoints());
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
                    }
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
                    position.x = Integer.parseInt(arg1[0]);
                    position.y = Integer.parseInt(arg1[1]);
                    state = State.CREATE_CLICKED;
                    break;
                case CREATE_CLICKED :
                    //On remplace la position
                    position.x = Integer.parseInt(arg1[0]);
                    position.y = Integer.parseInt(arg1[1]);
                    break;
                case CREATE_VOIX :
                    //On traite le click.
                    traiterClick();
                    state = State.CREATE;
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
                   try {
                       //Creation de la forme reco avec les parametres obtenus ou defaut
                       bus.sendMsg("Palette:CreerRectangle x=" + position.x + " y=" + position.y + " couleurFond=" + couleur.toString());
                   } catch (IvyException ex) {
                       Logger.getLogger(TestIvy.class.getName()).log(Level.SEVERE, null, ex);
                   }
                   break;

               case CREATE_CLICKED : 
                   //On supprime la position releve avec le click
                   position.x = 0; position.y = 0;
                   state = State.CREATE;
                   break;
               case CREATE_VOIX : 
                   //On revient dans l'Etat CREATE et on annule la commande reconnu
                   state = State.CREATE;
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
        couleur = Color.RED;
        position = new Point(0, 0);
        
        // =================
        // Test Store & Read
        // =================
        sg = new StoreGeste();
        gestes = new ArrayList<>();
        List<Point2D.Double> points = new ArrayList<>();
        for (int i = 0 ; i < 10 ; i++)
            points.add(new Point2D.Double(i, i));
        
        gestes.add(new Geste(points, "ligne droite"));
        try {
            sg.storeWorkflowCSV(gestes, "./test.csv");
        } catch (IOException ex) {
            Logger.getLogger(TestIvy.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            gestes = sg.readGestesCSV("./test.csv");
            
        } catch (IOException ex) {
            Logger.getLogger(TestIvy.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(Arrays.toString(gestes.toArray()));
    }
    
    /**
     * @param args the command line arguments
     * @throws fr.dgac.ivy.IvyException
     */
    public static void main(String[] args) throws IvyException {
            TestIvy testIvy;
            testIvy = new TestIvy();
            
            
    }
    
    public void traiterClick(){
        
        
        switch(commandeReconnu){
            
        }
    }
}
