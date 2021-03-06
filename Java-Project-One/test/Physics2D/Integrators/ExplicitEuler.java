/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Physics2D.Integrators;

import Physics2D.Integrators.Integrator;
import Physics2D.Integrators.NBody;
import Physics2D.Objects.Moon;
import Physics2D.Objects.Particle;
import Physics2D.Objects.Planet;
import Physics2D.Objects.PointBody;
import SpaceGame.Objects.SpaceArtificial;
import Physics2D.Objects.Star;
import Physics2D.Vector2;
import Physics2D.Vectors2;

/**
 *
 * @author bowen
 */
public class ExplicitEuler implements Integrator {
    
    private Vector2[] accelerations;
    
    @Override
    public void apply(PointBody[] bodies, double dt) {
        Vector2[] currentAcc = NBody.getAccelerations(bodies);
        
        for (int i=0; i<bodies.length; i++) {
            bodies[i].addPosition(Vectors2.prod(bodies[i].velocity(), dt));
            bodies[i].addVelocity(Vectors2.prod(currentAcc[i], dt));
        }
        accelerations = currentAcc;
    }
    @Override
    public Vector2[] getCurrentAccelerations() {
        return accelerations;
    }

    @Override
    public IntegratorType type() {
        return IntegratorType.EXPLICITEULER;
    }
    @Override
    public void reset() {
        accelerations = null;
    }

    @Override
    public void applyByPart(Star[] stars, Planet[] planets, Moon[] moons, Particle[] particles, SpaceArtificial[] spacecrafts) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
