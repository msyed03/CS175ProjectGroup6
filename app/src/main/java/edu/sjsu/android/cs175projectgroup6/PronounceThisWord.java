package edu.sjsu.android.cs175projectgroup6;
import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import nl.dionsegijn.konfetti.core.Party;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import nl.dionsegijn.konfetti.core.Position;
import nl.dionsegijn.konfetti.core.Rotation;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
public class PronounceThisWord extends Fragment {
    private ImageView ball;
    private static String wordtopro;
    private Party party;
    private static int score=0;
    private static int count=0;
    private MediaRecorder mediaRecorder;
    private String filename="";
    private FloatingActionButton recordButton;
    private Button button;
    private boolean isRecording=false;
    private Context context;
    private TextView scoreview;
    private TextView textView;
    private ImageView wall;
    private ImageView wall2;
    private nl.dionsegijn.konfetti.xml.KonfettiView konfettiView;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    public PronounceThisWord() {}
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
    private static void generateNewWord(TextView textView) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL("https://random-word-api.herokuapp.com/word?number=1&lang=es");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    connection.disconnect();
                    return response.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(result);
                        String word = jsonArray.getString(0).trim();
                        Log.d("RandomWord", "Spanish word: " + word);
                        wordtopro=word;
                        textView.setText(wordtopro);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pronounce_this_word, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstances) {
        super.onViewCreated(view,savedInstances);
        recordButton=view.findViewById(R.id.speakButton);
        button=view.findViewById(R.id.button);
        textView=view.findViewById(R.id.speechStatus);
        ball = view.findViewById(R.id.ball);
        scoreview=view.findViewById(R.id.scoreview);
        recordButton.setOnClickListener(v->{
            if (isRecording)
                stopRecording();
            else
                startRecording();
        });
        wall=view.findViewById(R.id.wall);
        ball.setBackgroundResource(R.drawable.bird_flying);
        AnimationDrawable birdAnimation = (AnimationDrawable) ball.getBackground();
        birdAnimation.start();
        button.setOnClickListener(v->playBack());
        wall2=view.findViewById(R.id.wall2);
        scoreview.setText(String.valueOf(score));
        //generateNewWord(textView);
        textView.setText("gracias");
        konfettiView=view.findViewById(R.id.viewKonfetti);
        party =new Party(
                0,
                360,
                2f,
                4f,
                0.9f,
                Collections.singletonList(new Size(12, 5, 2)),
                Arrays.asList(0xFFFFC107, 0xFF4CAF50, 0xFF2196F3),
                Collections.singletonList(Shape.Circle.INSTANCE),
                3000L,
                true,
                new Position.Relative(0.5, 0.0), // spawn from top
                0,
                Rotation.Companion.enabled(),
                new Emitter(3, TimeUnit.SECONDS).perSecond(100)
        );
    }
    private void playBack() {
        if (filename != null) {
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(filename);
                mediaPlayer.prepare();
                mediaPlayer.start();
                Toast.makeText(context, "Playback started", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e("PlaybackError", "playback failed: " + e.getMessage());
            }
        } else {
            Toast.makeText(context, "No file to play", Toast.LENGTH_SHORT).show();
        }
    }
    private void startRecording() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1001);
            return;
        }
        File outputDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        filename = new File(outputDir, "recorded_audio.m4a").getAbsolutePath();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mediaRecorder=new MediaRecorder(context);
        }
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(filename);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording=true;
            count++;
            Toast.makeText(context,"Recording started",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("RecordingError", "prepare() failed");
        }
    }
    private void stopRecording() {
        if (mediaRecorder!=null){
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                isRecording=false;
                Toast.makeText(context,"Recording stopped",Toast.LENGTH_SHORT).show();
                executorService.execute(() -> {
                    sendPostRequest(filename);
                });
            } catch (Exception e) {
                Log.e("RecordingError","stop() failed");
            }
        }
    }
    private void sendPostRequest(String filename) {
        String response = "";
        try {
            File file = new File(filename);
            if (!file.exists()) {
                Log.e("TranscribeError", "File does not exist: " + filename);
                return;
            }
            HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8000/transcribe").openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
            OutputStream outputStream = connection.getOutputStream();
            String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
            outputStream.write(("--" + boundary + "\r\n").getBytes());
            outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n").getBytes());
            outputStream.write(("Content-Type: audio/m4a\r\n\r\n").getBytes());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                outputStream.write(fileToBytes(file));
            }
            outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());
            outputStream.flush();
            int responseCode = connection.getResponseCode();
            Log.i("TranscribeResponse", "Response Code: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                response = convertStreamToString(inputStream);
            } else {
                response = "Error: " + responseCode;
            }
            connection.disconnect();
        } catch (IOException e) {
            Log.e("TranscribeError", "IOException: " + e.getMessage());
        }
        if (!response.isEmpty()) {
            final String finalResponse = response;
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    JSONObject jsonObject;
                    Toast.makeText(context, finalResponse, Toast.LENGTH_LONG).show();
                    try {
                        jsonObject=new JSONObject(finalResponse);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        if (textView.getText().toString().equalsIgnoreCase(jsonObject.getString("text"))) {
                            Log.d("Server Response", "Values are equal!");
                            score++;
                            float holePositionX = ball.getX();
                            int screenWidth= Resources.getSystem().getDisplayMetrics().widthPixels;
                            float distanceX1 = -screenWidth;
                            float distanceY1 = 245f;
                            float diagonalOffsetY = 560f;
                            ObjectAnimator wall1X = ObjectAnimator.ofFloat(wall, "translationX", 0f, distanceX1);
                            ObjectAnimator wall1Y = ObjectAnimator.ofFloat(wall, "translationY", 0f, distanceY1);
                            ObjectAnimator wall2X = ObjectAnimator.ofFloat(wall2, "translationX", screenWidth, 0f);
                            ObjectAnimator wall2Y = ObjectAnimator.ofFloat(wall2, "translationY",  -diagonalOffsetY,-distanceY1);
                            AnimatorSet wallAnimSet = new AnimatorSet();
                            wallAnimSet.playTogether(wall1X, wall1Y, wall2X, wall2Y);
                            wallAnimSet.setDuration(2000);
                            wall1X.addUpdateListener(animation -> {
                                float holeX = (float) animation.getAnimatedValue()+screenWidth-500;
                                if (holePositionX >= holeX - 170 && holePositionX<= holeX+485) {
                                    ball.setVisibility(View.INVISIBLE);
                                } else {
                                    ball.setVisibility(View.VISIBLE);
                                }
                            });
                            wallAnimSet.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    try {
                                        konfettiView.start(party);
                                    } catch (Exception e) {
                                        Log.e("Konfetti", Objects.requireNonNull(e.getMessage()));
                                    }
                                }
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    // Reset both walls to vertical position 0
                                    wall.setTranslationY(0f);
                                    wall2.setTranslationY(0f);
                                    ball.setTranslationX(0f);
                                    ball.setTranslationY(0f);
                                    ball.setVisibility(View.VISIBLE);
                                    count=0;
                                    generateNewWord(textView);
                                }

                            });
                            wallAnimSet.start();
                            scoreview.setText(String.valueOf(score));
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    if (count>=3) {
                        count=0;
                        Toast.makeText(context,"Maximum attempts (3) reached for this word ("+wordtopro+"); generating new word",Toast.LENGTH_LONG).show();
                        generateNewWord(textView);
                        return;
                    }
                    Log.d("Server Response", finalResponse);
                });
            }
        } else {
            Log.e("TranscribeError", "No response received or response was empty");
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private byte[] fileToBytes(File file) throws IOException {
        byte[] fileBytes = new byte[(int) file.length()];
        InputStream inputStream = Files.newInputStream(file.toPath());
        inputStream.read(fileBytes);
        inputStream.close();
        return fileBytes;
    }
    private String convertStreamToString(InputStream is) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        int character;
        while ((character = is.read()) != -1) {
            stringBuilder.append((char) character);
        }
        return stringBuilder.toString();
    }
}