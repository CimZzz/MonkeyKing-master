package com.chenzp.moneyking;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chenzp.moneyking.model.User;

import java.util.Locale;

/**
 * Created by CimZzz on 2018/4/26.<br>
 * Project Name : YIQIMMM<br>
 * Since : YIQIMMM_1.99<br>
 * Description:<br>
 */
public class UserInfoActivity extends Activity {


    private TextView userName;
    private TextView userScore;
    private Button quitBtn;

    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        userName = (TextView) findViewById(R.id.userName);
        userScore = (TextView) findViewById(R.id.userScore);
        quitBtn = (Button) findViewById(R.id.quitBtn);

        user = (User) getIntent().getSerializableExtra("user");

        userName.setText(user.getUserName());
        userScore.setText(String.format(Locale.CHINA,"%.1f(分)",user.getScore()));

        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(101);
                finish();
            }
        });
    }
}
