package com.anaadih.facebookshare;


import java.util.Arrays;
import java.util.List;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {
	
	private UiLifecycleHelper uiHelper;
	private final String PENDING_ACTION_BUNDLE_KEY = "com.anaadih.facebookshare.mainactivity:PendingAction";
    private PendingAction pendingAction = PendingAction.NONE;
    private static final List<String> PERMISSIONS = Arrays.asList("publish_stream, publish_actions");
    private Session.StatusCallback statusCallback = new SessionStatusCallback();
    Session session;
    
    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            updateView();
        }
    }
    
    
    
    
    private void updateView() {
        session = Session.getActiveSession();
        
        if (session.isOpened()) {
        	Log.e("updateView==>1", "Session is opened"+session.toString());
        	publishFeedDialog2();
        } else {
        	Log.e("updateView==>2", "Session is Closed"+session.toString()); 
        }
    	
    	
    }
    
    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }
    
   /* private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            Log.e("inSideCall==>", session +"=="+ state);
            onSessionStateChange(session, state, exception);
        }
    };
    
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (pendingAction != PendingAction.NONE &&
                (exception instanceof FacebookOperationCanceledException ||
                exception instanceof FacebookAuthorizationException)) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.cancelled)
                    .setMessage(R.string.permission_not_granted)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            pendingAction = PendingAction.NONE;
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            handlePendingAction();
        }
        updateUI();
    } */

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }
        
        /*try { 
        	PackageInfo info = getPackageManager().
        			getPackageInfo("com.anaadih.facebookshare", PackageManager.GET_SIGNATURES); 
        	for (Signature signature : info.signatures) 
        	{ 
        		MessageDigest md = MessageDigest.getInstance("SHA"); 
        		md.update(signature.toByteArray()); 
        		String sign=Base64.encodeToString(md.digest(), Base64.DEFAULT); 
        		Log.e("MY KEY HASH:", sign); 
        		//textInstructionsOrLink = (TextView)findViewById(R.id.textstring); 
        		//textInstructionsOrLink.setText(sign); Toast.makeText(getApplicationContext(),sign, Toast.LENGTH_LONG).show(); 
        	} 
        } catch (NameNotFoundException e) { 
        	Log.d("joginder1==>",e.getMessage()); 
        } catch (NoSuchAlgorithmException e) { 
        	Log.d("joginder2==>",e.getMessage());
        }*/
        
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(this);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            }
        }
    }	

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }
    
    public void shareALink(View v) {
    	Log.e("inSide==>", "shareALink");
    	if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
    			FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
    			    // Publish the post using the Share Dialog
    			    /*FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
    			            .setLink("https://developers.facebook.com/android")
    			            .build();*/
    				FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
    					.setLink("http://hrm.testserver87.com/transportation/blinx/webadmin/recommend/images/1/1.jpg")
    					.setName("This is Event Name")
    					.setApplicationName("Blinx Music")
    					.setCaption("Blinx Developer")
    					.setDescription("This is Event Descrption.........")
    					.build();
    			    uiHelper.trackPendingDialogCall(shareDialog.present());
    		

    			} else {
    				Log.e("inSide==>", "FB App is Not install");
    			    // Fallback. For example, publish the post using the Feed Dialog
    			    publishFeedDialog();
    			}
    	
    }
    
    private void publishFeedDialog() {
    	
    	Log.e("inSidePublishFeedDialog==>", "publishFeedDialog");
        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        Session session = Session.getActiveSession();
        
        if(session.isOpened()) {
        	session.closeAndClearTokenInformation();
        }
        
        
        if (!session.isOpened() && !session.isClosed()) {
        	Log.e("JogiSession1==>", session.toString());
            session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            Log.e("JogiSession11==>", session.toString());
        } else {
        	Log.e("JogiSession2==>", session.toString());
            Session.openActiveSession(this, true, statusCallback);
            if(session.isOpened()) {
            	
            }
            
        }
    }
    
    private void publishFeedDialog2() {
    	
    	Bundle params = new Bundle();
        params.putString("name", "This is Event Name");
        params.putString("caption", "Blinx Developer");
        params.putString("description", "This is Event Descrption..........");
        params.putString("link", "https://developers.facebook.com/android");
        params.putString("picture", "http://hrm.testserver87.com/transportation/blinx/webadmin/recommend/images/1/1.jpg");
        
        session = Session.getActiveSession();
    	
    	WebDialog feedDialog = (
                new WebDialog.FeedDialogBuilder(this,
                		session,
                    params))
                .setOnCompleteListener(new OnCompleteListener() {

                   @Override
    				public void onComplete(Bundle values, FacebookException error) {
    					// TODO Auto-generated method stub
    					
    					if (error == null) {
                            final String postId = values.getString("post_id");
                            if (postId != null) {
                            	Log.e("Posted story, id", postId);
                            } else {
                            	Log.e("Publish cancelled", "User clicked the Cancel button");
                                
                           }
                        } else if (error instanceof FacebookOperationCanceledException) {
                        	Log.e("Publish cancelled", "User clicked the x button");
                        } else {
                        	Log.e("Error posting story", "network error");
                        }
    					
    				}

                })
                .build();
            feedDialog.show();
    	
    }
    
}
