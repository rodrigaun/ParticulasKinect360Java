package center.rodrigo.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import org.openni.VideoFrameRef;
import org.openni.VideoStream;

import center.rodrigo.model.Particula;

import com.primesense.nite.JointType;
import com.primesense.nite.Point2D;
import com.primesense.nite.SkeletonJoint;
import com.primesense.nite.SkeletonState;
import com.primesense.nite.UserData;
import com.primesense.nite.UserTracker;
import com.primesense.nite.UserTrackerFrameRef;

public class Core extends Component implements VideoStream.NewFrameListener, UserTracker.NewFrameListener {

    private ArrayList<Particula> arrayParticulaDireita = new ArrayList<Particula>();
    private ArrayList<Particula> arrayParticulaEsquerda = new ArrayList<Particula>();
    private final int qtdParticulas = 15000;

    private UserTracker userTracker;
    private UserTrackerFrameRef lastTrackerFrame;

    private VideoStream videoStream;
    private VideoFrameRef lastVideoFrame;
    private BufferedImage bufferedImage;
    private int[] imagePixels;

    public Core(VideoStream videoStream, UserTracker tracker) {
        this.videoStream = videoStream;
        this.userTracker = tracker;
        this.videoStream.addNewFrameListener(this);
        this.userTracker.addNewFrameListener(this);

        for (int i = 0; i < qtdParticulas; i++) {
            arrayParticulaDireita.add(new Particula(Color.yellow));
            arrayParticulaEsquerda.add(new Particula(Color.red));
        }
    }

    public synchronized void paint(Graphics g) {

        if (lastVideoFrame == null)
            return;

        if (bufferedImage == null)
            bufferedImage = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);

        bufferedImage.setRGB(0, 0, 640, 480, imagePixels, 0, 640);
        g.drawImage(bufferedImage, 0, 0, null);

        /* Atualiza partucula e desenha */
        for (int i = 0; i < arrayParticulaDireita.size(); i++) {
            arrayParticulaDireita.get(i).update();
            arrayParticulaDireita.get(i).render(g);

            arrayParticulaEsquerda.get(i).update();
            arrayParticulaEsquerda.get(i).render(g);
        }
    }

    private void executa() {

        for (UserData user : lastTrackerFrame.getUsers()) {

            if (user.getSkeleton().getState() == SkeletonState.TRACKED) {

                SkeletonJoint sjMaoDireita = user.getSkeleton().getJoint(JointType.RIGHT_HAND);
                SkeletonJoint sjMaoEsquerda = user.getSkeleton().getJoint(JointType.LEFT_HAND);

                if (sjMaoDireita.getPositionConfidence() == 0.0 || sjMaoEsquerda.getPositionConfidence() == 0.0)
                    return;

                Point2D<Float> pontoMaoDireita = userTracker.convertJointCoordinatesToDepth(sjMaoDireita.getPosition());
                Point2D<Float> pontoMaoEsquerda = userTracker.convertJointCoordinatesToDepth(sjMaoEsquerda.getPosition());

                /* RESET */
                for (int i = 0; i < qtdParticulas; i++) {
                    if (!arrayParticulaDireita.get(i).isLife()) {
                        arrayParticulaDireita.get(i).reset(pontoMaoDireita.getX().intValue() + ((int) ((Math.random() * 31) - 15)),
                                pontoMaoDireita.getY().intValue() + ((int) ((Math.random() * 31) - 15)));
                    }
                    if (!arrayParticulaEsquerda.get(i).isLife()) {
                        arrayParticulaEsquerda.get(i).reset(pontoMaoEsquerda.getX().intValue() + ((int) ((Math.random() * 41) - 20)),
                                pontoMaoEsquerda.getY().intValue() + ((int) ((Math.random() * 41) - 20)));
                    }
                }
            }
        }
    }

    @Override
    public void onNewFrame(UserTracker arg0) {
        if (lastTrackerFrame != null) {
            lastTrackerFrame.release();
            lastTrackerFrame = null;
        }

        lastTrackerFrame = userTracker.readFrame();

        for (UserData user : lastTrackerFrame.getUsers()) {
            if (user.isNew())
                userTracker.startSkeletonTracking(user.getId());
        }
        executa();
    }

    @Override
    public void onFrameReady(VideoStream arg0) {
        lastVideoFrame = videoStream.readFrame();
        ByteBuffer frameData = lastVideoFrame.getData().order(ByteOrder.LITTLE_ENDIAN);

        if (imagePixels == null || imagePixels.length < lastVideoFrame.getWidth() * lastVideoFrame.getHeight())
            imagePixels = new int[lastVideoFrame.getWidth() * lastVideoFrame.getHeight()];

        int pos = 0;
        while (frameData.remaining() > 0) {
            int red = (int) frameData.get() & 0xFF;
            int green = (int) frameData.get() & 0xFF;
            int blue = (int) frameData.get() & 0xFF;
            imagePixels[pos] = 0xFF000000 | (red << 16) | (green << 8) | blue;
            pos++;
        }
        repaint();
    }
}