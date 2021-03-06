package com.chenzp.moneyking;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chenzp.moneyking.db.DBHelper;
import com.chenzp.moneyking.model.User;

/**
 * Created by CimZzz on 2018/4/25.<br>
 * Description:<br>
 */
public class LoginActivity extends Activity {

    private EditText userName;
    private EditText userPwd;
    private Button loginBtn;
    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userName = (EditText) findViewById(R.id.userName);
        userPwd = (EditText) findViewById(R.id.userPwd);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        registerBtn = (Button) findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBHelper helper = new DBHelper(LoginActivity.this);

                String userNameTxt = userName.getText().toString();
                String userPwdTxt = userPwd.getText().toString();

                if(TextUtils.isEmpty(userNameTxt)) {
                    Toast.makeText(LoginActivity.this,"用户名不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(userPwdTxt)) {
                    Toast.makeText(LoginActivity.this,"密码不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }

                User user = helper.findUserBy(userNameTxt,userPwdTxt);
                if(user != null) {
                    Intent intent = new Intent(LoginActivity.this,MainGameActivity.class);
                    intent.putExtra("user",user);
                    startActivity(intent);
                    Toast.makeText(LoginActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(LoginActivity.this,"用户名或密码出错",Toast.LENGTH_SHORT).show();
                }

                helper.close();
            }
        });
    }
}
