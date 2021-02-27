/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testivy;

import java.awt.geom.Point2D;
import java.util.List;

/**
 *
 * @author Thomas
 */
public class Geste {
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