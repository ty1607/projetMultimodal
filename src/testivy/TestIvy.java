/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testivy;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import java.util.Timer;
import java.util.TimerTask;

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
        
        
        private Timer timer;
        
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
        
        System.out.println("Start Monitoring");
        bus.bindMsg("^Palette:MousePressed x=(.*) y=(.*)", (IvyClient arg0, String[] arg1) -> {
            System.out.println("Event Souris Pressed x:" + arg1[0] + " - y" + arg1[1]);
            stroke = new Stroke();
            canvas.setPoints(stroke.getPoints());
            stroke.addPoint(Integer.parseInt(arg1[0]), Integer.parseInt(arg1[1]));
        });
        
        bus.bindMsg("^Palette:MouseDragged x=(.*) y=(.*)", (IvyClient arg0, String[] arg1) -> {
            System.out.println("Event Souris Dragged x:" + arg1[0] + " - y" + arg1[1]);
            stroke.addPoint(Integer.parseInt(arg1[0]), Integer.parseInt(arg1[1]));
        });
        
        bus.bindMsg("^Palette:MouseReleased x=(.*) y=(.*)", (IvyClient arg0, String[] arg1) -> {
            System.out.println("Event Souris Dragged x:" + arg1[0] + " - y" + arg1[1]);
            stroke.addPoint(Integer.parseInt(arg1[0]), Integer.parseInt(arg1[1]));
            stroke.normalize();
            canvas.setNormPoints(stroke.getPoints());
            canvas.repaint();
        });
        
        bus.sendMsg("Palette:CreerEllipse");
    }
    
    class handleTimerTask extends TimerTask {
        public void run() {
           System.out.println("Hello World!"); 
        }
    }
    
    
    /**
     * Intialise les differents variables et elements du code.
     */
    private void init(){
        state = State.IDLE;
        commandeReconnu = VoiceRecog.NOTHING;
        timer = new Timer();
        
    }
    
    /**
     * @param args the command line arguments
     * @throws fr.dgac.ivy.IvyException
     */
    public static void main(String[] args) throws IvyException {
            TestIvy testIvy;
            testIvy = new TestIvy();
            
    }
}
