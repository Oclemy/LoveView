package info.camposha.loves;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    LoveView firstLoveView, secondLoveView, thirdLoveView;
    TextView firstLoveTxt, secondLoveTxt, thirdLoveTxt;

    private void initializeWidgets(){
        firstLoveView = findViewById(R.id.firstLoveView);
        secondLoveView = findViewById(R.id.secondLoveView);
        thirdLoveView = findViewById(R.id.thirdLoveView);
        firstLoveView.setLove();
        firstLoveTxt = findViewById(R.id.firstLoveTxt);
        secondLoveTxt = findViewById(R.id.secondLoveTxt);
        thirdLoveTxt = findViewById(R.id.thirdLoveTxt);

        thirdLoveView.setUnLikeType(LoveView.LoveType.broken);
        thirdLoveView.setCracksColor(Color.WHITE);
        thirdLoveView.setFillColor(Color.rgb(11, 200, 77));
        thirdLoveView.setEdgeColor(Color.rgb(33, 3, 219));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.initializeWidgets();

        thirdLoveView.setOnThumbUp(new LoveView.OnLove() {
            @Override
            public void Love(boolean trueLove) {
                if (trueLove) {
                    thirdLoveTxt.setText(String.valueOf(Integer.valueOf(thirdLoveTxt.getText().toString()) + 1));
                } else {
                    thirdLoveTxt.setText(String.valueOf(Integer.valueOf(thirdLoveTxt.getText().toString()) - 1));
                }
            }
        });

        secondLoveView.setOnThumbUp(new LoveView.OnLove() {
            @Override
            public void Love(boolean trueLove) {
                if (trueLove) {
                    secondLoveTxt.setText(String.valueOf(Integer.valueOf(secondLoveTxt.getText().toString()) + 1));
                } else {
                    secondLoveTxt.setText(String.valueOf(Integer.valueOf(secondLoveTxt.getText().toString()) - 1));
                }
            }
        });
        firstLoveView.setOnThumbUp(new LoveView.OnLove() {
            @Override
            public void Love(boolean trueLove) {
                if (trueLove) {
                    firstLoveTxt.setText(String.valueOf(Integer.valueOf(firstLoveTxt.getText().toString()) + 1));
                } else {
                    firstLoveTxt.setText(String.valueOf(Integer.valueOf(firstLoveTxt.getText().toString()) - 1));
                }
            }
        });

    }

    public void loveEveryone(View v) {
        firstLoveView.love();
        secondLoveView.love();
        thirdLoveView.love();
    }

    public void unLoveEveryone(View v) {
        firstLoveView.unLove();
        secondLoveView.unLove();
        thirdLoveView.unLove();
    }
}
