/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Physics2D;

import Physics.FutureSimulation;
import Physics.Simulation;
import Physics.Integrators.ExplicitEuler;
import Physics.Integrators.Integrator;
import Physics.Integrators.Integrator.IntegratorType;
import Physics.Integrators.Symplectic1;
import Physics.Integrators.Symplectic4;
import Physics2D.Objects.SpaceObject;
import World2D.Objects.DisplayObject;
import World2D.Objects.Line;
import World2D.World;

/**
 *
 * @author bowen
 */
public class NBodySimulation implements Runnable, World, Simulation {
    private Thread thread;
    private SpaceObject[] objects;
    private Integrator integrator;
    private FutureSimulation futureIntegrator;
    
    private double updatesPerSecond; //How many "Big steps" per second
    private int miniSteps; //How many "Small Steps" per Big step
    private double secondsPerMiniStep; //How many seconds pass in each "Small Steps"
    private double initialRatio;
    private double ratio; //Ratio between simulated time and real time, higher is faster
    
    private int accel = 1;
    
    
    private boolean isPaused;
    /*
    public NBodySimulation(IntegratorType integrator, double ratio, double updatesPerSecond, int miniSteps, SpaceObject... objects) {
        this(integrator, ratio, updatesPerSecond, miniSteps, new NBodyFuturePath(integrator, ratio, 100, updatesPerSecond/2, objects), objects);
    }*/
    public NBodySimulation(IntegratorType integrator, double ratio, double updatesPerSecond, int miniSteps, FutureSimulation futureSimulation, SpaceObject... objects) {
        this.isPaused = true;
        this.objects = objects;
        this.initialRatio = ratio;
        this.ratio = ratio;
        this.updatesPerSecond = updatesPerSecond;
        this.secondsPerMiniStep = ratio/updatesPerSecond/miniSteps;
        this.miniSteps = (miniSteps > 0) ? miniSteps : 1;
        switch(integrator) {
            case NOFORCE:
                break;
            case EXPLICITEULER:
                this.integrator = new ExplicitEuler();
                break;
            case SYMPLECTIC4:
                this.integrator = new Symplectic4();
                break;
            case SYMPLECTIC1:
                this.integrator = new Symplectic1();
                break;
        }
        this.futureIntegrator = futureSimulation;
        this.thread = new Thread(this);
    }
    @Override
    public void step() {
        forward(1);
    }
    public void forward(int steps) {
        for (int i=0; i<steps; i++) {
            integrator.apply(objects, secondsPerMiniStep);
        }
    }
    private void updateSpatialPositions() {
        Vector2[] currentAccelerations = integrator.getCurrentAccelerations();
        for (int i=0; i<objects.length; i++) {
            objects[i].update();
        }
    }
    private void updateInterpolationSimulationTime(double time) { //Total time to interpolate before next physics Big Step
        for (int i=0; i<objects.length; i++) {
            objects[i].displayComponent.setInterpolationSimulationTime(time);
        }
    }
    @Override
    public void speedUp() {
        if (accel < 10E6) {
            accel *= 2;
            ratio = initialRatio * accel;
        }
    }
    @Override
    public void speedDown() {
        if (accel > 1) {
            accel /= 2;
            ratio = initialRatio * accel;
        }
    }
    @Override
    public double getSpeed() {
        return initialRatio * accel;
    }
    @Override
    public void start() {
        this.thread.start();
        this.futureIntegrator.start();
        unpause();
        this.futureIntegrator.unpause();
    }
    @Override
    public void pause() {
        this.isPaused = true;
        this.futureIntegrator.pause();
    }
    @Override
    public void unpause() {
        this.isPaused = false;
        this.futureIntegrator.unpause();
    }
    @Override
    public void run() {
        
        double desiredSleepms = 1000D/updatesPerSecond; //Desired sleep time in miliseconds
        double desiredSleepns = 1000000000D/updatesPerSecond;
        
        long startTime;
        long endTime;
        long sleepTime;
        
        int orbitIters = 0;
        final int orbitTimer = 100000;
        
        double realLagRatio;
        
        while (true) {
            if (!isPaused) {
                
                startTime = System.nanoTime();
                if (orbitIters > orbitTimer) {
                    futureIntegrator.forceUpdateOrbitPositions();
                    orbitIters = 0;
                }
                
                forward(miniSteps*accel);
                updateSpatialPositions();
                orbitIters += accel;
                endTime = System.nanoTime();
                
                
                sleepTime = (long)(desiredSleepms*1000000) - (endTime-startTime);
                realLagRatio = desiredSleepns/(endTime-startTime)*ratio;
                //realLagRatio = 0;
                updateInterpolationSimulationTime((realLagRatio > ratio) ? ratio : realLagRatio);
                if (sleepTime < 0) {
                    //sleepTime = 0;
                    System.out.println("Simulation Thread Overload");
                    //speedDown();
                } else {
                  
                    long sleepms = Math.floorDiv(sleepTime, 1000000);
                    int sleepns = (int)Math.floorMod(sleepTime, 1000000);

                    try {
                        Thread.sleep(sleepms, sleepns);
                    } catch (InterruptedException ex) {
                        System.out.println("Thread Error");
                    }
                    //updateInterpolationSimulationTime(0);
                }
                
            }
        }
    }

    @Override
    public DisplayObject[] getDisplayObjects() {
        DisplayObject[] lines = this.futureIntegrator.getDisplayObjects();
        
        
        DisplayObject[] displayObjects = new DisplayObject[objects.length + lines.length];
        for (int i=0; i<displayObjects.length; i++) {
            if (i < objects.length) {
                displayObjects[i] = objects[i].getDisplayObject();
            } else {
                displayObjects[i] = lines[i-objects.length];
            }
        }
        return displayObjects;
    }

    @Override
    public long getTicks() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Simulation getSimulation() {
        return this;
    }
}