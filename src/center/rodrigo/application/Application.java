package center.rodrigo.application;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import org.openni.Device;
import org.openni.DeviceInfo;
import org.openni.OpenNI;
import org.openni.SensorType;
import org.openni.VideoMode;
import org.openni.VideoStream;

import center.rodrigo.core.Core;

import com.primesense.nite.NiTE;
import com.primesense.nite.UserTracker;

public class Application {

    private JFrame frame;
    private Device device;
    private UserTracker tracker;
    private Core core;
    private VideoStream meuVideoStream;
    private SensorType tipo = SensorType.COLOR;
    private boolean emExecucao = true;
    private List<DeviceInfo> listaDevices;
    private List<VideoMode> listaVideoSuportado;

    public Application() {

        if (!verificarKinect())
            System.exit(0);

        carregarKinect(1);
        frame = new JFrame("Particula Kinect");
        frame.add(core);
        frame.setSize(640, 480);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void run() {
        while (emExecucao) {
            /*
             * precisamos deste loop infinito. Caso contrario o metodo main
             * acaba e o programa encerra
             */
        }
    }

    public boolean verificarKinect() {

        OpenNI.initialize();
        listaDevices = OpenNI.enumerateDevices();

        if (listaDevices.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Kinect Não Encontrado !", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public void carregarKinect(int i) {
        NiTE.initialize();
        device = Device.open(listaDevices.get(0).getUri());
        meuVideoStream = VideoStream.create(device, tipo);
        listaVideoSuportado = meuVideoStream.getSensorInfo().getSupportedVideoModes();
        meuVideoStream.setVideoMode(listaVideoSuportado.get(i));
        meuVideoStream.start();
        tracker = UserTracker.create();
        core = new Core(meuVideoStream, tracker);
    }
}
