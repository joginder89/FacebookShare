package com.anaadih.facebookshare;

import java.util.Arrays;
import java.util.List;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

public abstract class FacebookReadyBaseActivity extends Activity {
    private static final String TAG = "FacebookReadyBaseActivity";

    private UiLifecycleHelper uiHelper;

    protected PendingAction pendingAction = PendingAction.NONE;
    private static final String PERMISSION = "publish_actions";
    protected boolean canPresentShareDialogWithPhotos;
    private boolean canPresentShareDialog;
    private GraphUser user;
    private GraphPlace place;
    private List<GraphUser> tags;
    protected Bitmap pendingBitmap;

    protected enum PendingAction {
        NONE, POST_PHOTO, POST_STATUS_UPDATE
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state,
                Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        canPresentShareDialog = FacebookDialog.canPresentShareDialog(this,
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG);
        canPresentShareDialogWithPhotos = FacebookDialog.canPresentShareDialog(
                this, FacebookDialog.ShareDialogFeature.PHOTOS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data,
                new FacebookDialog.Callback() {
                    @Override
                    public void onError(FacebookDialog.PendingCall pendingCall,
                            Exception error, Bundle data) {
                        Log.e(TAG, String.format("Error: %s", error.toString()));
                    }

                    @Override
                    public void onComplete(
                            FacebookDialog.PendingCall pendingCall, Bundle data) {
                        Log.i(TAG, "Success!");
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

    protected UiLifecycleHelper getFacebookUiHelper() {
        return uiHelper;
    }

    protected void performPublish(PendingAction action, boolean allowNoSession) {
        Session session = Session.getActiveSession();
        if (session != null) {
            pendingAction = action;
            if (hasPublishPermission()) {
                // We can do the action right away.
                handlePendingAction();
                return;
            } else if (session.isOpened()) {
                // We need to get new permissions, then complete the action when
                // we get called back.
                session.requestNewPublishPermissions(new Session.NewPermissionsRequest(
                        this, PERMISSION));
                return;
            }
        }

        if (allowNoSession) {
            pendingAction = action;
            handlePendingAction();
        }
    }

    private boolean hasPublishPermission() {
        Session session = Session.getActiveSession();
        return session != null
                && session.getPermissions().contains("publish_actions");
    }

    @SuppressWarnings("incomplete-switch")
    private void handlePendingAction() {
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but
        // we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;

        switch (previouslyPendingAction) {
        case POST_PHOTO:
            postPhoto();
            break;
        case POST_STATUS_UPDATE:
            postStatusUpdate();
            break;
        }
    }

    private void postPhoto() {

        if (canPresentShareDialogWithPhotos) {
            FacebookDialog shareDialog = createShareDialogBuilderForPhoto(
                    pendingBitmap).build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        } else if (hasPublishPermission()) {
            Request request = Request.newUploadPhotoRequest(
                    Session.getActiveSession(), pendingBitmap,
                    new Request.Callback() {
                        
                    	@Override
						public void onCompleted(Response response) {
							// TODO Auto-generated method stub
							/*InfoDialog.newInstance(
                                    FacebookReadyBaseActivity.this, "Done",
                                    "Photo share finished.", null).show();*/
							
						}
                    });
            request.executeAsync();
        } else {
            pendingAction = PendingAction.POST_PHOTO;
        }
    }

    private void postStatusUpdate() {
        if (canPresentShareDialog) {
            FacebookDialog shareDialog = createShareDialogBuilderForLink()
                    .build();
            uiHelper.trackPendingDialogCall(shareDialog.present());
        } else if (user != null && hasPublishPermission()) {
            // TODO fixme
            final String message = "Status update finished.";
            Request request = Request.newStatusUpdateRequest(
                    Session.getActiveSession(), message, place, tags,
                    new Request.Callback() {
                        @Override
                        public void onCompleted(Response response) {

                           /* InfoDialog.newInstance(
                                    FacebookReadyBaseActivity.this, "Done",
                                    message, null).show();*/
                        }
                    });
            request.executeAsync();

        } else {
            pendingAction = PendingAction.POST_STATUS_UPDATE;
        }
    }

    private FacebookDialog.PhotoShareDialogBuilder createShareDialogBuilderForPhoto(
            Bitmap... photos) {
        return new FacebookDialog.PhotoShareDialogBuilder(this)
                .addPhotos(Arrays.asList(photos));
    }

    private FacebookDialog.ShareDialogBuilder createShareDialogBuilderForLink() {
        return new FacebookDialog.ShareDialogBuilder(this)
                .setName("Hello Facebook")
                .setDescription(
                        "The 'Hello Facebook' sample application showcases simple Facebook integration")
                .setLink("http://developers.facebook.com/android");
    }


    private void onSessionStateChange(Session session, SessionState state,
            Exception exception) {
        if (pendingAction != PendingAction.NONE
                && (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
            //InfoDialog.newInstance(FacebookReadyBaseActivity.this, "Failed",
              //      "Not granted.", null).show();
            pendingAction = PendingAction.NONE;
        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
            handlePendingAction();
        }

    }

}

