package com.chenzp.moneyking;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;


import org.cocos2d.actions.base.CCRepeatForever;
import org.cocos2d.actions.ease.CCEaseIn;
import org.cocos2d.actions.instant.CCCallFuncN;
import org.cocos2d.actions.interval.CCAnimate;
import org.cocos2d.actions.interval.CCBezierTo;
import org.cocos2d.actions.interval.CCFadeIn;
import org.cocos2d.actions.interval.CCFadeOut;
import org.cocos2d.actions.interval.CCMoveBy;
import org.cocos2d.actions.interval.CCMoveTo;
import org.cocos2d.actions.interval.CCSequence;
import org.cocos2d.actions.interval.CCSpawn;
import org.cocos2d.actions.tile.CCShatteredTiles3D;
import org.cocos2d.layers.CCColorLayer;
import org.cocos2d.layers.CCLayer;
import org.cocos2d.layers.CCMultiplexLayer;
import org.cocos2d.layers.CCScene;
import org.cocos2d.menus.CCMenu;
import org.cocos2d.menus.CCMenuItemSprite;
import org.cocos2d.nodes.CCAnimation;
import org.cocos2d.nodes.CCDirector;
import org.cocos2d.nodes.CCNode;
import org.cocos2d.nodes.CCSprite;
import org.cocos2d.nodes.CCSpriteFrame;
import org.cocos2d.nodes.CCSpriteFrameCache;
import org.cocos2d.nodes.CCSpriteSheet;
import org.cocos2d.nodes.CCTextureCache;
import org.cocos2d.opengl.CCBitmapFontAtlas;
import org.cocos2d.opengl.CCGLSurfaceView;
import org.cocos2d.particlesystem.CCParticleFlower;
import org.cocos2d.particlesystem.CCParticleSystem;
import org.cocos2d.sound.SoundEngine;
import org.cocos2d.transitions.*;
import org.cocos2d.types.CCBezierConfig;
import org.cocos2d.types.CGPoint;
import org.cocos2d.types.CGRect;
import org.cocos2d.types.CGSize;
import org.cocos2d.types.ccColor3B;
import org.cocos2d.types.ccColor4B;
import org.cocos2d.types.ccGridSize;
import org.cocos2d.utils.CCFormatter;


import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import com.chenzp.moneyking.db.DBHelper;
import com.chenzp.moneyking.model.User;

public class MainGameActivity extends Activity {

	protected CCGLSurfaceView _glSurfaceView;
	
	CCScene scene;

	private User user;
	/**
	 * �洢������sharedPreference�ı�ʶ
	 */
	public static final String SCORETAG = "com.chenzp.moneyking.score";
	
	/**
	 * �洢ǩ��ʱ���sharedPreference�ı�ʶ
	 */
	public static final String LASTSIGN = "com.chenzp.moneyking.signTime";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		
		// ����ȫ����������
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// ���ø�Activity��View
		_glSurfaceView = new CCGLSurfaceView(this);
		setContentView(_glSurfaceView);

