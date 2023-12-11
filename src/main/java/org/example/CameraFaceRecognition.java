package org.example;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.CvType;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfFloat;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
//import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class CameraFaceRecognition{

    private static final String FACE_IMAGES_DIRECTORY = "E:\\face\\src\\images"; // Replace with the actual path

    public static void main(String[] args) {

        nu.pattern.OpenCV.loadLocally();

        // Load the cascade classifier for face detection
        CascadeClassifier faceCascade = new CascadeClassifier();
        faceCascade.load("E:\\face\\src\\main\\resources\\haarcascade_frontalface_default.xml"); // Replace with the actual path

        // Open the default camera (usually camera index 0)
        VideoCapture videoCapture = new VideoCapture(0);

        if (!videoCapture.isOpened()) {
            System.out.println("Error: Camera not found.");
            return;
        }

        // Initialize LBPH face recognizer
        LBPHFaceRecognizer faceRecognizer = LBPHFaceRecognizer.create();
        faceRecognizer.read("E:\\face\\src\\main\\resources\\trained_model.xml"); // Replace with the actual path

        JFrame frame = new JFrame("Camera Face Recognition");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        while (true) {
            Mat frameMat = new Mat();
            videoCapture.read(frameMat);

            // Detect faces in the current frame
            MatOfRect faceDetections = new MatOfRect();
            faceCascade.detectMultiScale(frameMat, faceDetections);

            // Draw rectangles around the detected faces
            for (Rect rect : faceDetections.toArray()) {
                Mat faceROI = new Mat(frameMat, rect);
                Imgproc.rectangle(frameMat, rect.tl(), rect.br(), new Scalar(0, 255, 0), 3);

                // Convert faceROI to grayscale for recognition
                Mat grayFace = new Mat();
                Imgproc.cvtColor(faceROI, grayFace, Imgproc.COLOR_BGR2GRAY);
                Imgproc.resize(grayFace, grayFace, new Size(100, 100));

                // Recognize the face
                int[] label = new int[1];
                double[] confidence = new double[1];
                faceRecognizer.predict(grayFace, label, confidence);

                // Display the label (person ID) on the frame
                String labelStr = "Person ID: " + label[0];
                Imgproc.putText(frameMat, labelStr, rect.tl(), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, new Scalar(255, 255, 255), 2);
            }

            // Display the current frame with face recognition
            showResult(frameMat, frame);

            // Sleep for a short duration to control the frame rate
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void showResult(Mat image, JFrame frame) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".png", image, matOfByte);
        byte[] byteArray = matOfByte.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame.getContentPane().removeAll();
        frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
        frame.pack();
        frame.setVisible(true);
    }
}
