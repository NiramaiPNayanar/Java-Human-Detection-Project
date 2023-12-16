import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CameraException extends Exception {
    public CameraException(String message) {
        super(message);
    }
}

class AdditionalException extends Exception {
    public AdditionalException(String message) {
        super(message);
    }
}

interface Detector {
    boolean detect();
}

interface BodyDetector extends Detector {
}

interface MotionDetector extends Detector {
}

interface HandDetector extends Detector {
    float getCoordinatesX();
    float getCoordinatesY();
}

class BodyDetection implements BodyDetector {
    public boolean detect() {
        System.out.println("Body detected.");
        return true;
    }
}

class MotionDetection implements MotionDetector {
    public boolean detect() {
        System.out.println("Motion detected.");
        return true;
    }
}

class HandDetection implements HandDetector {
    private String model;
    private boolean detected;
    private String type;

    public HandDetection(String model, boolean detected, String type) {
        this.model = model;
        this.detected = detected;
        this.type = type;
    }

    public boolean detect() {
        System.out.println(this.model + " is detecting a hand.");
        return true;
    }

    public float getCoordinatesX() {
        return (float) 1.245;
    }

    public float getCoordinatesY() {
        return (float) 1.4567;
    }
}

abstract class CameraOff {
    public abstract void turnOn() throws CameraException;
    public abstract void turnOff();
}

class CameraOn extends CameraOff implements BodyDetector, MotionDetector, HandDetector, Runnable, Detector {
    private Thread detectionThread;
    private Thread video;
    private boolean isDetectionCompleted = false;

    private static boolean isCameraTurnedOn = false;
    private static Lock lock = new ReentrantLock();

    public CameraOn() {
        synchronized (CameraOn.class) {
            if (!isCameraTurnedOn) {
                try {
                    turnOn();
                    Thread.sleep(3333);
                    CameraOn.class.notify();
                    isCameraTurnedOn = true;
                } catch (CameraException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void turnOn() throws CameraException {
        System.out.println("Camera is ON.");
        detectionThread = new Thread(this);
        detectionThread.start();
        boolean someErrorCondition = true;
        if (someErrorCondition) {
            throw new CameraException("Camera error: Some error occurred.");
        }
    }

    public void turnOff() {
        System.out.println("Camera is OFF.");
        detectionThread.interrupt();
    }

    public void run() {
        captureAndDetect();
    }

    public synchronized void captureAndDetect() {
        System.out.println("Capturing image or video frame...");

        boolean combinedDetectionResult = detect();

        if (combinedDetectionResult) {
            System.out.println("Combined detection result: Detected.");
            isDetectionCompleted = true;
            notify();
        }
    }

    public synchronized void waitForDetectionCompletion() {
        while (!isDetectionCompleted) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void video(int x, int y, int z) {
        System.out.println("Capturing video with parameters x=" + x + ", y=" + y + ", z=" + z);
        video = new Thread();
        video.start();
    }

    public void video(int x, int y) {
        System.out.println("Capturing video with parameters x=" + x + ", y=" + y);
        video = new Thread();
        video.start();
    }

    public float getCoordinatesX() {
        return (float) 1.245;
    }

    public float getCoordinatesY() {
        return (float) 1.4567;
    }

    public boolean detect() {
        System.out.println("Combined detection logic.");
        return true;
    }

    public Thread getVideoThread() {
        return video;
    }
}

public class Main {
    public static void main(String[] args) {
        CameraOn camera1 = new CameraOn();
        CameraOn camera2 = new CameraOn();

        camera1.video(1920, 1080, 30);
        camera1.waitForDetectionCompletion();
        System.out.println("camera 1 is alive?" + camera1.getVideoThread().isAlive());
        camera1.turnOff();

        synchronized (CameraOn.class) {
            try {
                camera2.turnOn();
            } catch (CameraException e) {
                System.err.println("CameraException caught in main: " + e.getMessage());
            }
        }

        camera2.video(1280, 720);
        camera2.waitForDetectionCompletion();
        System.out.println("camera 2 is alive?" + camera2.getVideoThread().isAlive());

        camera2.turnOff();

        HandDetection handDetector = new HandDetection("ModelXYZ", true, "Gesture");
        handDetector.detect();
        System.out.println("Coordinates X: " + handDetector.getCoordinatesX());
        System.out.println("Coordinates Y: " + handDetector.getCoordinatesY());

        try {
            camera1.getVideoThread().join();
            camera2.getVideoThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("camera 1 is alive?" + camera1.getVideoThread().isAlive());
        System.out.println("camera 2 is alive?" + camera2.getVideoThread().isAlive());
    }
}

