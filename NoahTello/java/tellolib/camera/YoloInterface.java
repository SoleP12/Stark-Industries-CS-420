package tellolib.camera;

import ai.onnxruntime.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class YoloInterface {
    private OrtEnvironment env;
    private OrtSession session;

    public YoloInterface(String modelPath) throws OrtException, IOException {
        env = OrtEnvironment.getEnvironment();
        session = env.createSession(Files.readAllBytes(Paths.get(modelPath)), new OrtSession.SessionOptions());
    }

    public float[][][] detect(float[][][][] input) throws OrtException {
        OnnxTensor inputTensor = OnnxTensor.createTensor(env, input);
        OrtSession.Result output = session.run(Collections.singletonMap("images", inputTensor));
        float[][][] outputData = (float[][][]) output.get(0).getValue();
        inputTensor.close();
        output.close();
        return outputData;
    }

    public void close() throws OrtException {
        session.close();
        env.close();
    }
}
