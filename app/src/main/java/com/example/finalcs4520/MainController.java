package com.example.finalcs4520;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static java.security.AccessController.getContext;
/*
public class MainController extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.MainActivityContainer, new SearchTravelFragment(), "searchTravelFragment")
                .addToBackStack(null)
                .commit();
    }
}*/

public class MainController extends AppCompatActivity implements RegisterLogInFragment.IRegister,
        cameraPreviewFragment.IPreviewImg,
        CameraFragment.ICameraPicture,
        ProfileFragment.IProfileTrip,
        SearchProfileAdapter.IFromSearchProfileAdapterToActivity,
        ExploreFragment.IExploreUpdate,
        TransitSummaryAdapter.IOpenTransitRoute,
        TransitRouteFragment.ITransitRoute,
        PublicTransitSearchFragment.IPublicSearch,
        SearchProfileFragment.ISearchProfile,
        SearchTravelFragment.ISearchTravel,
        FlightAdapter.IFlightAdapter,
        InfoFragment.IInfoProfile{

    private int screenCamera;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private int positionTrip = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.MainActivityContainer, new RegisterLogInFragment(), "registerFragment")
                .addToBackStack(null)
                .commit();
    }

    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()==RESULT_OK){
                        Intent data = result.getData();
                        Uri newUri = data.getData();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.MainActivityContainer,
                                        cameraPreviewFragment.newInstance(screenCamera, newUri),
                                        "cameraPreviewFragment")
                                .addToBackStack(null)
                                .commit();
                    }
                }
            }
    );


    @Override
    public void onGalleryPressed(int screen) {
        screenCamera = screen;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        galleryLauncher.launch(intent);

    }

    @Override
    public void onCapturePressed(int i, Uri imgUri) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, cameraPreviewFragment.newInstance(i, imgUri),
                        "cameraPreviewFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onLoginPressed() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new ProfileFragment(), "profileFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSignUpImagePressed() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, CameraFragment.newInstance(0), "cameraFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onRetakePressed(int screen) {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onUploadSignUp(Uri imgUri) {
        RegisterLogInFragment registerFragment = (RegisterLogInFragment) getSupportFragmentManager()
                .findFragmentByTag("registerFragment");
        registerFragment.setSignUpImage(imgUri);
        // popping two times to get the fragment without creating a new one
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onUploadTripPicture(Uri imgUri) {
        ProfileFragment profileFragment = (ProfileFragment) getSupportFragmentManager()
                .findFragmentByTag("profileFragment");

        if (positionTrip != -1) {
            profileFragment.setTripUri(imgUri, positionTrip);
        }
        positionTrip = -1;
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onUploadProfilePicture(Uri imgUri) {
        ProfileFragment profileFragment = (ProfileFragment) getSupportFragmentManager()
                .findFragmentByTag("profileFragment");
        profileFragment.setProfilePic(imgUri);
        getSupportFragmentManager().popBackStack();
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onLogOutPressed() {
        mAuth.signOut();
        mUser = null;
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < (fm.getBackStackEntryCount() - 1); ++i) {
            fm.popBackStack();
        }

    }

    @Override
    public void onSearchPressed() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new SearchTravelFragment(), "travelFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onImgPressed() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, CameraFragment.newInstance(2), "cameraFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDeletePressed(TripProfile deleteTrip) {
        ProfileFragment profileFragment = (ProfileFragment) getSupportFragmentManager()
                .findFragmentByTag("profileFragment");
        profileFragment.deleteTrip(deleteTrip);

    }

    @Override
    public void onCompleteTripPressed(TripProfile compTrip) {
        ProfileFragment profileFragment = (ProfileFragment) getSupportFragmentManager()
                .findFragmentByTag("profileFragment");
        profileFragment.completeTrip(compTrip);

    }

    @Override
    public void onAddFriendsPressed() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new SearchProfileFragment(), "searchFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onTripImgPressed(int position) {
        positionTrip = position;
        String stringpos = "3" + positionTrip;
        int newPos = Integer.valueOf(stringpos);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, CameraFragment.newInstance(newPos), "cameraFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPublicPressed() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new PublicTransitSearchFragment(), "publicFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onExplorePressed() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new ExploreFragment(), "exploreFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onProfilePressedP() {
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < (fm.getBackStackEntryCount() - 2); ++i) {
            fm.popBackStack();
        }
    }

    @Override
    public void onInfoPressed() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new InfoFragment(), "infoFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void openProfile(String userEmail, String myEmail) {
        if (userEmail.equals(myEmail)) {
            // return to main profile screen if you select your own profile
            FragmentManager fm = getSupportFragmentManager();
            for(int i = 0; i < (fm.getBackStackEntryCount() - 2); ++i) {
                fm.popBackStack();
            }
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.MainActivityContainer, ProfileFragment.newInstance(userEmail), "cameraFragment")
                    .addToBackStack(null)
                    .commit();
        }

    }

    @Override
    public void onLocationSelected(String location, String destination) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, PublicTransitSearchFragment.newInstance(location, destination), "publicFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void openRoute(TransitRoute route, String departure, String destination) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, TransitRouteFragment.newInstance(route, departure, destination), "routeFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onAcceptPressed(String userEmail) {
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < (fm.getBackStackEntryCount() - 2); ++i) {
            fm.popBackStack();
        }
    }

    // TRANSIT ROUT FRAGMENT MENU

    @Override
    public void onSearchPressedTR() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new SearchTravelFragment(), "travelFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPublicPressedTR() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new ExploreFragment(), "exploreFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onAddFriendPressdeTR() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new SearchProfileFragment(), "searchFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onProfilePessedTR() {
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < (fm.getBackStackEntryCount() - 2); ++i) {
            fm.popBackStack();
        }
    }

    // PUBLIC TRANSPORT FRAGMENT MENU

    @Override
    public void onSearchPressedPT() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new SearchTravelFragment(), "travelFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPublicPressedPT() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new ExploreFragment(), "exploreFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onAddFriendPressdePT() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new SearchProfileFragment(), "searchFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onProfilePessedPT() {
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < (fm.getBackStackEntryCount() - 2); ++i) {
            fm.popBackStack();
        }
    }

    // SEARCH PROFILE FRAGMENT MENU

    @Override
    public void onSearchPressedPS() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new SearchTravelFragment(), "travelFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPublicPressedPS() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new ExploreFragment(), "exploreFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onAddFriendPressdePS() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new SearchProfileFragment(), "searchFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onProfilePessedPS() {
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < (fm.getBackStackEntryCount() - 2); ++i) {
            fm.popBackStack();
        }
    }

    // SEARCH TRAVEL FRAGMENT

    @Override
    public void onSearchPressedT() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new SearchTravelFragment(), "travelFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPublicPressedT() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new ExploreFragment(), "exploreFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onAddFriendPressdeT() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.MainActivityContainer, new SearchProfileFragment(), "searchFragment")
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onProfilePessedT() {
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < (fm.getBackStackEntryCount() - 2); ++i) {
            fm.popBackStack();
        }
    }

    @Override
    public void onAcceptClicked(String userEmail) {
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < (fm.getBackStackEntryCount() - 2); ++i) {
            fm.popBackStack();
        }
    }

    @Override
    public void onBackPressedInfo() {
        getSupportFragmentManager().popBackStack();
    }
}