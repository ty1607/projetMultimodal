/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testivy;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author washbrth
 */
public class MonCanvas extends JComponent implements ActionListener  {
    List<Point2D.Double> points;
    List<Point2D.Double> normPoint;
    List<Geste> gestes;
    JFrame frame;
    int i;
    private enum State {
        APPRENTISSAGE,
        RECONNAISSANCE
    }
    
    private class Geste {
        List<Point2D.Double> points;
        String nom;
        public Geste(List<Point2D.Double> points, String nom) {
            this.points = points;
            this.nom = nom;
        }

        public List<Point2D.Double> getPoints() {
            return points;
        }

        public String getNom() {
            return nom;
        }

        public void setPoints(List<Point2D.Double> points) {
            this.points = points;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }  
    }
    
    State state;
            
            
    public MonCanvas(ArrayList<Point2D.Double> ps) {
        super();
        this.points = ps;
        this.i = 0;
        this.gestes = new ArrayList<Geste>();
        setPreferredSize(new Dimension(400, 400));
    }
    
    public void setPoints(List<Point2D.Double> points) {
        this.points = points;
    }
    
    public void setNormPoints(List<Point2D.Double> points) {
        this.normPoint = points;
        this.i++;
        if (state == State.APPRENTISSAGE) {
            this.addGeste();
        } else {
            this.recoGeste(points);
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        points.forEach(point -> {
            g2.drawOval((int) point.getX(), (int) point.getY(), 2, 2);
        });
        if (normPoint != null){
            normPoint.forEach(point -> {
            g2.drawOval((int) point.getX(), (int) point.getY(), 2, 2);
        });
        }
    }
    
    public void openFrame() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 2));
        JRadioButton radioApprentissage = new JRadioButton("Apprentissage");
        radioApprentissage.setSelected(true);
        this.state = State.APPRENTISSAGE;
        JRadioButton radioReconnaissance = new JRadioButton("Reconnaissance");
        radioReconnaissance.setSelected(false);
        ButtonGroup bGroup = new ButtonGroup();
        bGroup.add(radioApprentissage);
        bGroup.add(radioReconnaissance);
        
        radioApprentissage.addActionListener(this);
        radioReconnaissance.addActionListener(this);
        
        panel.add(radioApprentissage);
        panel.add(radioReconnaissance);
        panel.add(this);
        
        
        frame = new JFrame("Draw Points");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setSize(new Dimension(800,800));
        frame.setVisible(true);
        
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (state == State.RECONNAISSANCE) {
            this.state = State.APPRENTISSAGE;
            System.out.println("Switch Apprent");
        } else {
            this.state = State.RECONNAISSANCE;
            System.out.println("Switch Reco");
        }
    }
    
    private void addGeste() {
        System.out.println("Geste enregistré");
        gestes.add(new Geste(this.normPoint,"Geste" + i));
    }
    
    private Geste recoGeste(List<Point2D.Double> points) {
        double somme = 0;
        List<Double> listeScore = new ArrayList<>();
       
        
        for (Geste geste : gestes){
            somme = 0;
            for (int i = 0; i < points.size(); i++){
                double x = points.get(i).x;
                double y = points.get(i).y;
                double gX = geste.points.get(i).x;
                double gY = geste.points.get(i).y;
                somme += Math.sqrt((x - gX)*(x - gX) + (y - gY)*(y - gY));
            }
            somme =  somme/ points.size();
            somme = 1 - (somme/(0.5*Math.sqrt(points.size()*points.size() + geste.points.size()*geste.points.size())));
            listeScore.add(somme);
        }
        Double best_score = 0.0;
        Geste bestScore = null; 
        for (Double val : listeScore) {
           if (best_score < val) {
               best_score = val;
               bestScore = gestes.get(listeScore.indexOf(val));
           }
        }
        System.out.println("Gest reconnu = " + bestScore.nom + " + score " + best_score.toString());
        return bestScore;
    }
}
