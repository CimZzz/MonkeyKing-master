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
 * Created by CimZzz on 2018/4/26.<br>
 * Project Name : YIQIMMM<br>
 * Since : YIQIMMM_1.99<br>
 * Description:<br>
 */
public class RegisterActivity extends Activity {

    private EditText userName;
    private EditText userPwd;
    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userName = (EditText) findViewById(R.id.userName);
        userPwd = (EditText) findViewById(R.id.userPwd);
        registerBtn = (Button) findViewById(R.id.registerBtn);


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBHelper helper = new DBHelper(RegisterActivity.this);

                String userNameTxt = userName.getText().toString();
                String userPwdTxt = userPwd.getText().toString();


                if(TextUtils.isEmpty(userNameTxt)) {
                    Toast.makeText(RegisterActivity.this,"用户名不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(userPwdTxt)) {
                    Toast.makeText(RegisterActivity.this,"密码不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }

                User user = new User();
                user.setUserName(userNameTxt);
                user.setUserPwd(userPwdTxt);


                if(helper.insertUser(user)) {
                    Intent intent = new Intent(RegisterActivity.this,MainGameActivity.class);
                    intent.putExtra("user",user);
                    startActivity(intent);
                    Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(RegisterActivity.this,"用户名或密码出错",Toast.LENGTH_SHORT).show();
                }

                helper.close();
            }
        });
    }
}