		user = (User) getIntent().getSerializableExtra("user");
		
	
	}


	@Override
	protected void onStart() {

		super.onStart();
		Log.d("TEST", "����start����");
		
		CCDirector.sharedDirector().attachInView(_glSurfaceView);

		CCDirector.sharedDirector().setDeviceOrientation(CCDirector.kCCDeviceOrientationLandscapeLeft);
		
		CCDirector.sharedDirector().setAnimationInterval(1.0f / 60.0f);

		scene = GameLayer.scene(user,this);
		
		CCDirector.sharedDirector().runWithScene(scene);
	
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Log.d("TEST", "����pause����");
		CCDirector.sharedDirector().pause();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		Log.d("TEST", "����resume����");
		CCDirector.sharedDirector().resume();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		Log.d("TEST", "����stop����");
		CCDirector.sharedDirector().end();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		// �����˳���
		case KeyEvent.KEYCODE_BACK:
			return true;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 100) {
			if(resultCode == 101) {
				finish();
				startActivity(new Intent(this,LoginActivity.class));
			}
		}
	}

	/**
	 * �ڲ��࣬��ʾ��Ϸ������
	 * @author ����
	 *
	 */
	public static class GameLayer extends CCColorLayer {

		private CCSprite ground; // ���澫��
		
		private CCSprite monkey; // ���Ӿ���
		
		private CCSprite cloud; // �ƾ���

		private CCRepeatForever monkeryForever; // ����һֱ�ɵ�action
		
	    private CCRepeatForever repeatForever; // �����ƶ���action
		
		private CCSpriteSheet spriteSheet; // ��������
		
		private List<CCSprite> visibleBads;   // �ɼ������֣�������ײ���
		
		private boolean gameOver = false;    // ��Ϸ�Ƿ�������涨δ��ʼʱΪfalse.
		
		private boolean isOn = false;   // ��Ϸ�Ƿ��ڽ�����

		private float time = 0; // ����������ʱ�䣬Ҳ���Ƿ���
		
		private CCEaseIn fall; // ���Ӻ������½��Ķ���(�������)
		
		private CCMoveBy sharpFall;  // ���Ӻ������½��Ķ���(��ײ��ļ����½���
		
		private long firstTime = 0; // ����ʵ�������������˳�

		private User user;
		
		/**
		 * ȫ�ֱ���������layer��������
		 */
		private int layIndex;
		
		/**
		 * �಼����
		 */
		CCMultiplexLayer multiplexLayer;
		
		
		/**
		 * ��Ļ��С
		 */
		public final static CGSize WINSIZE = CCDirector.sharedDirector().displaySize();

		/**
		 * ��Ļ�е�λ��
		 */
		private final CGPoint MPOINT;
		
		/**
		 * ��ǰActivity
		 */
		public static Activity app;
		
		/**
		 * ���һ�κ��������ĸ߶�
		 */
		public int RISE;
		
		/**
		 * ��ΪCCSpriteSheet spriteSheet��Tag��ʶ
		 */
		public static final int SHEETTAG = 1;

		/**
		 * ��Ϊ���ӵ�Tag��ʶ
		 */
		public static final int MONKEYTAG = 2;

		/**
		 * ��Ϊʱ���ǩ��Tag��ʶ
		 */
		public static final int TIMETAG = 3;
		
		/**
		 * ��Ϊ�����˵���Tag��ʶ
		 */
		public static final int POPTAG = 4;
		
		/**
		 * ��Ϊ������ǩ��Tag��ʶ
		 */
		public static final int SCORE = 5;
		
		/**
		 * ��Ϊ��߷ֵ�Tag��ʶ
		 */
		public static final int BEST = 6;
		
		
		/**
		 * ��Ϊ�಼������ķ���Tag��ʶ
		 */
		
		public static final int BACKTAG = 7;
		
		/**
		 * ��Ϊ�಼��������л��˵�Tag��ʶ
		 */
		public static final int SWITCHTAG = 88;
		
		/**
		 * �ñ������ڵ�����Ϸ�Ѷ�
		 */
		private float K = 6.0f;  
		
		/**
		 * ��Ϸ�Ѷ������ʣ���Ϊ��10�뵽110����� 6 -> 2.
		 */
		public static final float RATE = 0.04f;
		
		/**
		 * ������Ӧ�ֱ���<br/>
		 * X_K��Y_K�������Ǻ����ǣ�180*145�����������õ���ĻΪ1280*720
		 */
		public static final float X_K = 180.0f / 1280.0f;
		public static final float Y_K = 145.0f / 720.0f;
		
		/**
		 * �������õ���ĻΪ1280*720
		 */
		public static final float WELL_X = 1280.0f;
		public static final float WELL_Y = 720.f;
		
		/**
		 * ��ʶ�Ƿ������ѵĵĵȼ���
		 */
		private boolean HARD = false;
		
		
		CCMenuItemSprite bigP;
		CCMenuItemSprite smaP;
		
		CCParticleSystem emitter;
		
		int smallNum;
		int bigNum;
		
		/**
		 * ��ʶ�Ƿ��ڴ���״̬
		 */
		boolean isInBig = false;
		/**
		 * ��̬����������GameLayer�ĳ���(Scene)
		 * 
		 * @return
		 */
		public static CCScene scene(User user,Activity app) {
			CCScene scene = CCScene.node();
			GameLayer.app = app;
			GameLayer layer = new GameLayer(ccColor4B.ccc4(139, 131, 134,
					255));
			layer.user = user;
			layer.smallNum = MonkeyUtil.getDataFromShared(LASTSIGN, "SMALLNUM", app);
			layer.bigNum = MonkeyUtil.getDataFromShared(LASTSIGN, "BIGNUM", app);
			scene.addChild(layer);
			return scene;
		}

		protected GameLayer(ccColor4B color) {
			super(color);

			// ���ø�Layer�Ĵ����¼���Ч
			this.setIsTouchEnabled(true);

			// ��ʼ������
			
			MPOINT = CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 2);
			visibleBads = new ArrayList<CCSprite>();
			
			
			// ������������Ļ��
			float fallDuration = 2.0f;
			// �½�
			CCMoveBy fallMoveBy = CCMoveBy.action(fallDuration, CGPoint.ccp(0, - WINSIZE.height));
			fall = CCEaseIn.action(fallMoveBy, 2.0f);
			sharpFall = CCMoveBy.action(1.0f, CGPoint.ccp(0, -WINSIZE.height));
			RISE = (int) (60 * WINSIZE.height / WELL_Y);

			//Ԥ������Ƶ
			SoundEngine.sharedEngine().preloadEffect(app, R.raw.click);
			SoundEngine.sharedEngine().preloadEffect(app, R.raw.bang);
			

			Log.d("TEST", "��Ļ��С"+WINSIZE.width+" -- "+WINSIZE.height);
			// ��Ϸ����
		/*	CCSprite bg = CCSprite.sprite("bg.png");
			
			bg.setScale(WINSIZE.height / WELL_Y);
			
			bg.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 5 + (WINSIZE.height / WELL_Y) * bg.getContentSize().height / 2));
			addChild(bg);*/
			
			// ��Ϸ����,ռ��Ļ 1/5��.��Ļ���ܸ���1000.
			ground = CCSprite.sprite("ground.png");
			//Log.d("TEST", "�ɵĵ���" + ground.getContentSize().height);
			   // ���С��������
			ground.setScale(WINSIZE.height / WELL_Y);
		//	Log.d("TEST", "�µĵ���" + ground.getContentSize().height);
			ground.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 5 - (WINSIZE.height / WELL_Y) * ground.getContentSize().height / 2));
			addChild(ground,2);
			
			// �����ƶ�Ч��
			float length = 50.0f * (WINSIZE.height / WELL_Y);
			CCMoveBy moveBy1 = CCMoveBy.action(0.2f, CGPoint.ccp(-length, 0));
			CCMoveBy moveBy2 = CCMoveBy.action(0, CGPoint.ccp(length , 0));
			repeatForever = CCRepeatForever.action(CCSequence.actions(moveBy1, moveBy2));
			ground.runAction(repeatForever);
			
			// �����Ǻ��Ӷ�����ʵ��
			// 1, ����֡����,ͨ������.plist�ļ�
			CCSpriteFrameCache.sharedSpriteFrameCache().addSpriteFrames(
					"monkey_packer.plist");
			// 2, ��ʼ���������飬ͨ������.png�ļ� (�°��CCSpriteSheet��CCSpriteBatchNode)
			spriteSheet = CCSpriteSheet
					.spriteSheet("monkey_packer.png");
			
			//spriteSheet.setScale(WINSIZE.height / WELL_Y);
			// 3. ��������������Ϊ��������ӽڵ�
			addChild(spriteSheet, 1, SHEETTAG);

			monkey = CCSprite.sprite("monkey1.png", true);
			monkey.setScale(WINSIZE.height / WELL_Y);
			monkey.setPosition(MPOINT);
			
			spriteSheet.addChild(monkey, 1, MONKEYTAG);
			
			
			

			// ֡����
			ArrayList<CCSpriteFrame> frames = new ArrayList<CCSpriteFrame>();
			for (int i = 1; i < 5; i++) {
				String pngName = "monkey" + i + ".png";
				CCSpriteFrame frame = CCSpriteFrameCache
						.sharedSpriteFrameCache().spriteFrameByName(pngName);
				frames.add(frame);
			}

			CCAnimation animation = CCAnimation
					.animation("monkey", 0.1f, frames);
			CCAnimate animate = CCAnimate.action(animation, false);
			monkeryForever = CCRepeatForever.action(animate);

			monkey.runAction(monkeryForever);
			
			
			// ����
			cloud = CCSprite.sprite("cloud.png", true);
			float gap = 70.0f * (WINSIZE.height / WELL_Y);
			cloud.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 2 - gap));
			cloud.setScale(WINSIZE.height / WELL_Y);
			addChild(cloud);
			
			// ��ʼ����Ļ�˵�
		    CCSprite aboutSprite = CCSprite.sprite("about.png", true);
		    aboutSprite.setScale(WINSIZE.height / WELL_Y);
		    CCMenuItemSprite aboutItem = CCMenuItemSprite.item(aboutSprite, aboutSprite, this, "toAbout");
		    aboutItem.setScale(WINSIZE.height / WELL_Y);
		    
		    CCSprite shareSprite = CCSprite.sprite("share.png", true);
		    shareSprite.setScale(WINSIZE.height / WELL_Y);
		    CCMenuItemSprite shareItem = CCMenuItemSprite.item(shareSprite, shareSprite, this, "toShare");
		    shareItem.setScale(WINSIZE.height / WELL_Y);
		    
		    CCSprite exitSprite = CCSprite.sprite("exit.png", true);
		    exitSprite.setScale(WINSIZE.height / WELL_Y);
		    CCMenuItemSprite exitItem = CCMenuItemSprite.item(exitSprite, exitSprite, this, "toExit");
		    exitItem.setScale(WINSIZE.height / WELL_Y);
		    
		    // �����µ���Ļ�˵���(����),�ƻ�ʹ��scene���л�
			CCSprite toolSprite = CCSprite.sprite("tool.png",true);
			toolSprite.setScale(WINSIZE.height / WELL_Y);
			CCMenuItemSprite toolItem = CCMenuItemSprite.item(toolSprite, toolSprite, this, "toTool");
			toolItem.setScale(WINSIZE.height / WELL_Y);

			CCSprite userInfoSprite = CCSprite.sprite(BitmapFactory.decodeResource(app.getResources(),R.drawable.userinfo),"userinfo.png");
			userInfoSprite.setScale(WINSIZE.height / WELL_Y / 2.5f);
			CCMenuItemSprite userInfoItem = CCMenuItemSprite.item(userInfoSprite, userInfoSprite, this, "toUserInfo");
			userInfoItem.setScale(WINSIZE.height / WELL_Y/ 2.5f);

			CCSprite webViewSprite = CCSprite.sprite(BitmapFactory.decodeResource(app.getResources(),R.drawable.web),"web.png");
			webViewSprite.setScale(WINSIZE.height / WELL_Y / 3);
			CCMenuItemSprite webViewItem = CCMenuItemSprite.item(webViewSprite, webViewSprite, this, "toWebView");
			webViewItem.setScale(WINSIZE.height / WELL_Y/ 3);
            
		    
		    
		    CCMenu screenMenu = CCMenu.menu(aboutItem,shareItem,exitItem,toolItem,userInfoItem,webViewItem);
		    screenMenu.alignItemsHorizontally(80 * WINSIZE.width / WELL_X);
		    screenMenu.setPosition(WINSIZE.width / 2 ,60 * WINSIZE.height / WELL_Y);
		    addChild(screenMenu,3);
		    
		    // ��ʾʱ�䣬Ҳ���Ƿ���
		    CCBitmapFontAtlas timeLabel = CCBitmapFontAtlas.bitmapFontAtlas("0.0", "bitmapFontTest.fnt");
		    timeLabel.setColor(ccColor3B.ccc3(110, 110, 110));
		    addChild(timeLabel,5,TIMETAG);
		    timeLabel.setScale(1.5f * WINSIZE.height / WELL_Y);
		    
		    timeLabel.setPosition(CGPoint.ccp(150 * WINSIZE.height / WELL_Y, WINSIZE.height - 100 * WINSIZE.height / WELL_Y));
		    
		    // �����˵�,��ʼ������
		    CCSprite popMenu = CCSprite.sprite("pop_menu.png",true);
		    popMenu.setPosition(CGPoint.ccp(WINSIZE.width / 2, -150));
		    
		    // ���¿�ʼ�˵���
		  /*  CCSprite restartSprite = CCSprite.sprite("start.png", true);*/
		   // ע�ʹ��������� plist�ļ����صģ�������Ϊ�˲����µ�ͼƬ��ɫ
		    CCSprite restartSprite = CCSprite.sprite("start.png",true);
		    restartSprite.setScale(WINSIZE.height / WELL_Y);
		    CCMenuItemSprite restartItem = CCMenuItemSprite.item(restartSprite, restartSprite, this, "restart");
		    restartItem.setScale(WINSIZE.height / WELL_Y);
		    
		    CCMenu reStartMenu = CCMenu.menu(restartItem);
		    popMenu.addChild(reStartMenu);
		    // �������¿�ʼ��ť��λ��
		    reStartMenu.setPosition(reStartMenu.getParent().getContentSize().width /2,
		    		reStartMenu.getParent().getContentSize().height /2);
		    
		    // ��������߷ֵı�ǩ
		    CCBitmapFontAtlas scoreLabel = CCBitmapFontAtlas.bitmapFontAtlas("0.0", "bitmapFontTest.fnt");
		    popMenu.addChild(scoreLabel,1,SCORE);
		    scoreLabel.setPosition(462, scoreLabel.getParent().getContentSize().height - 94); // �˴ν�λ��д���ˡ���ΪͼƬ��ϢΨһ����֪��
		    
		    CCBitmapFontAtlas bestLabel = CCBitmapFontAtlas.bitmapFontAtlas("0.0", "bitmapFontTest.fnt");
		    popMenu.addChild(bestLabel,1,BEST);
		    bestLabel.setPosition(462, bestLabel.getParent().getContentSize().height - 202);
		    
		    addChild(popMenu,1,POPTAG);
		    
		    
		    
		    // �����������ӣ��������⼼��
		    
		    CCSprite colorPeach = CCSprite.sprite("color_peach.png",true);
		    bigP = CCMenuItemSprite.item(colorPeach, colorPeach, this, "bigKongFu");
		    
		    CCSprite colorPeach2 = CCSprite.sprite("color_peach.png",true);
		    smaP = CCMenuItemSprite.item(colorPeach2, colorPeach2, this, "smallKongFu");
		     bigP.setScale(1.8f * WINSIZE.height / WELL_Y);
		     smaP.setScale(1.2f * WINSIZE.height / WELL_Y);
		   showPeach();
		     
		     
		    CCMenu skillMenu = CCMenu.menu(bigP,smaP);
		    skillMenu.alignItemsHorizontally();
		    skillMenu.setPosition(WINSIZE.width - bigP.getContentSize().width - 100, WINSIZE.height - bigP.getContentSize().height);
		    addChild(skillMenu,5);
		    
		    // ���Դ���
			 emitter = CCParticleFlower.node(500);
				emitter.setTexture(CCTextureCache.sharedTextureCache().addImage("stars_grayscale.png"));
	    		emitter.setLifeVar(0);
	    		emitter.setLife(5);
	    		emitter.setSpeed(100);
	    		emitter.setSpeedVar(0);
	    		emitter.setEmissionRate(10000);
			 
			    addChild(emitter,5);
			 
	    		emitter.setPosition(WINSIZE.width / 2 , WINSIZE.height / 2);
             /* CCMoveBy emittMoveBy = CCMoveBy.action(2.0f, CGPoint.ccp(200, 0));
              CCMoveBy emittMoveBy2 = emittMoveBy.reverse();
              CCSequence emitteSequence = CCSequence.actions(emittMoveBy, emittMoveBy2);
              emitter.runAction(CCRepeatForever.action(emitteSequence));*/
              
	    		
	    	//	setEmitterPosition();
	    		
	    		emitter.setVisible(false);
		    // ���Խ���
		    schedule("createBad", 1.5f); // �������ֵĵ���
		    
		    schedule("check");  // �����ײ�ĵ���
		    
		    
			
		}
	
		// �ж��Ƿ���Ҫ�������ӵ�͸����
		private void showPeach()
		{
			if(bigNum <= 0)
			    {
				   Log.d("TEST", "�����ӡ�0");
			    	 bigP.setOpacity(50);
			    }
			
			if(smallNum <= 0)
			    {
				     Log.d("TEST", "С����<0");
			    	 smaP.setOpacity(50);
			    }
		
		}
		
		//��Ļ�˵��¼�����Ӧ
		
		// 1. ���ڲ˵�
		public void toAbout(Object sender){
			Log.d("TEXT", "���ڲ˵�����");
			Uri webpage = Uri.parse("https://github.com/ChenZhongPu/MonkeyKing");
			Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
			app.startActivity(webIntent);
			
		}
		// 2. ����˵�
		public void toShare(Object sender){
			
		      Intent intent=new Intent(Intent.ACTION_SEND);
		      
		      intent.setType("text/plain");
		      intent.putExtra(Intent.EXTRA_SUBJECT, "share");
		      intent.putExtra(Intent.EXTRA_TEXT, "#Monkey King# ������һ��������Ϸ---Monkey King,С����ǿ����ذ�! ");
		      app.startActivity(Intent.createChooser(intent, "SHARE MONKEY KING"));
		      
		}
		
		// 3. �뿪�˵�
		public void toExit(Object sender){
		
			long secondTime = System.currentTimeMillis();
			
			if(secondTime - firstTime > 2000)
			{
				
				CCBitmapFontAtlas tipAtlas = CCBitmapFontAtlas.bitmapFontAtlas("Click one more to leave","bitmapFontTest.fnt");
				tipAtlas.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 5 + 20));
				addChild(tipAtlas);
			    CCFadeIn fadeIn = CCFadeIn.action(1.5f);
			    CCFadeOut fadeOut = fadeIn.reverse();
			    tipAtlas.runAction(CCSequence.actions(fadeIn, fadeOut));
				firstTime = secondTime;
			}
			else {
				System.exit(0);
			}
			
		}
		
		// 4. ���¿�ʼ�˵�
		public void restart(Object sender){
			Log.d("TEST", "���¿�ʼ");
			 
			// ������Ϸ�Ѷ�
			K = 6;
			
			// ������Ϊ0
			time = 0;
			CCBitmapFontAtlas label =
					(CCBitmapFontAtlas) getChildByTag(TIMETAG);
			label.setString("0.00");
			
			// �����Ļ�������
			spriteSheet.removeAllChildren(false);
			
			// �����˵��ص���Ļ֮�⣨��������
			CCSprite popSprite =
					(CCSprite) getChildByTag(POPTAG);
			popSprite.setPosition(CGPoint.ccp(WINSIZE.width / 2, -150));
			
			// �Ѻ��Ӻ��������·�����Ļ�м�
			monkey.setPosition(MPOINT);
			cloud.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 2 - 70));
			float gap = 70.0f * (WINSIZE.height / WELL_Y);
			cloud.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 2 - gap));
			
			spriteSheet.addChild(monkey, 1, MONKEYTAG);
			addChild(cloud);
			
			monkey.runAction(fall.copy());
			cloud.runAction(fall.copy());
			monkey.runAction(monkeryForever.copy());
			
			ground.setPosition(CGPoint.ccp(WINSIZE.width / 2, WINSIZE.height / 5 - (WINSIZE.height / WELL_Y) * ground.getContentSize().height / 2));
			ground.runAction(repeatForever);
		
			
			
			// �������ֵĵ���
			schedule("createBad", 1.5f);
			
			// ������Ϸ״̬
			isOn = true;
			gameOver = false;
			
		}
		
		// 5. ���߲˵�
		public void toTool(Object sender){
			
			Log.d("TEST", "toTool..");
			CCDirector.sharedDirector().replaceScene(CCSlideInBTransition.transition(0.1f, getToolScene()));	
		}

		public void toUserInfo(Object sender) {
			Intent intent  = new Intent(app,UserInfoActivity.class);
			intent.putExtra("user",user);
			app.startActivityForResult(intent,100);
		}

		public void toWebView(Object sender) {
			Log.d("TEST", "toWebView..");
			app.startActivity(new Intent(app,WebActivity.class));
		}
		
		
		// С�ż��ܣ�ɱ����Ļ����������
		@SuppressWarnings("unchecked")
		public void smallKongFu(Object sender)
		{
			Log.d("TEST", "�ɼ������� ---->"+ visibleBads.size());
			
			if(!isOn) return;
			
			if(smallNum <= 0)
				return;
			
			smallNum--;
			showPeach();
			
			// �첽�������Ӻͽ��
			int money = MonkeyUtil.getDataFromShared(LASTSIGN, "MONEY", app);
			Log.d("TEST", "Ǯ"+money);
			new UpdatePeachTask(app).execute(money,bigNum,smallNum);
			
			
			// ������������
			if(visibleBads != null && visibleBads.size() > 0)
			{
				for(CCNode node : visibleBads)
				{
					node.removeFromParentAndCleanup(true);
				}
			}
			Log.d("TEST", "����С��");
			
		
			
		}
		
		// ��ż��ܣ���������ʮ��
		@SuppressWarnings("unchecked")
		public void bigKongFu(Object sender){
				 
			if(!isOn) return;
			if(bigNum <= 0) return;
			
			bigNum--;
			showPeach();
			
			// �첽��������
			int money = MonkeyUtil.getDataFromShared(LASTSIGN, "MONEY", app);
			Log.d("TEST", "Ǯ"+money);
			new UpdatePeachTask(app).execute(money,bigNum,smallNum);
			
			// �Ŵ���
			
			 emitter.setVisible(true);
			 isInBig = true;
			 this.schedule("delBig", 10); // ʮ����е��ȡ�
			 Log.d("TEST", "�Ŵ���");
			
		}
		
		// ���������
		public void delBig(float dt)
		{
			emitter.setVisible(false);
			isInBig = false;
			// ����õ���
			unschedule("delBig");	
		}
		
		// �������е�����
		public void createBad(float dt){
			// ����Ϸδ�����������У�ʱ
			if(isOn && !gameOver)
			{
				Random random = new Random();
				// ����������ֵ�����
				int badIndex = random.nextInt(6) + 1;
				CCSprite bad = CCSprite.sprite("bad"+badIndex+".png",true);
				
				// ����������ֵĳ�ʼλ��
				int badMinY =  (int) (bad.getContentSize().height / 2.0f + WINSIZE.height / 5);
				int badMaxY = (int) (WINSIZE.height - bad.getContentSize().height / 2.0f);
				int actualY = random.nextInt(badMaxY - badMinY) + badMinY;
				
				bad.setPosition(CGPoint.ccp(WINSIZE.width + 10, actualY));
				bad.setScale(WINSIZE.height / WELL_Y);
			
				
				// ���ֵĶ���
				CCMoveBy moveBy = CCMoveBy.action(2.0f, CGPoint.ccp(-(110+WINSIZE.width), 0));
				CCCallFuncN removeBad = CCCallFuncN.action(this, "removeBad");
				CCSequence badSequence = CCSequence.actions(moveBy, removeBad);
				bad.runAction(badSequence);
				
				
				// ���뵽spriteSheet
				spriteSheet.addChild(bad);
	
				// �ڲ��Ǻ��ѵ�ʱ����Ѷȵ���
				if(!HARD)
				{
					if(badIndex == 1 || badIndex == 3)
					{
						
						Log.d("TEST", "5,6�������������");
						int keyBadIndex = random.nextInt(6) + 1;
						CCSprite keyBad = CCSprite.sprite("bad"+keyBadIndex+".png", true);
						
						int actualKeyY = random.nextInt(150) + (int)WINSIZE.height - 150;
						keyBad.setPosition(CGPoint.ccp(WINSIZE.width + 10, actualKeyY));
						keyBad.setScale(WINSIZE.height / WELL_Y);
						keyBad.runAction(badSequence.copy());
						spriteSheet.addChild(keyBad);
					}
					
					else if(badIndex == 5){
						Log.d("TEST", "5,6�������������");
						int keyBadIndex = random.nextInt(6) + 1;
						CCSprite keyBad = CCSprite.sprite("bad"+keyBadIndex+".png", true);
						
						int actuaKeyY = random.nextInt(150) + (int) WINSIZE.height / 5;
						keyBad.setPosition(CGPoint.ccp(WINSIZE.width + 10, actuaKeyY));
						keyBad.setScale(WINSIZE.height / WELL_Y);
						keyBad.runAction(badSequence.copy());
						spriteSheet.addChild(keyBad);
					}
				}
				
				if (badIndex == 2) {
					Log.d("TEST", "�����м������");
					int keyBadIndex = random.nextInt(6) + 1;
					CCSprite keyBad = CCSprite.sprite("bad"+keyBadIndex+".png", true);
					
					int keyMinY = (int) ((2 * WINSIZE.height) / 5.0);
					int keyMaxY = (int) ((3 * WINSIZE.height) / 5.0);
					int actuaKeyY = random.nextInt(keyMaxY - keyMinY) + keyMinY;
					keyBad.setPosition(CGPoint.ccp(WINSIZE.width + 10, actuaKeyY));
					keyBad.setScale(WINSIZE.height / WELL_Y);
					keyBad.runAction(badSequence.copy());
					spriteSheet.addChild(keyBad);
				}
		
			}
			
		}

		/**
		 * �ص������������������
		 * @param sender
		 */
		public void removeBad(Object sender){
			
			Log.d("TEST", "���ֱ��Ƴ�");
			spriteSheet.removeChild((CCSprite) sender, true);
		}
		
		/**
		 * �ص����������ڼ����ײ
		 */
		public void check(float dt){
			// ����Ϸ������ʱ�ż��
			if(isOn)
			{
			//	Log.d("TEST", "ison...");
				time += dt;
				// ���·���
				String string = CCFormatter.format("%2.3f", time / 10);
				CCBitmapFontAtlas label =
						(CCBitmapFontAtlas) getChildByTag(TIMETAG);
				label.setString(string);
				
				// ���ݵ�ǰ������������Ϸ�Ѷȣ����޸�k��ֵ
				
				// ����10��(1��)ʱ������Ϸ�Ѷ�
		        if (time > 10 && time < 110) {
					
		        	K = 6 - RATE * (time - 10);
		        	Log.d("TEST", K + "�µ�K");
				}
		        
		        // ���k<2.5�������ѵĽ׶�
		        if(HARD == false && K < 2.5f)
		        {
		        	HARD = true;
		        }
				
				// ������ӵ������棨1/5 ��Ļ��)
				if(monkey.getPosition().y <= WINSIZE.height / 5)
				{
					isOn = false; 
					gameOver = true;
					spriteSheet.removeChild(monkey, false); // �Ƴ�����
					monkey.stopAllActions();
					removeChild(cloud, false);  // �Ƴ�����
					cloud.stopAllActions();
					ground.stopAllActions();  // ֹͣ������˶�
					
					// ����������£����й���Ҳ��ʧЧ
					isInBig = false;
					emitter.setVisible(false);
					
					// ����пɼ�������
					if(visibleBads.size() > 0)
					{
						// �ɼ������ֵ�ֹͣ�˶�
						for(CCSprite bad : visibleBads)
						{
							bad.stopAllActions();
						}
					}
					
					unschedule("createBad"); // ����������ֵĵ���
					
					// �����˵�����ʾ
					CCSprite popSprite =
							(CCSprite) getChildByTag(POPTAG);
					// �޸ķ�����ǩ
					CCBitmapFontAtlas scoreAtlas = (CCBitmapFontAtlas) popSprite.getChildByTag(SCORE);
					scoreAtlas.setString(CCFormatter.format("%2.1f", time / 10));
					// ��߷�
					float best = user.getScore();
					
					// �����߷�С�ڵ�ǰ������������߷�
					if(best < (time / 10))
					{
						best = time / 10;
						// д�� SharedPreferences
						user.setScore(best);
						DBHelper helper = new DBHelper(app);
						helper.updateUser(user);
						helper.close();
					}
					
					// �޸���߷ֱ�ǩ
					CCBitmapFontAtlas bestAtlas = (CCBitmapFontAtlas) popSprite.getChildByTag(BEST);
					bestAtlas.setString(CCFormatter.format("%2.1f", best));		
					
					popSprite.runAction(CCMoveTo.action(.2f, MPOINT));
					
					return;
				}
				
				// ����Ƿ����������
				visibleBads.clear();
				 
				List<CCNode> bads = spriteSheet.getChildren();
				
				// ���visibleBads
				if(bads != null && bads.size() > 0)
				{
					for(CCNode node : bads)
					{
						CCSprite sprite = (CCSprite) node;
						if(sprite.getTag() != MONKEYTAG)
						{
							visibleBads.add(sprite);
						}
					}
				}
				
				// �����ײ
				if(!isInBig && visibleBads.size() > 0)
				{
					CGRect monkeyRect = CGRect.make(monkey.getPosition().x - monkey.getContentSize().width / K ,
							monkey.getPosition().y - monkey.getContentSize().height / K,
							(monkey.getContentSize().width / K) * 2,
							(monkey.getContentSize().height / K) * 2);
					for(CCSprite sprite : visibleBads)
					{
						// ��������ײ
						if(CGRect.intersects(monkeyRect, sprite.getBoundingBox()))
						{
							SoundEngine.sharedEngine().playEffect(app, R.raw.bang);
							gameOver = true;
							monkey.runAction(sharpFall.copy());
							cloud.runAction(sharpFall.copy());
							
						}
					}
				}
				 
			}
		}
		
		/**
		 * ��д�����¼�
		 */
		@Override
		public boolean ccTouchesBegan(MotionEvent event) {
			
			if(gameOver) return true; // �����Ϸ�����ˣ�����������),����Ļ������Ч
			
			// ����isOn��ʼֵΪfalse,ͨ������������Ϸ
			if(!isOn) isOn = true; 
			
			// ��Ч
			SoundEngine.sharedEngine().playEffect(app, R.raw.click);
			
			float monkeyY = monkey.getPosition().y;
			
			// ʵ�ʵ������߶ȣ���ֹ������Ļ
			float acturalRise =( (monkeyY + RISE) > WINSIZE.height - 20)? (WINSIZE.height - 20 - monkeyY):RISE;
			
			// ����
			CCMoveBy riseMoveBy = CCMoveBy.action(.1f, CGPoint.ccp(0, acturalRise));
			
			// �����������½�
			CCSequence sequence = CCSequence.actions(riseMoveBy, fall.copy());
			
			// ֹͣ���Ӻ����ŵĶ���
	        monkey.stopAllActions();
	        cloud.stopAllActions();
	        
	        // ����󣬺��Ӻ�����ͬ�����������½�
	        monkey.runAction(sequence);
	        cloud.runAction(sequence.copy());
	        
	        // ���ӵķ��ж���
	        monkey.runAction(monkeryForever.copy());
	        
			return super.ccTouchesBegan(event);
		}
		
		
		
		
		/**
		 * ��ȡTool����
		 * @return
		 */
		public  CCScene getToolScene()
		{
			multiplexLayer = CCMultiplexLayer.node
					(new ToolLayer(ccColor4B.ccc4(139, 131, 134,
							255)), new HelpLayer(),null);
			CCScene scene = CCScene.node();
			scene.addChild(multiplexLayer);
			
		     // �ص���Ϸ������
			CCSprite backSprite = CCSprite.sprite("back.png",true);
			CCMenuItemSprite back = CCMenuItemSprite.item(backSprite,backSprite, this, "toBack");		
			back.setScale(WINSIZE.height / WELL_Y);
			CCMenu menuBack = CCMenu.menu(back);
			// �˴�������Ҫ�޸ġ�����������
			menuBack.setPosition(backSprite.getContentSize().width, WINSIZE.height -  backSprite.getContentSize().width);
			scene.addChild(menuBack,1,BACKTAG);
			
			//  ǩ���Ȳ�����
			
			CCSprite tabSprite = CCSprite.sprite("tab.png",true);
			CCMenuItemSprite one = CCMenuItemSprite.item(tabSprite,tabSprite, this, "toChange");
			one.setScale(1.4f * WINSIZE.height / WELL_Y);
			CCMenuItemSprite two = CCMenuItemSprite.item(tabSprite,tabSprite, this, "toChange");
			two.setScale(1.4f * WINSIZE.height / WELL_Y);
			one.setTag(0);
			two.setTag(1);
			
			CCMenu menu = CCMenu.menu(one,two);
			menu.setPosition((one.getContentSize().width / 2 - 10)* WINSIZE.height / WELL_Y, WINSIZE.height / 2);
			scene.addChild(menu,SWITCHTAG);
		    menu.alignItemsVertically();
		    
			multiplexLayer.switchTo(0);
			layIndex = 0;
			return scene;
		}
		
		public void toChange(Object sender)
		{
			int i = ((CCMenuItemSprite)(sender)).getTag();
			if(i == layIndex) return;
			layIndex = i;
			Log.d("TEXT", "toChange " + i);
			multiplexLayer.switchTo(i);
		}
		
		public void toBack(Object sender) {
			CCDirector.sharedDirector().replaceScene(CCSlideInTTransition.transition(0.1f, GameLayer.scene(user,app)));
		}
		
	}
	

	/**
	 * �ڲ����ʾ�㿪���߲˵���Ĳ�����
	 * @author ����
	 *
	 */
	static class ToolLayer extends CCColorLayer{

		private SharedPreferences signTimePreferences;
	    private boolean hasSign;
		public int money;
	    
		public static final int MONEYTAG = 10;
		public static final int COINTAG = 11;
		public static final int SINGTAG = 12;
		public static final int PEACHTAG = 13;
		
		/**
		 * ��������Ŀ
		 */
		public static final int BIGTAG = 14;
		/**
		 * С������Ŀ
		 */
		public static final int SMALLTAG = 15;
        /**
         * �˵���Ĵ�����		
         */
		public static final int BIGPTAG = 16;
		/**
		 * �˵����С����
		 */
		public static final int SMAPTAG = 17;
		
		public static boolean isBig;
		
		
		protected ToolLayer(ccColor4B color) {
			super(color);
			
			Log.d("TEST", "toollayer ���캯��");
	       
		/*	CCSpriteFrameCache.sharedSpriteFrameCache().addSpriteFrames(
					"monkey_packer.plist");
			CCSpriteSheet sheet = CCSpriteSheet
					.spriteSheet("monkey_packer.png");
			addChild(sheet);*/
			
			// ��ʼ��ǩ��ʱ��
			signTimePreferences = GameLayer.app.getSharedPreferences(LASTSIGN, Context.MODE_PRIVATE);
			
			// ��ʼ��
			money = signTimePreferences.getInt("MONEY", 0);
		
			// ǩ���Ĳ˵�
			CCMenuItemSprite signItem;
			
			
			if(checkSign())
			{
				Log.d("TEST", "����ǩ��");
				CCSprite signSprite = CCSprite.sprite("sign1.png",true);
				signItem = CCMenuItemSprite.item(signSprite, signSprite, this, "toSign");
				hasSign = false;
			}
			else {
				CCSprite signSprite = CCSprite.sprite("sign2.png",true);
				signItem =  CCMenuItemSprite.item(signSprite, signSprite, this, "toSign");
				hasSign = true;
			}
			signItem.setScale(GameLayer.WINSIZE.height / GameLayer.WELL_Y);
			
			CCMenu signMenu = CCMenu.menu(signItem);
			signMenu.setPosition(GameLayer.WINSIZE.width / 3,GameLayer.WINSIZE.height - 100 * GameLayer.WINSIZE.height / GameLayer.WELL_Y);
		    addChild(signMenu, 1, SINGTAG);
		    
		    // ������
		    CCBitmapFontAtlas moneyLabel = CCBitmapFontAtlas.bitmapFontAtlas("$ "+money, "bitmapFontTest.fnt");
		    moneyLabel.setPosition(GameLayer.WINSIZE.width / 2 + GameLayer.WINSIZE.height / 2, GameLayer.WINSIZE.height / 2 + 200);
		    moneyLabel.setScale(1.8f * GameLayer.WINSIZE.height / GameLayer.WELL_Y);
		    moneyLabel.setColor(ccColor3B.ccc3(105, 105, 105));
		    addChild(moneyLabel,1, MONEYTAG);
		    
		    // ���������ӵ��߼�
		    CCSprite pSprite = CCSprite.sprite("peach.png",true);
		    CCMenuItemSprite itemBig = CCMenuItemSprite.item(pSprite, pSprite, this, "toBuy");
		    CCMenuItemSprite itemSmall = CCMenuItemSprite.item(pSprite, pSprite, this, "toBuy");
		    itemBig.setScale(GameLayer.WINSIZE.height / GameLayer.WELL_Y);
		    itemSmall.setScale(0.8f * GameLayer.WINSIZE.height / GameLayer.WELL_Y);
		    itemBig.setTag(BIGPTAG);
		    itemSmall.setTag(SMAPTAG);
		    
		    
		    CCMenu buyMenu = CCMenu.menu(itemBig,itemSmall);
		    buyMenu.alignItemsHorizontally(20 * GameLayer.WINSIZE.width / GameLayer.WELL_X);
		    buyMenu.setPosition(GameLayer.WINSIZE.width / 2,  GameLayer.WINSIZE.height / 2);
		    addChild(buyMenu,1, PEACHTAG);
		    
		    
		    // ��ʾ���ӵĸ���
		    CCBitmapFontAtlas bigPeach = CCBitmapFontAtlas.bitmapFontAtlas("Big Peach : " + MonkeyUtil.getDataFromShared(LASTSIGN, "BIGNUM", GameLayer.app),
		    		"bitmapFontTest.fnt");
		    CCBitmapFontAtlas smallPeach = CCBitmapFontAtlas.bitmapFontAtlas("Small Peach : " + MonkeyUtil.getDataFromShared(LASTSIGN, "SMALLNUM", GameLayer.app),
		    		"bitmapFontTest.fnt");
		    bigPeach.setPosition(GameLayer.WINSIZE.width / 2 - 200, GameLayer.WINSIZE.height / 2 - 2.2f * bigPeach.getContentSize().height);
		    bigPeach.setScale(1.2f * GameLayer.WINSIZE.height / GameLayer.WELL_Y);
		    smallPeach.setPosition(GameLayer.WINSIZE.width / 2 + 200, GameLayer.WINSIZE.height / 2 - 2.2f * bigPeach.getContentSize().height);
		    smallPeach.setScale(1.2f * GameLayer.WINSIZE.height / GameLayer.WELL_Y);
		    
		    bigPeach.setTag(BIGTAG);
		    smallPeach.setTag(SMALLTAG);
		    
		    bigPeach.setColor(ccColor3B.ccc3(112, 128, 144));
		    smallPeach.setColor(ccColor3B.ccc3(112, 128, 144));
		    
		    addChild(bigPeach);
		    addChild(smallPeach);
		   
		}
		
	/**
	 * ���ڼ���û��Ƿ����ǩ��
	 * @return
	 */
		private boolean checkSign()
		{
			Calendar newCalendar = Calendar.getInstance();
			//Log.d("TEST", newCalendar.get(Calendar.YEAR)+" "+newCalendar.get(Calendar.MONTH));
			Calendar oldCalendar = Calendar.getInstance();
			int year = signTimePreferences.getInt("YEAR", 1999);
			Log.d("TEST", "�洢��Year "+year);
			int month = signTimePreferences.getInt("MONTH", 1);
			int day = signTimePreferences.getInt("DAY", 1);
			oldCalendar.set(year, month, day);
			return MonkeyUtil.isAfterADay(newCalendar, oldCalendar);
		}
		
		/**
		 * ��Ӧǩ���¼�
		 */
		public void toSign(Object sender)
		{
			if(hasSign) return;
			
			hasSign = true;
			Calendar calendar = Calendar.getInstance();
			CCBitmapFontAtlas moneyAtlas = 
			(CCBitmapFontAtlas)(getChildByTag(MONEYTAG));
			money += 10;
			
			// ���߳̽��ļ�д��
			new WriteMoneyTask(signTimePreferences,money).execute(calendar);
			
		
			// ��ҵĲ���Ч��
			CCShatteredTiles3D shatter = CCShatteredTiles3D.action(10, true, ccGridSize.ccg(15, 10), 1.5f);
			// ��ұ�ɪ������Ч��
			CCBezierTo[] beziers = new CCBezierTo[5];
			for(int i = 0; i< 5;i++)
			{
				int flag;
				if(i % 2 == 0) flag = 1;
				else {
					flag = -1;
				}
				CCBezierConfig config = new CCBezierConfig();
				config.endPosition = getChildByTag(MONEYTAG).getPosition();
				config.controlPoint_1 = CGPoint.ccp(GameLayer.WINSIZE.width + 60 * i *flag, GameLayer.WINSIZE.height / 2 + 40 * i *flag);
				config.controlPoint_2 = CGPoint.ccp(GameLayer.WINSIZE.width, GameLayer.WINSIZE.height);
				beziers[i] = CCBezierTo.action(1.5f, config);
			}
			
			// һ���ص�����������������
			CCCallFuncN delCoin = CCCallFuncN.action(this, "toDelCoin");
			
			
			// ��ʼ��5�����
			CCSprite[] coins = new CCSprite[5];
			for(int i = 0;i < 5; i++)
			{
				
				coins[i] = CCSprite.sprite("coin1.png",true);
				coins[i].setScale(GameLayer.WINSIZE.height / GameLayer.WELL_Y);
				coins[i].setPosition(GameLayer.WINSIZE.width / 2, GameLayer.WINSIZE.height / 2);
				addChild(coins[i],1);
			}
			
			
			for(int i = 0; i < 5; i++)
			{
				// ���������˶������
				CCSpawn spawn = CCSpawn.actions(shatter.copy(), beziers[i]);
			
				coins[i].runAction(CCSequence.actions(spawn, delCoin));
				
			}
			
			moneyAtlas.setString("$ "+money);
			
		}
		
		
		/**
		 * �ص�����������������
		 * @param sender
		 */
		public void toDelCoin(Object sender){
			
			removeChild((CCSprite)sender, true);
			
		}
	
		
		public void toBuy(Object sender){
			Log.d("TEST", "toBuy");
			
			Log.d("TEST", ((CCMenuItemSprite)sender).getTag()+"");
			
			if(((CCMenuItemSprite)sender).getTag() == BIGPTAG)
			{
				if(money < 3) return;
				isBig = true;
			}
			else if(((CCMenuItemSprite)sender).getTag() == SMAPTAG){
				if(money < 1) return;
				isBig = false;
			}
			
			BuyDialog buyDialog =new BuyDialog(ccColor4B.ccc4(41, 41, 41,
					50));
			//this.setOpacity(200);
			
			// Ϊʵ��ģ̬�Ի����Ч�������ø��������͸����,
			// Ϊ����ģ��Ի��򣬽���ز˵�����
			CCBitmapFontAtlas sprite = (CCBitmapFontAtlas) getChildByTag(MONEYTAG);
            sprite.setOpacity(50);
            
            sprite =
            		(CCBitmapFontAtlas) getChildByTag(BIGTAG);
            sprite.setOpacity(50);
            
            sprite =
            		(CCBitmapFontAtlas) getChildByTag(SMALLTAG);
            sprite.setOpacity(50);
            
            
            CCMenu menu = (CCMenu) getChildByTag(PEACHTAG);
            menu.setOpacity(50);
            menu.setIsTouchEnabled(false);
            
            menu = (CCMenu) getChildByTag(SINGTAG);
            menu.setOpacity(50);
			menu.setIsTouchEnabled(false);
            
            menu = 
            (CCMenu) this.getParent().getParent().getChildByTag(GameLayer.BACKTAG);
            menu.setOpacity(50);
            menu.setIsTouchEnabled(false);
            
         
        
            
            Log.d("TEST", this.getParent().getParent()+"..!!");
			
			buyDialog.setPosition(GameLayer.WINSIZE.width / 5.0f,GameLayer.WINSIZE.height / 6.0f);
			addChild(buyDialog);
			
			
		}
	    

	}

	
	
	
	
	/**
	 * ʹ��˵����
	 * @author ����
	 *
	 */
	public static class HelpLayer extends CCLayer{
		
		protected HelpLayer() {
			
			CCBitmapFontAtlas loveAtlas = 
			  CCBitmapFontAtlas.bitmapFontAtlas("They are both convinced", "bitmapFontTest.fnt");
			loveAtlas.setPosition(GameLayer.WINSIZE.width / 2, GameLayer.WINSIZE.height / 2 + loveAtlas.getContentSize().height);
			addChild(loveAtlas);
			
			loveAtlas =
			  CCBitmapFontAtlas.bitmapFontAtlas("that a sudden passion joined them.", "bitmapFontTest.fnt");
			loveAtlas.setPosition(GameLayer.WINSIZE.width / 2, GameLayer.WINSIZE.height / 2);
			addChild(loveAtlas);
			
			loveAtlas =
					CCBitmapFontAtlas.bitmapFontAtlas("Such certainty is beautiful,", "bitmapFontTest.fnt");
			loveAtlas.setPosition(GameLayer.WINSIZE.width / 2, GameLayer.WINSIZE.height / 2 - loveAtlas.getContentSize().height);
			addChild(loveAtlas);
			
			
			loveAtlas =
					CCBitmapFontAtlas.bitmapFontAtlas("But uncertainty is more beautiful still.", "bitmapFontTest.fnt");
			loveAtlas.setPosition(GameLayer.WINSIZE.width / 2, GameLayer.WINSIZE.height / 2 - 2 * loveAtlas.getContentSize().height);
			addChild(loveAtlas);
			
			loveAtlas =
					CCBitmapFontAtlas.bitmapFontAtlas("From -- Love at first sight", "bitmapFontTest.fnt");
			
			loveAtlas.setPosition(GameLayer.WINSIZE.width / 2 + loveAtlas.getContentSize().width / 3, GameLayer.WINSIZE.height / 2 - 3 * loveAtlas.getContentSize().height);
			addChild(loveAtlas);
		}
	}
    
}
